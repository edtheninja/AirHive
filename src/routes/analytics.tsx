import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { pageVariants } from "@/lib/ams/motion";
import { PageHeader, SectionCard } from "@/components/ams/primitives";
import { useLiveOps } from "@/lib/ams/hooks";

export const Route = createFileRoute("/analytics")({
  head: () => ({
    meta: [
      { title: "Analytics — AirHive AMS" },
      {
        name: "description",
        content: "Operational flight and fleet analytics across the network.",
      },
      { property: "og:title", content: "Analytics — AirHive AMS" },
      {
        property: "og:description",
        content: "Operational flight and fleet analytics across the network.",
      },
    ],
  }),
  component: AnalyticsPage,
});

const STATUS_COLORS = [
  "hsl(var(--primary))",
  "hsl(var(--chart-2))",
  "hsl(var(--chart-3))",
  "hsl(var(--chart-4))",
  "hsl(var(--chart-5))",
  "hsl(var(--muted-foreground))",
  "hsl(var(--destructive))",
  "hsl(var(--accent-foreground))",
  "hsl(var(--secondary-foreground))",
];

function AnalyticsPage() {
  const { flights, aircraft, kpis, loading, error } = useLiveOps();

  const flightStatusCounts = flights.reduce<Record<string, number>>((counts, flight) => {
    counts[flight.status] = (counts[flight.status] ?? 0) + 1;
    return counts;
  }, {});

  const statusRows = [
    ["Scheduled", flightStatusCounts.Scheduled ?? 0],
    ["Boarding", flightStatusCounts.Boarding ?? 0],
    ["Taxiing", flightStatusCounts.Taxiing ?? 0],
    ["Departed", flightStatusCounts.Departed ?? 0],
    ["In Air", flightStatusCounts["In Air"] ?? 0],
    ["Landing", flightStatusCounts.Landing ?? 0],
    ["Landed", flightStatusCounts.Landed ?? 0],
    ["Delayed", flightStatusCounts.Delayed ?? 0],
    ["Cancelled", flightStatusCounts.Cancelled ?? 0],
  ] as const;

  const statusChartData = statusRows
    .filter(([, value]) => value > 0)
    .map(([name, value]) => ({
      name,
      value,
    }));

  const airportActivity = flights.reduce<Record<string, { departures: number; arrivals: number }>>(
    (activity, flight) => {
      if (!activity[flight.origin]) {
        activity[flight.origin] = {
          departures: 0,
          arrivals: 0,
        };
      }

      if (!activity[flight.destination]) {
        activity[flight.destination] = {
          departures: 0,
          arrivals: 0,
        };
      }

      activity[flight.origin].departures += 1;
      activity[flight.destination].arrivals += 1;

      return activity;
    },
    {},
  );

  const airportChartData = Object.entries(airportActivity)
    .map(([airport, activity]) => ({
      airport,
      departures: activity.departures,
      arrivals: activity.arrivals,
      total: activity.departures + activity.arrivals,
    }))
    .sort((a, b) => b.total - a.total)
    .slice(0, 10);

  const routeActivity = flights.reduce<Record<string, number>>((routes, flight) => {
    const route = `${flight.origin} → ${flight.destination}`;
    routes[route] = (routes[route] ?? 0) + 1;
    return routes;
  }, {});

  const routeChartData = Object.entries(routeActivity)
    .map(([route, count]) => ({
      route,
      count,
    }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10);
  const delayedFlights = flights.filter((flight) => flight.status === "Delayed");

  const delayedRouteActivity = delayedFlights.reduce<Record<string, number>>((routes, flight) => {
    const route = `${flight.origin} → ${flight.destination}`;
    routes[route] = (routes[route] ?? 0) + 1;

    return routes;
  }, {});

  const delayedRouteChartData = Object.entries(delayedRouteActivity)
    .map(([route, count]) => ({
      route,
      count,
    }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10);

  const delayedAirportActivity = delayedFlights.reduce<Record<string, number>>(
    (airports, flight) => {
      airports[flight.origin] = (airports[flight.origin] ?? 0) + 1;
      airports[flight.destination] = (airports[flight.destination] ?? 0) + 1;

      return airports;
    },
    {},
  );

  const delayedAirportChartData = Object.entries(delayedAirportActivity)
    .map(([airport, count]) => ({
      airport,
      count,
    }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10);

  const aircraftFlightCounts = flights.reduce<Record<string, number>>((counts, flight) => {
    counts[flight.aircraft] = (counts[flight.aircraft] ?? 0) + 1;

    return counts;
  }, {});

  const aircraftUtilizationData = aircraft
    .map((aircraftItem) => {
      const assignedFlights = aircraftFlightCounts[aircraftItem.registrationNumber] ?? 0;

      return {
        registration: aircraftItem.registrationNumber,
        aircraftType: aircraftItem.aircraftTypeCode,
        assignedFlights,
        status: aircraftItem.status,
      };
    })
    .sort((a, b) => b.assignedFlights - a.assignedFlights);

  const aircraftStatusCounts = aircraft.reduce<Record<string, number>>((counts, aircraftItem) => {
    const status = aircraftItem.status || "Unknown";
    counts[status] = (counts[status] ?? 0) + 1;

    return counts;
  }, {});

  const aircraftStatusChartData = Object.entries(aircraftStatusCounts).map(([name, value]) => ({
    name,
    value,
  }));

  const utilizedAircraftCount = aircraftUtilizationData.filter(
    (aircraftItem) => aircraftItem.assignedFlights > 0,
  ).length;

  const aircraftUtilizationShare =
    aircraft.length > 0 ? Math.round((utilizedAircraftCount / aircraft.length) * 100) : 0;

  const airborneShare =
    kpis.totalFlights > 0 ? Math.round((kpis.activeFlights / kpis.totalFlights) * 100) : 0;

  const delayedShare =
    kpis.totalFlights > 0 ? Math.round((kpis.delayedFlights / kpis.totalFlights) * 100) : 0;

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader
        title="Analytics"
        description="Operational performance across the AirHive network."
      />

      {loading ? (
        <SectionCard title="Operational analytics" subtitle="Loading live flight data">
          <p className="text-sm text-muted-foreground">Loading analytics…</p>
        </SectionCard>
      ) : error ? (
        <SectionCard title="Operational analytics" subtitle="Live data unavailable">
          <p className="text-sm text-destructive">{error}</p>
        </SectionCard>
      ) : (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <SectionCard title="Total flights" subtitle="Current network">
              <p className="num mt-4 text-3xl font-semibold">{kpis.totalFlights}</p>
            </SectionCard>

            <SectionCard title="Currently airborne" subtitle="Flights in air">
              <p className="num mt-4 text-3xl font-semibold">{kpis.activeFlights}</p>
            </SectionCard>

            <SectionCard title="Currently delayed" subtitle="Active delays">
              <p className="num mt-4 text-3xl font-semibold">{kpis.delayedFlights}</p>
            </SectionCard>

            <SectionCard title="Fleet availability" subtitle="Based on available fleet">
              <p className="num mt-4 text-3xl font-semibold">{kpis.fleetAvailability}%</p>
            </SectionCard>
          </div>

          <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
            <SectionCard
              title="Flight status distribution"
              subtitle="Current distribution across the network"
            >
              {statusChartData.length > 0 ? (
                <div className="h-[320px] w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={statusChartData}
                        dataKey="value"
                        nameKey="name"
                        cx="50%"
                        cy="50%"
                        innerRadius={72}
                        outerRadius={112}
                        paddingAngle={3}
                      >
                        {statusChartData.map((entry, index) => (
                          <Cell
                            key={entry.name}
                            fill={STATUS_COLORS[index % STATUS_COLORS.length]}
                          />
                        ))}
                      </Pie>
                      <Tooltip formatter={(value, name) => [value, name]} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">No flight status data available.</p>
              )}

              <div className="mt-4 grid grid-cols-2 gap-3 text-sm sm:grid-cols-3">
                {statusRows.map(([label, value]) => (
                  <div key={label} className="rounded-lg border border-border/60 px-3 py-2">
                    <p className="text-muted-foreground">{label}</p>
                    <p className="num mt-1 text-lg font-semibold">{value}</p>
                  </div>
                ))}
              </div>
            </SectionCard>

            <SectionCard
              title="Airport activity"
              subtitle="Top airports by departures and arrivals"
            >
              {airportChartData.length > 0 ? (
                <div className="h-[360px] w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                      data={airportChartData}
                      layout="vertical"
                      margin={{
                        top: 8,
                        right: 12,
                        left: 8,
                        bottom: 8,
                      }}
                    >
                      <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.25} />
                      <XAxis type="number" allowDecimals={false} />
                      <YAxis type="category" dataKey="airport" width={48} />
                      <Tooltip />
                      <Bar
                        dataKey="departures"
                        name="Departures"
                        fill="hsl(var(--primary))"
                        radius={[0, 4, 4, 0]}
                      />
                      <Bar
                        dataKey="arrivals"
                        name="Arrivals"
                        fill="hsl(var(--chart-2))"
                        radius={[0, 4, 4, 0]}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">No airport activity data available.</p>
              )}
            </SectionCard>
          </div>

          <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
            <SectionCard
              title="Route activity"
              subtitle="Most active routes in the current network"
            >
              {routeChartData.length > 0 ? (
                <div className="h-[360px] w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                      data={routeChartData}
                      layout="vertical"
                      margin={{
                        top: 8,
                        right: 12,
                        left: 8,
                        bottom: 8,
                      }}
                    >
                      <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.25} />
                      <XAxis type="number" allowDecimals={false} />
                      <YAxis type="category" dataKey="route" width={112} />
                      <Tooltip />
                      <Bar
                        dataKey="count"
                        name="Flights"
                        fill="hsl(var(--chart-3))"
                        radius={[0, 4, 4, 0]}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">No route activity data available.</p>
              )}
            </SectionCard>

            <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
              <SectionCard
                title="Delayed flights by route"
                subtitle="Routes with the highest number of delayed flights"
              >
                {delayedRouteChartData.length > 0 ? (
                  <div className="h-[360px] w-full">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart
                        data={delayedRouteChartData}
                        layout="vertical"
                        margin={{
                          top: 8,
                          right: 12,
                          left: 8,
                          bottom: 8,
                        }}
                      >
                        <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.25} />
                        <XAxis type="number" allowDecimals={false} />
                        <YAxis type="category" dataKey="route" width={112} />
                        <Tooltip />
                        <Bar
                          dataKey="count"
                          name="Delayed flights"
                          fill="hsl(var(--destructive))"
                          radius={[0, 4, 4, 0]}
                        />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground">No delayed flights by route.</p>
                )}
              </SectionCard>

              <SectionCard
                title="Delayed flights by airport"
                subtitle="Airport involvement in delayed flights"
              >
                {delayedAirportChartData.length > 0 ? (
                  <div className="h-[360px] w-full">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart
                        data={delayedAirportChartData}
                        layout="vertical"
                        margin={{
                          top: 8,
                          right: 12,
                          left: 8,
                          bottom: 8,
                        }}
                      >
                        <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.25} />
                        <XAxis type="number" allowDecimals={false} />
                        <YAxis type="category" dataKey="airport" width={48} />
                        <Tooltip />
                        <Bar
                          dataKey="count"
                          name="Delayed flights"
                          fill="hsl(var(--chart-4))"
                          radius={[0, 4, 4, 0]}
                        />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground">No delayed flights by airport.</p>
                )}
              </SectionCard>
            </div>

            <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
              <SectionCard
                title="Aircraft utilization"
                subtitle="Flights assigned to each aircraft"
              >
                {aircraftUtilizationData.length > 0 ? (
                  <div className="h-[360px] w-full">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart
                        data={aircraftUtilizationData}
                        layout="vertical"
                        margin={{
                          top: 8,
                          right: 12,
                          left: 8,
                          bottom: 8,
                        }}
                      >
                        <CartesianGrid strokeDasharray="3 3" horizontal={false} opacity={0.25} />
                        <XAxis type="number" allowDecimals={false} />
                        <YAxis type="category" dataKey="registration" width={76} />
                        <Tooltip />
                        <Bar
                          dataKey="assignedFlights"
                          name="Assigned flights"
                          fill="hsl(var(--chart-5))"
                          radius={[0, 4, 4, 0]}
                        />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground">
                    No aircraft utilization data available.
                  </p>
                )}
              </SectionCard>

              <SectionCard title="Aircraft status distribution" subtitle="Current fleet status">
                {aircraftStatusChartData.length > 0 ? (
                  <div className="h-[320px] w-full">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={aircraftStatusChartData}
                          dataKey="value"
                          nameKey="name"
                          cx="50%"
                          cy="50%"
                          innerRadius={72}
                          outerRadius={112}
                          paddingAngle={3}
                        >
                          {aircraftStatusChartData.map((entry, index) => (
                            <Cell
                              key={entry.name}
                              fill={STATUS_COLORS[index % STATUS_COLORS.length]}
                            />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground">
                    No aircraft status data available.
                  </p>
                )}
              </SectionCard>
            </div>
            <SectionCard title="Operational snapshot" subtitle="Live network indicators">
              <dl className="space-y-4 text-sm">
                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Aircraft utilization</dt>
                  <dd className="num font-medium">{aircraftUtilizationShare}%</dd>
                </div>

                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Aircraft tracked</dt>
                  <dd className="num font-medium">{aircraft.length}</dd>
                </div>
                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Delayed flights tracked</dt>
                  <dd className="num font-medium">{delayedFlights.length}</dd>
                </div>
                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Flights tracked</dt>
                  <dd className="num font-medium">{flights.length}</dd>
                </div>

                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Airborne share</dt>
                  <dd className="num font-medium">{airborneShare}%</dd>
                </div>

                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Delayed share</dt>
                  <dd className="num font-medium">{delayedShare}%</dd>
                </div>

                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Fleet availability</dt>
                  <dd className="num font-medium">{kpis.fleetAvailability}%</dd>
                </div>
              </dl>
            </SectionCard>
          </div>
        </>
      )}
    </motion.div>
  );
}
