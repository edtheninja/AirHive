import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { Building2 } from "lucide-react";
import { useEffect, useState } from "react";
import { pageVariants, itemVariants, listVariants } from "@/lib/ams/motion";
import { getAirports, type Airport } from "@/lib/api/airports";
import { GlassCard, PageHeader } from "@/components/ams/primitives";

export const Route = createFileRoute("/airports")({
  head: () => ({
    meta: [
      { title: "Airports & Stations — Aerion AMS" },
      {
        name: "description",
        content: "Station status, terminal counts and daily movements across the network.",
      },
      {
        property: "og:title",
        content: "Airports & Stations — Aerion AMS",
      },
      {
        property: "og:description",
        content: "Station status and daily movements across the network.",
      },
    ],
  }),
  component: AirportsPage,
});

function AirportsPage() {
  const [airports, setAirports] = useState<Airport[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadAirports() {
      try {
        const data = await getAirports();
        setAirports(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load airports");
      } finally {
        setLoading(false);
      }
    }

    loadAirports();
  }, []);

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader title="Airports" description="Station readiness and daily movement volumes." />

      {loading && (
        <div className="py-12 text-center text-sm text-muted-foreground">Loading airports...</div>
      )}

      {error && (
        <GlassCard className="p-5">
          <p className="text-sm text-destructive">{error}</p>
        </GlassCard>
      )}

      {!loading && !error && (
        <motion.div
          variants={listVariants}
          initial="initial"
          animate="animate"
          className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4"
        >
          {airports.map((airport) => (
            <motion.div key={airport.id} variants={itemVariants}>
              <GlassCard className="p-5">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="num text-2xl font-semibold">{airport.iataCode}</p>

                    <p className="mt-1 text-sm text-muted-foreground">
                      {airport.city}, {airport.country}
                    </p>
                  </div>

                  <span className="flex h-9 w-9 items-center justify-center rounded-2xl bg-accent/12 text-accent">
                    <Building2 className="h-4.5 w-4.5" strokeWidth={1.7} />
                  </span>
                </div>

                <div className="mt-6 flex items-center justify-between text-xs">
                  <span className="text-muted-foreground">
                    <span className="num text-foreground">{airport.terminalCount}</span> terminals
                  </span>

                  <span className="text-muted-foreground">
                    <span className="num text-foreground">{airport.icaoCode}</span> ICAO
                  </span>
                </div>

                <span className="mt-4 inline-block rounded-full bg-success/14 px-2.5 py-1 text-xs text-success">
                  {airport.status}
                </span>
              </GlassCard>
            </motion.div>
          ))}
        </motion.div>
      )}
    </motion.div>
  );
}
