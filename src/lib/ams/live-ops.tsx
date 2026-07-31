import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import {
  FLIGHTS,
  FLIGHT_STATUS_FLOW,
  NOTIFICATION_POOL,
  SEED_NOTIFICATIONS,
  type Flight,
  type OpsNotification,
} from "./data";

export type Kpis = {
  totalFlights: number;
  activeFlights: number;
  delayedFlights: number;
  revenueToday: number;
  passengers: number;
  fleetAvailability: number;
};

type LiveOpsValue = {
  flights: Flight[];
  notifications: OpsNotification[];
  kpis: Kpis;
  dismiss: (id: string) => void;
  push: (n: Omit<OpsNotification, "id" | "time">) => void;
};

const LiveOpsContext = createContext<LiveOpsValue | null>(null);

function advance(flight: Flight): Flight {
  if (flight.status === "Cancelled") return flight;
  if (flight.status === "Boarding" && flight.boarding < 100) {
    return { ...flight, boarding: Math.min(100, flight.boarding + 6 + Math.round(Math.random() * 9)) };
  }
  if (flight.status === "Delayed") {
    return Math.random() > 0.75 ? { ...flight, status: "Boarding", boarding: 10 } : flight;
  }
  const idx = FLIGHT_STATUS_FLOW.indexOf(flight.status);
  if (idx === -1 || idx === FLIGHT_STATUS_FLOW.length - 1) return flight;
  if (Math.random() > 0.55) {
    const next = FLIGHT_STATUS_FLOW[idx + 1];
    return { ...flight, status: next, boarding: next === "Boarding" ? 8 : flight.boarding };
  }
  return flight;
}

export function LiveOpsProvider({ children }: { children: ReactNode }) {
  const [flights, setFlights] = useState<Flight[]>(FLIGHTS);
  const [notifications, setNotifications] = useState<OpsNotification[]>(SEED_NOTIFICATIONS);
  const [kpis, setKpis] = useState<Kpis>({
    totalFlights: 248,
    activeFlights: 64,
    delayedFlights: 7,
    revenueToday: 1284500,
    passengers: 38420,
    fleetAvailability: 92,
  });

  useEffect(() => {
    const tick = setInterval(() => {
      setFlights((prev) => prev.map((f) => (Math.random() > 0.6 ? advance(f) : f)));
      setKpis((prev) => ({
        totalFlights: prev.totalFlights + (Math.random() > 0.7 ? 1 : 0),
        activeFlights: Math.max(40, Math.min(88, prev.activeFlights + Math.round((Math.random() - 0.5) * 4))),
        delayedFlights: Math.max(2, Math.min(18, prev.delayedFlights + Math.round((Math.random() - 0.5) * 2))),
        revenueToday: prev.revenueToday + Math.round(Math.random() * 9000),
        passengers: prev.passengers + Math.round(Math.random() * 140),
        fleetAvailability: Math.max(78, Math.min(99, prev.fleetAvailability + Math.round((Math.random() - 0.5) * 3))),
      }));
    }, 3200);

    const notify = setInterval(() => {
      const seed = NOTIFICATION_POOL[Math.floor(Math.random() * NOTIFICATION_POOL.length)];
      setNotifications((prev) =>
        [{ ...seed, id: `N${Date.now()}`, time: "just now" }, ...prev].slice(0, 12),
      );
    }, 6500);

    return () => {
      clearInterval(tick);
      clearInterval(notify);
    };
  }, []);

  const value = useMemo<LiveOpsValue>(
    () => ({
      flights,
      notifications,
      kpis,
      dismiss: (id) => setNotifications((prev) => prev.filter((n) => n.id !== id)),
      push: (n) => setNotifications((prev) => [{ ...n, id: `N${Date.now()}`, time: "just now" }, ...prev].slice(0, 12)),
    }),
    [flights, notifications, kpis],
  );

  return <LiveOpsContext.Provider value={value}>{children}</LiveOpsContext.Provider>;
}

export function useLiveOps() {
  const ctx = useContext(LiveOpsContext);
  if (!ctx) throw new Error("useLiveOps must be used inside LiveOpsProvider");
  return ctx;
}
