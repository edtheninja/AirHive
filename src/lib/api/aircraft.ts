import { apiRequest } from "./client";

export interface ApiAircraft {
  id: number;
  registrationNumber: string;
  aircraftTypeId: number;
  aircraftTypeCode: string;
  status: string;
}

export async function getAircraft(): Promise<ApiAircraft[]> {
  return apiRequest<ApiAircraft[]>("/aircraft");
}

export async function getAircraftById(id: number): Promise<ApiAircraft> {
  return apiRequest<ApiAircraft>(`/aircraft/${id}`);
}
