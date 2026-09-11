package com.airhive.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airhive.backend.dto.NotificationResponseDTO;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Notification;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.notification.NotificationSeverity;
import com.airhive.backend.notification.NotificationType;
import com.airhive.backend.notification.NotificationWebSocketService;
import com.airhive.backend.repository.AppUserRepository;
import com.airhive.backend.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AppUserRepository appUserRepository;
    private final NotificationWebSocketService notificationWebSocketService;

    public NotificationService(
            NotificationRepository notificationRepository,
            AppUserRepository appUserRepository,
            NotificationWebSocketService notificationWebSocketService) {
        this.notificationRepository = notificationRepository;
        this.appUserRepository = appUserRepository;
        this.notificationWebSocketService = notificationWebSocketService;
    }

    @Transactional
    public NotificationResponseDTO createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            NotificationSeverity severity,
            String relatedEntityType,
            Long relatedEntityId) {

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setSeverity(severity);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        NotificationResponseDTO response = toResponse(notificationRepository.save(notification));

        notificationWebSocketService.publishNotification(response);

        return response;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUserNotifications(Long userId) {
        ensureUserExists(userId);

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUnreadNotifications(Long userId) {
        ensureUserExists(userId);

        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void clearNotifications(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }

    public long getUnreadCount(Long userId) {
        ensureUserExists(userId);

        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponseDTO markAsRead(Long userId, Long notificationId) {
        Notification notification = findUserNotification(userId, notificationId);

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        ensureUserExists(userId);

        List<Notification> notifications = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);

        LocalDateTime readAt = LocalDateTime.now();

        notifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(readAt);
        });

        notificationRepository.saveAll(notifications);

        return notifications.size();
    }

    private Notification findUserNotification(
            Long userId,
            Long notificationId) {

        return notificationRepository.findById(notificationId)
                .filter(notification -> notification.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));
    }

    private void ensureUserExists(Long userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + userId);
        }
    }

    private NotificationResponseDTO toResponse(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getUser().getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getSeverity(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt());
    }
}
