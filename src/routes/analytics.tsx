import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { pageVariants } from "@/lib/ams/motion";
import { PageHeader, SectionCard } from "@/components/ams/primitives";
import { CompletionChart, DelayChart, FuelChart, OccupancyChart, RevenueChart } from "@/components/ams/charts";
import { COMPLETION_SERIES, DELAY_SERIES, FUEL_SERIES, OCCUPANCY_SERIES, REVENUE_SERIES } from "@/lib/ams/data";

export const Route = createFileRoute("/analytics")({
  head: () => ({
    meta: [
      { title: "Analytics — Aerion AMS" },
      { name: "description", content: "Revenue, occupancy, flight completion, fuel burn and delay-cause analytics." },
      { property: "og:title", content: "Analytics — Aerion AMS" },
      { property: "og:description", content: "Revenue, occupancy, fuel burn and delay-cause analytics." },
    ],
  }),
  component: AnalyticsPage,
});

function AnalyticsPage() {
  return (
    <motion.div variants={pageVariants} initial="initial" animate="animate" className="mx-auto max-w-[1600px] space-y-6 py-6">
      <PageHeader title="Analytics" description="Commercial and operational performance across the network." />
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <SectionCard title="Revenue" subtitle="Daily revenue against target, thousands USD">
          <RevenueChart data={REVENUE_SERIES} />
        </SectionCard>
        <SectionCard title="Occupancy" subtitle="Load factor by cabin class">
          <OccupancyChart data={OCCUPANCY_SERIES} />
        </SectionCard>
        <SectionCard title="Flight completion" subtitle="Weekly completion factor">
          <CompletionChart data={COMPLETION_SERIES} />
        </SectionCard>
        <SectionCard title="Fuel usage" subtitle="Burn versus plan, tonnes per rolling day">
          <FuelChart data={FUEL_SERIES} />
        </SectionCard>
        <SectionCard title="Delay analysis" subtitle="Root cause distribution, percent of delays" className="lg:col-span-2">
          <DelayChart data={DELAY_SERIES} />
        </SectionCard>
      </div>
    </motion.div>
  );
}
