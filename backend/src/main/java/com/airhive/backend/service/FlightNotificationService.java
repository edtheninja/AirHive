package com.airhive.backend.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.airhive.backend.dto.FlightResponseDTO;
import com.airhive.backend.entity.AppUser;
import com.airhive.backend.entity.Role;
import com.airhive.backend.notification.NotificationSeverity;
import com.airhive.backend.notification.NotificationType;
import com.airhive.backend.repository.AppUserRepository;

@Service
public class FlightNotificationService {

    private final NotificationService notificationService;
    private final AppUserRepository appUserRepository;

    public FlightNotificationService(
            NotificationService notificationService,
            AppUserRepository appUserRepository) {
        this.notificationService = notificationService;
        this.appUserRepository = appUserRepository;
    }

    public void notifyFlightStatusChange(
            FlightResponseDTO flight,
            String previousStatus) {

        String currentStatus = normalizeStatus(flight.getStatus());
        String oldStatus = normalizeStatus(previousStatus);

        if (currentStatus.equals(oldStatus)) {
            {
                return;
            }
        }
            NotificationType type;
            NotificationSeverity severity;
            String title;

            switch (currentStatus) {
                case "DELAYED" -> {
                    type = NotificationType.FLIGHT_DELAYED;
                    severity = NotificationSeverity.WARNING;
                    title = "Flight Delayed";
                }
                case "CANCELLED" -> {
                    type = NotificationType.FLIGHT_CANCELLED;
                    severity = NotificationSeverity.CRITICAL;
                    title = "Flight Cancelled";
                }
                default -> {
                    type = NotificationType.FLIGHT_STATUS_CHANGED;
                    severity = NotificationSeverity.INFO;
                    title = "Flight Status Changed";
                }
            }

            String message = String.format(
                    "Flight %s status changed from %s to %s.",
                    flight.getFlightNumber(),
                    previousStatus,
                    currentStatus);

            notifyOperations(
                    type,
                    title,
                    message,
                    severity,
                    flight.getId());
        }

    

    private void notifyOperations(
            NotificationType type,
            String title,
            String message,
            NotificationSeverity severity,
            Long flightId) {

        Collection<Role> roles = List.of(
                Role.ADMIN,
                Role.OPERATOR);

        List<AppUser> recipients = appUserRepository.findByRoleInAndEnabledTrue(roles);

        for (AppUser user : recipients) {
            notificationService.createNotification(
                    user.getId(),
                    type,
                    title,
                    message,
                    severity,
                    "FLIGHT",
                    flightId);
        }
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }

        return status.trim().toUpperCase();
    }
}
