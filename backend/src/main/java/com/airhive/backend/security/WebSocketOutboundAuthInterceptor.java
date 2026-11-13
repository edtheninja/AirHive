
package com.airhive.backend.security;

import java.security.Principal;
import java.util.Set;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.repository.AppUserRepository;

@Component
public class WebSocketOutboundAuthInterceptor implements ChannelInterceptor {

    private static final String FLIGHTS_TOPIC = "/topic/flights";

    private static final String NOTIFICATIONS_TOPIC_PREFIX =
            "/topic/notifications/";

    private final SimpUserRegistry simpUserRegistry;
    private final AppUserRepository appUserRepository;

    public WebSocketOutboundAuthInterceptor(
            @Lazy SimpUserRegistry simpUserRegistry,
            AppUserRepository appUserRepository) {
        this.simpUserRegistry = simpUserRegistry;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.wrap(message);

        String destination = accessor.getDestination();

        if (destination == null) {
            return message;
        }

        if (FLIGHTS_TOPIC.equals(destination)) {
            return isAuthorizedForFlights(accessor) ? message : null;
        }

        if (destination.startsWith(NOTIFICATIONS_TOPIC_PREFIX)) {
            return isAuthorizedForNotifications(accessor, destination)
                    ? message
                    : null;
        }

        // Do not interfere with unrelated outbound messages.
        return message;
    }

    private boolean isAuthorizedForFlights(
            SimpMessageHeaderAccessor accessor) {

        AppUser user = resolveCurrentUser(accessor);

        if (user == null) {
            return false;
        }

        Role role = user.getRole();

        return role == Role.ADMIN
                || role == Role.OPERATOR
                || role == Role.VIEWER;
    }

    private boolean isAuthorizedForNotifications(
            SimpMessageHeaderAccessor accessor,
            String destination) {

        AppUser user = resolveCurrentUser(accessor);

        if (user == null) {
            return false;
        }

        String userIdPart = destination.substring(
                NOTIFICATIONS_TOPIC_PREFIX.length());

        if (userIdPart.isBlank() || !userIdPart.matches("\\d+")) {
            return false;
        }

        return user.getId() != null
                && user.getId().toString().equals(userIdPart);
    }

    private AppUser resolveCurrentUser(
            SimpMessageHeaderAccessor accessor) {

        String sessionId = accessor.getSessionId();

        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }

        Principal principal = resolveSessionPrincipal(sessionId);

        if (!(principal instanceof JwtAuthenticationToken authentication)
                || !authentication.isAuthenticated()) {
            return null;
        }

        Jwt jwt = authentication.getToken();

        String username = jwt.getSubject();
        String roleClaim = jwt.getClaimAsString("role");
        Long userId = extractUserId(jwt);

        if (username == null || username.isBlank()
                || roleClaim == null || roleClaim.isBlank()
                || userId == null || userId <= 0) {
            return null;
        }

        Role tokenRole;

        try {
            tokenRole = Role.valueOf(roleClaim);
        } catch (IllegalArgumentException exception) {
            return null;
        }

        AppUser user = appUserRepository.findById(userId)
                .orElse(null);

        if (user == null
                || !user.isEnabled()
                || !username.equals(user.getUsername())
                || user.getRole() != tokenRole) {
            return null;
        }

        return user;
    }

    private Principal resolveSessionPrincipal(String sessionId) {
        Set<SimpUser> users = simpUserRegistry.getUsers();

        if (users == null || users.isEmpty()) {
            return null;
        }

        for (SimpUser user : users) {
            if (user.getSession(sessionId) != null) {
                return user.getPrincipal();
            }
        }

        return null;
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
}
