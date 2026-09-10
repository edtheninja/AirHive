package com.airhive.backend.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhive.backend.dto.FlightResponseDTO;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.notification.NotificationSeverity;
import com.airhive.backend.notification.NotificationType;
import com.airhive.backend.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class FlightNotificationServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private AppUserRepository appUserRepository;

    private FlightNotificationService flightNotificationService;

    private AppUser admin;
    private AppUser operator;
    private AppUser viewer;


    @BeforeEach
    void setUp() {
        flightNotificationService =
                new FlightNotificationService(
                        notificationService,
                        appUserRepository);

        admin = user(67L, "admin", Role.ADMIN);
        operator = user(66L, "operator", Role.OPERATOR);
        viewer = user(68L, "viewer", Role.VIEWER);
    }

    private void stubOperationsRecipients() {
        when(appUserRepository.findByRoleInAndEnabledTrue(
                List.of(Role.ADMIN, Role.OPERATOR)))
                .thenReturn(List.of(admin, operator));
    }

    @Test
    void delayedFlightCreatesWarningNotification() {
        stubOperationsRecipients();

        FlightResponseDTO flight = flight("DELAYED");

        flightNotificationService.notifyFlightStatusChange(
                flight,
                "SCHEDULED");

        verify(notificationService).createNotification(
                67L,
                NotificationType.FLIGHT_DELAYED,
                "Flight Delayed",
                "Flight AH101 status changed from SCHEDULED to DELAYED.",
                NotificationSeverity.WARNING,
                "FLIGHT",
                101L);

        verify(notificationService).createNotification(
                66L,
                NotificationType.FLIGHT_DELAYED,
                "Flight Delayed",
                "Flight AH101 status changed from SCHEDULED to DELAYED.",
                NotificationSeverity.WARNING,
                "FLIGHT",
                101L);
    }

    @Test
    void cancelledFlightCreatesCriticalNotification() {
        stubOperationsRecipients();

        stubOperationsRecipients();

        FlightResponseDTO flight = flight("CANCELLED");

        flightNotificationService.notifyFlightStatusChange(
                flight,
                "SCHEDULED");

        verify(notificationService).createNotification(
                67L,
                NotificationType.FLIGHT_CANCELLED,
                "Flight Cancelled",
                "Flight AH101 status changed from SCHEDULED to CANCELLED.",
                NotificationSeverity.CRITICAL,
                "FLIGHT",
                101L);

        verify(notificationService).createNotification(
                66L,
                NotificationType.FLIGHT_CANCELLED,
                "Flight Cancelled",
                "Flight AH101 status changed from SCHEDULED to CANCELLED.",
                NotificationSeverity.CRITICAL,
                "FLIGHT",
                101L);
    }

    @Test
    void normalStatusChangeCreatesInfoNotification() {
        stubOperationsRecipients();

        stubOperationsRecipients();

        FlightResponseDTO flight = flight("BOARDING");

        flightNotificationService.notifyFlightStatusChange(
                flight,
                "SCHEDULED");

        verify(notificationService).createNotification(
                67L,
                NotificationType.FLIGHT_STATUS_CHANGED,
                "Flight Status Changed",
                "Flight AH101 status changed from SCHEDULED to BOARDING.",
                NotificationSeverity.INFO,
                "FLIGHT",
                101L);
    }

    @Test
    void unchangedStatusCreatesNoNotification() {

        FlightResponseDTO flight = flight("SCHEDULED");

        flightNotificationService.notifyFlightStatusChange(
                flight,
                "SCHEDULED");

        verifyNoInteractions(notificationService);
        verifyNoInteractions(appUserRepository);
    }

    @Test
    void onlyEnabledAdminAndOperatorUsersReceiveNotifications() {
        stubOperationsRecipients();

        stubOperationsRecipients();

        FlightResponseDTO flight = flight("DELAYED");

        flightNotificationService.notifyFlightStatusChange(
                flight,
                "SCHEDULED");

        verify(notificationService).createNotification(
                67L,
                NotificationType.FLIGHT_DELAYED,
                "Flight Delayed",
                "Flight AH101 status changed from SCHEDULED to DELAYED.",
                NotificationSeverity.WARNING,
                "FLIGHT",
                101L);

        verify(notificationService).createNotification(
                66L,
                NotificationType.FLIGHT_DELAYED,
                "Flight Delayed",
                "Flight AH101 status changed from SCHEDULED to DELAYED.",
                NotificationSeverity.WARNING,
                "FLIGHT",
                101L);
    }

    private FlightResponseDTO flight(String status) {
        FlightResponseDTO flight = new FlightResponseDTO();
        flight.setId(101L);
        flight.setFlightNumber("AH101");
        flight.setStatus(status);
        return flight;
    }

    private AppUser user(Long id, String username, Role role) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }
}
