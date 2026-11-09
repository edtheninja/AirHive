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

type BookingApiResponse = {
  id: number;
  pnr: string;
  passenger: string;
  flight: string;
  route: string;
  cabin: string;
  seat: string;
  status: string;
  amount: number;
  travelDate: string;
};

export type CreateBookingRequest = {
  flightId: number;
  passenger: string;
  cabin: "ECONOMY";
};

function normalizeCabin(cabin: string): Booking["cabin"] {
  switch (cabin.toUpperCase()) {
    case "ECONOMY":
      return "Economy";
    case "PREMIUM":
      return "Premium";
    case "BUSINESS":
      return "Business";
    case "FIRST":
      return "First";
    default:
      return "Economy";
  }
}

function normalizeStatus(status: string): Booking["status"] {
  switch (status.toUpperCase().replace(/_/g, " ")) {
    case "CONFIRMED":
      return "Confirmed";
    case "CHECKED IN":
      return "Checked In";
    case "PENDING":
      return "Pending";
    case "CANCELLED":
      return "Cancelled";
    default:
      return "Pending";
  }
}

function normalizeBooking(booking: BookingApiResponse): Booking {
  return {
    ...booking,
    cabin: normalizeCabin(booking.cabin),
    status: normalizeStatus(booking.status),
    amount: Number(booking.amount),
  };
}

export async function getBookings(): Promise<Booking[]> {
  const result = await apiRequest<BookingApiResponse[]>("/bookings");

  return result.map(normalizeBooking);
}

export async function createBooking(request: CreateBookingRequest): Promise<Booking> {
  const result = await apiRequest<BookingApiResponse>("/bookings", {
    method: "POST",
    body: JSON.stringify(request),
  });

  return normalizeBooking(result);
}
