import { createFileRoute, redirect, useNavigate } from "@tanstack/react-router";
import { motion } from "motion/react";
import { getSession } from "@/lib/auth/session";
import {
  AlertOctagon,
  ClipboardList,
  Clock3,
  Gauge,
  Megaphone,
  PlaneTakeoff,
  Plane,
  Radar,
  UserPlus,
} from "lucide-react";
import { pageVariants } from "@/lib/ams/motion";
import { useLiveOps } from "@/lib/ams/hooks";
import { KpiCard } from "@/components/ams/kpi-card";
import { FlightsTable } from "@/components/ams/flights-table";
import { FlightMap } from "@/components/ams/flight-map";
import { NotificationsPanel } from "@/components/ams/notifications-panel";
import { PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/")({
  beforeLoad: () => {
    if (!getSession()) {
      throw redirect({
        to: "/login",
      });
    }
  },
  head: () => ({
    meta: [
      { title: "Operations Dashboard — AirHive AMS" },
      {
        name: "description",
        content: "Live airline operations: flights, delays, revenue, fleet availability and crew.",
      },
      { property: "og:title", content: "Operations Dashboard — AirHive AMS" },
      {
        property: "og:description",
        content: "Live airline operations control centre for flights, fleet and crew.",
      },
    ],
  }),
  component: Dashboard,
});

const QUICK_ACTIONS = [
  { label: "Add Flight", icon: PlaneTakeoff, tone: "text-accent", path: "/flights" },
  { label: "Assign Aircraft", icon: Plane, tone: "text-accent", path: "/aircraft" },
  { label: "Schedule Crew", icon: UserPlus, tone: "text-accent", path: "/crew" },
  {
    label: "Generate Report",
    icon: ClipboardList,
    tone: "text-muted-foreground",
    path: "/analytics",
  },
  {
    label: "Emergency Alert",
    icon: AlertOctagon,
    tone: "text-destructive",
    path: "/notifications",
  },
  { label: "Publish Delay", icon: Megaphone, tone: "text-warning", path: "/flights" },
];

function Dashboard() {
  const navigate = useNavigate();
  const { kpis, flights } = useLiveOps();
  const flightStatusCounts = flights.reduce<Record<string, number>>((counts, flight) => {
    counts[flight.status] = (counts[flight.status] ?? 0) + 1;
    return counts;
  }, {});
  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader
        title="Operations Dashboard"
        description="Network-wide situational awareness, updating live."
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <KpiCard
          index={0}
          label="Total Flights"
          value={kpis.totalFlights}
          icon={PlaneTakeoff}
          tone="primary"
          trend="Scheduled flights"
        />
        <KpiCard
          index={1}
          label="Active Flights"
          value={kpis.activeFlights}
          icon={Radar}
          tone="accent"
          trend="Currently airborne"
        />
        <KpiCard
          index={2}
          label="Delayed Flights"
          value={kpis.delayedFlights}
          icon={Clock3}
          tone="warning"
          trend="Currently delayed"
        />

        <KpiCard
          index={3}
          label="Fleet Availability"
          value={kpis.fleetAvailability}
          icon={Gauge}
          tone="success"
          suffix="%"
          trend="Based on active fleet"
        />
      </div>
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
        <div className="space-y-6">
          <SectionCard
            title="Live flight operations"
            subtitle="Statuses refresh automatically from the operations feed."
          >
            <FlightsTable flights={flights} />
          </SectionCard>

          <SectionCard title="Live world map" subtitle="Real-time aircraft tracking preview">
            <FlightMap />
          </SectionCard>
        </div>
        <div className="space-y-6">
          <SectionCard
            title="Quick actions"
            subtitle="Common operations tasks"
            bodyClassName="px-4 pb-5"
          >
            <div className="grid grid-cols-2 gap-2">
              {QUICK_ACTIONS.map((a) => (
                <motion.button
                  key={a.label}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  onClick={() => navigate({ to: a.path })}
                  className="flex flex-col items-start gap-3 rounded-2xl border border-border/60 bg-foreground/3 p-3 text-left text-sm transition-colors hover:bg-foreground/6"
                >
                  <a.icon className={`h-4.5 w-4.5 ${a.tone}`} strokeWidth={1.7} />
                  <span className="leading-tight">{a.label}</span>
                </motion.button>
              ))}
            </div>
          </SectionCard>
          <SectionCard title="Network status" subtitle="Current flight distribution">
            <dl className="space-y-3 text-sm">
              {[
                ["Scheduled", flightStatusCounts.Scheduled ?? 0],
                ["Boarding", flightStatusCounts.Boarding ?? 0],
                ["In air", flightStatusCounts["In Air"] ?? 0],
                ["Landed", flightStatusCounts.Landed ?? 0],
                ["Delayed", flightStatusCounts.Delayed ?? 0],
                ["Cancelled", flightStatusCounts.Cancelled ?? 0],
              ].map(([label, value]) => (
                <div key={label} className="flex items-center justify-between">
                  <dt className="text-muted-foreground">{label}</dt>
                  <dd className="num font-medium">{value}</dd>
                </div>
              ))}
            </dl>
          </SectionCard>

          <NotificationsPanel />
        </div>
      </div>
    </motion.div>
  );
}
