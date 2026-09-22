import { Client, type IMessage } from "@stomp/stompjs";
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
let flightSubscription: ReturnType<Client["subscribe"]> | null = null;
let notificationSubscription: ReturnType<Client["subscribe"]> | null = null;

let flightMessageHandler: ((event: FlightEvent) => void) | null = null;
let notificationMessageHandler: ((notification: Notification) => void) | null = null;
let notificationUserId: number | null = null;
client.beforeConnect = async () => {
  const token = getToken();

  client.connectHeaders = token
    ? {
        Authorization: `Bearer ${token}`,
      }
    : {};
};
client.onConnect = () => {
  if (flightMessageHandler && !flightSubscription) {
    flightSubscription = client.subscribe(FLIGHTS_TOPIC, (message: IMessage) => {
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

        flightMessageHandler?.(event as FlightEvent);
      } catch (error) {
        if (import.meta.env.DEV) {
          console.error("[STOMP] Failed to process flight event:", error);
        }
      }
    });
  }

  if (notificationMessageHandler && notificationUserId !== null && !notificationSubscription) {
    const topic = `/topic/notifications/${notificationUserId}`;

    notificationSubscription = client.subscribe(topic, (message: IMessage) => {
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

        notificationMessageHandler?.(notification);
      } catch (error) {
        if (import.meta.env.DEV) {
          console.error("[STOMP] Failed to process notification:", error);
        }
      }
    });
  }
};

client.onWebSocketClose = () => {
  flightSubscription = null;
  notificationSubscription = null;
};

export function connectToFlightUpdates(onMessage: (event: FlightEvent) => void) {
  flightMessageHandler = onMessage;

  if (!client.active) {
    client.activate();
  } else if (client.connected && !flightSubscription) {
    client.onConnect?.(client as never);
  }

  return () => {
    flightMessageHandler = null;
    flightSubscription?.unsubscribe();
    flightSubscription = null;
  };
}

export function connectToNotificationUpdates(
  userId: number,
  onMessage: (notification: Notification) => void,
) {
  notificationUserId = userId;
  notificationMessageHandler = onMessage;

  if (!client.active) {
    client.activate();
  } else if (client.connected && !notificationSubscription) {
    client.onConnect?.(client as never);
  }

  return () => {
    notificationMessageHandler = null;
    notificationUserId = null;
    notificationSubscription?.unsubscribe();
    notificationSubscription = null;
  };
}

export async function disconnectWebSocket() {
  flightSubscription?.unsubscribe();
  notificationSubscription?.unsubscribe();

  flightSubscription = null;
  notificationSubscription = null;

  flightMessageHandler = null;
  notificationMessageHandler = null;
  notificationUserId = null;

  if (client.active) {
    await client.deactivate();
  }
}
