package com.airhive.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airhive.backend.dto.NotificationResponseDTO;
import com.airhive.backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = getUserId(jwt);

        return ResponseEntity.ok(
                notificationService.getUserNotifications(userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = getUserId(jwt);

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = getUserId(jwt);

        return ResponseEntity.ok(
                notificationService.getUnreadCount(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        Long userId = getUserId(jwt);

        return ResponseEntity.ok(
                notificationService.markAsRead(userId, id));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Integer> markAllAsRead(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = getUserId(jwt);

        return ResponseEntity.ok(
                notificationService.markAllAsRead(userId));
    }

    private Long getUserId(Jwt jwt) {
        return jwt.getClaim("userId");
    }
}
