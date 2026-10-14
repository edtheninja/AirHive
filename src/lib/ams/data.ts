export type FlightStatus =
  | "Scheduled"
  | "Boarding"
  | "Taxiing"
  | "Departed"
  | "In Air"
  | "Landing"
  | "Landed"
  | "Delayed"
  | "Cancelled";

export type Flight = {
  id: string;
  number: string;
  aircraft: string;
  aircraftModel: string;
  origin: string;
  destination: string;
  gate: string;
  boarding: number;
  departure: string;
  arrival: string;
  scheduledDeparture: string;
  scheduledArrival: string;
  delay: number;
  crew: string;
  crewCount: number;
  status: FlightStatus;
  pax: number;
};

export type OpsNotification = {
  id: string;
  title: string;
  detail: string;
  tone: "info" | "success" | "warning" | "danger";
  time: string;
};
