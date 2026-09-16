import { createFileRoute, Link } from "@tanstack/react-router";
import { ArrowLeft, Plane, ShieldCheck } from "lucide-react";
import { useEffect, useState } from "react";
import { motion } from "motion/react";

import { pageVariants } from "@/lib/ams/motion";
import { GlassCard, PageHeader } from "@/components/ams/primitives";
import { getAircraftById, type ApiAircraft } from "@/lib/api/aircraft";
import { getAircraftTypes, type AircraftType } from "@/lib/api/aircraft-types";

export const Route = createFileRoute("/aircraft/$aircraftid")({
  head: () => ({
    meta: [
      { title: "Aircraft Details — AirHive AMS" },
      {
        name: "description",
        content: "Detailed aircraft information and operational status.",
      },
    ],
  }),
  component: AircraftDetailPage,
});

function statusLabel(status: string) {
  switch (status.toUpperCase()) {
    case "ACTIVE":
      return "In Service";
    case "MAINTENANCE":
      return "Maintenance";
    case "GROUNDED":
      return "Grounded";
    case "STANDBY":
      return "Standby";
    default:
      return status;
  }
}

function statusTone(status: string) {
  switch (status.toUpperCase()) {
    case "ACTIVE":
      return "text-success";
    case "MAINTENANCE":
      return "text-warning";
    case "GROUNDED":
      return "text-destructive";
    case "STANDBY":
      return "text-info";
    default:
      return "text-muted-foreground";
  }
}

function DetailItem({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-2xl bg-muted/40 p-4">
      <p className="text-xs text-muted-foreground">{label}</p>
      <p className="mt-1 text-sm font-medium">{value}</p>
    </div>
  );
}

function AircraftDetailPage() {
  const params = Route.useParams();
  const aircraftId = params.aircraftid;

  const [aircraft, setAircraft] = useState<ApiAircraft | null>(null);
  const [aircraftType, setAircraftType] = useState<AircraftType | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadAircraft() {
      try {
        setLoading(true);
        setError("");

        const aircraftData = await getAircraftById(Number(aircraftId));

        let typeData: AircraftType | null = null;

        try {
          const aircraftTypes = await getAircraftTypes();

          typeData = aircraftTypes.find((type) => type.id === aircraftData.aircraftTypeId) ?? null;
        } catch {
          typeData = null;
        }

        if (!cancelled) {
          setAircraft(aircraftData);
          setAircraftType(typeData);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Failed to load aircraft details.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadAircraft();

    return () => {
      cancelled = true;
    };
  }, [aircraftId]);

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1200px] space-y-6 py-6"
    >
      <PageHeader
        title={aircraft ? `Aircraft ${aircraft.registrationNumber}` : "Aircraft Details"}
        description="Aircraft configuration and operational information."
        action={
          <Link
            to="/aircraft"
            className="inline-flex items-center gap-2 rounded-full bg-foreground/5 px-4 py-2 text-sm font-medium transition-colors hover:bg-foreground/10"
          >
            <ArrowLeft className="h-4 w-4" />
            Back to Aircraft
          </Link>
        }
      />

      {loading && (
        <GlassCard className="p-6">
          <p className="text-sm text-muted-foreground">Loading aircraft details...</p>
        </GlassCard>
      )}

      {!loading && error && (
        <GlassCard className="p-6">
          <p className="text-sm text-destructive">{error}</p>
        </GlassCard>
      )}

      {!loading && !error && aircraft && (
        <>
          <GlassCard hover={false} className="p-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-center gap-3">
                <div className="rounded-2xl bg-primary/10 p-3">
                  <Plane className="h-6 w-6 text-primary" />
                </div>

                <div>
                  <p className="text-xs text-muted-foreground">Registration number</p>
                  <h2 className="num text-2xl font-semibold">{aircraft.registrationNumber}</h2>
                </div>
              </div>

              <span
                className={`inline-flex w-fit items-center gap-2 rounded-full bg-muted/60 px-3 py-1.5 text-sm font-medium ${statusTone(
                  aircraft.status,
                )}`}
              >
                <ShieldCheck className="h-4 w-4" />
                {statusLabel(aircraft.status)}
              </span>
            </div>
          </GlassCard>

          <div className="grid gap-4 md:grid-cols-2">
            <GlassCard hover={false} className="p-6">
              <h2 className="mb-4 font-semibold">Aircraft information</h2>

              <div className="grid gap-3 sm:grid-cols-2">
                <DetailItem label="Aircraft ID" value={aircraft.id} />
                <DetailItem label="Registration" value={aircraft.registrationNumber} />
                <DetailItem label="Aircraft type ID" value={aircraft.aircraftTypeId} />
                <DetailItem label="Aircraft type code" value={aircraft.aircraftTypeCode} />
              </div>
            </GlassCard>

            <GlassCard hover={false} className="p-6">
              <h2 className="mb-4 font-semibold">Configuration</h2>

              <div className="grid gap-3 sm:grid-cols-2">
                <DetailItem label="Manufacturer" value={aircraftType?.manufacturer ?? "—"} />
                <DetailItem
                  label="Model"
                  value={aircraftType?.model ?? aircraft.aircraftTypeCode}
                />
                <DetailItem
                  label="Passenger capacity"
                  value={aircraftType?.passengerCapacity ?? "—"}
                />
                <DetailItem label="Crew capacity" value={aircraftType?.crewCapacity ?? "—"} />
              </div>
            </GlassCard>
          </div>
        </>
      )}
    </motion.div>
  );
}
