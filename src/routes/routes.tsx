import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { pageVariants } from "@/lib/ams/motion";
import { ROUTES } from "@/lib/ams/data";
import { Meter, PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/routes")({
  head: () => ({
    meta: [
      { title: "Network Routes — Aerion AMS" },
      { name: "description", content: "Route profitability, load factor, frequency and sector length across the network." },
      { property: "og:title", content: "Network Routes — Aerion AMS" },
      { property: "og:description", content: "Route profitability and load factor across the network." },
    ],
  }),
  component: RoutesPage,
});

function RoutesPage() {
  return (
    <motion.div variants={pageVariants} initial="initial" animate="animate" className="mx-auto max-w-[1600px] space-y-6 py-6">
      <PageHeader title="Routes" description="Sector performance and network planning." />
      <SectionCard title="Active routes" subtitle="Load factor and revenue contribution per sector">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[720px] border-separate border-spacing-y-1 text-sm">
            <thead>
              <tr className="text-left text-xs text-muted-foreground">
                {["Route", "Distance", "Block time", "Frequency", "Load factor", "Revenue"].map((h) => (
                  <th key={h} className="px-3 pb-2 font-medium">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {ROUTES.map((r, i) => (
                <motion.tr
                  key={r.id}
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.04 }}
                  className="hover:bg-foreground/4"
                >
                  <td className="num rounded-l-2xl px-3 py-3 font-medium">{r.code}</td>
                  <td className="num px-3 py-3 text-muted-foreground">{r.distance.toLocaleString()} km</td>
                  <td className="num px-3 py-3 text-muted-foreground">{r.duration}</td>
                  <td className="px-3 py-3 text-muted-foreground">{r.frequency}</td>
                  <td className="w-44 px-3 py-3">
                    <Meter value={r.load} tone={r.load > 85 ? "success" : "accent"} />
                    <span className="num mt-1 block text-[11px] text-muted-foreground">{r.load}%</span>
                  </td>
                  <td className="num rounded-r-2xl px-3 py-3">${r.revenue.toLocaleString()}</td>
                </motion.tr>
              ))}
            </tbody>
          </table>
        </div>
      </SectionCard>
    </motion.div>
  );
}
