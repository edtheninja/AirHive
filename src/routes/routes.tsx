import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { motion } from "motion/react";

import { pageVariants } from "@/lib/ams/motion";
import { getRoutes, type Route as ApiRoute } from "@/lib/api/routes";
import { PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/routes")({
  head: () => ({
    meta: [
      { title: "Network Routes — AirHive AMS" },
      {
        name: "description",
        content: "Network routes, distance, duration and operational status.",
      },
      {
        property: "og:title",
        content: "Network Routes — AirHive AMS",
      },
      {
        property: "og:description",
        content: "Network routes, distance, duration and operational status.",
      },
    ],
  }),
  component: RoutesPage,
});

function formatDuration(minutes: number) {
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;

  if (hours === 0) {
    return `${remainingMinutes}m`;
  }

  if (remainingMinutes === 0) {
    return `${hours}h`;
  }

  return `${hours}h ${remainingMinutes}m`;
}

function statusLabel(status: string) {
  switch (status.toUpperCase()) {
    case "ACTIVE":
      return "Active";
    case "INACTIVE":
      return "Inactive";
    default:
      return status;
  }
}

function statusTone(status: string) {
  switch (status.toUpperCase()) {
    case "ACTIVE":
      return "text-success";
    case "INACTIVE":
      return "text-muted-foreground";
    default:
      return "text-warning";
  }
}

function RoutesPage() {
  const [routes, setRoutes] = useState<ApiRoute[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadRoutes() {
      try {
        setLoading(true);
        setError("");

        const data = await getRoutes();
        setRoutes(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load routes.");
      } finally {
        setLoading(false);
      }
    }

    loadRoutes();
  }, []);

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader title="Routes" description="Sector performance and network planning." />

      <SectionCard
        title="Network routes"
        subtitle="Live route information from the AirHive backend"
      >
        {loading && (
          <div className="px-3 py-6">
            <p className="text-sm text-muted-foreground">Loading routes...</p>
          </div>
        )}

        {!loading && error && (
          <div className="px-3 py-6">
            <p className="text-sm text-destructive">{error}</p>
          </div>
        )}

        {!loading && !error && routes.length === 0 && (
          <div className="px-3 py-6">
            <p className="text-sm text-muted-foreground">No routes found.</p>
          </div>
        )}

        {!loading && !error && routes.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] border-separate border-spacing-y-1 text-sm">
              <thead>
                <tr className="text-left text-xs text-muted-foreground">
                  {["Route", "Distance", "Block time", "Departure", "Arrival", "Status"].map(
                    (heading) => (
                      <th key={heading} className="px-3 pb-2 font-medium">
                        {heading}
                      </th>
                    ),
                  )}
                </tr>
              </thead>

              <tbody>
                {routes.map((route, index) => (
                  <motion.tr
                    key={route.id}
                    initial={{ opacity: 0, y: 8 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{
                      delay: index * 0.04,
                    }}
                    className="hover:bg-foreground/4"
                  >
                    <td className="num rounded-l-2xl px-3 py-3 font-medium">
                      {route.departureAirportCode} → {route.arrivalAirportCode}
                    </td>

                    <td className="num px-3 py-3 text-muted-foreground">
                      {route.distanceKm.toLocaleString()} km
                    </td>

                    <td className="num px-3 py-3 text-muted-foreground">
                      {formatDuration(route.estimatedDurationMinutes)}
                    </td>

                    <td className="px-3 py-3 text-muted-foreground">
                      {route.departureAirportCode}
                    </td>

                    <td className="px-3 py-3 text-muted-foreground">{route.arrivalAirportCode}</td>

                    <td className="rounded-r-2xl px-3 py-3">
                      <span className={statusTone(route.status)}>{statusLabel(route.status)}</span>
                    </td>
                  </motion.tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </SectionCard>
    </motion.div>
  );
}
