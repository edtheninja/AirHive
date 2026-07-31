import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { useMemo, useState } from "react";
import { Search } from "lucide-react";
import { pageVariants } from "@/lib/ams/motion";
import { useLiveOps } from "@/lib/ams/live-ops";
import { FlightsTable } from "@/components/ams/flights-table";
import { PageHeader, SectionCard } from "@/components/ams/primitives";
import type { FlightStatus } from "@/lib/ams/data";

export const Route = createFileRoute("/flights")({
  head: () => ({
    meta: [
      { title: "Flight Operations — Aerion AMS" },
      { name: "description", content: "Monitor and filter every scheduled, boarding, airborne and delayed flight." },
      { property: "og:title", content: "Flight Operations — Aerion AMS" },
      { property: "og:description", content: "Monitor every scheduled, airborne and delayed flight in one board." },
    ],
  }),
  component: FlightsPage,
});

const FILTERS: (FlightStatus | "All")[] = ["All", "Scheduled", "Boarding", "In Air", "Landed", "Delayed"];

function FlightsPage() {
  const { flights } = useLiveOps();
  const [filter, setFilter] = useState<FlightStatus | "All">("All");
  const [query, setQuery] = useState("");

  const visible = useMemo(
    () =>
      flights.filter(
        (f) =>
          (filter === "All" || f.status === filter) &&
          (query === "" ||
            `${f.number}${f.origin}${f.destination}${f.aircraft}`.toLowerCase().includes(query.toLowerCase())),
      ),
    [flights, filter, query],
  );

  return (
    <motion.div variants={pageVariants} initial="initial" animate="animate" className="mx-auto max-w-[1600px] space-y-6 py-6">
      <PageHeader title="Flights" description="Every movement across the network with live status transitions." />

      <SectionCard
        title="Flight board"
        subtitle={`${visible.length} flights matching current filters`}
        action={
          <div className="flex items-center gap-2 rounded-2xl bg-foreground/4 px-3 py-2 text-sm">
            <Search className="h-4 w-4 text-muted-foreground" strokeWidth={1.7} />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search flight or airport"
              className="w-52 bg-transparent outline-none placeholder:text-muted-foreground"
            />
          </div>
        }
      >
        <div className="mb-4 flex flex-wrap gap-2">
          {FILTERS.map((f) => (
            <motion.button
              key={f}
              whileTap={{ scale: 0.98 }}
              onClick={() => setFilter(f)}
              className={`rounded-full px-3.5 py-1.5 text-xs transition-colors ${
                filter === f ? "bg-primary text-primary-foreground" : "bg-foreground/5 text-muted-foreground hover:text-foreground"
              }`}
            >
              {f}
            </motion.button>
          ))}
        </div>
        <FlightsTable flights={visible} />
      </SectionCard>
    </motion.div>
  );
}
