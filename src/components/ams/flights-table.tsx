import { AnimatePresence, motion } from "motion/react";
import { spring } from "@/lib/ams/motion";
import { StatusPill } from "@/components/ams/primitives";
import type { Flight } from "@/lib/ams/data";
import { useLiveOps } from "@/lib/ams/hooks";
import { Link } from "@tanstack/react-router";

const HEAD = [
  "Flight",
  "Aircraft",
  "Origin",
  "Destination",
  "Departure",
  "Arrival",
  "Block time",
  "Status",
];

function formatDuration(departure: string, arrival: string) {
  const departureDate = new Date(departure);
  const arrivalDate = new Date(arrival);

  if (Number.isNaN(departureDate.getTime()) || Number.isNaN(arrivalDate.getTime())) {
    return "—";
  }

  let minutes = Math.round((arrivalDate.getTime() - departureDate.getTime()) / 60000);

  if (minutes < 0) {
    minutes += 24 * 60;
  }

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

export function FlightsTable({ flights }: { flights: Flight[] }) {
  const { loading, error } = useLiveOps();

  return (
    <div className="-mx-2 overflow-x-auto px-2">
      {loading ? (
        <div className="px-3 py-8 text-sm text-muted-foreground">Loading flight operations…</div>
      ) : error ? (
        <div className="px-3 py-8 text-sm text-destructive">{error}</div>
      ) : flights.length === 0 ? (
        <div className="px-3 py-8 text-sm text-muted-foreground">
          No flight operations are currently available.
        </div>
      ) : (
        <table className="w-full min-w-[900px] border-separate border-spacing-y-1 text-sm">
          <thead>
            <tr className="text-left text-xs font-medium text-muted-foreground">
              {HEAD.map((heading) => (
                <th key={heading} className="whitespace-nowrap px-3 pb-2 font-medium">
                  {heading}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            <AnimatePresence initial={false}>
              {flights.map((flight, index) => (
                <motion.tr
                  key={flight.id}
                  layout
                  initial={{ opacity: 0, y: 8 }}
                  animate={{
                    opacity: 1,
                    y: 0,
                    transition: {
                      ...spring,
                      delay: index * 0.03,
                    },
                  }}
                  exit={{ opacity: 0 }}
                  className="group rounded-2xl transition-colors hover:bg-foreground/4"
                >
                  <td className="num rounded-l-2xl px-3 py-3 font-medium">
                    <Link
                      to="/flights/$flightid"
                      params={{ flightid: String(flight.id) }}
                      className="relative z-10 inline-block cursor-pointer hover:text-primary hover:underline"
                    >
                      {flight.number}
                    </Link>
                  </td>

                  <td className="px-3 py-3">
                    <div className="num text-[13px]">{flight.aircraft}</div>
                    <div className="text-xs text-muted-foreground">{flight.aircraftModel}</div>
                  </td>

                  <td className="num px-3 py-3">{flight.origin}</td>

                  <td className="num px-3 py-3">{flight.destination}</td>

                  <td className="num px-3 py-3">{flight.departure}</td>

                  <td className="num px-3 py-3">{flight.arrival}</td>

                  <td className="num px-3 py-3 text-muted-foreground">
                    {formatDuration(flight.scheduledDeparture, flight.scheduledArrival)}
                  </td>

                  <td className="rounded-r-2xl px-3 py-3">
                    <StatusPill status={flight.status} />
                  </td>
                </motion.tr>
              ))}
            </AnimatePresence>
          </tbody>
        </table>
      )}
    </div>
  );
}
