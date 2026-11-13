
package com.airhive.backend.security;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.repository.AppUserRepository;

class WebSocketOutboundAuthInterceptorTest {

    private static final Long USER_ID = 10L;
    private static final String SESSION_ID = "session-1";

    private SimpUserRegistry simpUserRegistry;
    private AppUserRepository appUserRepository;
    private WebSocketOutboundAuthInterceptor interceptor;
    private MessageChannel channel;

    @BeforeEach
    void setUp() {
        simpUserRegistry = mock(SimpUserRegistry.class);
        appUserRepository = mock(AppUserRepository.class);

        interceptor = new WebSocketOutboundAuthInterceptor(
                simpUserRegistry,
                appUserRepository);

        channel = mock(MessageChannel.class);
    }

    @Test
    void enabledAdminShouldReceiveFlightUpdates() {
        assertFlightMessageAllowed(Role.ADMIN);
    }

    @Test
    void enabledOperatorShouldReceiveFlightUpdates() {
        assertFlightMessageAllowed(Role.OPERATOR);
    }

    @Test
    void enabledViewerShouldReceiveFlightUpdates() {
        assertFlightMessageAllowed(Role.VIEWER);
    }

    @Test
    void passengerShouldNotReceiveFlightUpdates() {
        assertFlightMessageRejected(Role.PASSENGER);
    }

    @Test
    void disabledUserShouldNotReceiveFlightUpdates() {
        mockSession(Role.VIEWER, false);

        Message<?> result = interceptor.preSend(
                createOutboundMessage("/topic/flights", SESSION_ID),
                channel);

        assertNull(result);
    }

    @Test
    void demotedUserShouldNotReceiveFlightUpdates() {
        mockSession(Role.PASSENGER, true);

        Message<?> result = interceptor.preSend(
                createOutboundMessage("/topic/flights", SESSION_ID),
                channel);

        assertNull(result);
    }

    @Test
    void userShouldReceiveOwnNotifications() {
        mockSession(Role.PASSENGER, true);

        Message<?> message = createOutboundMessage(
                "/topic/notifications/10",
                SESSION_ID);

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
    }

    @Test
    void userShouldNotReceiveAnotherUsersNotifications() {
        mockSession(Role.PASSENGER, true);

        Message<?> result = interceptor.preSend(
                createOutboundMessage(
                        "/topic/notifications/20",
                        SESSION_ID),
                channel);

        assertNull(result);
    }

    @Test
    void disabledUserShouldNotReceiveNotifications() {
        mockSession(Role.PASSENGER, false);

        Message<?> result = interceptor.preSend(
                createOutboundMessage(
                        "/topic/notifications/10",
                        SESSION_ID),
                channel);

        assertNull(result);
    }

    @Test
    void staleJwtRoleShouldNotReceiveNotifications() {
        JwtAuthenticationToken authentication =
                createAuthentication(Role.VIEWER);

        mockSession(authentication, Role.PASSENGER, true);

        Message<?> result = interceptor.preSend(
                createOutboundMessage(
                        "/topic/notifications/10",
                        SESSION_ID),
                channel);

        assertNull(result);
    }

    @Test
    void missingSessionShouldNotReceiveProtectedMessages() {
        when(simpUserRegistry.getUsers()).thenReturn(Set.of());

        Message<?> result = interceptor.preSend(
                createOutboundMessage("/topic/flights", SESSION_ID),
                channel);

        assertNull(result);
    }

    @Test
    void missingSessionIdShouldNotReceiveProtectedMessages() {
        mockSession(Role.VIEWER, true);

        Message<?> result = interceptor.preSend(
                createOutboundMessage("/topic/flights", null),
                channel);

        assertNull(result);
    }

    @Test
    void nonexistentUserShouldNotReceiveProtectedMessages() {
        JwtAuthenticationToken authentication =
                createAuthentication(Role.VIEWER);

        mockSessionRegistry(authentication);

        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        Message<?> result = interceptor.preSend(
                createOutboundMessage("/topic/flights", SESSION_ID),
                channel);

        assertNull(result);
    }

    @Test
    void unrelatedOutboundMessageShouldRemainUnaffected() {
        Message<?> message = createOutboundMessage(
                "/topic/unrelated",
                SESSION_ID);

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
    }

    @Test
    void messageWithoutDestinationShouldRemainUnaffected() {
        Message<?> message = createOutboundMessage(null, SESSION_ID);

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
    }

    @Test
    void malformedNotificationDestinationShouldBeRejected() {
        mockSession(Role.PASSENGER, true);

        Message<?> result = interceptor.preSend(
                createOutboundMessage(
                        "/topic/notifications/not-a-user",
                        SESSION_ID),
                channel);

        assertNull(result);
    }

    private void assertFlightMessageAllowed(Role role) {
        mockSession(role, true);

        Message<?> message = createOutboundMessage(
                "/topic/flights",
                SESSION_ID);

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
    }

    private void assertFlightMessageRejected(Role role) {
        mockSession(role, true);

        Message<?> result = interceptor.preSend(
                createOutboundMessage("/topic/flights", SESSION_ID),
                channel);

        assertNull(result);
    }

    private void mockSession(Role role, boolean enabled) {
        mockSession(createAuthentication(role), role, enabled);
    }

    private void mockSession(
            JwtAuthenticationToken authentication,
            Role currentRole,
            boolean enabled) {

        mockSessionRegistry(authentication);

        AppUser user = new AppUser();
        user.setId(USER_ID);
        user.setUsername("user");
        user.setRole(currentRole);
        user.setEnabled(enabled);

        when(appUserRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
    }

    private void mockSessionRegistry(
            JwtAuthenticationToken authentication) {

        SimpUser user = mock(SimpUser.class);
        SimpSession session = mock(SimpSession.class);

        when(user.getSession(SESSION_ID)).thenReturn(session);
        when(user.getPrincipal()).thenReturn(authentication);
        when(simpUserRegistry.getUsers()).thenReturn(Set.of(user));
    }

    private Message<?> createOutboundMessage(
            String destination,
            String sessionId) {

        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);

        accessor.setDestination(destination);
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders());
    }

    private JwtAuthenticationToken createAuthentication(Role role) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("user")
                .claim("userId", USER_ID)
                .claim("role", role.name())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        return new JwtAuthenticationToken(
                jwt,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role.name())));
    }
}
