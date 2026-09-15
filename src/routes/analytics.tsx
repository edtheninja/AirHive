import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { pageVariants } from "@/lib/ams/motion";
import { PageHeader, SectionCard } from "@/components/ams/primitives";
import { useLiveOps } from "@/lib/ams/hooks";

export const Route = createFileRoute("/analytics")({
  head: () => ({
    meta: [
      { title: "Analytics — AirHive AMS" },
      {
        name: "description",
        content: "Operational flight and fleet analytics across the network.",
      },
      { property: "og:title", content: "Analytics — AirHive AMS" },
      {
        property: "og:description",
        content: "Operational flight and fleet analytics across the network.",
      },
    ],
  }),
  component: AnalyticsPage,
});

function AnalyticsPage() {
  const { flights, kpis, loading, error } = useLiveOps();

  const flightStatusCounts = flights.reduce<Record<string, number>>((counts, flight) => {
    counts[flight.status] = (counts[flight.status] ?? 0) + 1;
    return counts;
  }, {});

  const statusRows = [
    ["Scheduled", flightStatusCounts.SCHEDULED ?? 0],
    ["Boarding", flightStatusCounts.BOARDING ?? 0],
    ["In air", flightStatusCounts["IN AIR"] ?? 0],
    ["Landed", flightStatusCounts.LANDED ?? 0],
    ["Delayed", flightStatusCounts.DELAYED ?? 0],
    ["Cancelled", flightStatusCounts.CANCELLED ?? 0],
  ] as const;

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader
        title="Analytics"
        description="Operational performance across the AirHive network."
      />

      {loading ? (
        <SectionCard title="Operational analytics" subtitle="Loading live flight data">
          <p className="text-sm text-muted-foreground">Loading analytics…</p>
        </SectionCard>
      ) : error ? (
        <SectionCard title="Operational analytics" subtitle="Live data unavailable">
          <p className="text-sm text-destructive">{error}</p>
        </SectionCard>
      ) : (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <SectionCard title="Total flights" subtitle="Current network">
              <p className="num mt-4 text-3xl font-semibold">{kpis.totalFlights}</p>
            </SectionCard>

            <SectionCard title="Currently airborne" subtitle="Flights in air">
              <p className="num mt-4 text-3xl font-semibold">{kpis.activeFlights}</p>
            </SectionCard>

            <SectionCard title="Currently delayed" subtitle="Active delays">
              <p className="num mt-4 text-3xl font-semibold">{kpis.delayedFlights}</p>
            </SectionCard>

            <SectionCard title="Fleet availability" subtitle="Based on active fleet">
              <p className="num mt-4 text-3xl font-semibold">{kpis.fleetAvailability}%</p>
            </SectionCard>
          </div>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <SectionCard title="Flight status" subtitle="Current distribution across the network">
              <dl className="space-y-3 text-sm">
                {statusRows.map(([label, value]) => (
                  <div key={label} className="flex items-center justify-between">
                    <dt className="text-muted-foreground">{label}</dt>
                    <dd className="num font-medium">{value}</dd>
                  </div>
                ))}
              </dl>
            </SectionCard>

            <SectionCard title="Operational snapshot" subtitle="Live network indicators">
              <dl className="space-y-3 text-sm">
                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Flights tracked</dt>
                  <dd className="num font-medium">{flights.length}</dd>
                </div>
                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Airborne share</dt>
                  <dd className="num font-medium">
                    {kpis.totalFlights > 0
                      ? Math.round((kpis.activeFlights / kpis.totalFlights) * 100)
                      : 0}
                    %
                  </dd>
                </div>
                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Delayed share</dt>
                  <dd className="num font-medium">
                    {kpis.totalFlights > 0
                      ? Math.round((kpis.delayedFlights / kpis.totalFlights) * 100)
                      : 0}
                    %
                  </dd>
                </div>
                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Fleet availability</dt>
                  <dd className="num font-medium">{kpis.fleetAvailability}%</dd>
                </div>
              </dl>
            </SectionCard>
          </div>
        </>
      )}
    </motion.div>
  );
}
