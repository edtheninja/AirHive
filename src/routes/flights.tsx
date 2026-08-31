import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { useEffect, useMemo, useState } from "react";
import { Search } from "lucide-react";

import { pageVariants } from "@/lib/ams/motion";
import { getFlights, type Flight as ApiFlight } from "@/lib/api/flights";
// Note: ApiFlight is the API type, Flight is the local type defined in this file
import { FlightsTable } from "@/components/ams/flights-table";
import { PageHeader, SectionCard } from "@/components/ams/primitives";
import type { FlightStatus, Flight } from "@/lib/ams/data";

export const Route = createFileRoute("/flights")({
  head: () => ({
    meta: [
      { title: "Flight Operations — Aerion AMS" },
      {
        name: "description",
        content: "Monitor and filter every scheduled, boarding, airborne and delayed flight.",
      },
      {
        property: "og:title",
        content: "Flight Operations — Aerion AMS",
      },
      {
        property: "og:description",
        content: "Monitor every scheduled, airborne and delayed flight in one board.",
      },
    ],
  }),
  component: FlightsPage,
});

const FILTERS: (FlightStatus | "All")[] = [
  "All",
  "Scheduled",
  "Boarding",
  "In Air",
  "Landed",
  "Delayed",
];

function formatTime(dateTime: string) {
  const date = new Date(dateTime);

  if (Number.isNaN(date.getTime())) {
    return dateTime;
  }

  return date.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
}

function mapFlight(flight: ApiFlight): Flight {
  return {
    id: String(flight.id),
    number: flight.flightNumber,
    aircraft: flight.aircraftRegistration,
    aircraftModel: "Aircraft",
    origin: flight.departureAirportCode,
    destination: flight.arrivalAirportCode,
    gate: "—",
    boarding: 0,
    departure: formatTime(flight.scheduledDeparture),
    delay: 0,
    crew: "—",
    status:
      flight.status === "SCHEDULED"
        ? "Scheduled"
        : flight.status === "BOARDING"
          ? "Boarding"
          : flight.status === "IN_AIR"
            ? "In Air"
            : flight.status === "LANDED"
              ? "Landed"
              : flight.status === "DELAYED"
                ? "Delayed"
                : (flight.status as FlightStatus),
    crewCount: 0,
    pax: 0,
  };
}

function FlightsPage() {
  const [flights, setFlights] = useState<Flight[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [filter, setFilter] = useState<FlightStatus | "All">("All");

  const [query, setQuery] = useState("");

  useEffect(() => {
    async function loadFlights() {
      try {
        setLoading(true);
        setError("");

        const data = await getFlights();

        setFlights(data.map(mapFlight));
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load flights.");
      } finally {
        setLoading(false);
      }
    }

    loadFlights();
  }, []);

  const visible = useMemo(
    () =>
      flights.filter(
        (flight) =>
          (filter === "All" || flight.status === filter) &&
          (query === "" ||
            `${flight.number}${flight.origin}${flight.destination}${flight.aircraft}`
              .toLowerCase()
              .includes(query.toLowerCase())),
      ),
    [flights, filter, query],
  );

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader
        title="Flights"
        description="Every movement across the network with live status transitions."
      />

      <SectionCard
        title="Flight board"
        subtitle={
          loading ? "Loading flights..." : `${visible.length} flights matching current filters`
        }
        action={
          <div className="flex items-center gap-2 rounded-2xl bg-foreground/4 px-3 py-2 text-sm">
            <Search className="h-4 w-4 text-muted-foreground" strokeWidth={1.7} />

            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Search flight or airport"
              className="w-52 bg-transparent outline-none placeholder:text-muted-foreground"
            />
          </div>
        }
      >
        <div className="mb-4 flex flex-wrap gap-2">
          {FILTERS.map((status) => (
            <motion.button
              key={status}
              whileTap={{ scale: 0.98 }}
              onClick={() => setFilter(status)}
              className={`rounded-full px-3.5 py-1.5 text-xs transition-colors ${
                filter === status
                  ? "bg-primary text-primary-foreground"
                  : "bg-foreground/5 text-muted-foreground hover:text-foreground"
              }`}
            >
              {status}
            </motion.button>
          ))}
        </div>

        {loading && (
          <div className="px-3 py-8 text-sm text-muted-foreground"> Loading flights...</div>
        )}

        {!loading && error && <div className="px-3 py-8 text-sm text-destructive">{error}</div>}

        {!loading && !error && <FlightsTable flights={visible} />}
      </SectionCard>
    </motion.div>
  );
}
