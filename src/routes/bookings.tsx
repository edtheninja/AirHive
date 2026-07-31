import { createFileRoute } from "@tanstack/react-router";
import { AnimatePresence, motion } from "motion/react";
import { useMemo, useState } from "react";
import { ChevronLeft, ChevronRight, Download, Search, X } from "lucide-react";
import { toast } from "sonner";
import { pageVariants, spring } from "@/lib/ams/motion";
import { BOOKINGS, type Booking } from "@/lib/ams/data";
import { PageHeader, SectionCard } from "@/components/ams/primitives";

export const Route = createFileRoute("/bookings")({
  head: () => ({
    meta: [
      { title: "Bookings — Aerion AMS" },
      { name: "description", content: "Search, filter, export and inspect every booking across the network." },
      { property: "og:title", content: "Bookings — Aerion AMS" },
      { property: "og:description", content: "Search, filter and inspect every booking across the network." },
    ],
  }),
  component: BookingsPage,
});

const PAGE_SIZE = 8;
const CABINS = ["All", "Economy", "Premium", "Business", "First"] as const;

const statusTone: Record<Booking["status"], string> = {
  Confirmed: "bg-info/12 text-info",
  "Checked In": "bg-success/14 text-success",
  Pending: "bg-warning/16 text-warning",
  Cancelled: "bg-destructive/14 text-destructive",
};

function BookingsPage() {
  const [query, setQuery] = useState("");
  const [cabin, setCabin] = useState<(typeof CABINS)[number]>("All");
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<Booking | null>(null);

  const filtered = useMemo(
    () =>
      BOOKINGS.filter(
        (b) =>
          (cabin === "All" || b.cabin === cabin) &&
          (query === "" || `${b.passenger}${b.id}${b.flight}${b.route}`.toLowerCase().includes(query.toLowerCase())),
      ),
    [query, cabin],
  );

  const pages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const current = Math.min(page, pages - 1);
  const rows = filtered.slice(current * PAGE_SIZE, current * PAGE_SIZE + PAGE_SIZE);

  return (
    <motion.div variants={pageVariants} initial="initial" animate="animate" className="mx-auto max-w-[1600px] space-y-6 py-6">
      <PageHeader
        title="Bookings"
        description="Reservation management with search, filters and export."
        action={
          <motion.button
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            onClick={() => toast("Export queued", { description: "CSV will be emailed to ops@aerion.example" })}
            className="glass flex items-center gap-2 rounded-2xl px-4 py-2.5 text-sm font-medium"
          >
            <Download className="h-4 w-4" strokeWidth={1.7} /> Export
          </motion.button>
        }
      />

      <SectionCard
        title="All bookings"
        subtitle={`${filtered.length} records`}
        action={
          <div className="flex flex-wrap items-center gap-2">
            <div className="flex items-center gap-2 rounded-2xl bg-foreground/4 px-3 py-2 text-sm">
              <Search className="h-4 w-4 text-muted-foreground" strokeWidth={1.7} />
              <input
                value={query}
                onChange={(e) => {
                  setQuery(e.target.value);
                  setPage(0);
                }}
                placeholder="Search passenger or PNR"
                className="w-52 bg-transparent outline-none placeholder:text-muted-foreground"
              />
            </div>
            {CABINS.map((c) => (
              <button
                key={c}
                onClick={() => {
                  setCabin(c);
                  setPage(0);
                }}
                className={`rounded-full px-3 py-1.5 text-xs transition-colors ${
                  cabin === c ? "bg-primary text-primary-foreground" : "bg-foreground/5 text-muted-foreground hover:text-foreground"
                }`}
              >
                {c}
              </button>
            ))}
          </div>
        }
      >
        <div className="overflow-x-auto">
          <table className="w-full min-w-[860px] border-separate border-spacing-y-1 text-sm">
            <thead>
              <tr className="text-left text-xs text-muted-foreground">
                {["PNR", "Passenger", "Flight", "Route", "Cabin", "Seat", "Amount", "Status", ""].map((h) => (
                  <th key={h} className="px-3 pb-2 font-medium">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              <AnimatePresence initial={false} mode="popLayout">
                {rows.map((b, i) => (
                  <motion.tr
                    key={b.id}
                    layout
                    initial={{ opacity: 0, y: 8 }}
                    animate={{ opacity: 1, y: 0, transition: { ...spring, delay: i * 0.03 } }}
                    exit={{ opacity: 0 }}
                    className="hover:bg-foreground/4"
                  >
                    <td className="num rounded-l-2xl px-3 py-3 font-medium">{b.id}</td>
                    <td className="px-3 py-3">{b.passenger}</td>
                    <td className="num px-3 py-3">{b.flight}</td>
                    <td className="num px-3 py-3 text-muted-foreground">{b.route}</td>
                    <td className="px-3 py-3 text-muted-foreground">{b.cabin}</td>
                    <td className="num px-3 py-3">{b.seat}</td>
                    <td className="num px-3 py-3">${b.amount.toLocaleString()}</td>
                    <td className="px-3 py-3">
                      <span className={`rounded-full px-2.5 py-1 text-xs ${statusTone[b.status]}`}>{b.status}</span>
                    </td>
                    <td className="rounded-r-2xl px-3 py-3 text-right">
                      <button
                        onClick={() => setSelected(b)}
                        className="rounded-xl px-2.5 py-1 text-xs text-accent transition-colors hover:bg-accent/10"
                      >
                        View
                      </button>
                    </td>
                  </motion.tr>
                ))}
              </AnimatePresence>
            </tbody>
          </table>
        </div>

        <div className="mt-4 flex items-center justify-between text-sm text-muted-foreground">
          <span className="num text-xs">
            Page {current + 1} of {pages}
          </span>
          <div className="flex gap-2">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              className="rounded-xl border border-border/60 p-2 transition-colors hover:bg-foreground/5"
              aria-label="Previous page"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            <button
              onClick={() => setPage((p) => Math.min(pages - 1, p + 1))}
              className="rounded-xl border border-border/60 p-2 transition-colors hover:bg-foreground/5"
              aria-label="Next page"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      </SectionCard>

      <AnimatePresence>
        {selected ? (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setSelected(null)}
            className="fixed inset-0 z-50 flex items-center justify-center bg-foreground/20 p-4 backdrop-blur-sm"
          >
            <motion.div
              initial={{ opacity: 0, scale: 0.96 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.96 }}
              transition={spring}
              onClick={(e) => e.stopPropagation()}
              className="glass w-full max-w-md rounded-3xl p-6 shadow-[var(--elev-3)]"
            >
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">Booking</p>
                  <h2 className="num text-xl font-semibold">{selected.id}</h2>
                </div>
                <button onClick={() => setSelected(null)} className="rounded-xl p-2 hover:bg-foreground/6" aria-label="Close">
                  <X className="h-4 w-4" />
                </button>
              </div>
              <dl className="mt-6 space-y-3 text-sm">
                {[
                  ["Passenger", selected.passenger],
                  ["Flight", selected.flight],
                  ["Route", selected.route],
                  ["Cabin", selected.cabin],
                  ["Seat", selected.seat],
                  ["Travel date", selected.date],
                  ["Amount", `$${selected.amount.toLocaleString()}`],
                  ["Status", selected.status],
                ].map(([k, v]) => (
                  <div key={k} className="flex items-center justify-between">
                    <dt className="text-muted-foreground">{k}</dt>
                    <dd className="num font-medium">{v}</dd>
                  </div>
                ))}
              </dl>
            </motion.div>
          </motion.div>
        ) : null}
      </AnimatePresence>
    </motion.div>
  );
}
