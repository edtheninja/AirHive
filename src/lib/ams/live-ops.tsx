import { useEffect, useMemo, useState, type ReactNode } from "react";
import {
  FLIGHT_STATUS_FLOW,
  FLIGHTS,
  NOTIFICATION_POOL,
  SEED_NOTIFICATIONS,
  type Flight,
  type OpsNotification,
} from "./data";
import { getFlights, type ApiFlight } from "@/lib/api/flights";
import { getAircraft } from "@/lib/api/aircraft";
import { LiveOpsContext, type LiveOpsValue, type Kpis } from "./context";
function mapApiFlight(flight: ApiFlight): Flight {
  return {
    id: String(flight.id),
    number: flight.flightNumber,
    aircraft: flight.aircraftRegistration,
    aircraftModel: "—",
    origin: flight.departureAirportCode,
    destination: flight.arrivalAirportCode,
    gate: "—",
    boarding: 0,
    departure: new Date(flight.scheduledDeparture).toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    }),
    delay: 0,
    crew: "—",
    crewCount: 0,
    pax: 0,
    status: normalizeStatus(flight.status),
  };
}

function normalizeStatus(status: string): Flight["status"] {
  switch (status.toUpperCase()) {
    case "SCHEDULED":
      return "Scheduled";

    case "BOARDING":
      return "Boarding";

    case "IN_AIR":
    case "IN AIR":
    case "AIRBORNE":
      return "In Air";

    case "LANDED":
      return "Landed";

    case "DELAYED":
      return "Delayed";

    case "CANCELLED":
      return "Cancelled";

    default:
      return "Scheduled";
  }
}

function advance(flight: Flight): Flight {
  if (flight.status === "Cancelled") return flight;
  if (flight.status === "Boarding" && flight.boarding < 100) {
    return {
      ...flight,
      boarding: Math.min(100, flight.boarding + 6 + Math.round(Math.random() * 9)),
    };
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
  const [flights, setFlights] = useState<Flight[]>([]);
  const [notifications, setNotifications] = useState<OpsNotification[]>(SEED_NOTIFICATIONS);
  const [kpis, setKpis] = useState<Kpis>({
    totalFlights: 0,
    activeFlights: 0,
    delayedFlights: 0,
    revenueToday: 0,
    passengers: 0,
    fleetAvailability: 0,
  });
  useEffect(() => {
    let cancelled = false;

    async function loadDashboardData() {
      try {
        const [flightData, aircraftData] = await Promise.all([getFlights(), getAircraft()]);

        if (cancelled) return;

        const mappedFlights = flightData.map(mapApiFlight);

        setFlights(mappedFlights);

        const totalFlights = mappedFlights.length;

        const activeFlights = mappedFlights.filter((flight) => flight.status === "In Air").length;

        const delayedFlights = mappedFlights.filter((flight) => flight.status === "Delayed").length;

        const activeAircraft = aircraftData.filter(
          (aircraft) => aircraft.status.toUpperCase() === "ACTIVE",
        ).length;

        const fleetAvailability =
          aircraftData.length > 0 ? Math.round((activeAircraft / aircraftData.length) * 100) : 0;

        setKpis({
          totalFlights,
          activeFlights,
          delayedFlights,
          revenueToday: 0,
          passengers: 0,
          fleetAvailability,
        });
      } catch (error) {
        console.error("Failed to load dashboard data:", error);
      }
    }

    loadDashboardData();

    return () => {
      cancelled = true;
    };
  }, []);
  useEffect(() => {
    const notify = setInterval(() => {
      const seed = NOTIFICATION_POOL[Math.floor(Math.random() * NOTIFICATION_POOL.length)];
      setNotifications((prev) =>
        [{ ...seed, id: `N${Date.now()}`, time: "just now" }, ...prev].slice(0, 12),
      );
    }, 6500);

    return () => {
      clearInterval(notify);
    };
  }, []);

  const value = useMemo<LiveOpsValue>(
    () => ({
      flights,
      notifications,
      kpis,
      dismiss: (id) => setNotifications((prev) => prev.filter((n) => n.id !== id)),
      push: (n) =>
        setNotifications((prev) =>
          [{ ...n, id: `N${Date.now()}`, time: "just now" }, ...prev].slice(0, 12),
        ),
    }),
    [flights, notifications, kpis],
  );

  return <LiveOpsContext.Provider value={value}>{children}</LiveOpsContext.Provider>;
}
