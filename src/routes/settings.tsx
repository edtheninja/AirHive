import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { pageVariants } from "@/lib/ams/motion";
import { PageHeader, SectionCard } from "@/components/ams/primitives";
import { Switch } from "@/components/ui/switch";

export const Route = createFileRoute("/settings")({
  head: () => ({
    meta: [
      { title: "Settings — Aerion AMS" },
      { name: "description", content: "Configure operations preferences, alert thresholds and integrations." },
      { property: "og:title", content: "Settings — Aerion AMS" },
      { property: "og:description", content: "Configure operations preferences and alert thresholds." },
    ],
  }),
  component: SettingsPage,
});

const TOGGLES = [
  ["Live status streaming", "Push flight state changes to all consoles", true],
  ["Auto delay publishing", "Broadcast delays above 15 minutes automatically", true],
  ["Crew duty warnings", "Warn when rest hours drop below 10", true],
  ["Maintenance escalation", "Escalate overdue work orders to the duty manager", false],
  ["Anonymous analytics", "Share aggregate performance telemetry", false],
] as const;

function SettingsPage() {
  return (
    <motion.div variants={pageVariants} initial="initial" animate="animate" className="mx-auto max-w-[1000px] space-y-6 py-6">
      <PageHeader title="Settings" description="Operational defaults for the control centre." />
      <SectionCard title="Automation" subtitle="How the platform reacts to operational events">
        <ul className="divide-y divide-border">
          {TOGGLES.map(([title, detail, on]) => (
            <li key={title} className="flex items-center justify-between gap-6 py-4">
              <div>
                <p className="text-sm font-medium">{title}</p>
                <p className="mt-0.5 text-xs text-muted-foreground">{detail}</p>
              </div>
              <Switch defaultChecked={on} />
            </li>
          ))}
        </ul>
      </SectionCard>
      <SectionCard title="Integrations" subtitle="Systems connected to the operations platform">
        <ul className="space-y-3 text-sm">
          {[
            ["Departure control system", "Connected"],
            ["Crew rostering", "Connected"],
            ["Maintenance ERP", "Connected"],
            ["Backend API", "Ready for Spring Boot service"],
          ].map(([k, v]) => (
            <li key={k} className="flex items-center justify-between">
              <span className="text-muted-foreground">{k}</span>
              <span className="font-medium">{v}</span>
            </li>
          ))}
        </ul>
      </SectionCard>
    </motion.div>
  );
}
