import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { toast } from "sonner";
import {
  AlertOctagon,
  CalendarPlus,
  ClipboardList,
  Clock3,
  Gauge,
  Megaphone,
  PlaneTakeoff,
  Plane,
  Radar,
  Users,
  Wallet,
  UserPlus,
} from "lucide-react";
import { pageVariants } from "@/lib/ams/motion";
import { useLiveOps } from "@/lib/ams/live-ops";
import { KpiCard } from "@/components/ams/kpi-card";
import { FlightsTable } from "@/components/ams/flights-table";
import { FlightMap } from "@/components/ams/flight-map";
import { NotificationsPanel } from "@/components/ams/notifications-panel";
import { PageHeader, SectionCard } from "@/components/ams/primitives";
import { RevenueChart, OccupancyChart } from "@/components/ams/charts";
import { OCCUPANCY_SERIES, REVENUE_SERIES } from "@/lib/ams/data";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Operations Dashboard — Aerion AMS" },
      { name: "description", content: "Live airline operations: flights, delays, revenue, fleet availability and crew." },
      { property: "og:title", content: "Operations Dashboard — Aerion AMS" },
      { property: "og:description", content: "Live airline operations control centre for flights, fleet and crew." },
    ],
  }),
  component: Dashboard,
});

const QUICK_ACTIONS = [
  { label: "Add Flight", icon: PlaneTakeoff, tone: "text-accent" },
  { label: "Assign Aircraft", icon: Plane, tone: "text-accent" },
  { label: "Schedule Crew", icon: UserPlus, tone: "text-accent" },
  { label: "Generate Report", icon: ClipboardList, tone: "text-muted-foreground" },
  { label: "Emergency Alert", icon: AlertOctagon, tone: "text-destructive" },
  { label: "Publish Delay", icon: Megaphone, tone: "text-warning" },
];

function Dashboard() {
  const { kpis, flights } = useLiveOps();

  return (
    <motion.div variants={pageVariants} initial="initial" animate="animate" className="mx-auto max-w-[1600px] space-y-6 py-6">
      <PageHeader
        title="Operations Dashboard"
        description="Network-wide situational awareness, updating live."
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">
        <KpiCard index={0} label="Total Flights" value={kpis.totalFlights} icon={PlaneTakeoff} tone="primary" trend="Scheduled today" />
        <KpiCard index={1} label="Active Flights" value={kpis.activeFlights} icon={Radar} tone="accent" trend="Currently airborne" />
        <KpiCard index={2} label="Delayed Flights" value={kpis.delayedFlights} icon={Clock3} tone="warning" trend="Above 15 minutes" />
        <KpiCard index={3} label="Revenue Today" value={kpis.revenueToday} icon={Wallet} tone="success" prefix="$" trend="Net of refunds" />
        <KpiCard index={4} label="Passenger Count" value={kpis.passengers} icon={Users} tone="accent" trend="Checked in network-wide" />
        <KpiCard index={5} label="Fleet Availability" value={kpis.fleetAvailability} icon={Gauge} tone="success" suffix="%" trend="42 of 46 aircraft" />
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

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <SectionCard title="Revenue" subtitle="Last 7 days, thousands USD">
              <RevenueChart data={REVENUE_SERIES} />
            </SectionCard>
            <SectionCard title="Cabin occupancy" subtitle="Average load factor by cabin">
              <OccupancyChart data={OCCUPANCY_SERIES} />
            </SectionCard>
          </div>
        </div>

        <div className="space-y-6">
          <SectionCard title="Quick actions" subtitle="Common operations tasks" bodyClassName="px-4 pb-5">
            <div className="grid grid-cols-2 gap-2">
              {QUICK_ACTIONS.map((a) => (
                <motion.button
                  key={a.label}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  onClick={() => toast(a.label, { description: "Action queued in the operations centre." })}
                  className="flex flex-col items-start gap-3 rounded-2xl border border-border/60 bg-foreground/3 p-3 text-left text-sm transition-colors hover:bg-foreground/6"
                >
                  <a.icon className={`h-[18px] w-[18px] ${a.tone}`} strokeWidth={1.7} />
                  <span className="leading-tight">{a.label}</span>
                </motion.button>
              ))}
            </div>
          </SectionCard>

          <NotificationsPanel />

          <SectionCard title="Today at a glance" subtitle="Network operating summary">
            <dl className="space-y-3 text-sm">
              {[
                ["On-time performance", "94.2%"],
                ["Average delay", "11 min"],
                ["Aircraft in maintenance", "3"],
                ["Crew on duty", "218"],
                ["Diversions", "0"],
              ].map(([k, v]) => (
                <div key={k} className="flex items-center justify-between">
                  <dt className="text-muted-foreground">{k}</dt>
                  <dd className="num font-medium">{v}</dd>
                </div>
              ))}
            </dl>
          </SectionCard>
        </div>
      </div>
    </motion.div>
  );
}
