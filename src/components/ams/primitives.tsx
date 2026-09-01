import { motion, useMotionValue, useSpring, useTransform } from "motion/react";
import { useEffect, type ReactNode } from "react";
import { cn } from "@/lib/utils";
import { spring } from "@/lib/ams/motion";
import type { FlightStatus } from "@/lib/ams/data";

export function GlassCard({
  className,
  children,
  hover = true,
  ...rest
}: {
  className?: string;
  children: ReactNode;
  hover?: boolean;
} & React.ComponentProps<typeof motion.div>) {
  return (
    <motion.div
      whileHover={hover ? { scale: 1.02, boxShadow: "var(--elev-2)" } : undefined}
      transition={spring}
      className={cn("glass rounded-3xl", className)}
      {...rest}
    >
      {children}
    </motion.div>
  );
}

export function SectionCard({
  title,
  subtitle,
  action,
  className,
  bodyClassName,
  children,
}: {
  title: string;
  subtitle?: string;
  action?: ReactNode;
  className?: string;
  bodyClassName?: string;
  children: ReactNode;
}) {
  return (
    <GlassCard hover={false} className={cn("overflow-hidden", className)}>
      <div className="flex flex-wrap items-center justify-between gap-4 px-6 pt-6 pb-4">
        <div>
          <h2 className="text-base font-semibold">{title}</h2>
          {subtitle ? <p className="mt-1 text-sm text-muted-foreground">{subtitle}</p> : null}
        </div>
        {action}
      </div>
      <div className={cn("px-6 pb-6", bodyClassName)}>{children}</div>
    </GlassCard>
  );
}

export function CountUp({
  value,
  prefix = "",
  suffix = "",
  decimals = 0,
  className,
}: {
  value: number;
  prefix?: string;
  suffix?: string;
  decimals?: number;
  className?: string;
}) {
  const mv = useMotionValue(value);
  const smooth = useSpring(mv, { stiffness: 90, damping: 22, mass: 0.8 });
  const text = useTransform(
    smooth,
    (v) =>
      `${prefix}${v.toLocaleString(undefined, { minimumFractionDigits: decimals, maximumFractionDigits: decimals })}${suffix}`,
  );

  useEffect(() => {
    mv.set(value);
  }, [value, mv]);

  return <motion.span className={cn("num", className)}>{text}</motion.span>;
}

const statusTone: Record<FlightStatus, string> = {
  Scheduled: "bg-muted text-muted-foreground",
  Boarding: "bg-info/12 text-info",
  Taxiing: "bg-info/12 text-info",
  Departed: "bg-primary/12 text-primary dark:text-info",
  "In Air": "bg-success/14 text-success",
  Landing: "bg-success/14 text-success",
  Landed: "bg-muted text-muted-foreground",
  Delayed: "bg-warning/16 text-warning",
  Cancelled: "bg-destructive/14 text-destructive",
};

const livePulse: FlightStatus[] = ["Boarding", "Taxiing", "In Air", "Landing", "Departed"];

export function StatusPill({ status }: { status: FlightStatus }) {
  return (
    <motion.span
      key={status}
      initial={{ opacity: 0, y: -6, filter: "blur(4px)" }}
      animate={{ opacity: 1, y: 0, filter: "blur(0px)" }}
      transition={spring}
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium whitespace-nowrap",
        statusTone[status],
      )}
    >
      <span className="relative flex h-1.5 w-1.5">
        {livePulse.includes(status) ? (
          <span
            className="absolute inline-flex h-full w-full rounded-full bg-current"
            style={{ animation: "ams-pulse-ring 2s ease-out infinite" }}
          />
        ) : null}
        <span className="relative inline-flex h-1.5 w-1.5 rounded-full bg-current" />
      </span>
      {status}
    </motion.span>
  );
}

export function Meter({
  value,
  tone = "accent",
}: {
  value: number;
  tone?: "accent" | "success" | "warning" | "danger";
}) {
  const toneClass = {
    accent: "bg-accent",
    success: "bg-success",
    warning: "bg-warning",
    danger: "bg-destructive",
  }[tone];
  return (
    <div className="h-1.5 w-full overflow-hidden rounded-full bg-foreground/8">
      <motion.div
        className={cn("h-full rounded-full", toneClass)}
        initial={{ width: 0 }}
        animate={{ width: `${Math.max(0, Math.min(100, value))}%` }}
        transition={{ type: "spring", stiffness: 120, damping: 26 }}
      />
    </div>
  );
}

export function Shimmer({ className }: { className?: string }) {
  return (
    <div className={cn("relative overflow-hidden rounded-xl bg-foreground/6", className)}>
      <div
        className="absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-foreground/10 to-transparent"
        style={{ animation: "ams-shimmer 1.6s infinite" }}
      />
    </div>
  );
}

export function PageHeader({
  title,
  description,
  action,
}: {
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
        <p className="mt-1.5 text-sm text-muted-foreground">{description}</p>
      </div>
      {action}
    </div>
  );
}
