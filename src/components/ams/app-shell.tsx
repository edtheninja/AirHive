import { Link, useRouterState } from "@tanstack/react-router";
import { AnimatePresence, motion } from "motion/react";
import { useEffect, useState, type ReactNode } from "react";
import {
  Activity,
  BarChart3,
  Bell,
  Building2,
  CalendarClock,
  ChevronLeft,
  Cog,
  LayoutDashboard,
  Moon,
  PlaneTakeoff,
  Plane,
  Route as RouteIcon,
  Search,
  Sun,
  Ticket,
  UserRound,
  Users,
  Wrench,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { spring } from "@/lib/ams/motion";
import { useLiveOps } from "@/lib/ams/hooks";

function useIsDesktop() {
  const [isDesktop, setIsDesktop] = useState(false);
  useEffect(() => {
    const mql = window.matchMedia("(min-width: 1024px)");
    const onChange = () => setIsDesktop(mql.matches);
    onChange();
    mql.addEventListener("change", onChange);
    return () => mql.removeEventListener("change", onChange);
  }, []);
  return isDesktop;
}

const NAV = [
  { to: "/", label: "Dashboard", icon: LayoutDashboard },
  { to: "/flights", label: "Flights", icon: PlaneTakeoff },
  { to: "/aircraft", label: "Aircraft", icon: Plane },
  { to: "/routes", label: "Routes", icon: RouteIcon },
  { to: "/passengers", label: "Passengers", icon: Users },
  { to: "/bookings", label: "Bookings", icon: Ticket },
  { to: "/crew", label: "Crew", icon: UserRound },
  { to: "/airports", label: "Airports", icon: Building2 },
  { to: "/maintenance", label: "Maintenance", icon: Wrench },
  { to: "/operations", label: "Operations", icon: Activity },
  { to: "/analytics", label: "Analytics", icon: BarChart3 },
  { to: "/notifications", label: "Notifications", icon: Bell },
  { to: "/settings", label: "Settings", icon: Cog },
  { to: "/profile", label: "User Profile", icon: CalendarClock },
] as const;

function useTheme() {
  const [dark, setDark] = useState(false);
  useEffect(() => {
    const stored = window.localStorage.getItem("ams-theme");
    const initial = stored === "dark";
    setDark(initial);
    document.documentElement.classList.toggle("dark", initial);
  }, []);
  const toggle = () => {
    setDark((d) => {
      const next = !d;
      document.documentElement.classList.toggle("dark", next);
      window.localStorage.setItem("ams-theme", next ? "dark" : "light");
      return next;
    });
  };
  return { dark, toggle };
}

function NavItems({ collapsed, onNavigate }: { collapsed: boolean; onNavigate?: () => void }) {
  const pathname = useRouterState({ select: (s) => s.location.pathname });
  return (
    <nav className="flex flex-col gap-1">
      {NAV.map((item) => {
        const active = pathname === item.to;
        return (
          <Link
            key={item.to}
            to={item.to}
            onClick={onNavigate}
            className="group relative block"
            aria-label={item.label}
          >
            <motion.div
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              transition={spring}
              className={cn(
                "relative flex items-center gap-3 rounded-2xl px-3 py-2.5 text-sm transition-colors",
                active
                  ? "text-sidebar-accent-foreground"
                  : "text-muted-foreground hover:text-foreground",
              )}
            >
              {active ? (
                <motion.span
                  layoutId="nav-active"
                  transition={spring}
                  className="absolute inset-0 rounded-2xl bg-sidebar-accent shadow-float"
                />
              ) : null}
              <item.icon
                className={cn(
                  "relative z-10 h-4.5 w-4.5 shrink-0 transition-transform duration-300",
                  active ? "scale-110" : "group-hover:scale-110",
                )}
                strokeWidth={1.6}
              />
              {!collapsed ? (
                <span className={cn("relative z-10 truncate", active && "font-medium")}>
                  {item.label}
                </span>
              ) : null}
            </motion.div>
          </Link>
        );
      })}
    </nav>
  );
}

export function AppShell({ children }: { children: ReactNode }) {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const { dark, toggle } = useTheme();
  const isDesktop = useIsDesktop();
  const { notifications } = useLiveOps();
  const pathname = useRouterState({ select: (s) => s.location.pathname });
  const current = NAV.find((n) => n.to === pathname)?.label ?? "Dashboard";

  return (
    <div className="relative min-h-screen w-full overflow-x-hidden bg-background">
      <div className="pointer-events-none fixed inset-0 -z-10">
        <div className="absolute -top-40 -left-32 h-105 w-105 rounded-full bg-accent/12 blur-[120px]" />
        <div className="absolute top-1/3 -right-40 h-120 w-120 rounded-full bg-primary/10 blur-[140px]" />
        <div className="absolute bottom-0 left-1/3 h-90 w-90 rounded-full bg-success/8 blur-[130px]" />
      </div>

      <motion.aside
        animate={{ width: collapsed ? 88 : 264 }}
        transition={spring}
        className="fixed inset-y-0 left-0 z-40 hidden flex-col p-4 lg:flex"
      >
        <div className="glass flex h-full flex-col rounded-3xl p-4">
          <div className="flex items-center gap-3 px-1 pb-6">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-primary text-primary-foreground shadow-float">
              <PlaneTakeoff className="h-5 w-5" strokeWidth={1.7} />
            </div>
            {!collapsed ? (
              <div className="min-w-0">
                <p className="truncate text-sm font-semibold">AirHive AMS</p>
                <p className="truncate text-xs text-muted-foreground">Operations Control</p>
              </div>
            ) : null}
          </div>

          <div className="-mr-2 flex-1 overflow-y-auto pr-2">
            <NavItems collapsed={collapsed} />
          </div>

          <button
            onClick={() => setCollapsed((c) => !c)}
            className="mt-4 flex items-center justify-center gap-2 rounded-2xl border border-border/60 py-2 text-xs text-muted-foreground transition-colors hover:text-foreground"
          >
            <ChevronLeft
              className={cn("h-4 w-4 transition-transform duration-300", collapsed && "rotate-180")}
            />
            {!collapsed ? "Collapse" : null}
          </button>
        </div>
      </motion.aside>

      <motion.div
        animate={{ marginLeft: isDesktop ? (collapsed ? 88 : 264) : 0 }}
        transition={spring}
      >
        <header className="sticky top-0 z-30 px-4 pt-4">
          <div className="glass flex items-center gap-3 rounded-3xl px-4 py-3">
            <div className="hidden min-w-0 flex-col lg:flex">
              <span className="text-xs text-muted-foreground">AirHive</span>
              <span className="truncate text-sm font-semibold">{current}</span>
            </div>
            <div className="flex h-10 min-w-0 flex-1 items-center gap-2 rounded-2xl bg-foreground/4 px-3 text-sm text-muted-foreground">
              <Search className="h-4 w-4 shrink-0" strokeWidth={1.7} />
              <input
                placeholder="Search flights, aircraft, crew…"
                className="h-5 min-w-0 truncate w-full bg-transparent text-sm outline-none placeholder:text-muted-foreground"
              />
              <kbd className="hidden rounded-md border border-border px-1.5 py-0.5 text-[10px] sm:block">
                ⌘K
              </kbd>
            </div>
            <button
              onClick={toggle}
              aria-label="Toggle theme"
              className="rounded-2xl p-2 text-muted-foreground transition-colors hover:bg-foreground/5 hover:text-foreground"
            >
              {dark ? (
                <Sun className="h-4.5 w-4.5" strokeWidth={1.7} />
              ) : (
                <Moon className="h-4.5 w-4.5" strokeWidth={1.7} />
              )}
            </button>
            <Link
              to="/notifications"
              aria-label="Open Notifications"
              className="relative rounded-2xl p-2 text-muted-foreground transition-colors hover:bg-foreground/5 hover:text-foreground"
            >
              <Bell className="h-4.5 w-4.5" strokeWidth={1.7} />
              {notifications.length ? (
                <span className="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-destructive" />
              ) : null}
            </Link>
            <Link
              to="/profile"
              className="flex items-center gap-2 rounded-2xl py-1 pr-2 pl-1 hover:bg-foreground/5"
            >
              <span className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground">
                NK
              </span>
              <span className="hidden text-sm sm:block">N. Kaur</span>
            </Link>
          </div>
        </header>

        <main className="px-4 pt-4 pb-36 lg:pb-10">{children}</main>
      </motion.div>

      {/* Mobile floating navigation */}
      <div className="fixed inset-x-0 bottom-0 z-40 flex justify-center p-3 lg:hidden">
        <motion.button
          whileTap={{ scale: 0.98 }}
          onClick={() => setMobileOpen(true)}
          aria-label="Open navigation"
          className="glass flex items-center gap-2 rounded-full px-4 py-2.5 text-sm font-medium shadow-lift"
        >
          <LayoutDashboard className="h-4 w-4" strokeWidth={1.7} />
          {current}
        </motion.button>
      </div>

      <AnimatePresence>
        {mobileOpen ? (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-foreground/20 backdrop-blur-sm lg:hidden"
            onClick={() => setMobileOpen(false)}
          >
            <motion.div
              initial={{ y: 32, opacity: 0, scale: 0.96 }}
              animate={{ y: 0, opacity: 1, scale: 1 }}
              exit={{ y: 24, opacity: 0, scale: 0.98 }}
              transition={spring}
              onClick={(e) => e.stopPropagation()}
              className="glass absolute inset-x-4 bottom-4 max-h-[72vh] overflow-y-auto rounded-3xl p-4"
            >
              <div className="mb-3 flex items-center justify-between px-1">
                <span className="text-sm font-semibold">Navigation</span>

                <button
                  type="button"
                  onClick={() => setMobileOpen(false)}
                  aria-label="Close navigation menu"
                  className="rounded-xl p-2 text-muted-foreground transition-colors hover:bg-foreground/5 hover:text-foreground"
                >
                  <ChevronLeft className="h-4 w-4 rotate-180" />
                </button>
              </div>

              <NavItems collapsed={false} onNavigate={() => setMobileOpen(false)} />
            </motion.div>
          </motion.div>
        ) : null}
      </AnimatePresence>
    </div>
  );
}
