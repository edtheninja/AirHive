package com.airhive.backend.security;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.messaging.support.MessageBuilder;

import com.airhive.backend.entity.Role;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String NOTIFICATIONS_TOPIC_PREFIX =
            "/topic/notifications/";

    private static final String FLIGHTS_TOPIC =
            "/topic/flights";

    private final JwtDecoder jwtDecoder;

    public WebSocketAuthInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
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

    // Return a message containing the updated STOMP headers,
    // including the authenticated Principal on CONNECT.
    accessor.setLeaveMutable(true);

    return MessageBuilder.createMessage(
            message.getPayload(),
            accessor.getMessageHeaders());
}


    private void authenticateConnection(
            StompHeaderAccessor accessor) {

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

        Role role;

        try {
            role = Role.valueOf(roleClaim);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "WebSocket JWT contains an invalid role",
                    exception);
        }

        Authentication authentication =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role.name())));

        accessor.setUser(authentication);
    }

    private Long extractUserId(Jwt jwt) {

        Object userIdClaim = jwt.getClaim("userId");

        if (userIdClaim instanceof Number number) {
            long userId = number.longValue();

            // Reject non-integral numeric values.
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

        String destination = accessor.getDestination();

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "WebSocket subscription destination is missing");
        }

        if (FLIGHTS_TOPIC.equals(destination)) {
            authorizeFlightSubscription(authentication);
            return;
        }

        if (destination.startsWith(NOTIFICATIONS_TOPIC_PREFIX)) {
            authorizeNotificationSubscription(
                    authentication,
                    destination);
            return;
        }

        throw new IllegalArgumentException(
                "Unauthorized WebSocket subscription: "
                        + destination);
    }

    private void authorizeFlightSubscription(
            Authentication authentication) {

        boolean authorized =
                authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN")
                                        || authority.getAuthority().equals("ROLE_OPERATOR")
                                        || authority.getAuthority().equals("ROLE_VIEWER"));

        if (!authorized) {
            throw new IllegalArgumentException(
                    "Role is not authorized to subscribe to flight updates");
        }
    }

    private void authorizeNotificationSubscription(
            Authentication authentication,
            String destination) {

        String userIdPart = destination.substring(
                NOTIFICATIONS_TOPIC_PREFIX.length());

        if (userIdPart.isBlank()
                || !userIdPart.matches("\\d+")) {
            throw new IllegalArgumentException(
                    "Invalid notification subscription destination");
        }

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new IllegalArgumentException(
                    "WebSocket authentication must use a JWT");
        }

        Long authenticatedUserId =
                extractUserId(jwtAuthentication.getToken());

        if (authenticatedUserId == null
                || authenticatedUserId <= 0) {
            throw new IllegalArgumentException(
                    "WebSocket JWT is missing a valid userId claim");
        }

        if (!authenticatedUserId.toString().equals(userIdPart)) {
            throw new IllegalArgumentException(
                    "Cannot subscribe to another user's notifications");
        }
    }
}
