import { AnimatePresence, motion } from "motion/react";
import {
  AlertTriangle,
  Bell,
  CheckCircle2,
  Info,
  OctagonAlert,
} from "lucide-react";
import { useNotifications } from "@/lib/notifications/notification-context";
import { spring } from "@/lib/ams/motion";
import { cn } from "@/lib/utils";
import type { NotificationSeverity } from "@/lib/api/notifications";

const toneMap: Record<
  NotificationSeverity,
  { icon: typeof Info; className: string }
> = {
  INFO: { icon: Info, className: "text-info bg-info/12" },
  WARNING: { icon: AlertTriangle, className: "text-warning bg-warning/16" },
  CRITICAL: {
    icon: OctagonAlert,
    className: "text-destructive bg-destructive/14",
  },
};

export function NotificationsPanel({
  limit = 6,
  className,
}: {
  limit?: number;
  className?: string;
}) {
  const { notifications, loading, markAsRead } = useNotifications();

  return (
    <div className={cn("glass overflow-hidden rounded-3xl", className)}>
      <div className="flex items-center justify-between px-6 pt-6 pb-4">
        <div className="flex items-center gap-2">
          <Bell className="h-4 w-4 text-muted-foreground" strokeWidth={1.7} />
          <h2 className="text-base font-semibold">Notifications</h2>
        </div>

        <span className="num rounded-full bg-foreground/5 px-2.5 py-1 text-xs text-muted-foreground">
          {notifications.length}
        </span>
      </div>

      <div className="max-h-105 space-y-2 overflow-y-auto px-4 pb-5">
        {loading ? (
          <div className="px-3 py-8 text-center text-sm text-muted-foreground">
            Loading notifications...
          </div>
        ) : (
          <AnimatePresence initial={false}>
            {notifications.slice(0, limit).map((notification) => {
              const tone = toneMap[notification.severity];
              const Icon = tone.icon;

              return (
                <motion.div
                  key={notification.id}
                  layout
                  initial={{ opacity: 0, x: 40, filter: "blur(6px)" }}
                  animate={{ opacity: 1, x: 0, filter: "blur(0px)" }}
                  exit={{
                    opacity: 0,
                    x: 24,
                    height: 0,
                    marginBottom: 0,
                  }}
                  transition={spring}
                  className={cn(
                    "group flex items-start gap-3 rounded-2xl px-3 py-3 transition-colors hover:bg-foreground/4",
                    !notification.read && "bg-foreground/3",
                  )}
                >
                  <span
                    className={cn(
                      "mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-xl",
                      tone.className,
                    )}
                  >
                    <Icon className="h-4 w-4" strokeWidth={1.8} />
                  </span>

                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium">
                      {notification.title}
                    </p>

                    <p className="mt-0.5 text-xs text-muted-foreground">
                      {notification.message}
                    </p>

                    <p className="num mt-1 text-[11px] text-muted-foreground/80">
                      {new Date(notification.createdAt).toLocaleString()}
                    </p>
                  </div>

                  {!notification.read && (
                    <button
                      onClick={() => void markAsRead(notification.id)}
                      aria-label={`Mark ${notification.title} as read`}
                      className="rounded-lg p-1 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100 hover:bg-foreground/6"
                    >
                      <CheckCircle2 className="h-3.5 w-3.5" />
                    </button>
                  )}
                </motion.div>
              );
            })}
          </AnimatePresence>
        )}

        {!loading && notifications.length === 0 && (
          <div className="px-3 py-8 text-center text-sm text-muted-foreground">
            No notifications.
          </div>
        )}
      </div>
    </div>
  );
}
