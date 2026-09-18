package com.airhive.backend.security;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
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

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
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
            String role = jwt.getClaimAsString("role");

            if (username == null || username.isBlank()
                    || role == null || role.isBlank()) {
                throw new IllegalArgumentException(
                        "WebSocket JWT is missing required claims");
            }

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role)));

            accessor.setUser(authentication);
        }

        return message;
    }
}