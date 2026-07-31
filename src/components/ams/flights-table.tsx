import { AnimatePresence, motion } from "motion/react";
import { spring } from "@/lib/ams/motion";
import { CountUp, Meter, StatusPill } from "@/components/ams/primitives";
import type { Flight } from "@/lib/ams/data";

const HEAD = [
  "Flight",
  "Aircraft",
  "Origin",
  "Destination",
  "Gate",
  "Boarding",
  "Departure",
  "Delay",
  "Crew",
  "Status",
];

export function FlightsTable({ flights }: { flights: Flight[] }) {
  return (
    <div className="-mx-2 overflow-x-auto px-2">
      <table className="w-full min-w-[980px] border-separate border-spacing-y-1 text-sm">
        <thead>
          <tr className="text-left text-xs font-medium text-muted-foreground">
            {HEAD.map((h) => (
              <th key={h} className="px-3 pb-2 font-medium whitespace-nowrap">
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          <AnimatePresence initial={false}>
            {flights.map((f, i) => (
              <motion.tr
                key={f.id}
                layout
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0, transition: { ...spring, delay: i * 0.03 } }}
                exit={{ opacity: 0 }}
                className="group rounded-2xl transition-colors hover:bg-foreground/4"
              >
                <td className="num rounded-l-2xl px-3 py-3 font-medium">{f.number}</td>
                <td className="px-3 py-3">
                  <div className="num text-[13px]">{f.aircraft}</div>
                  <div className="text-xs text-muted-foreground">{f.aircraftModel}</div>
                </td>
                <td className="num px-3 py-3">{f.origin}</td>
                <td className="num px-3 py-3">{f.destination}</td>
                <td className="num px-3 py-3 text-muted-foreground">{f.gate}</td>
                <td className="w-32 px-3 py-3">
                  <Meter value={f.boarding} tone={f.boarding >= 100 ? "success" : "accent"} />
                  <span className="num mt-1 block text-[11px] text-muted-foreground">
                    <CountUp value={f.boarding} suffix="%" />
                  </span>
                </td>
                <td className="num px-3 py-3">{f.departure}</td>
                <td className="num px-3 py-3">
                  {f.delay ? <span className="text-warning">+{f.delay}m</span> : <span className="text-muted-foreground">On time</span>}
                </td>
                <td className="px-3 py-3 whitespace-nowrap text-muted-foreground">{f.crew}</td>
                <td className="rounded-r-2xl px-3 py-3">
                  <StatusPill status={f.status} />
                </td>
              </motion.tr>
            ))}
          </AnimatePresence>
        </tbody>
      </table>
    </div>
  );
}
