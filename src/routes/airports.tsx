import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { Building2 } from "lucide-react";
import { pageVariants, itemVariants, listVariants } from "@/lib/ams/motion";
import { AIRPORTS } from "@/lib/ams/data";
import { GlassCard, PageHeader } from "@/components/ams/primitives";

export const Route = createFileRoute("/airports")({
  head: () => ({
    meta: [
      { title: "Airports & Stations — Aerion AMS" },
      { name: "description", content: "Station status, terminal counts and daily movements across the network." },
      { property: "og:title", content: "Airports & Stations — Aerion AMS" },
      { property: "og:description", content: "Station status and daily movements across the network." },
    ],
  }),
  component: AirportsPage,
});

const tone: Record<string, string> = {
  Normal: "bg-success/14 text-success",
  Congested: "bg-warning/16 text-warning",
  Weather: "bg-info/12 text-info",
};

function AirportsPage() {
  return (
    <motion.div variants={pageVariants} initial="initial" animate="animate" className="mx-auto max-w-[1600px] space-y-6 py-6">
      <PageHeader title="Airports" description="Station readiness and daily movement volumes." />
      <motion.div variants={listVariants} initial="initial" animate="animate" className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {AIRPORTS.map((a) => (
          <motion.div key={a.code} variants={itemVariants}>
            <GlassCard className="p-5">
              <div className="flex items-start justify-between">
                <div>
                  <p className="num text-2xl font-semibold">{a.code}</p>
                  <p className="mt-1 text-sm text-muted-foreground">{a.city}, {a.country}</p>
                </div>
                <span className="flex h-9 w-9 items-center justify-center rounded-2xl bg-accent/12 text-accent">
                  <Building2 className="h-[18px] w-[18px]" strokeWidth={1.7} />
                </span>
              </div>
              <div className="mt-6 flex items-center justify-between text-xs">
                <span className="text-muted-foreground">
                  <span className="num text-foreground">{a.terminals}</span> terminals
                </span>
                <span className="text-muted-foreground">
                  <span className="num text-foreground">{a.flights}</span> movements
                </span>
              </div>
              <span className={`mt-4 inline-block rounded-full px-2.5 py-1 text-xs ${tone[a.status]}`}>{a.status}</span>
            </GlassCard>
          </motion.div>
        ))}
      </motion.div>
    </motion.div>
  );
}
