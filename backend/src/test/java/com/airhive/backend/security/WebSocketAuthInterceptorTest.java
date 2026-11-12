
package com.airhive.backend.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.repository.AppUserRepository;

class WebSocketAuthInterceptorTest {

    private JwtDecoder jwtDecoder;
    private AppUserRepository appUserRepository;
    private WebSocketAuthInterceptor interceptor;
    private MessageChannel channel;

    @BeforeEach
    void setUp() {
        jwtDecoder = org.mockito.Mockito.mock(JwtDecoder.class);
        appUserRepository = org.mockito.Mockito.mock(AppUserRepository.class);

        interceptor = new WebSocketAuthInterceptor(
                jwtDecoder,
                appUserRepository);

        channel = org.mockito.Mockito.mock(MessageChannel.class);
    }

    @Test
    void connectWithoutAuthorizationHeaderShouldBeRejected() {
        Message<?> message = createMessage(StompCommand.CONNECT);

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void connectWithInvalidJwtShouldBeRejected() {
        when(jwtDecoder.decode("invalid-token"))
                .thenThrow(new JwtException("Invalid token"));

        Message<?> message = createConnectMessage("Bearer invalid-token");

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void connectWithValidJwtShouldAuthenticateUser() {
        Jwt jwt = createJwt("viewer", 10L, "VIEWER");

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);
        mockCurrentUser(10L, "viewer", Role.VIEWER, true);

        Message<?> message = createConnectMessage("Bearer valid-token");

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);

        Authentication authentication =
                (Authentication) StompHeaderAccessor.wrap(result).getUser();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());

        assertTrue(
                authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_VIEWER")));
    }

    @Test
    void connectWithMissingUserIdShouldBeRejected() {
        Jwt jwt = createJwtWithoutUserId("viewer", "VIEWER");

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

        Message<?> message = createConnectMessage("Bearer valid-token");

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void connectWithUnknownRoleShouldBeRejected() {
        Jwt jwt = createJwt("user", 10L, "UNKNOWN");

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

        Message<?> message = createConnectMessage("Bearer valid-token");

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void connectWithNonexistentUserShouldBeRejected() {
        Jwt jwt = createJwt("user", 10L, "VIEWER");

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);
        when(appUserRepository.findById(10L)).thenReturn(Optional.empty());

        Message<?> message = createConnectMessage("Bearer valid-token");

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void connectWithDisabledUserShouldBeRejected() {
        Jwt jwt = createJwt("user", 10L, "VIEWER");

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);
        mockCurrentUser(10L, "user", Role.VIEWER, false);

        Message<?> message = createConnectMessage("Bearer valid-token");

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void connectWithUsernameMismatchShouldBeRejected() {
        Jwt jwt = createJwt("different-user", 10L, "VIEWER");

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);
        mockCurrentUser(10L, "user", Role.VIEWER, true);

        Message<?> message = createConnectMessage("Bearer valid-token");

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void connectWithOutdatedRoleShouldBeRejected() {
        Jwt jwt = createJwt("user", 10L, "VIEWER");

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);
        mockCurrentUser(10L, "user", Role.PASSENGER, true);

        Message<?> message = createConnectMessage("Bearer valid-token");

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void adminShouldBeAllowedToSubscribeToFlights() {
        assertFlightSubscriptionAllowed("ADMIN");
    }

    @Test
    void operatorShouldBeAllowedToSubscribeToFlights() {
        assertFlightSubscriptionAllowed("OPERATOR");
    }

    @Test
    void viewerShouldBeAllowedToSubscribeToFlights() {
        assertFlightSubscriptionAllowed("VIEWER");
    }

    @Test
    void passengerShouldNotBeAllowedToSubscribeToFlights() {
        mockCurrentUser(10L, "user", Role.PASSENGER, true);

        Message<?> message = createSubscribeMessage(
                "/topic/flights",
                createAuthentication("PASSENGER", 10L));

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void subscriptionWithDisabledUserShouldBeRejected() {
        mockCurrentUser(10L, "user", Role.VIEWER, false);

        Message<?> message = createSubscribeMessage(
                "/topic/flights",
                createAuthentication("VIEWER", 10L));

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void subscriptionWithOutdatedRoleShouldBeRejected() {
        mockCurrentUser(10L, "user", Role.PASSENGER, true);

        Message<?> message = createSubscribeMessage(
                "/topic/flights",
                createAuthentication("VIEWER", 10L));

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void userShouldBeAllowedToSubscribeToOwnNotifications() {
        mockCurrentUser(10L, "user", Role.PASSENGER, true);

        Message<?> message = createSubscribeMessage(
                "/topic/notifications/10",
                createAuthentication("PASSENGER", 10L));

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
    }

    @Test
    void userShouldNotBeAllowedToSubscribeToAnotherUsersNotifications() {
        mockCurrentUser(10L, "user", Role.PASSENGER, true);

        Message<?> message = createSubscribeMessage(
                "/topic/notifications/20",
                createAuthentication("PASSENGER", 10L));

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void unknownSubscriptionDestinationShouldBeRejected() {
        mockCurrentUser(10L, "user", Role.VIEWER, true);

        Message<?> message = createSubscribeMessage(
                "/topic/unknown",
                createAuthentication("VIEWER", 10L));

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    @Test
    void clientSendFrameShouldBeRejected() {
        Message<?> message = createMessage(StompCommand.SEND);

        assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(message, channel));
    }

    private void assertFlightSubscriptionAllowed(String role) {
        mockCurrentUser(10L, "user", Role.valueOf(role), true);

        Message<?> message = createSubscribeMessage(
                "/topic/flights",
                createAuthentication(role, 10L));

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
    }

    private void mockCurrentUser(
            Long userId,
            String username,
            Role role,
            boolean enabled) {

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setRole(role);
        user.setEnabled(enabled);

        when(appUserRepository.findById(userId))
                .thenReturn(Optional.of(user));
    }

    private Message<?> createConnectMessage(String authorization) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);

        accessor.setNativeHeader("Authorization", authorization);
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders());
    }

    private Message<?> createSubscribeMessage(
            String destination,
            Authentication authentication) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        accessor.setDestination(destination);
        accessor.setUser(authentication);
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders());
    }

    private Message<?> createMessage(StompCommand command) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(command);

        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders());
    }

    private Authentication createAuthentication(
            String role,
            Long userId) {

        Jwt jwt = createJwt("user", userId, role);

        return new JwtAuthenticationToken(
                jwt,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role)));
    }

    private Jwt createJwt(
            String subject,
            Long userId,
            String role) {

        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(subject)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private Jwt createJwtWithoutUserId(
            String subject,
            String role) {

        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(subject)
                .claim("role", role)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
