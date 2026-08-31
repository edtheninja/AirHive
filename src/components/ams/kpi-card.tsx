import { motion } from "motion/react";
import type { LucideIcon } from "lucide-react";
import { CountUp, GlassCard } from "@/components/ams/primitives";
import { cn } from "@/lib/utils";

export function KpiCard({
  label,
  value,
  icon: Icon,
  prefix,
  suffix,
  trend,
  tone = "accent",
  index = 0,
}: {
  label: string;
  value: number;
  icon: LucideIcon;
  prefix?: string;
  suffix?: string;
  trend?: string;
  tone?: "accent" | "success" | "warning" | "danger" | "primary";
  index?: number;
}) {
  const toneClass = {
    accent: "bg-accent/12 text-accent",
    success: "bg-success/14 text-success",
    warning: "bg-warning/16 text-warning",
    danger: "bg-destructive/14 text-destructive",
    primary: "bg-primary/12 text-primary dark:text-info",
  }[tone];

  return (
    <motion.div
      initial={{ opacity: 0, y: 14 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.05, type: "spring", stiffness: 220, damping: 26 }}
    >
      <GlassCard className="p-5">
        <div className="flex items-start justify-between">
          <span className="text-sm text-muted-foreground">{label}</span>
          <span className={cn("flex h-9 w-9 items-center justify-center rounded-2xl", toneClass)}>
            <Icon className="h-4.5 w-4.5" strokeWidth={1.7} />
          </span>
        </div>
        <p className="mt-6 text-3xl font-semibold">
          <CountUp value={value} prefix={prefix} suffix={suffix} />
        </p>
        {trend ? <p className="mt-1.5 text-xs text-muted-foreground">{trend}</p> : null}
      </GlassCard>
    </motion.div>
  );
}
