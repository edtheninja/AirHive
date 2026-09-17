import { apiRequest } from "./client";

export type Booking = {
  id: number;
  pnr: string;
  passenger: string;
  flight: string;
  route: string;
  cabin: "Economy" | "Premium" | "Business" | "First";
  seat: string;
  status: "Confirmed" | "Checked In" | "Pending" | "Cancelled";
  amount: number;
  travelDate: string;
};

export async function getBookings(): Promise<Booking[]> {
  return apiRequest<Booking[]>("/bookings");
}
