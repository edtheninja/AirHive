import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { pageVariants } from "@/lib/ams/motion";
import { PASSENGERS } from "@/lib/ams/data";
import { PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/passengers")({
  head: () => ({
    meta: [
      { title: "Passengers — Aerion AMS" },
      {
        name: "description",
        content: "Passenger manifests, loyalty tier, check-in state and baggage per flight.",
      },
      { property: "og:title", content: "Passengers — Aerion AMS" },
      {
        property: "og:description",
        content: "Passenger manifests, loyalty tier and check-in state.",
      },
    ],
  }),
  component: PassengersPage,
});

const tierTone: Record<string, string> = {
  Blue: "bg-foreground/6 text-muted-foreground",
  Silver: "bg-info/12 text-info",
  Gold: "bg-warning/16 text-warning",
  Platinum: "bg-primary/12 text-primary dark:text-info",
};

function PassengersPage() {
  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader title="Passengers" description="Manifest view across today's departures." />
      <SectionCard title="Manifest" subtitle={`${PASSENGERS.length} passengers on active flights`}>
        <div className="overflow-x-auto">
          <table className="w-full min-w-[720px] border-separate border-spacing-y-1 text-sm">
            <thead>
              <tr className="text-left text-xs text-muted-foreground">
                {["Passenger", "Ref", "Tier", "Flight", "Route", "Seat", "Bags", "Check-in"].map(
                  (h) => (
                    <th key={h} className="px-3 pb-2 font-medium">
                      {h}
                    </th>
                  ),
                )}
              </tr>
            </thead>
            <tbody>
              {PASSENGERS.map((p, i) => (
                <motion.tr
                  key={p.id}
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.02 }}
                  className="hover:bg-foreground/4"
                >
                  <td className="rounded-l-2xl px-3 py-3 font-medium">{p.name}</td>
                  <td className="num px-3 py-3 text-muted-foreground">{p.id}</td>
                  <td className="px-3 py-3">
                    <span className={`rounded-full px-2.5 py-1 text-xs ${tierTone[p.tier]}`}>
                      {p.tier}
                    </span>
                  </td>
                  <td className="num px-3 py-3">{p.flight}</td>
                  <td className="num px-3 py-3 text-muted-foreground">{p.route}</td>
                  <td className="num px-3 py-3">{p.seat}</td>
                  <td className="num px-3 py-3 text-muted-foreground">{p.bags}</td>
                  <td className="rounded-r-2xl px-3 py-3">
                    <span
                      className={`text-xs ${p.checkedIn ? "text-success" : "text-muted-foreground"}`}
                    >
                      {p.checkedIn ? "Checked in" : "Pending"}
                    </span>
                  </td>
                </motion.tr>
              ))}
            </tbody>
          </table>
        </div>
      </SectionCard>
    </motion.div>
  );
}
