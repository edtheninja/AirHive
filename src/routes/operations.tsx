import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { Activity, Clock3, Gauge, ShieldCheck } from "lucide-react";
import { pageVariants } from "@/lib/ams/motion";
import { useLiveOps } from "@/lib/ams/hooks";
import { KpiCard } from "@/components/ams/kpi-card";
import { FlightMap } from "@/components/ams/flight-map";
import { NotificationsPanel } from "@/components/ams/notifications-panel";
import { PageHeader, SectionCard } from "@/components/ams/primitives";
import { FlightsTable } from "@/components/ams/flights-table";

export const Route = createFileRoute("/operations")({
  head: () => ({
    meta: [
      { title: "Operations Control — Aerion AMS" },
      { name: "description", content: "Operations control centre view: airborne fleet, disruptions and live event feed." },
      { property: "og:title", content: "Operations Control — Aerion AMS" },
      { property: "og:description", content: "Airborne fleet, disruptions and the live event feed." },
    ],
  }),
  component: OperationsPage,
});

function OperationsPage() {
  const { kpis, flights } = useLiveOps();
  const disrupted = flights.filter((f) => f.status === "Delayed" || f.status === "Cancelled");

  return (
    <motion.div variants={pageVariants} initial="initial" animate="animate" className="mx-auto max-w-[1600px] space-y-6 py-6">
      <PageHeader title="Operations" description="Control centre view of the live network." />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <KpiCard index={0} label="Airborne" value={kpis.activeFlights} icon={Activity} tone="accent" />
        <KpiCard index={1} label="Disruptions" value={kpis.delayedFlights} icon={Clock3} tone="warning" />
        <KpiCard index={2} label="Fleet availability" value={kpis.fleetAvailability} suffix="%" icon={Gauge} tone="success" />
        <KpiCard index={3} label="Safety events" value={0} icon={ShieldCheck} tone="primary" />
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
        <div className="space-y-6">
          <SectionCard title="Network map" subtitle="Aircraft positions and active corridors">
            <FlightMap />
          </SectionCard>
          <SectionCard title="Disruption watchlist" subtitle={`${disrupted.length} flights require attention`}>
            {disrupted.length ? (
              <FlightsTable flights={disrupted} />
            ) : (
              <p className="py-8 text-center text-sm text-muted-foreground">No active disruptions across the network.</p>
            )}
          </SectionCard>
        </div>
        <NotificationsPanel limit={8} />
      </div>
    </motion.div>
  );
}
