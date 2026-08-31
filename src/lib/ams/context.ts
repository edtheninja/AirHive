import { createContext } from "react";
import { type Flight, type OpsNotification } from "./data";

export type Kpis = {
  totalFlights: number;
  activeFlights: number;
  delayedFlights: number;
  revenueToday: number;
  passengers: number;
  fleetAvailability: number;
};

export type LiveOpsValue = {
  flights: Flight[];
  notifications: OpsNotification[];
  kpis: Kpis;
  dismiss: (id: string) => void;
  push: (n: Omit<OpsNotification, "id" | "time">) => void;
};

export const LiveOpsContext = createContext<LiveOpsValue | null>(null);
