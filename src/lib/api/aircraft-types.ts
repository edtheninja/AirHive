import { apiRequest } from "./client";

export interface AircraftType {
  id: number;
  typeCode: string;
  manufacturer: string;
  model: string;
  passengerCapacity: number;
  crewCapacity: number;
}

export async function getAircraftTypes(): Promise<AircraftType[]> {
  return apiRequest<AircraftType[]>("/aircraft-types");
}

export async function getAircraftTypeById(id: number): Promise<AircraftType> {
  return apiRequest<AircraftType>(`/aircraft-types/${id}`);
}
