import { motion } from "motion/react";
import { useLiveOps } from "@/lib/ams/hooks";

const PATHS = [
  { d: "M 90 210 C 260 90, 520 90, 700 170", dur: 9 },
  { d: "M 140 300 C 330 250, 560 330, 820 250", dur: 12 },
  { d: "M 60 130 C 300 200, 480 120, 760 320", dur: 14 },
  { d: "M 220 360 C 420 300, 620 380, 880 300", dur: 10 },
];

const HUBS = [
  { x: 90, y: 210, code: "DEL" },
  { x: 700, y: 170, code: "LHR" },
  { x: 820, y: 250, code: "JFK" },
  { x: 140, y: 300, code: "BOM" },
  { x: 480, y: 120, code: "DXB" },
];

export function FlightMap() {
  const { loading, kpis } = useLiveOps();

  return (
    <div className="relative h-[340px] w-full overflow-hidden rounded-2xl bg-foreground/3">
      <svg
        viewBox="0 0 960 420"
        className="h-full w-full"
        role="img"
        aria-label="Live aircraft tracking map"
      >
        <defs>
          <linearGradient id="pathGrad" x1="0" x2="1">
            <stop offset="0%" stopColor="var(--color-accent)" stopOpacity="0.05" />
            <stop offset="50%" stopColor="var(--color-accent)" stopOpacity="0.6" />
            <stop offset="100%" stopColor="var(--color-accent)" stopOpacity="0.05" />
          </linearGradient>
          <radialGradient id="glow">
            <stop offset="0%" stopColor="var(--color-accent)" stopOpacity="0.9" />
            <stop offset="100%" stopColor="var(--color-accent)" stopOpacity="0" />
          </radialGradient>
        </defs>

        {Array.from({ length: 13 }).map((_, i) => (
          <line
            key={`v${i}`}
            x1={i * 80}
            y1="0"
            x2={i * 80}
            y2="420"
            stroke="currentColor"
            className="text-foreground/5"
            strokeWidth="1"
          />
        ))}
        {Array.from({ length: 7 }).map((_, i) => (
          <line
            key={`h${i}`}
            x1="0"
            y1={i * 70}
            x2="960"
            y2={i * 70}
            stroke="currentColor"
            className="text-foreground/5"
            strokeWidth="1"
          />
        ))}

        {PATHS.map((p, i) => (
          <g key={i}>
            <path
              d={p.d}
              fill="none"
              stroke="url(#pathGrad)"
              strokeWidth="1.6"
              strokeDasharray="6 8"
            />
            <circle r="16" fill="url(#glow)">
              <animateMotion dur={`${p.dur}s`} repeatCount="indefinite" path={p.d} />
            </circle>
            <circle r="3.5" fill="var(--color-accent)">
              <animateMotion dur={`${p.dur}s`} repeatCount="indefinite" path={p.d} />
            </circle>
          </g>
        ))}

        {HUBS.map((h) => (
          <g key={h.code}>
            <circle cx={h.x} cy={h.y} r="4" className="fill-primary dark:fill-info" />
            <circle
              cx={h.x}
              cy={h.y}
              r="10"
              className="fill-none stroke-primary/30 dark:stroke-info/30"
              strokeWidth="1"
            />
            <text
              x={h.x + 14}
              y={h.y + 4}
              className="fill-muted-foreground text-[11px]"
              fontFamily="var(--font-mono)"
            >
              {h.code}
            </text>
          </g>
        ))}
      </svg>

      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        className="glass absolute bottom-4 left-4 rounded-2xl px-4 py-2.5 text-xs"
      >
        <p className="text-muted-foreground">Live tracking preview</p>
        <p className="num mt-0.5 text-sm">
          {loading ? "—" : `${kpis.activeFlights} flights airborne`}
        </p>
      </motion.div>
    </div>
  );
}
