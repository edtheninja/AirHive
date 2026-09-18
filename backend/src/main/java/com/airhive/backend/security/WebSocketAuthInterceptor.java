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

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

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

        return message;
    }

    private void authenticateConnection(
            StompHeaderAccessor accessor) {

        String authorization
                = accessor.getFirstNativeHeader("Authorization");

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
        String role = jwt.getClaimAsString("role");

        if (username == null
                || username.isBlank()
                || role == null
                || role.isBlank()) {
            throw new IllegalArgumentException(
                    "WebSocket JWT is missing required claims");
        }

        Authentication authentication
                = new JwtAuthenticationToken(
                        jwt,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role)));

        accessor.setUser(authentication);
    }

    private void authorizeSubscription(
            StompHeaderAccessor accessor) {

        Authentication authentication = (Authentication) accessor.getUser();

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

        if (destination.startsWith("/topic/notifications/")) {
            authorizeNotificationSubscription(
                    authentication,
                    destination);
            return;
        }

        if ("/topic/flights".equals(destination)) {
            return;
        }

        throw new IllegalArgumentException(
                "Unauthorized WebSocket subscription: "
                + destination);
    }

    private void authorizeNotificationSubscription(
            Authentication authentication,
            String destination) {

        String userIdPart = destination.substring(
                "/topic/notifications/".length());

        if (userIdPart.isBlank()
                || !userIdPart.matches("\\d+")) {
            throw new IllegalArgumentException(
                    "Invalid notification subscription destination");
        }

        JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;

        String authenticatedUserId = jwtAuthentication.getToken()
                .getClaimAsString("userId");

        if (authenticatedUserId == null
                || authenticatedUserId.isBlank()) {
            throw new IllegalArgumentException(
                    "WebSocket JWT is missing userId claim");
        }

        if (!authenticatedUserId.equals(userIdPart)) {
            throw new IllegalArgumentException(
                    "Cannot subscribe to another user's notifications");
        }
    }
}
