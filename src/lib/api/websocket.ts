import { Client, type IMessage } from "@stomp/stompjs";

const WS_URL = "ws://localhost:8080/ws";
const FLIGHTS_TOPIC = "/topic/flights";

export type FlightEventType =
  | "FLIGHT_CREATED"
  | "FLIGHT_UPDATED"
  | "FLIGHT_DELETED";

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

export function connectToFlightUpdates(
  onMessage: (event: FlightEvent) => void,
) {
  client.onConnect = () => {
    subscription = client.subscribe(
      FLIGHTS_TOPIC,
      (message: IMessage) => {
        const event = JSON.parse(message.body) as FlightEvent;
        onMessage(event);
      },
    );
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
