import { createFileRoute, Link } from "@tanstack/react-router";
import { ArrowLeft, Clock3, Plane, Route as RouteIcon } from "lucide-react";
import { useEffect, useState } from "react";
import { motion } from "motion/react";

import { getFlightById, updateFlightStatus, type Flight } from "@/lib/api/flights";
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

const flightStatusOptions = [
  { label: "Scheduled", value: "SCHEDULED" },
  { label: "Boarding", value: "BOARDING" },
  { label: "Taxiing", value: "TAXIING" },
  { label: "Departed", value: "DEPARTED" },
  { label: "In Air", value: "IN AIR" },
  { label: "Landing", value: "LANDING" },
  { label: "Landed", value: "LANDED" },
  { label: "Delayed", value: "DELAYED" },
  { label: "Cancelled", value: "CANCELLED" },
];

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
  const params = Route.useParams();
  const flightId = params.flightid;

  const [flight, setFlight] = useState<Flight | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedStatus, setSelectedStatus] = useState("");
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [statusMessage, setStatusMessage] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadFlight() {
      try {
        setLoading(true);
        setError("");
        setStatusMessage("");

        const data = await getFlightById(Number(flightId));

        if (!cancelled) {
          setFlight(data);
          setSelectedStatus(data.status);
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

  async function handleStatusUpdate() {
    if (!flight || !selectedStatus || selectedStatus === flight.status) {
      return;
    }

    try {
      setUpdatingStatus(true);
      setStatusMessage("");
      setError("");

      const updatedFlight = await updateFlightStatus(flight, selectedStatus);

      setFlight(updatedFlight);
      setSelectedStatus(updatedFlight.status);
      setStatusMessage("Flight status updated successfully.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update flight status.");
    } finally {
      setUpdatingStatus(false);
    }
  }

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
          {statusMessage && (
            <p className="text-sm text-emerald-600 dark:text-emerald-400">{statusMessage}</p>
          )}

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

              <div className="flex flex-wrap items-center gap-3">
                <StatusPill status={normalizeStatus(flight.status)} />

                <select
                  value={selectedStatus}
                  onChange={(event) => {
                    setSelectedStatus(event.target.value);
                    setStatusMessage("");
                  }}
                  disabled={updatingStatus}
                  aria-label="Select flight status"
                  className="rounded-full border border-border/60 bg-background px-3 py-2 text-sm font-medium outline-none transition-colors focus:border-primary disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {flightStatusOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>

                <button
                  type="button"
                  onClick={handleStatusUpdate}
                  disabled={updatingStatus || !selectedStatus || selectedStatus === flight.status}
                  className="rounded-full bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {updatingStatus ? "Updating..." : "Update Status"}
                </button>
              </div>
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
