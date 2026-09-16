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

export async function getFlights(): Promise<Flight[]> {
  return apiRequest<Flight[]>("/flights");
}

export async function getFlightById(id: number): Promise<Flight> {
  return apiRequest<Flight>(`/flights/${id}`);
}

export type FlightActivity = {
  id: number;
  eventType: string;
  message: string;
  previousStatus: string | null;
  currentStatus: string | null;
  reason: string | null;
  createdAt: string;
};

export async function getFlightActivities(id: number): Promise<FlightActivity[]> {
  return apiRequest<FlightActivity[]>(`/flights/${id}/activities`);
}

export async function updateFlightStatus(
  flight: Flight,
  status: string,
  reason?: string,
): Promise<Flight> {
  return apiRequest<Flight>(`/flights/${flight.id}/status`, {
    method: "PATCH",
    body: JSON.stringify({
      status,
      reason: reason?.trim() || null,
    }),
  });
}
