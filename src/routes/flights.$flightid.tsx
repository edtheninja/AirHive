import { createFileRoute, Link } from "@tanstack/react-router";
import { ArrowLeft, Clock3, Plane, Route as RouteIcon } from "lucide-react";
import { useEffect, useState } from "react";
import { motion } from "motion/react";

import { getFlightById, type Flight } from "@/lib/api/flights";
import { pageVariants } from "@/lib/ams/motion";
import { GlassCard, PageHeader, StatusPill } from "@/components/ams/primitives";
import type { FlightStatus } from "@/lib/ams/data";

export const Route = createFileRoute("/flights/$flightid")({
  head: () => ({
    meta: [
      { title: "Flight Details — AirHive AMS" },
      {
        name: "description",
        content: "Detailed operational information for a flight.",
      },
    ],
  }),
  component: FlightDetailPage,
});

function normalizeStatus(status: string): FlightStatus {
  switch (status.toUpperCase()) {
    case "SCHEDULED":
      return "Scheduled";
    case "BOARDING":
      return "Boarding";
    case "TAXIING":
      return "Taxiing";
    case "DEPARTED":
      return "Departed";
    case "IN AIR":
    case "IN_AIR":
    case "AIRBORNE":
      return "In Air";
    case "LANDING":
      return "Landing";
    case "LANDED":
      return "Landed";
    case "DELAYED":
      return "Delayed";
    case "CANCELLED":
      return "Cancelled";
    default:
      return "Scheduled";
  }
}

function formatDateTime(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return date.toLocaleString([], {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function DetailItem({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-2xl bg-muted/40 p-4">
      <p className="text-xs text-muted-foreground">{label}</p>
      <p className="mt-1 text-sm font-medium">{value}</p>
    </div>
  );
}

function FlightDetailPage() {
  console.log("FLIGHT DETAIL PAGE LOADED");
  const params = Route.useParams();
  const flightId = params.flightid;
  const [flight, setFlight] = useState<Flight | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadFlight() {
      try {
        setLoading(true);
        setError("");

        const data = await getFlightById(Number(flightId));

        if (!cancelled) {
          setFlight(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Failed to load flight details.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadFlight();

    return () => {
      cancelled = true;
    };
  }, [flightId]);

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1200px] space-y-6 py-6"
    >
      <PageHeader
        title={flight ? `Flight ${flight.flightNumber}` : "Flight Details"}
        description="Operational information and scheduled movement details."
        action={
          <Link
            to="/flights"
            className="inline-flex items-center gap-2 rounded-full bg-foreground/5 px-4 py-2 text-sm font-medium transition-colors hover:bg-foreground/10"
          >
            <ArrowLeft className="h-4 w-4" />
            Back to Flights
          </Link>
        }
      />

      {loading && (
        <GlassCard hover={false} className="p-6">
          <p className="text-sm text-muted-foreground">Loading flight details...</p>
        </GlassCard>
      )}

      {!loading && error && (
        <GlassCard hover={false} className="p-6">
          <p className="text-sm text-destructive">{error}</p>
        </GlassCard>
      )}

      {!loading && !error && !flight && (
        <GlassCard hover={false} className="p-6">
          <p className="text-sm text-muted-foreground">Flight not found.</p>
        </GlassCard>
      )}

      {!loading && !error && flight && (
        <>
          <GlassCard hover={false} className="overflow-hidden">
            <div className="flex flex-wrap items-center justify-between gap-4 border-b border-border/60 px-6 py-5">
              <div className="flex items-center gap-3">
                <div className="rounded-2xl bg-primary/10 p-3 text-primary">
                  <Plane className="h-6 w-6" />
                </div>

                <div>
                  <p className="text-xs text-muted-foreground">Flight number</p>
                  <h2 className="num text-2xl font-semibold">{flight.flightNumber}</h2>
                </div>
              </div>

              <StatusPill status={normalizeStatus(flight.status)} />
            </div>

            <div className="grid gap-4 p-6 md:grid-cols-[1fr_auto_1fr] md:items-center">
              <div>
                <p className="text-xs text-muted-foreground">Departure</p>
                <p className="num mt-1 text-3xl font-semibold">{flight.departureAirportCode}</p>
                <p className="mt-1 text-sm text-muted-foreground">
                  {formatDateTime(flight.scheduledDeparture)}
                </p>
              </div>

              <div className="flex items-center justify-center gap-2 text-muted-foreground">
                <div className="h-px w-8 bg-border" />
                <RouteIcon className="h-5 w-5" />
                <div className="h-px w-8 bg-border" />
              </div>

              <div className="md:text-right">
                <p className="text-xs text-muted-foreground">Arrival</p>
                <p className="num mt-1 text-3xl font-semibold">{flight.arrivalAirportCode}</p>
                <p className="mt-1 text-sm text-muted-foreground">
                  {formatDateTime(flight.scheduledArrival)}
                </p>
              </div>
            </div>
          </GlassCard>

          <div className="grid gap-4 md:grid-cols-2">
            <GlassCard hover={false} className="p-6">
              <div className="mb-4 flex items-center gap-2">
                <Plane className="h-5 w-5 text-primary" />
                <h2 className="font-semibold">Aircraft information</h2>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <DetailItem label="Registration" value={flight.aircraftRegistration} />
                <DetailItem label="Aircraft ID" value={flight.aircraftId} />
                <DetailItem label="Route ID" value={flight.routeId} />
                <DetailItem label="Flight ID" value={flight.id} />
              </div>
            </GlassCard>

            <GlassCard hover={false} className="p-6">
              <div className="mb-4 flex items-center gap-2">
                <Clock3 className="h-5 w-5 text-primary" />
                <h2 className="font-semibold">Schedule information</h2>
              </div>

              <div className="grid gap-3">
                <DetailItem
                  label="Scheduled departure"
                  value={formatDateTime(flight.scheduledDeparture)}
                />
                <DetailItem
                  label="Scheduled arrival"
                  value={formatDateTime(flight.scheduledArrival)}
                />
              </div>
            </GlassCard>
          </div>
        </>
      )}
    </motion.div>
  );
}
