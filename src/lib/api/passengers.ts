import { apiRequest } from "./client";

export type Passenger = {
  id: number;
  passengerCode: string;
  name: string;
  tier: string;
  flight: string;
  route: string;
  seat: string;
  bags: number;
  checkedIn: boolean;
};

export async function getPassengers(): Promise<Passenger[]> {
  return apiRequest<Passenger[]>("/passengers");
}
