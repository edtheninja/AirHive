package com.airhive.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Notification;
import com.airhive.backend.entity.Role;
import com.airhive.backend.notification.NotificationSeverity;
import com.airhive.backend.notification.NotificationType;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void findByUserIdOrderByCreatedAtDesc_shouldReturnNewestFirst() {
        AppUser user = createUser("notification-user-1");

        Notification older = createNotification(
                user,
                "Older notification",
                false,
                LocalDateTime.now().minusMinutes(10)
        );

        Notification newer = createNotification(
                user,
                "Newer notification",
                false,
                LocalDateTime.now()
        );

        notificationRepository.saveAll(List.of(older, newer));

        List<Notification> result =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Newer notification");
        assertThat(result.get(1).getTitle()).isEqualTo("Older notification");
    }

    @Test
    void findByUserIdAndReadFalseOrderByCreatedAtDesc_shouldReturnOnlyUnread() {
        AppUser user = createUser("notification-user-2");

        Notification unread = createNotification(
                user,
                "Unread notification",
                false,
                LocalDateTime.now()
        );

        Notification read = createNotification(
                user,
                "Read notification",
                true,
                LocalDateTime.now().minusMinutes(5)
        );

        notificationRepository.saveAll(List.of(unread, read));

        List<Notification> result =
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Unread notification");
    }

    @Test
    void countByUserIdAndReadFalse_shouldCountUnreadNotifications() {
        AppUser user = createUser("notification-user-3");

        notificationRepository.saveAll(List.of(
                createNotification(
                        user,
                        "Unread 1",
                        false,
                        LocalDateTime.now()
                ),
                createNotification(
                        user,
                        "Unread 2",
                        false,
                        LocalDateTime.now().minusMinutes(1)
                ),
                createNotification(
                        user,
                        "Read",
                        true,
                        LocalDateTime.now().minusMinutes(2)
                )
        ));

        long count =
                notificationRepository.countByUserIdAndReadFalse(user.getId());

        assertThat(count).isEqualTo(2);
    }

    private AppUser createUser(String username) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword("test-password");
        user.setRole(Role.VIEWER);
        user.setEnabled(true);

        return appUserRepository.save(user);
    }

    private Notification createNotification(
            AppUser user,
            String title,
            boolean read,
            LocalDateTime createdAt) {

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.SYSTEM);
        notification.setTitle(title);
        notification.setMessage("Test notification");
        notification.setSeverity(NotificationSeverity.INFO);
        notification.setRead(read);
        notification.setCreatedAt(createdAt);

        return notification;
    }
}
