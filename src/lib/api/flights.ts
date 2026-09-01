import { apiRequest } from "./client";

export type Flight = {
  id: number;
  flightNumber: string;

  aircraftId: number;
  aircraftRegistration: string;

  routeId: number;

  departureAirportId: number;
  departureAirportCode: string;

  arrivalAirportId: number;
  arrivalAirportCode: string;

  scheduledDeparture: string;
  scheduledArrival: string;

  distanceKm?: number;
  estimatedDurationMinutes?: number;

  status: string;
};

const API_BASE_URL = "http://localhost:8080";

export async function getFlights(): Promise<Flight[]> {
  const response = await fetch(`${API_BASE_URL}/api/flights`);

  if (!response.ok) {
    throw new Error(`Failed to fetch flights: ${response.status}`);
  }

  return response.json();
}
