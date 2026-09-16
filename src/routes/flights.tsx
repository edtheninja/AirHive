import { createFileRoute, Outlet } from "@tanstack/react-router";
import { motion } from "motion/react";
import { Search, SlidersHorizontal, X } from "lucide-react";
import { useMemo, useState } from "react";
import { useLiveOps } from "@/lib/ams/hooks";
import { pageVariants } from "@/lib/ams/motion";
import type { Flight } from "@/lib/ams/data";
import { FlightsTable } from "@/components/ams/flights-table";
import { PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/flights")({
  head: () => ({
    meta: [
      { title: "Flight Operations — AirHive AMS" },
      {
        name: "description",
        content: "Monitor and filter every scheduled, boarding, airborne and delayed flight.",
      },
      {
        property: "og:title",
        content: "Flight Operations — AirHive AMS",
      },
      {
        property: "og:description",
        content: "Monitor and filter every scheduled, boarding, airborne and delayed flight in one board.",
      },
    ],
  }),
  component: FlightsPage,
});

const FILTERS = [
  { label: "All", value: "ALL" },
  { label: "Scheduled", value: "SCHEDULED" },
  { label: "Boarding", value: "BOARDING" },
  { label: "In Air", value: "IN_AIR" },
  { label: "Landed", value: "LANDED" },
  { label: "Delayed", value: "DELAYED" },
] as const;

const STATUS_MAP: Record<Exclude<(typeof FILTERS)[number]["value"], "ALL">, Flight["status"]> = {
  SCHEDULED: "Scheduled",
  BOARDING: "Boarding",
  IN_AIR: "In Air",
  LANDED: "Landed",
  DELAYED: "Delayed",
};

function FlightsPage() {
  const { flights, loading, error } = useLiveOps();

  const [filter, setFilter] = useState<(typeof FILTERS)[number]["value"]>("ALL");
  const [query, setQuery] = useState("");
  const [origin, setOrigin] = useState("ALL");
  const [destination, setDestination] = useState("ALL");
  const [departureDate, setDepartureDate] = useState("");

  const airports = useMemo(() => {
    const uniqueAirports = new Set<string>();

    flights.forEach((flight) => {
      uniqueAirports.add(flight.origin);
      uniqueAirports.add(flight.destination);
    });

    return Array.from(uniqueAirports).sort();
  }, [flights]);

  const hasAdvancedFilters =
    origin !== "ALL" || destination !== "ALL" || departureDate !== "";

  const clearFilters = () => {
    setFilter("ALL");
    setQuery("");
    setOrigin("ALL");
    setDestination("ALL");
    setDepartureDate("");
  };

  const visible = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    return flights.filter((flight) => {
      const matchesStatus = filter === "ALL" || flight.status === STATUS_MAP[filter];

      const searchable = [flight.number, flight.aircraft, flight.origin, flight.destination]
        .join(" ")
        .toLowerCase();

      const matchesSearch =
        normalizedQuery === "" || searchable.includes(normalizedQuery);

      const matchesOrigin = origin === "ALL" || flight.origin === origin;

      const matchesDestination =
        destination === "ALL" || flight.destination === destination;

      const matchesDepartureDate =
        departureDate === "" || flight.scheduledDeparture.slice(0, 10) === departureDate;

      return (
        matchesStatus &&
        matchesSearch &&
        matchesOrigin &&
        matchesDestination &&
        matchesDepartureDate
      );
    });
  }, [flights, filter, query, origin, destination, departureDate]);

  return (
    <>
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
            <div className="flex items-center gap-2 rounded-2xl bg-foreground/5 px-3 py-2 text-sm">
              <Search className="h-4 w-4 text-muted-foreground" strokeWidth={1.7} />

              <input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Search flight, aircraft or airport"
                className="w-52 bg-transparent outline-none placeholder:text-muted-foreground"
              />
            </div>
          }
        >
          <div className="mb-4 space-y-3">
            <div className="flex items-center gap-2 text-sm font-medium">
              <SlidersHorizontal className="h-4 w-4 text-muted-foreground" />
              Advanced filters
            </div>

            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <select
                value={origin}
                onChange={(event) => setOrigin(event.target.value)}
                className="rounded-2xl bg-foreground/5 px-3 py-2 text-sm outline-none"
                aria-label="Filter by origin airport"
              >
                <option value="ALL">All origin airports</option>
                {airports.map((airport) => (
                  <option key={airport} value={airport}>
                    From {airport}
                  </option>
                ))}
              </select>

              <select
                value={destination}
                onChange={(event) => setDestination(event.target.value)}
                className="rounded-2xl bg-foreground/5 px-3 py-2 text-sm outline-none"
                aria-label="Filter by destination airport"
              >
                <option value="ALL">All destination airports</option>
                {airports.map((airport) => (
                  <option key={airport} value={airport}>
                    To {airport}
                  </option>
                ))}
              </select>

              <input
                type="date"
                value={departureDate}
                onChange={(event) => setDepartureDate(event.target.value)}
                className="rounded-2xl bg-foreground/5 px-3 py-2 text-sm outline-none"
                aria-label="Filter by scheduled departure date"
              />

              <button
                type="button"
                onClick={clearFilters}
                disabled={filter === "ALL" && query === "" && !hasAdvancedFilters}
                className="inline-flex items-center justify-center gap-2 rounded-2xl bg-foreground/5 px-3 py-2 text-sm text-muted-foreground transition-colors hover:text-foreground disabled:cursor-not-allowed disabled:opacity-40"
              >
                <X className="h-4 w-4" />
                Clear filters
              </button>
            </div>
          </div>

          <div className="mb-4 flex flex-wrap gap-2">
            {FILTERS.map((status) => (
              <motion.button
                key={status.value}
                whileTap={{ scale: 0.98 }}
                onClick={() => setFilter(status.value)}
                className={`rounded-full px-3.5 py-1.5 text-xs transition-colors ${
                  filter === status.value
                    ? "bg-primary text-primary-foreground"
                    : "bg-foreground/5 text-muted-foreground hover:text-foreground"
                }`}
              >
                {status.label}
              </motion.button>
            ))}
          </div>

          {loading && (
            <div className="px-3 py-8 text-sm text-muted-foreground">Loading flights...</div>
          )}

          {!loading && error && (
            <div className="px-3 py-8 text-sm text-destructive">{error}</div>
          )}

          {!loading && !error && visible.length === 0 && (
            <div className="px-3 py-8 text-sm text-muted-foreground">
              No flights match the current filters.
            </div>
          )}

          {!loading && !error && visible.length > 0 && <FlightsTable flights={visible} />}
        </SectionCard>
      </motion.div>
      <Outlet />
    </>
  );
}
