import { apiRequest } from "./client";

export type NotificationType =
  | "FLIGHT_DELAYED"
  | "FLIGHT_CANCELLED"
  | "FLIGHT_STATUS_CHANGED"
  | "SCHEDULE_CONFLICT"
  | "SYSTEM";

export type NotificationSeverity = "INFO" | "WARNING" | "CRITICAL";

export type Notification = {
  id: number;
  userId: number;

  type: NotificationType;
  title: string;
  message: string;
  severity: NotificationSeverity;

  relatedEntityType?: string;
  relatedEntityId?: number;

  read: boolean;
  createdAt: string;
  readAt?: string;
};

export async function getNotifications(): Promise<Notification[]> {
  return apiRequest<Notification[]>("/notifications");
}

export async function getUnreadNotifications(): Promise<Notification[]> {
  return apiRequest<Notification[]>("/notifications/unread");
}

export async function getUnreadNotificationCount(): Promise<number> {
  return apiRequest<number>("/notifications/unread/count");
}

export async function markNotificationAsRead(
  id: number,
): Promise<Notification> {
  return apiRequest<Notification>(`/notifications/${id}/read`, {
    method: "PATCH",
  });
}

export async function markAllNotificationsAsRead(): Promise<number> {
  return apiRequest<number>("/notifications/read-all", {
    method: "PATCH",
  });
}
