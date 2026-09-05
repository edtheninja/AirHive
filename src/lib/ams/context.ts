import { createContext } from "react";
import { type Flight, type OpsNotification } from "./data";

export type Kpis = {
  totalFlights: number;
  activeFlights: number;
  delayedFlights: number;
  fleetAvailability: number;
};

export type LiveOpsValue = {
  flights: Flight[];
  notifications: OpsNotification[];
  kpis: Kpis;
  loading: boolean;
  error: string | null;
  dismiss: (id: string) => void;
  push: (n: Omit<OpsNotification, "id" | "time">) => void;
};

export const LiveOpsContext = createContext<LiveOpsValue | null>(null);
