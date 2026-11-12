package com.airhive.backend.security;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.repository.AppUserRepository;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String NOTIFICATIONS_TOPIC_PREFIX =
            "/topic/notifications/";

    private static final String FLIGHTS_TOPIC =
            "/topic/flights";

    private final JwtDecoder jwtDecoder;
    private final AppUserRepository appUserRepository;

    public WebSocketAuthInterceptor(
            JwtDecoder jwtDecoder,
            AppUserRepository appUserRepository) {
        this.jwtDecoder = jwtDecoder;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            authenticateConnection(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor);
        }

        if (StompCommand.SEND.equals(command)) {
            throw new IllegalArgumentException(
                    "Client-originated WebSocket messages are not allowed");
        }

        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(
                message.getPayload(),
                accessor.getMessageHeaders());
    }

    private void authenticateConnection(StompHeaderAccessor accessor) {
        String authorization =
                accessor.getFirstNativeHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException(
                    "Missing WebSocket Authorization header");
        }

        String token = authorization.substring(7).trim();

        if (token.isBlank()) {
            throw new IllegalArgumentException(
                    "Empty WebSocket JWT token");
        }

        Jwt jwt;

        try {
            jwt = jwtDecoder.decode(token);
        } catch (JwtException exception) {
            throw new IllegalArgumentException(
                    "Invalid WebSocket JWT token",
                    exception);
        }

        String username = jwt.getSubject();
        String roleClaim = jwt.getClaimAsString("role");
        Long userId = extractUserId(jwt);

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "WebSocket JWT is missing subject claim");
        }

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "WebSocket JWT is missing a valid userId claim");
        }

        if (roleClaim == null || roleClaim.isBlank()) {
            throw new IllegalArgumentException(
                    "WebSocket JWT is missing role claim");
        }

        Role tokenRole = parseRole(roleClaim);
        AppUser user = loadAndValidateCurrentUser(userId, username, tokenRole);

        Authentication authentication =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name())));

        accessor.setUser(authentication);
    }

    private AppUser loadAndValidateCurrentUser(
            Long userId,
            String username,
            Role tokenRole) {

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "WebSocket user does not exist"));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException(
                    "WebSocket user account is disabled");
        }

        if (!user.getUsername().equals(username)) {
            throw new IllegalArgumentException(
                    "WebSocket JWT subject does not match the current user");
        }

        if (user.getRole() != tokenRole) {
            throw new IllegalArgumentException(
                    "WebSocket JWT role is no longer current");
        }

        return user;
    }

    private Role parseRole(String roleClaim) {
        try {
            return Role.valueOf(roleClaim);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "WebSocket JWT contains an invalid role",
                    exception);
        }
    }

    private Long extractUserId(Jwt jwt) {
        Object userIdClaim = jwt.getClaim("userId");

        if (userIdClaim instanceof Number number) {
            long userId = number.longValue();

            if (number.doubleValue() != (double) userId) {
                return null;
            }

            return userId;
        }

        if (userIdClaim instanceof String userIdString
                && userIdString.matches("\\d+")) {
            try {
                return Long.parseLong(userIdString);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        return null;
    }

    private void authorizeSubscription(
            StompHeaderAccessor accessor) {

        Authentication authentication =
                (Authentication) accessor.getUser();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException(
                    "WebSocket session is not authenticated");
        }

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new IllegalArgumentException(
                    "WebSocket authentication must use a JWT");
        }

        Jwt jwt = jwtAuthentication.getToken();
        Long userId = extractUserId(jwt);
        String username = jwt.getSubject();
        String roleClaim = jwt.getClaimAsString("role");

        if (userId == null || userId <= 0
                || username == null || username.isBlank()
                || roleClaim == null || roleClaim.isBlank()) {
            throw new IllegalArgumentException(
                    "WebSocket JWT identity is invalid");
        }

        Role tokenRole = parseRole(roleClaim);
        AppUser currentUser =
                loadAndValidateCurrentUser(userId, username, tokenRole);

        String destination = accessor.getDestination();

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "WebSocket subscription destination is missing");
        }

        if (FLIGHTS_TOPIC.equals(destination)) {
            authorizeFlightSubscription(currentUser.getRole());
            return;
        }

        if (destination.startsWith(NOTIFICATIONS_TOPIC_PREFIX)) {
            authorizeNotificationSubscription(userId, destination);
            return;
        }

        throw new IllegalArgumentException(
                "Unauthorized WebSocket subscription: " + destination);
    }

    private void authorizeFlightSubscription(Role role) {
        boolean authorized =
                role == Role.ADMIN
                        || role == Role.OPERATOR
                        || role == Role.VIEWER;

        if (!authorized) {
            throw new IllegalArgumentException(
                    "Role is not authorized to subscribe to flight updates");
        }
    }

    private void authorizeNotificationSubscription(
            Long authenticatedUserId,
            String destination) {

        String userIdPart = destination.substring(
                NOTIFICATIONS_TOPIC_PREFIX.length());

        if (userIdPart.isBlank()
                || !userIdPart.matches("\\d+")) {
            throw new IllegalArgumentException(
                    "Invalid notification subscription destination");
        }

        if (!authenticatedUserId.toString().equals(userIdPart)) {
            throw new IllegalArgumentException(
                    "Cannot subscribe to another user's notifications");
        }
    }
}
