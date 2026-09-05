import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { motion } from "motion/react";
import { Users, Plane } from "lucide-react";

import { pageVariants, itemVariants, listVariants } from "@/lib/ams/motion";
import { GlassCard, PageHeader } from "@/components/ams/primitives";
import { getAircraft, type ApiAircraft as Aircraft } from "@/lib/api/aircraft";
import { getAircraftTypes, type AircraftType } from "@/lib/api/aircraft-types";

import narrowbody from "@/assets/aircraft-narrowbody.jpg";
import widebody from "@/assets/aircraft-widebody.jpg";
import hangar from "@/assets/aircraft-hangar.jpg";

export const Route = createFileRoute("/aircraft")({
  head: () => ({
    meta: [
      { title: "Fleet & Aircraft — AirHive AMS" },
      {
        name: "description",
        content: "Fleet aircraft, configuration and operational status.",
      },
      {
        property: "og:title",
        content: "Fleet & Aircraft — AirHive AMS",
      },
      {
        property: "og:description",
        content: "Fleet aircraft, configuration and operational status.",
      },
    ],
  }),
  component: AircraftPage,
});

function imageFor(aircraft: Aircraft, aircraftType?: AircraftType) {
  if (aircraft.status === "MAINTENANCE" || aircraft.status === "GROUNDED") {
    return hangar;
  }

  if (aircraftType && aircraftType.passengerCapacity > 250) {
    return widebody;
  }

  return narrowbody;
}

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

function AircraftPage() {
  const [aircraft, setAircraft] = useState<Aircraft[]>([]);
  const [aircraftTypes, setAircraftTypes] = useState<AircraftType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        setError("");

        const [aircraftData, typeData] = await Promise.all([getAircraft(), getAircraftTypes()]);

        setAircraft(aircraftData);
        setAircraftTypes(typeData);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load aircraft data.");
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, []);

  const getType = (aircraftItem: Aircraft) =>
    aircraftTypes.find((type) => type.id === aircraftItem.aircraftTypeId);

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader
        title="Aircraft"
        description="Fleet condition, configuration and operational status."
      />

      {loading && (
        <GlassCard className="p-6">
          <p className="text-sm text-muted-foreground">Loading aircraft...</p>
        </GlassCard>
      )}

      {!loading && error && (
        <GlassCard className="p-6">
          <p className="text-sm text-destructive">{error}</p>
        </GlassCard>
      )}

      {!loading && !error && aircraft.length === 0 && (
        <GlassCard className="p-6">
          <p className="text-sm text-muted-foreground">No aircraft found.</p>
        </GlassCard>
      )}

      {!loading && !error && aircraft.length > 0 && (
        <motion.div
          variants={listVariants}
          initial="initial"
          animate="animate"
          className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3"
        >
          {aircraft.map((a) => {
            const type = getType(a);

            return (
              <motion.div key={a.id} variants={itemVariants}>
                <GlassCard className="overflow-hidden">
                  <div className="relative h-40 overflow-hidden">
                    <img
                      src={imageFor(a, type)}
                      alt={`${type?.model ?? a.aircraftTypeCode} aircraft`}
                      loading="lazy"
                      width={1024}
                      height={640}
                      className="h-full w-full object-cover"
                    />

                    <span
                      className={`absolute right-3 top-3 rounded-full bg-background/85 px-2.5 py-1 text-xs font-medium shadow-[var(--elev-1)] backdrop-blur-md ${statusTone(a.status)}`}
                    >
                      {statusLabel(a.status)}
                    </span>
                  </div>

                  <div className="space-y-4 p-5">
                    <div className="flex items-baseline justify-between gap-4">
                      <h2 className="num text-lg font-semibold">{a.registrationNumber}</h2>

                      <span className="text-sm text-muted-foreground">
                        {type?.model ?? a.aircraftTypeCode}
                      </span>
                    </div>

                    <div className="rounded-2xl bg-muted/40 p-4">
                      <div className="flex items-center gap-3">
                        <Plane className="h-5 w-5 text-accent" strokeWidth={1.7} />

                        <div>
                          <p className="text-xs text-muted-foreground">Aircraft Type</p>

                          <p className="text-sm font-medium">
                            {type ? `${type.manufacturer} ${type.model}` : a.aircraftTypeCode}
                          </p>
                        </div>
                      </div>
                    </div>

                    <dl className="grid grid-cols-2 gap-3 pt-1 text-xs">
                      <div className="flex items-center gap-2 text-muted-foreground">
                        <Users className="h-3.5 w-3.5" strokeWidth={1.7} />

                        <span className="text-foreground">
                          {type?.passengerCapacity ?? "—"} seats
                        </span>
                      </div>

                      <div className="flex items-center gap-2 text-muted-foreground">
                        <span className="text-foreground">{type?.crewCapacity ?? "—"} crew</span>
                      </div>

                      <div className="col-span-2 text-muted-foreground">
                        Type Code: <span className="text-foreground">{a.aircraftTypeCode}</span>
                      </div>
                    </dl>
                  </div>
                </GlassCard>
              </motion.div>
            );
          })}
        </motion.div>
      )}
    </motion.div>
  );
}
