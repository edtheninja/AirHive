import { useEffect, useMemo, useState } from "react";
import { Link } from "@tanstack/react-router";
import { motion } from "motion/react";
import {
  ArrowRight,
  CalendarDays,
  CheckCircle2,
  Clock3,
  PlaneTakeoff,
  RefreshCw,
  Ticket,
} from "lucide-react";
import { PageHeader, SectionCard } from "@/components/ams/primitives";
import { pageVariants, spring } from "@/lib/ams/motion";
import { createBooking, getBookings, type Booking } from "@/lib/api/bookings";
import { getFlights, type Flight } from "@/lib/api/flights";
import { useAuth } from "@/lib/auth/auth-context";

const statusTone: Record<Booking["status"], string> = {
  Confirmed: "bg-info/12 text-info",
  "Checked In": "bg-success/14 text-success",
  Pending: "bg-warning/16 text-warning",
  Cancelled: "bg-destructive/14 text-destructive",
};

function formatDate(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value || "Date unavailable";
  }

  return date.toLocaleDateString(undefined, {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

function formatDateTime(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value || "Schedule unavailable";
  }

  return date.toLocaleString(undefined, {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
}

function isUpcoming(booking: Booking) {
  if (booking.status === "Cancelled") return false;

  const date = new Date(booking.travelDate);
  if (Number.isNaN(date.getTime())) return false;

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  date.setHours(0, 0, 0, 0);

  return date >= today;
}

function isBookableFlight(flight: Flight) {
  if (flight.status.toUpperCase() !== "SCHEDULED") return false;

  const departure = new Date(flight.scheduledDeparture);
  return !Number.isNaN(departure.getTime()) && departure > new Date();
}

export function PassengerDashboard() {
  const { session } = useAuth();

  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  const [flights, setFlights] = useState<Flight[]>([]);
  const [flightsLoading, setFlightsLoading] = useState(true);
  const [flightsError, setFlightsError] = useState<string | null>(null);
  const [flightReloadKey, setFlightReloadKey] = useState(0);

  const [passengerName, setPassengerName] = useState(session?.username ?? "");
  const [selectedFlightId, setSelectedFlightId] = useState<number | null>(null);
  const [bookingFlightId, setBookingFlightId] = useState<number | null>(null);
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [bookingConfirmation, setBookingConfirmation] = useState<Booking | null>(null);

  useEffect(() => {
    let mounted = true;

    async function load() {
      setLoading(true);
      setError(null);

      try {
        const result = await getBookings();
        if (mounted) setBookings(result);
      } catch {
        if (mounted) {
          setError("We couldn't load your bookings. Check your connection and try again.");
        }
      } finally {
        if (mounted) setLoading(false);
      }
    }

    void load();

    return () => {
      mounted = false;
    };
  }, [reloadKey]);

  useEffect(() => {
    let mounted = true;

    async function load() {
      setFlightsLoading(true);
      setFlightsError(null);

      try {
        const result = await getFlights();
        if (mounted) setFlights(result);
      } catch {
        if (mounted) {
          setFlightsError("We couldn't load available flights. Please try again.");
        }
      } finally {
        if (mounted) setFlightsLoading(false);
      }
    }

    void load();

    return () => {
      mounted = false;
    };
  }, [flightReloadKey]);

  const availableFlights = useMemo(
    () =>
      flights
        .filter(isBookableFlight)
        .sort(
          (a, b) =>
            new Date(a.scheduledDeparture).getTime() - new Date(b.scheduledDeparture).getTime(),
        ),
    [flights],
  );

  const selectedFlight = availableFlights.find((flight) => flight.id === selectedFlightId);

  const upcomingBookings = useMemo(
    () =>
      bookings
        .filter(isUpcoming)
        .sort((a, b) => new Date(a.travelDate).getTime() - new Date(b.travelDate).getTime()),
    [bookings],
  );

  const nextBooking = upcomingBookings[0];
  const confirmedCount = bookings.filter((booking) => booking.status === "Confirmed").length;

  const stats = [
    {
      label: "Total bookings",
      value: bookings.length,
      detail: "All your reservations",
      icon: Ticket,
      tone: "text-primary",
    },
    {
      label: "Upcoming trips",
      value: upcomingBookings.length,
      detail: "Travel still ahead",
      icon: PlaneTakeoff,
      tone: "text-info",
    },
    {
      label: "Confirmed",
      value: confirmedCount,
      detail: "Confirmed reservations",
      icon: CheckCircle2,
      tone: "text-success",
    },
  ];

  async function handleBooking() {
    if (!selectedFlight || !passengerName.trim()) {
      setBookingError("Select a flight and enter the passenger name.");
      return;
    }

    setBookingFlightId(selectedFlight.id);
    setBookingError(null);
    setBookingConfirmation(null);

    try {
      const result = await createBooking({
        flightId: selectedFlight.id,
        passenger: passengerName.trim(),
        cabin: "ECONOMY",
      });

      setBookings((current) => [result, ...current.filter((booking) => booking.id !== result.id)]);

      setBookingConfirmation(result);
      setSelectedFlightId(null);
    } catch (err) {
      setBookingError(
        err instanceof Error ? err.message : "We couldn't complete your booking. Please try again.",
      );
    } finally {
      setBookingFlightId(null);
    }
  }

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1400px] space-y-6 py-6"
    >
      <PageHeader
        title={`Welcome${session?.username ? `, ${session.username}` : ""}`}
        description="Your travel at a glance. Manage your reservations and get ready for your next trip."
        action={
          <Link
            to="/bookings"
            className="glass inline-flex items-center gap-2 rounded-2xl px-4 py-2.5 text-sm font-medium transition-colors hover:bg-foreground/5"
          >
            My bookings <ArrowRight className="h-4 w-4" />
          </Link>
        }
      />

      {error ? (
        <section
          role="alert"
          className="rounded-3xl border border-destructive/30 bg-destructive/5 p-5"
        >
          <h2 className="font-semibold">Bookings unavailable</h2>
          <p className="mt-1 text-sm text-muted-foreground">{error}</p>
          <button
            type="button"
            onClick={() => setReloadKey((key) => key + 1)}
            className="mt-4 inline-flex items-center gap-2 rounded-xl border border-border px-3 py-2 text-sm font-medium hover:bg-foreground/5"
          >
            <RefreshCw className="h-4 w-4" />
            Try again
          </button>
        </section>
      ) : null}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {stats.map((stat, index) => (
          <motion.div
            key={stat.label}
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ ...spring, delay: index * 0.05 }}
            className="glass rounded-3xl p-5"
          >
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="text-sm text-muted-foreground">{stat.label}</p>
                <p className="num mt-3 text-3xl font-semibold">{loading ? "—" : stat.value}</p>
                <p className="mt-1 text-xs text-muted-foreground">{stat.detail}</p>
              </div>
              <div className={`rounded-2xl bg-foreground/5 p-3 ${stat.tone}`}>
                <stat.icon className="h-5 w-5" strokeWidth={1.7} />
              </div>
            </div>
          </motion.div>
        ))}
      </div>

      <SectionCard
        title="Book a flight"
        subtitle="Choose from upcoming scheduled flights"
        action={<PlaneTakeoff className="h-5 w-5 text-muted-foreground" />}
      >
        <div className="space-y-5">
          {bookingConfirmation ? (
            <div role="status" className="rounded-2xl border border-success/30 bg-success/5 p-4">
              <div className="flex items-start gap-3">
                <CheckCircle2 className="mt-0.5 h-5 w-5 shrink-0 text-success" />
                <div>
                  <p className="font-semibold">Booking confirmed!</p>
                  <p className="mt-1 text-sm text-muted-foreground">
                    Your reservation for {bookingConfirmation.route} on flight{" "}
                    {bookingConfirmation.flight} has been created.
                  </p>
                  <p className="mt-2 text-sm">
                    PNR: <span className="num font-semibold">{bookingConfirmation.pnr}</span>
                  </p>
                  <p className="mt-1 text-sm text-muted-foreground">
                    Travel date: {formatDate(bookingConfirmation.travelDate)}
                  </p>
                  <Link
                    to="/bookings"
                    className="mt-3 inline-flex items-center gap-2 text-sm font-medium text-primary hover:underline"
                  >
                    View my bookings <ArrowRight className="h-4 w-4" />
                  </Link>
                </div>
              </div>
            </div>
          ) : null}

          {flightsError ? (
            <div
              role="alert"
              className="rounded-2xl border border-destructive/30 bg-destructive/5 p-4"
            >
              <p className="text-sm text-muted-foreground">{flightsError}</p>
              <button
                type="button"
                onClick={() => setFlightReloadKey((key) => key + 1)}
                className="mt-3 inline-flex items-center gap-2 rounded-xl border border-border px-3 py-2 text-sm font-medium hover:bg-foreground/5"
              >
                <RefreshCw className="h-4 w-4" />
                Retry
              </button>
            </div>
          ) : flightsLoading ? (
            <div className="space-y-3 py-3" aria-label="Loading flights">
              {[0, 1, 2].map((item) => (
                <div key={item} className="h-20 animate-pulse rounded-2xl bg-foreground/5" />
              ))}
            </div>
          ) : availableFlights.length === 0 ? (
            <div className="rounded-2xl border border-border/60 p-6 text-center">
              <PlaneTakeoff className="mx-auto h-8 w-8 text-muted-foreground" />
              <p className="mt-3 font-medium">No upcoming scheduled flights</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Please check back later for available flights.
              </p>
            </div>
          ) : (
            <>
              <div className="space-y-3">
                {availableFlights.map((flight) => {
                  const selected = selectedFlightId === flight.id;

                  return (
                    <button
                      key={flight.id}
                      type="button"
                      onClick={() => {
                        setSelectedFlightId(flight.id);
                        setBookingError(null);
                        setBookingConfirmation(null);
                      }}
                      className={`w-full rounded-2xl border p-4 text-left transition-colors ${
                        selected
                          ? "border-primary bg-primary/5"
                          : "border-border/60 hover:bg-foreground/[0.03]"
                      }`}
                      aria-pressed={selected}
                    >
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div>
                          <p className="font-semibold">
                            {flight.departureAirportCode} → {flight.arrivalAirportCode}
                          </p>
                          <p className="mt-1 text-sm text-muted-foreground">
                            Flight {flight.flightNumber}
                          </p>
                        </div>
                        <span className="rounded-full bg-success/12 px-2.5 py-1 text-xs font-medium text-success">
                          Scheduled
                        </span>
                      </div>
                      <div className="mt-3 flex items-center gap-2 text-sm text-muted-foreground">
                        <CalendarDays className="h-4 w-4" />
                        {formatDateTime(flight.scheduledDeparture)}
                      </div>
                    </button>
                  );
                })}
              </div>

              <div className="space-y-4 border-t border-border/60 pt-5">
                <div>
                  <label htmlFor="passenger-name" className="mb-2 block text-sm font-medium">
                    Passenger name
                  </label>
                  <input
                    id="passenger-name"
                    type="text"
                    value={passengerName}
                    onChange={(event) => setPassengerName(event.target.value)}
                    placeholder="Enter passenger's full name"
                    maxLength={120}
                    required
                    className="w-full rounded-xl border border-border bg-background px-3 py-2.5 text-sm outline-none transition-colors focus:border-primary"
                  />
                </div>

                {selectedFlight ? (
                  <div className="rounded-xl bg-foreground/[0.03] p-3 text-sm">
                    <p className="font-medium">Selected: {selectedFlight.flightNumber}</p>
                    <p className="mt-1 text-muted-foreground">
                      {selectedFlight.departureAirportCode} → {selectedFlight.arrivalAirportCode} ·
                      Economy
                    </p>
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground">
                    Select a flight above to continue with your booking.
                  </p>
                )}

                {bookingError ? (
                  <p role="alert" className="text-sm text-destructive">
                    {bookingError}
                  </p>
                ) : null}

                <button
                  type="button"
                  onClick={() => void handleBooking()}
                  disabled={!selectedFlight || !passengerName.trim() || bookingFlightId !== null}
                  className="inline-flex items-center justify-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-sm font-medium text-primary-foreground transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {bookingFlightId !== null ? (
                    <>
                      <RefreshCw className="h-4 w-4 animate-spin" />
                      Booking flight…
                    </>
                  ) : (
                    <>
                      <Ticket className="h-4 w-4" />
                      Confirm Economy booking
                    </>
                  )}
                </button>
              </div>
            </>
          )}
        </div>
      </SectionCard>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1.4fr)_minmax(300px,0.8fr)]">
        <SectionCard
          title="Your next trip"
          subtitle="Your nearest upcoming reservation"
          action={<CalendarDays className="h-5 w-5 text-muted-foreground" />}
        >
          {loading ? (
            <div className="space-y-3 py-4" aria-label="Loading next trip">
              <div className="h-5 w-1/3 animate-pulse rounded-lg bg-foreground/8" />
              <div className="h-8 w-2/3 animate-pulse rounded-lg bg-foreground/8" />
              <div className="h-4 w-1/2 animate-pulse rounded-lg bg-foreground/8" />
            </div>
          ) : error ? (
            <p className="py-4 text-sm text-muted-foreground">
              Your next trip will appear when bookings are available.
            </p>
          ) : nextBooking ? (
            <div className="rounded-2xl border border-border/60 bg-foreground/[0.02] p-5">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="text-xs text-muted-foreground">Booking reference</p>
                  <p className="num mt-1 text-lg font-semibold">{nextBooking.pnr}</p>
                </div>
                <span
                  className={`rounded-full px-2.5 py-1 text-xs font-medium ${statusTone[nextBooking.status]}`}
                >
                  {nextBooking.status}
                </span>
              </div>

              <div className="mt-5">
                <p className="text-xl font-semibold">{nextBooking.route}</p>
                <p className="mt-1 text-sm text-muted-foreground">Flight {nextBooking.flight}</p>
              </div>

              <div className="mt-5 flex flex-wrap gap-x-6 gap-y-3 border-t border-border/60 pt-4 text-sm">
                <div>
                  <p className="text-xs text-muted-foreground">Travel date</p>
                  <p className="mt-1 font-medium">{formatDate(nextBooking.travelDate)}</p>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">Cabin</p>
                  <p className="mt-1 font-medium">{nextBooking.cabin}</p>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">Seat</p>
                  <p className="mt-1 font-medium">{nextBooking.seat || "Not assigned"}</p>
                </div>
              </div>

              <Link
                to="/bookings"
                className="mt-5 inline-flex items-center gap-2 text-sm font-medium text-primary hover:underline"
              >
                View booking details <ArrowRight className="h-4 w-4" />
              </Link>
            </div>
          ) : (
            <div className="py-8 text-center">
              <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-foreground/5 text-muted-foreground">
                <PlaneTakeoff className="h-6 w-6" />
              </div>
              <h3 className="mt-4 font-semibold">No upcoming trips</h3>
              <p className="mt-1 text-sm text-muted-foreground">
                When you have an upcoming reservation, it will appear here.
              </p>
              <Link
                to="/bookings"
                className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-primary hover:underline"
              >
                View my bookings <ArrowRight className="h-4 w-4" />
              </Link>
            </div>
          )}
        </SectionCard>

        <SectionCard
          title="Recent bookings"
          subtitle="Your latest reservations"
          action={
            <Link to="/bookings" className="text-sm font-medium text-primary hover:underline">
              View all
            </Link>
          }
        >
          {loading ? (
            <div className="space-y-4 py-2" aria-label="Loading bookings">
              {[0, 1, 2].map((item) => (
                <div key={item} className="flex items-center gap-3">
                  <div className="h-10 w-10 animate-pulse rounded-xl bg-foreground/8" />
                  <div className="flex-1 space-y-2">
                    <div className="h-4 w-2/3 animate-pulse rounded bg-foreground/8" />
                    <div className="h-3 w-1/2 animate-pulse rounded bg-foreground/8" />
                  </div>
                </div>
              ))}
            </div>
          ) : error ? (
            <p className="py-4 text-sm text-muted-foreground">
              Recent bookings couldn't be loaded.
            </p>
          ) : bookings.length === 0 ? (
            <div className="py-8 text-center">
              <Ticket className="mx-auto h-8 w-8 text-muted-foreground" />
              <p className="mt-3 text-sm font-medium">No bookings yet</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Your reservations will show up here.
              </p>
            </div>
          ) : (
            <div className="space-y-1">
              {[...bookings]
                .sort((a, b) => new Date(b.travelDate).getTime() - new Date(a.travelDate).getTime())
                .slice(0, 5)
                .map((booking) => (
                  <div
                    key={booking.id}
                    className="flex items-center gap-3 rounded-2xl p-3 transition-colors hover:bg-foreground/4"
                  >
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-foreground/5">
                      <Ticket className="h-4 w-4 text-muted-foreground" />
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium">{booking.route}</p>
                      <p className="mt-1 text-xs text-muted-foreground">
                        {booking.pnr} · {formatDate(booking.travelDate)}
                      </p>
                    </div>
                    <span
                      className={`shrink-0 rounded-full px-2 py-1 text-[11px] font-medium ${statusTone[booking.status]}`}
                    >
                      {booking.status}
                    </span>
                  </div>
                ))}
            </div>
          )}
        </SectionCard>
      </div>

      <div className="flex items-start gap-3 rounded-2xl border border-border/60 p-4 text-sm text-muted-foreground">
        <Clock3 className="mt-0.5 h-4 w-4 shrink-0" />
        <p>
          Booking details reflect the information currently available from AirHive. Contact the
          airline for assistance with changes to your reservation.
        </p>
      </div>
    </motion.div>
  );
}
