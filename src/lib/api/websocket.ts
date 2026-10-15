import { Client, type IMessage, type StompSubscription } from "@stomp/stompjs";
import { getToken } from "@/lib/auth/session";

import type { Notification } from "./notifications";

const WS_URL = import.meta.env.VITE_WS_URL ?? "ws://localhost:8080/ws";
const FLIGHTS_TOPIC = "/topic/flights";

export type FlightEventType = "FLIGHT_CREATED" | "FLIGHT_UPDATED" | "FLIGHT_DELETED";

export type FlightEvent = {
  eventType: FlightEventType;
  flight: {
    id: number;
    flightNumber: string;
    aircraftId: number;
    aircraftRegistration: string;
    departureAirportId: number;
    departureAirportCode: string;
    arrivalAirportId: number;
    arrivalAirportCode: string;
    routeId: number;
    scheduledDeparture: string;
    scheduledArrival: string;
    status: string;
  };
};

type FlightHandler = (event: FlightEvent) => void;
type NotificationHandler = (notification: Notification) => void;

const client = new Client({
  brokerURL: WS_URL,
  connectHeaders: {},
  reconnectDelay: 5000,
  debug: (message) => {
    if (import.meta.env.DEV) {
      console.debug("[STOMP]", message);
    }
  },
});

const flightHandlers = new Set<FlightHandler>();
const notificationHandlers = new Map<number, Set<NotificationHandler>>();
const notificationSubscriptions = new Map<number, StompSubscription>();

let flightSubscription: StompSubscription | null = null;

client.beforeConnect = async () => {
  const token = getToken();

  client.connectHeaders = token ? { Authorization: `Bearer ${token}` } : {};
};

function processFlightMessage(message: IMessage) {
  try {
    const parsed: unknown = JSON.parse(message.body);

    if (!parsed || typeof parsed !== "object") {
      throw new Error("Invalid flight event payload");
    }

    const event = parsed as Partial<FlightEvent>;

    if (
      event.eventType !== "FLIGHT_CREATED" &&
      event.eventType !== "FLIGHT_UPDATED" &&
      event.eventType !== "FLIGHT_DELETED"
    ) {
      throw new Error(`Unknown flight event type: ${String(event.eventType)}`);
    }

    if (!event.flight || typeof event.flight !== "object") {
      throw new Error("Flight event is missing flight data");
    }

    flightHandlers.forEach((handler) => handler(event as FlightEvent));
  } catch (error) {
    if (import.meta.env.DEV) {
      console.error("[STOMP] Failed to process flight event:", error);
    }
  }
}

function processNotificationMessage(message: IMessage, userId: number) {
  try {
    const parsed: unknown = JSON.parse(message.body);

    if (!parsed || typeof parsed !== "object") {
      throw new Error("Invalid notification payload");
    }

    const notification = parsed as Notification;

    if (
      typeof notification.id !== "number" ||
      typeof notification.userId !== "number" ||
      typeof notification.title !== "string" ||
      typeof notification.message !== "string"
    ) {
      throw new Error("Invalid notification payload");
    }

    // Never deliver a notification to handlers registered for another user.
    if (notification.userId !== userId) {
      throw new Error("Notification user does not match subscription user");
    }

    notificationHandlers.get(userId)?.forEach((handler) => {
      handler(notification);
    });
  } catch (error) {
    if (import.meta.env.DEV) {
      console.error("[STOMP] Failed to process notification:", error);
    }
  }
}

function subscribeToFlightUpdates() {
  if (!client.connected || flightSubscription || flightHandlers.size === 0) {
    return;
  }

  flightSubscription = client.subscribe(FLIGHTS_TOPIC, processFlightMessage);
}

function subscribeToNotificationUpdates(userId: number) {
  if (
    !client.connected ||
    notificationSubscriptions.has(userId) ||
    !notificationHandlers.has(userId)
  ) {
    return;
  }

  notificationSubscriptions.set(
    userId,
    client.subscribe(`/topic/notifications/${userId}`, (message) => {
      processNotificationMessage(message, userId);
    }),
  );
}

function subscribeToActiveStreams() {
  subscribeToFlightUpdates();

  notificationHandlers.forEach((_handlers, userId) => {
    subscribeToNotificationUpdates(userId);
  });
}

client.onConnect = () => {
  subscribeToActiveStreams();
};

client.onWebSocketClose = () => {
  // The broker has closed the connection. STOMP will reconnect automatically.
  // New subscriptions will be created by onConnect after reconnection.
  flightSubscription = null;
  notificationSubscriptions.clear();
};

function ensureConnected() {
  if (!client.active) {
    void client.activate();
  } else if (client.connected) {
    // Handle consumers registering while the client is already connected.
    subscribeToActiveStreams();
  }
}

export function connectToFlightUpdates(onMessage: FlightHandler) {
  flightHandlers.add(onMessage);
  ensureConnected();

  return () => {
    flightHandlers.delete(onMessage);

    if (flightHandlers.size === 0 && flightSubscription) {
      flightSubscription.unsubscribe();
      flightSubscription = null;
    }
  };
}

export function connectToNotificationUpdates(userId: number, onMessage: NotificationHandler) {
  let handlers = notificationHandlers.get(userId);

  if (!handlers) {
    handlers = new Set<NotificationHandler>();
    notificationHandlers.set(userId, handlers);
  }

  handlers.add(onMessage);
  ensureConnected();

  return () => {
    const currentHandlers = notificationHandlers.get(userId);
    currentHandlers?.delete(onMessage);

    if (currentHandlers?.size === 0) {
      notificationHandlers.delete(userId);

      const subscription = notificationSubscriptions.get(userId);
      subscription?.unsubscribe();
      notificationSubscriptions.delete(userId);
    }
  };
}

export async function disconnectWebSocket() {
  flightSubscription?.unsubscribe();
  flightSubscription = null;

  notificationSubscriptions.forEach((subscription) => {
    subscription.unsubscribe();
  });
  notificationSubscriptions.clear();

  flightHandlers.clear();
  notificationHandlers.clear();

  if (client.active) {
    await client.deactivate();
  }
}
