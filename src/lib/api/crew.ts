import { apiRequest } from "./client";

export interface ApiCrewMember {
  id: number;
  crewCode: string;
  name: string;
  role: string;
  base: string;
  availability: "Available" | "On Duty" | "Resting" | "Leave";
  restHours: number;
  nextFlight: string;
  medical: string;
  initials: string;
}

export async function getCrew(): Promise<ApiCrewMember[]> {
  return apiRequest<ApiCrewMember[]>("/crew");
}
