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

export async function updateFlightStatus(flight: Flight, status: string): Promise<Flight> {
  return apiRequest<Flight>(`/flights/${flight.id}`, {
    method: "PUT",
    body: JSON.stringify({
      flightNumber: flight.flightNumber,
      aircraftId: flight.aircraftId,
      routeId: flight.routeId,
      departureAirportId: flight.departureAirportId,
      arrivalAirportId: flight.arrivalAirportId,
      scheduledDeparture: flight.scheduledDeparture,
      scheduledArrival: flight.scheduledArrival,
      status,
    }),
  });
}
