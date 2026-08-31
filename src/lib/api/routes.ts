import { apiRequest } from "./client";

export interface Route {
  id: number;
  departureAirportId: number;
  departureAirportCode: string;
  arrivalAirportId: number;
  arrivalAirportCode: string;
  distanceKm: number;
  estimatedDurationMinutes: number;
  status: string;
}

export async function getRoutes(): Promise<Route[]> {
  return apiRequest<Route[]>("/routes");
}

export async function getRouteById(id: number): Promise<Route> {
  return apiRequest<Route>(`/routes/${id}`);
}
