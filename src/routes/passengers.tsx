import { useEffect, useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { pageVariants } from "@/lib/ams/motion";
import { getPassengers, type Passenger } from "@/lib/api/passengers";
import { PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/passengers")({
  head: () => ({
    meta: [
      { title: "Passengers — AirHive AMS" },
      {
        name: "description",
        content: "Passenger manifests, loyalty tier, check-in state and baggage per flight.",
      },
      { property: "og:title", content: "Passengers — AirHive AMS" },
      {
        property: "og:description",
        content: "Passenger manifests, loyalty tier and check-in state.",
      },
    ],
  }),
  component: PassengersPage,
});

const tierTone: Record<string, string> = {
  Blue: "bg-foreground/6 text-muted-foreground",
  Silver: "bg-info/12 text-info",
  Gold: "bg-warning/16 text-warning",
  Platinum: "bg-primary/12 text-primary dark:text-info",
};

function PassengersPage() {
  const [passengers, setPassengers] = useState<Passenger[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadPassengers() {
      try {
        const data = await getPassengers();

        if (!cancelled) {
          setPassengers(data);
        }
      } catch {
        if (!cancelled) {
          setError("Unable to load passengers. Please try again.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void loadPassengers();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader
        title="Passengers"
        description="Manifest view across today's departures."
      />

      <SectionCard
        title="Manifest"
        subtitle={
          loading
            ? "Loading passengers..."
            : `${passengers.length} passengers on active flights`
        }
      >
        {loading ? (
          <div className="py-12 text-center text-sm text-muted-foreground">
            Loading passenger manifest...
          </div>
        ) : error ? (
          <div className="py-12 text-center text-sm text-destructive">
            {error}
          </div>
        ) : passengers.length === 0 ? (
          <div className="py-12 text-center text-sm text-muted-foreground">
            No passengers found.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] border-separate border-spacing-y-1 text-sm">
              <thead>
                <tr className="text-left text-xs text-muted-foreground">
                  {[
                    "Passenger",
                    "Ref",
                    "Tier",
                    "Flight",
                    "Route",
                    "Seat",
                    "Bags",
                    "Check-in",
                  ].map((heading) => (
                    <th key={heading} className="px-3 pb-2 font-medium">
                      {heading}
                    </th>
                  ))}
                </tr>
              </thead>

              <tbody>
                {passengers.map((passenger, index) => (
                  <motion.tr
                    key={passenger.id}
                    initial={{ opacity: 0, y: 8 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.02 }}
                    className="hover:bg-foreground/4"
                  >
                    <td className="rounded-l-2xl px-3 py-3 font-medium">
                      {passenger.name}
                    </td>

                    <td className="num px-3 py-3 text-muted-foreground">
                      {passenger.passengerCode}
                    </td>

                    <td className="px-3 py-3">
                      <span
                        className={`rounded-full px-2.5 py-1 text-xs ${
                          tierTone[passenger.tier] ??
                          "bg-foreground/6 text-muted-foreground"
                        }`}
                      >
                        {passenger.tier}
                      </span>
                    </td>

                    <td className="num px-3 py-3">{passenger.flight}</td>

                    <td className="num px-3 py-3 text-muted-foreground">
                      {passenger.route}
                    </td>

                    <td className="num px-3 py-3">{passenger.seat}</td>

                    <td className="num px-3 py-3 text-muted-foreground">
                      {passenger.bags}
                    </td>

                    <td className="rounded-r-2xl px-3 py-3">
                      <span
                        className={`rounded-full px-2.5 py-1 text-xs ${
                          passenger.checkedIn
                            ? "bg-success/14 text-success"
                            : "bg-foreground/6 text-muted-foreground"
                        }`}
                      >
                        {passenger.checkedIn ? "Checked in" : "Pending"}
                      </span>
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
