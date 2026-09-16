import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { motion } from "motion/react";

import { pageVariants } from "@/lib/ams/motion";
import { getRouteById, type Route as ApiRoute } from "@/lib/api/routes";
import { PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/routes/$routeid")({
  component: RouteDetailPage,
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

function RouteDetailPage() {
  const { routeid } = Route.useParams();

  const [route, setRoute] = useState<ApiRoute | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadRoute() {
      try {
        setLoading(true);
        setError("");

        const data = await getRouteById(Number(routeid));
        setRoute(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load route.");
      } finally {
        setLoading(false);
      }
    }

    loadRoute();
  }, [routeid]);

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader
        title="Route details"
        description="Detailed information about this network route."
      />

      <SectionCard
        title="Route information"
        subtitle={`Route ID: ${routeid}`}
      >
        {loading && (
          <div className="px-3 py-6">
            <p className="text-sm text-muted-foreground">Loading route...</p>
          </div>
        )}

        {!loading && error && (
          <div className="space-y-4 px-3 py-6">
            <p className="text-sm text-destructive">{error}</p>

            <Link
              to="/routes"
              className="inline-flex text-sm font-medium text-primary hover:underline"
            >
              ← Back to Routes
            </Link>
          </div>
        )}

        {!loading && !error && route && (
          <div className="space-y-6 px-3 py-4">
            <div>
              <p className="text-xs uppercase tracking-wide text-muted-foreground">
                Route
              </p>
              <p className="mt-1 text-2xl font-semibold">
                {route.departureAirportCode} → {route.arrivalAirportCode}
              </p>
            </div>

            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <div>
                <p className="text-xs text-muted-foreground">Departure airport</p>
                <p className="mt-1 font-medium">{route.departureAirportCode}</p>
              </div>

              <div>
                <p className="text-xs text-muted-foreground">Arrival airport</p>
                <p className="mt-1 font-medium">{route.arrivalAirportCode}</p>
              </div>

              <div>
                <p className="text-xs text-muted-foreground">Distance</p>
                <p className="mt-1 font-medium">
                  {route.distanceKm.toLocaleString()} km
                </p>
              </div>

              <div>
                <p className="text-xs text-muted-foreground">Estimated duration</p>
                <p className="mt-1 font-medium">
                  {formatDuration(route.estimatedDurationMinutes)}
                </p>
              </div>
            </div>

            <div>
              <p className="text-xs text-muted-foreground">Status</p>
              <p className={`mt-1 font-medium ${statusTone(route.status)}`}>
                {statusLabel(route.status)}
              </p>
            </div>

            <Link
              to="/routes"
              className="inline-flex text-sm font-medium text-primary hover:underline"
            >
              ← Back to Routes
            </Link>
          </div>
        )}
      </SectionCard>
    </motion.div>
  );
}
