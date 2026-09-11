package com.airhive.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhive.backend.dto.NotificationResponseDTO;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Notification;
import com.airhive.backend.entity.Role;
import com.airhive.backend.exception.ResourceNotFoundException;
import com.airhive.backend.notification.NotificationSeverity;
import com.airhive.backend.notification.NotificationType;
import com.airhive.backend.notification.NotificationWebSocketService;
import com.airhive.backend.repository.AppUserRepository;
import com.airhive.backend.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private NotificationWebSocketService notificationWebSocketService;

    private NotificationService notificationService;

    private AppUser user;

    @BeforeEach
    void setUp() {
        notificationService =
        new NotificationService(
                notificationRepository,
                appUserRepository,
                notificationWebSocketService);

        user = new AppUser();
        user.setId(1L);
        user.setUsername("test-user");
        user.setRole(Role.VIEWER);
        user.setEnabled(true);
    }

    @Test
    void createNotification_shouldCreateAndReturnNotification() {
        when(appUserRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification notification = invocation.getArgument(0);
                    return notification;
                });

        NotificationResponseDTO result =
                notificationService.createNotification(
                        1L,
                        NotificationType.FLIGHT_DELAYED,
                        "Flight Delayed",
                        "Flight AH101 has been delayed.",
                        NotificationSeverity.WARNING,
                        "FLIGHT",
                        101L);

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.type()).isEqualTo(NotificationType.FLIGHT_DELAYED);
        assertThat(result.title()).isEqualTo("Flight Delayed");
        assertThat(result.message())
                .isEqualTo("Flight AH101 has been delayed.");
        assertThat(result.severity())
                .isEqualTo(NotificationSeverity.WARNING);
        assertThat(result.relatedEntityType()).isEqualTo("FLIGHT");
        assertThat(result.relatedEntityId()).isEqualTo(101L);
        assertThat(result.read()).isFalse();
        assertThat(result.createdAt()).isNotNull();

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_shouldThrowWhenUserDoesNotExist() {
        when(appUserRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                notificationService.createNotification(
                        99L,
                        NotificationType.SYSTEM,
                        "System",
                        "System notification",
                        NotificationSeverity.INFO,
                        null,
                        null))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void getUserNotifications_shouldReturnNotificationsNewestFirst() {
        Notification notification = createNotification(
                10L,
                false,
                LocalDateTime.now());

        when(appUserRepository.existsById(1L))
                .thenReturn(true);

        when(notificationRepository
                .findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponseDTO> result =
                notificationService.getUserNotifications(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);

        verify(notificationRepository)
                .findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getUnreadNotifications_shouldReturnOnlyUnreadNotifications() {
        Notification notification = createNotification(
                11L,
                false,
                LocalDateTime.now());

        when(appUserRepository.existsById(1L))
                .thenReturn(true);

        when(notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponseDTO> result =
                notificationService.getUnreadNotifications(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).read()).isFalse();

        verify(notificationRepository)
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(1L);
    }

    @Test
    void getUnreadCount_shouldReturnUnreadCount() {
        when(appUserRepository.existsById(1L))
                .thenReturn(true);

        when(notificationRepository.countByUserIdAndReadFalse(1L))
                .thenReturn(3L);

        long result = notificationService.getUnreadCount(1L);

        assertThat(result).isEqualTo(3L);
    }

    @Test
    void markAsRead_shouldMarkNotificationRead() {
        Notification notification =
                createNotification(
                        12L,
                        false,
                        LocalDateTime.now());

        when(notificationRepository.findById(12L))
                .thenReturn(Optional.of(notification));

        when(notificationRepository.save(notification))
                .thenReturn(notification);

        NotificationResponseDTO result =
                notificationService.markAsRead(1L, 12L);

        assertThat(result.read()).isTrue();
        assertThat(result.readAt()).isNotNull();

        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_shouldRejectNotificationBelongingToAnotherUser() {
        Notification notification =
                createNotification(
                        13L,
                        false,
                        LocalDateTime.now());

        AppUser anotherUser = new AppUser();
        anotherUser.setId(2L);
        notification.setUser(anotherUser);

        when(notificationRepository.findById(13L))
                .thenReturn(Optional.of(notification));

        assertThatThrownBy(() ->
                notificationService.markAsRead(1L, 13L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_shouldNotSaveAlreadyReadNotification() {
        Notification notification =
                createNotification(
                        14L,
                        true,
                        LocalDateTime.now());

        LocalDateTime originalReadAt = LocalDateTime.now().minusMinutes(5);
        notification.setReadAt(originalReadAt);

        when(notificationRepository.findById(14L))
                .thenReturn(Optional.of(notification));

        NotificationResponseDTO result =
                notificationService.markAsRead(1L, 14L);

        assertThat(result.read()).isTrue();
        assertThat(result.readAt()).isEqualTo(originalReadAt);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllAsRead_shouldMarkAllUnreadNotifications() {
        when(appUserRepository.existsById(1L)).thenReturn(true);
        Notification first =
                createNotification(
                        15L,
                        false,
                        LocalDateTime.now());

        Notification second =
                createNotification(
                        16L,
                        false,
                        LocalDateTime.now().minusMinutes(1));

        when(notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(first, second));

        int result = notificationService.markAllAsRead(1L);

        assertThat(result).isEqualTo(2);
        assertThat(first.isRead()).isTrue();
        assertThat(second.isRead()).isTrue();
        assertThat(first.getReadAt()).isNotNull();
        assertThat(second.getReadAt()).isNotNull();

        verify(notificationRepository).saveAll(List.of(first, second));
    }

    @Test
    void getUserNotifications_shouldThrowWhenUserDoesNotExist() {
        when(appUserRepository.existsById(99L))
                .thenReturn(false);

        assertThatThrownBy(() ->
                notificationService.getUserNotifications(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(notificationRepository, never())
                .findByUserIdOrderByCreatedAtDesc(any());
    }

    private Notification createNotification(
            Long id,
            boolean read,
            LocalDateTime createdAt) {

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.SYSTEM);
        notification.setTitle("Test notification");
        notification.setMessage("Test message");
        notification.setSeverity(NotificationSeverity.INFO);
        notification.setRead(read);
        notification.setCreatedAt(createdAt);

        try {
            var field = Notification.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(notification, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }

        return notification;
    }
}
