import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { Fuel, HeartPulse, MapPin, Users, Wrench } from "lucide-react";
import { pageVariants, itemVariants, listVariants } from "@/lib/ams/motion";
import { AIRCRAFT, type Aircraft } from "@/lib/ams/data";
import { GlassCard, Meter, PageHeader } from "@/components/ams/primitives";
import narrowbody from "@/assets/aircraft-narrowbody.jpg";
import widebody from "@/assets/aircraft-widebody.jpg";
import hangar from "@/assets/aircraft-hangar.jpg";

export const Route = createFileRoute("/aircraft")({
  head: () => ({
    meta: [
      { title: "Fleet & Aircraft — Aerion AMS" },
      { name: "description", content: "Fleet health, fuel state, location and maintenance status for every aircraft." },
      { property: "og:title", content: "Fleet & Aircraft — Aerion AMS" },
      { property: "og:description", content: "Fleet health, fuel and maintenance status for every aircraft." },
    ],
  }),
  component: AircraftPage,
});

function imageFor(a: Aircraft) {
  if (a.status === "Maintenance" || a.status === "Grounded") return hangar;
  return a.capacity > 250 ? widebody : narrowbody;
}

const statusTone: Record<Aircraft["status"], string> = {
  "In Service": "text-success",
  Standby: "text-info",
  Maintenance: "text-warning",
  Grounded: "text-destructive",
};

function AircraftPage() {
  return (
    <motion.div variants={pageVariants} initial="initial" animate="animate" className="mx-auto max-w-[1600px] space-y-6 py-6">
      <PageHeader title="Aircraft" description="Fleet condition, readiness and turnaround status." />

      <motion.div
        variants={listVariants}
        initial="initial"
        animate="animate"
        className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3"
      >
        {AIRCRAFT.map((a) => (
          <motion.div key={a.id} variants={itemVariants}>
            <GlassCard className="overflow-hidden">
              <div className="relative h-40 overflow-hidden">
                <img
                  src={imageFor(a)}
                  alt={`${a.model} aircraft`}
                  loading="lazy"
                  width={1024}
                  height={640}
                  className="h-full w-full object-cover"
                />
                <span
                  className={`absolute top-3 right-3 rounded-full bg-background/85 px-2.5 py-1 text-xs font-medium shadow-[var(--elev-1)] backdrop-blur-md ${statusTone[a.status]}`}
                >
                  {a.status}
                </span>
              </div>
              <div className="space-y-4 p-5">
                <div className="flex items-baseline justify-between">
                  <h2 className="num text-lg font-semibold">{a.registration}</h2>
                  <span className="text-sm text-muted-foreground">{a.model}</span>
                </div>

                <div className="space-y-3">
                  <div>
                    <div className="mb-1 flex justify-between text-xs text-muted-foreground">
                      <span className="flex items-center gap-1.5">
                        <HeartPulse className="h-3.5 w-3.5" strokeWidth={1.7} /> Health
                      </span>
                      <span className="num">{a.health}%</span>
                    </div>
                    <Meter value={a.health} tone={a.health > 85 ? "success" : a.health > 70 ? "warning" : "danger"} />
                  </div>
                  <div>
                    <div className="mb-1 flex justify-between text-xs text-muted-foreground">
                      <span className="flex items-center gap-1.5">
                        <Fuel className="h-3.5 w-3.5" strokeWidth={1.7} /> Fuel
                      </span>
                      <span className="num">{a.fuel}%</span>
                    </div>
                    <Meter value={a.fuel} tone={a.fuel > 50 ? "accent" : "warning"} />
                  </div>
                </div>

                <dl className="grid grid-cols-2 gap-3 pt-1 text-xs">
                  <div className="flex items-center gap-2 text-muted-foreground">
                    <Users className="h-3.5 w-3.5" strokeWidth={1.7} />
                    <span className="num text-foreground">{a.capacity} seats</span>
                  </div>
                  <div className="flex items-center gap-2 text-muted-foreground">
                    <Wrench className="h-3.5 w-3.5" strokeWidth={1.7} />
                    <span className="text-foreground">{a.maintenance}</span>
                  </div>
                  <div className="col-span-2 flex items-center gap-2 text-muted-foreground">
                    <MapPin className="h-3.5 w-3.5" strokeWidth={1.7} />
                    <span className="text-foreground">{a.location}</span>
                  </div>
                </dl>
              </div>
            </GlassCard>
          </motion.div>
        ))}
      </motion.div>
    </motion.div>
  );
}
