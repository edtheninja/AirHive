import { createFileRoute } from "@tanstack/react-router";
import { AnimatePresence, motion } from "motion/react";
import { useEffect, useMemo, useState } from "react";
import { ChevronLeft, ChevronRight, Download, Search, X } from "lucide-react";
import { toast } from "sonner";
import { pageVariants, spring } from "@/lib/ams/motion";
import { getBookings, type Booking } from "@/lib/api/bookings";
import { PageHeader, SectionCard } from "@/components/ams/primitives";
import { useAuth } from "@/lib/auth/auth-context";

function PassengerBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    async function loadBookings() {
      setLoading(true);
      setError(false);

      try {
        const data = await getBookings();

        if (mounted) {
          setBookings(data);
        }
      } catch {
        if (mounted) {
          setError(true);
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    void loadBookings();

    return () => {
      mounted = false;
    };
  }, [retryKey]);

  const upcoming = useMemo(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    return bookings
      .filter((booking) => {
        if (booking.status === "Cancelled") return false;

        const date = new Date(booking.travelDate);
        if (Number.isNaN(date.getTime())) return false;

        date.setHours(0, 0, 0, 0);
        return date >= today;
      })
      .sort((a, b) => new Date(a.travelDate).getTime() - new Date(b.travelDate).getTime());
  }, [bookings]);

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1200px] space-y-6 py-6"
    >
      <PageHeader
        title="My bookings"
        description="View your reservations, travel dates, seats, and booking status."
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        {[
          { label: "Total bookings", value: bookings.length },
          {
            label: "Upcoming trips",
            value: upcoming.length,
          },
          {
            label: "Confirmed",
            value: bookings.filter((b) => b.status === "Confirmed").length,
          },
        ].map((stat) => (
          <div key={stat.label} className="glass rounded-3xl p-5">
            <p className="text-sm text-muted-foreground">{stat.label}</p>
            <p className="num mt-3 text-3xl font-semibold">{loading ? "—" : stat.value}</p>
          </div>
        ))}
      </div>

      <SectionCard title="Your reservations" subtitle="Your personal booking history">
        {loading ? (
          <p className="py-10 text-center text-sm text-muted-foreground">
            Loading your bookings...
          </p>
        ) : error ? (
          <div className="py-10 text-center">
            <p className="text-sm text-muted-foreground">
              We couldn't load your bookings. Please try again.
            </p>
            <button
              type="button"
              onClick={() => setRetryKey((key) => key + 1)}
              className="mt-4 rounded-xl border border-border px-4 py-2 text-sm hover:bg-foreground/5"
            >
              Retry
            </button>
          </div>
        ) : bookings.length === 0 ? (
          <div className="py-10 text-center">
            <p className="font-medium">No bookings yet</p>
            <p className="mt-2 text-sm text-muted-foreground">
              Your reservations will appear here when you have booked a trip.
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {bookings
              .slice()
              .sort((a, b) => new Date(b.travelDate).getTime() - new Date(a.travelDate).getTime())
              .map((booking) => (
                <article
                  key={booking.id}
                  className="rounded-2xl border border-border/60 p-4 sm:p-5"
                >
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <p className="text-xs text-muted-foreground">Booking reference</p>
                      <p className="num mt-1 font-semibold">{booking.pnr}</p>
                    </div>
                    <span
                      className={`rounded-full px-2.5 py-1 text-xs font-medium ${statusTone[booking.status]}`}
                    >
                      {booking.status}
                    </span>
                  </div>

                  <div className="mt-4">
                    <h3 className="text-lg font-semibold">{booking.route}</h3>
                    <p className="mt-1 text-sm text-muted-foreground">Flight {booking.flight}</p>
                  </div>

                  <div className="mt-4 grid grid-cols-2 gap-4 border-t border-border/60 pt-4 sm:grid-cols-4">
                    <div>
                      <p className="text-xs text-muted-foreground">Travel date</p>
                      <p className="mt-1 text-sm font-medium">
                        {booking.travelDate || "Not available"}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Cabin</p>
                      <p className="mt-1 text-sm font-medium">{booking.cabin}</p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Seat</p>
                      <p className="mt-1 text-sm font-medium">{booking.seat || "Not assigned"}</p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Amount</p>
                      <p className="num mt-1 text-sm font-medium">
                        ${booking.amount.toLocaleString()}
                      </p>
                    </div>
                  </div>
                </article>
              ))}
          </div>
        )}
      </SectionCard>
    </motion.div>
  );
}

export const Route = createFileRoute("/bookings")({
  head: () => ({
    meta: [
      { title: "Bookings — AirHive AMS" },
      {
        name: "description",
        content: "Search, filter, export and inspect every booking across the network.",
      },
      { property: "og:title", content: "Bookings — AirHive AMS" },
      {
        property: "og:description",
        content: "Search, filter and inspect every booking across the network.",
      },
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
  const { session } = useAuth();

  return session?.role === "PASSENGER" ? <PassengerBookingsPage /> : <StaffBookingsPage />;
}

function StaffBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState("");
  const [cabin, setCabin] = useState<(typeof CABINS)[number]>("All");
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<Booking | null>(null);

  // Keep the existing staff useEffect, filtering logic,
  // table, pagination, and booking-details modal below.
  useEffect(() => {
    let mounted = true;

    async function loadBookings() {
      try {
        const data = await getBookings();

        if (mounted) {
          setBookings(data);
        }
      } catch {
        if (mounted) {
          toast.error("Unable to load bookings", {
            description: "Please check the backend connection and try again.",
          });
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    loadBookings();

    return () => {
      mounted = false;
    };
  }, []);

  const filtered = useMemo(
    () =>
      bookings.filter(
        (b) =>
          (cabin === "All" || b.cabin === cabin) &&
          (query === "" ||
            `${b.passenger}${b.pnr}${b.flight}${b.route}`
              .toLowerCase()
              .includes(query.toLowerCase())),
      ),
    [bookings, query, cabin],
  );

  const pages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const current = Math.min(page, pages - 1);
  const rows = filtered.slice(current * PAGE_SIZE, current * PAGE_SIZE + PAGE_SIZE);

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader
        title="Bookings"
        description="Reservation management with search, filters and export."
        action={
          <motion.button
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            onClick={() =>
              toast("Export unavailable", {
                description: "Booking export will be available in a future update.",
              })
            }
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
                  cabin === c
                    ? "bg-primary text-primary-foreground"
                    : "bg-foreground/5 text-muted-foreground hover:text-foreground"
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
                {[
                  "PNR",
                  "Passenger",
                  "Flight",
                  "Route",
                  "Cabin",
                  "Seat",
                  "Amount",
                  "Status",
                  "",
                ].map((h) => (
                  <th key={h} className="px-3 pb-2 font-medium">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>

            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={9} className="px-3 py-10 text-center text-sm text-muted-foreground">
                    Loading bookings...
                  </td>
                </tr>
              ) : rows.length === 0 ? (
                <tr>
                  <td colSpan={9} className="px-3 py-10 text-center text-sm text-muted-foreground">
                    No bookings found.
                  </td>
                </tr>
              ) : (
                <AnimatePresence initial={false} mode="popLayout">
                  {rows.map((b, i) => (
                    <motion.tr
                      key={b.id}
                      layout
                      initial={{ opacity: 0, y: 8 }}
                      animate={{
                        opacity: 1,
                        y: 0,
                        transition: { ...spring, delay: i * 0.03 },
                      }}
                      exit={{ opacity: 0 }}
                      className="hover:bg-foreground/4"
                    >
                      <td className="num rounded-l-2xl px-3 py-3 font-medium">{b.pnr}</td>
                      <td className="px-3 py-3">{b.passenger}</td>
                      <td className="num px-3 py-3">{b.flight}</td>
                      <td className="num px-3 py-3 text-muted-foreground">{b.route}</td>
                      <td className="px-3 py-3 text-muted-foreground">{b.cabin}</td>
                      <td className="num px-3 py-3">{b.seat}</td>
                      <td className="num px-3 py-3">${b.amount.toLocaleString()}</td>
                      <td className="px-3 py-3">
                        <span
                          className={`rounded-full px-2.5 py-1 text-xs ${statusTone[b.status]}`}
                        >
                          {b.status}
                        </span>
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
              )}
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
              disabled={current === 0}
              className="rounded-xl border border-border/60 p-2 transition-colors hover:bg-foreground/5 disabled:cursor-not-allowed disabled:opacity-40"
              aria-label="Previous page"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>

            <button
              onClick={() => setPage((p) => Math.min(pages - 1, p + 1))}
              disabled={current === pages - 1}
              className="rounded-xl border border-border/60 p-2 transition-colors hover:bg-foreground/5 disabled:cursor-not-allowed disabled:opacity-40"
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
                  <h2 className="num text-xl font-semibold">{selected.pnr}</h2>
                </div>

                <button
                  onClick={() => setSelected(null)}
                  className="rounded-xl p-2 hover:bg-foreground/6"
                  aria-label="Close"
                >
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
                  ["Travel date", selected.travelDate],
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
