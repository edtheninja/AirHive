import { apiRequest } from "./client";

export interface Airport {
  id: number;
  iataCode: string;
  icaoCode: string;
  name: string;
  city: string;
  country: string;
  terminalCount: number;
  status: string;
}

export async function getAirports(): Promise<Airport[]> {
  return apiRequest<Airport[]>("/airports");
}

export async function getAirportById(id: number): Promise<Airport> {
  return apiRequest<Airport>(`/airports/${id}`);
}
