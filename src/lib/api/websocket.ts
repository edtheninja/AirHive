import { Client, type IMessage } from "@stomp/stompjs";

const WS_URL = "ws://localhost:8080/ws";
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
  reconnectDelay: 5000,
  debug: (message) => {
    if (import.meta.env.DEV) {
      console.debug("[STOMP]", message);
    }
  },
});

let subscription: ReturnType<Client["subscribe"]> | null = null;

export function connectToFlightUpdates(onMessage: (event: FlightEvent) => void) {
  client.onConnect = () => {
  subscription = client.subscribe(FLIGHTS_TOPIC, (message: IMessage) => {
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

      onMessage(event as FlightEvent);
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("[STOMP] Failed to process flight event:", error);
      }
    }
  });
};

  client.activate();

  return () => {
    subscription?.unsubscribe();
    subscription = null;
  };
}

export async function disconnectWebSocket() {
  subscription?.unsubscribe();
  subscription = null;

  if (client.active) {
    await client.deactivate();
  }
}
