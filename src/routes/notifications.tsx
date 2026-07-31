import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { pageVariants } from "@/lib/ams/motion";
import { NotificationsPanel } from "@/components/ams/notifications-panel";
import { PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/notifications")({
  head: () => ({
    meta: [
      { title: "Notifications — Aerion AMS" },
      { name: "description", content: "Live operational event feed: delays, gate changes, crew and readiness updates." },
      { property: "og:title", content: "Notifications — Aerion AMS" },
      { property: "og:description", content: "Live operational event feed for the whole network." },
    ],
  }),
  component: NotificationsPage,
});

function NotificationsPage() {
  return (
    <motion.div variants={pageVariants} initial="initial" animate="animate" className="mx-auto max-w-[1200px] space-y-6 py-6">
      <PageHeader title="Notifications" description="Everything happening across the operation, as it happens." />
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_320px]">
        <NotificationsPanel limit={12} />
        <SectionCard title="Delivery preferences" subtitle="Where operational alerts are sent">
          <ul className="space-y-3 text-sm">
            {[
              ["Ops control desk", "All events"],
              ["Duty manager", "Critical only"],
              ["Station SMS", "Delays & gates"],
              ["Email digest", "Hourly"],
            ].map(([k, v]) => (
              <li key={k} className="flex items-center justify-between">
                <span className="text-muted-foreground">{k}</span>
                <span className="font-medium">{v}</span>
              </li>
            ))}
          </ul>
        </SectionCard>
      </div>
    </motion.div>
  );
}
