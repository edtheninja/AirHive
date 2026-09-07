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
