import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { pageVariants } from "@/lib/ams/motion";
import { GlassCard, PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/profile")({
  head: () => ({
    meta: [
      { title: "User Profile — Aerion AMS" },
      {
        name: "description",
        content: "Operations account details, roles, permissions and recent activity.",
      },
      { property: "og:title", content: "User Profile — Aerion AMS" },
      {
        property: "og:description",
        content: "Operations account details, roles and recent activity.",
      },
    ],
  }),
  component: ProfilePage,
});

function ProfilePage() {
  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1000px] space-y-6 py-6"
    >
      <PageHeader title="User Profile" description="Your operations account and permissions." />
      <GlassCard hover={false} className="flex flex-wrap items-center gap-5 p-6">
        <span className="num flex h-16 w-16 items-center justify-center rounded-3xl bg-primary text-lg font-semibold text-primary-foreground">
          NK
        </span>
        <div>
          <h2 className="text-lg font-semibold">Navneet Kaur</h2>
          <p className="text-sm text-muted-foreground">
            Operations Supervisor · DEL Control Centre
          </p>
          <p className="num mt-1 text-xs text-muted-foreground">
            Employee ID 44-8120 · Shift 06:00–18:00
          </p>
        </div>
      </GlassCard>

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        <SectionCard title="Permissions" subtitle="Granted operational scopes">
          <ul className="space-y-3 text-sm">
            {[
              "Flight dispatch",
              "Delay publishing",
              "Crew reassignment",
              "Maintenance approval",
              "Emergency broadcast",
            ].map((p) => (
              <li key={p} className="flex items-center justify-between">
                <span className="text-muted-foreground">{p}</span>
                <span className="rounded-full bg-success/14 px-2.5 py-1 text-xs text-success">
                  Allowed
                </span>
              </li>
            ))}
          </ul>
        </SectionCard>
        <SectionCard title="Recent activity" subtitle="Last actions in this console">
          <ul className="space-y-3 text-sm">
            {[
              ["Published delay for AI302", "12 min ago"],
              ["Reassigned VT-AXN to AI458", "38 min ago"],
              ["Approved MX-4416 closure", "1 h ago"],
              ["Exported bookings report", "2 h ago"],
            ].map(([k, v]) => (
              <li key={k} className="flex items-center justify-between">
                <span>{k}</span>
                <span className="num text-xs text-muted-foreground">{v}</span>
              </li>
            ))}
          </ul>
        </SectionCard>
      </div>
    </motion.div>
  );
}
