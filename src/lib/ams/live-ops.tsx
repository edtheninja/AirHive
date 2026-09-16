import { useEffect, useMemo, useState, type ReactNode } from "react";
import { type Flight, type OpsNotification } from "./data";
import { connectToFlightUpdates, type FlightEvent } from "@/lib/api/websocket";
import { getFlights, type Flight as ApiFlight } from "@/lib/api/flights";
import { getAircraft } from "@/lib/api/aircraft";
import { useNotifications } from "@/lib/notifications/notification-context";
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
    arrival: new Date(flight.scheduledArrival).toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    }),
    scheduledDeparture: flight.scheduledDeparture,
    scheduledArrival: flight.scheduledArrival,
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

    case "TAXIING":
      return "Taxiing";

    case "DEPARTED":
      return "Departed";

    case "IN_AIR":
    case "IN AIR":
    case "AIRBORNE":
      return "In Air";

    case "LANDING":
      return "Landing";

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

export function LiveOpsProvider({ children }: { children: ReactNode }) {
  const [flights, setFlights] = useState<Flight[]>([]);
  const { notifications: apiNotifications } = useNotifications();

  const notifications = useMemo<OpsNotification[]>(
    () =>
      apiNotifications.map((notification) => ({
        id: String(notification.id),
        title: notification.title,
        detail: notification.message,
        tone:
          notification.severity === "CRITICAL"
            ? "danger"
            : notification.severity === "WARNING"
              ? "warning"
              : "info",
        time: new Date(notification.createdAt).toLocaleString(),
      })),
    [apiNotifications],
  );
  const [kpis, setKpis] = useState<Kpis>({
    totalFlights: 0,
    activeFlights: 0,
    delayedFlights: 0,
    fleetAvailability: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    let cancelled = false;

    async function loadDashboardData() {
      setLoading(true);
      setError(null);
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
          fleetAvailability,
        });
      } catch (error) {
        console.error("Failed to load dashboard data:", error);
        setError("Unable to load live operations data.");
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadDashboardData();

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    setKpis((current) => ({
      ...current,
      totalFlights: flights.length,
      activeFlights: flights.filter((flight) => flight.status === "In Air").length,
      delayedFlights: flights.filter((flight) => flight.status === "Delayed").length,
    }));
  }, [flights]);

  useEffect(() => {
    const handleFlightEvent = (event: FlightEvent) => {
      const updatedFlight = mapApiFlight(event.flight);

      setFlights((currentFlights) => {
        switch (event.eventType) {
          case "FLIGHT_CREATED":
            return [...currentFlights, updatedFlight];

          case "FLIGHT_UPDATED":
            return currentFlights.map((flight) =>
              flight.id === updatedFlight.id ? updatedFlight : flight,
            );

          case "FLIGHT_DELETED":
            return currentFlights.filter((flight) => flight.id !== updatedFlight.id);

          default:
            return currentFlights;
        }
      });
    };

    const disconnect = connectToFlightUpdates(handleFlightEvent);

    return () => {
      disconnect();
    };
  }, []);

  const value = useMemo<LiveOpsValue>(
    () => ({
      flights,
      notifications,
      kpis,
      loading,
      error,
      dismiss: () => undefined,
      push: () => undefined,
    }),
    [flights, notifications, kpis, loading, error],
  );

  return <LiveOpsContext.Provider value={value}>{children}</LiveOpsContext.Provider>;
}
