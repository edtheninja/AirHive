import { apiRequest } from "./client";

export interface ApiFlight {
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
  status: string;
}

export async function getFlights(): Promise<ApiFlight[]> {
  return apiRequest<ApiFlight[]>("/flights");
}

export async function getFlightById(id: number): Promise<ApiFlight> {
  return apiRequest<ApiFlight>(`/flights/${id}`);
}
