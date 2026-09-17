import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Line,
  LineChart,
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

const CHART_TEXT_COLOR = "hsl(var(--foreground))";
const CHART_MUTED_COLOR = "hsl(var(--muted-foreground))";
const CHART_GRID_COLOR = "hsl(var(--border))";
const CHART_TOOLTIP_BACKGROUND = "hsl(var(--popover))";
const CHART_TOOLTIP_BORDER = "hsl(var(--border))";

const CHART_AXIS_STYLE = {
  fill: CHART_TEXT_COLOR,
  fontSize: 12,
};

const CHART_AXIS_LINE_STYLE = {
  stroke: CHART_MUTED_COLOR,
};

const CHART_TOOLTIP_STYLE = {
  backgroundColor: CHART_TOOLTIP_BACKGROUND,
  border: `1px solid ${CHART_TOOLTIP_BORDER}`,
  borderRadius: "0.75rem",
  color: CHART_TEXT_COLOR,
};

const CHART_TOOLTIP_LABEL_STYLE = {
  color: CHART_TEXT_COLOR,
  fontWeight: 600,
};

const CHART_TOOLTIP_ITEM_STYLE = {
  color: CHART_TEXT_COLOR,
};

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

  const historicalFlightActivity = flights.reduce<
    Record<
      string,
      {
        total: number;
        delayed: number;
        cancelled: number;
      }
    >
  >((activity, flight) => {
    const date = new Date(flight.scheduledDeparture).toLocaleDateString("en-CA");

    if (!activity[date]) {
      activity[date] = {
        total: 0,
        delayed: 0,
        cancelled: 0,
      };
    }

    activity[date].total += 1;

    if (flight.status === "Delayed") {
      activity[date].delayed += 1;
    }

    if (flight.status === "Cancelled") {
      activity[date].cancelled += 1;
    }

    return activity;
  }, {});

  const historicalFlightChartData = Object.entries(historicalFlightActivity)
    .map(([date, activity]) => ({
      date,
      total: activity.total,
      delayed: activity.delayed,
      cancelled: activity.cancelled,
    }))
    .sort((a, b) => a.date.localeCompare(b.date));

  const exportAnalyticsReport = () => {
    const rows: string[][] = [
      ["AIRHIVE ANALYTICS REPORT"],
      [`Generated at`, new Date().toISOString()],
      [],
      ["FLIGHT STATUS SUMMARY"],
      ["Status", "Count"],
      ...statusRows.map(([status, count]) => [status, String(count)]),
      [],
      ["AIRPORT ACTIVITY"],
      ["Airport", "Departures", "Arrivals", "Total"],
      ...airportChartData.map((airport) => [
        airport.airport,
        String(airport.departures),
        String(airport.arrivals),
        String(airport.total),
      ]),
      [],
      ["ROUTE ACTIVITY"],
      ["Route", "Flights"],
      ...routeChartData.map((route) => [route.route, String(route.count)]),
      [],
      ["DELAYED FLIGHTS BY ROUTE"],
      ["Route", "Delayed Flights"],
      ...delayedRouteChartData.map((route) => [route.route, String(route.count)]),
      [],
      ["DELAYED FLIGHTS BY AIRPORT"],
      ["Airport", "Delayed Flights"],
      ...delayedAirportChartData.map((airport) => [airport.airport, String(airport.count)]),
      [],
      ["AIRCRAFT UTILIZATION"],
      ["Registration", "Aircraft Type", "Assigned Flights", "Status"],
      ...aircraftUtilizationData.map((aircraftItem) => [
        aircraftItem.registration,
        aircraftItem.aircraftType,
        String(aircraftItem.assignedFlights),
        aircraftItem.status,
      ]),
      [],
      ["AIRCRAFT STATUS DISTRIBUTION"],
      ["Status", "Count"],
      ...aircraftStatusChartData.map((status) => [status.name, String(status.value)]),
      [],
      ["HISTORICAL FLIGHT ACTIVITY"],
      ["Date", "Total Flights", "Delayed Flights", "Cancelled Flights"],
      ...historicalFlightChartData.map((activity) => [
        activity.date,
        String(activity.total),
        String(activity.delayed),
        String(activity.cancelled),
      ]),
      [],
      ["OPERATIONAL SNAPSHOT"],
      ["Metric", "Value"],
      ["Aircraft utilization", `${aircraftUtilizationShare}%`],
      ["Aircraft tracked", String(aircraft.length)],
      ["Delayed flights tracked", String(delayedFlights.length)],
      ["Flights tracked", String(flights.length)],
      ["Airborne share", `${airborneShare}%`],
      ["Delayed share", `${delayedShare}%`],
      ["Fleet availability", `${kpis.fleetAvailability}%`],
    ];

    const csv = rows
      .map((row) => row.map((value) => `"${value.replace(/"/g, '""')}"`).join(","))
      .join("\n");

    const blob = new Blob([`\uFEFF${csv}`], {
      type: "text/csv;charset=utf-8;",
    });

    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");

    link.href = url;
    link.download = `airhive-analytics-${new Date().toISOString().slice(0, 10)}.csv`;

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

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
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <PageHeader
          title="Analytics"
          description="Operational performance across the AirHive network."
        />

        <button
          type="button"
          onClick={exportAnalyticsReport}
          disabled={loading || Boolean(error)}
          className="inline-flex min-h-11 shrink-0 items-center justify-center gap-2 rounded-xl bg-primary px-5 py-3 text-sm font-semibold text-primary-foreground shadow-sm transition-all hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <span aria-hidden="true">↓</span>
          Export CSV Report
        </button>
      </div>
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
                        labelLine={false}
                        label={({ name, value }) => `${name}: ${value}`}
                      >
                        {statusChartData.map((entry, index) => (
                          <Cell
                            key={entry.name}
                            fill={STATUS_COLORS[index % STATUS_COLORS.length]}
                          />
                        ))}
                      </Pie>
                      <Tooltip
                        formatter={(value, name) => [value, name]}
                        contentStyle={CHART_TOOLTIP_STYLE}
                        labelStyle={CHART_TOOLTIP_LABEL_STYLE}
                        itemStyle={CHART_TOOLTIP_ITEM_STYLE}
                      />
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
                      <CartesianGrid
                        stroke={CHART_GRID_COLOR}
                        strokeDasharray="3 3"
                        opacity={0.45}
                      />
                      <XAxis
                        type="number"
                        allowDecimals={false}
                        tick={{ fill: CHART_TEXT_COLOR, fontSize: 12 }}
                        axisLine={{ stroke: CHART_MUTED_COLOR }}
                        tickLine={{ stroke: CHART_MUTED_COLOR }}
                      />
                      <YAxis
                        type="category"
                        dataKey="airport"
                        width={48}
                        tick={CHART_AXIS_STYLE}
                        axisLine={CHART_AXIS_LINE_STYLE}
                        tickLine={CHART_AXIS_LINE_STYLE}
                      />
                      <Tooltip
                        contentStyle={CHART_TOOLTIP_STYLE}
                        labelStyle={CHART_TOOLTIP_LABEL_STYLE}
                        itemStyle={CHART_TOOLTIP_ITEM_STYLE}
                      />
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
                      <CartesianGrid
                        stroke={CHART_GRID_COLOR}
                        strokeDasharray="3 3"
                        horizontal={false}
                        opacity={0.45}
                      />
                      <XAxis
                        type="number"
                        allowDecimals={false}
                        tick={{ fill: CHART_TEXT_COLOR, fontSize: 12 }}
                        axisLine={{ stroke: CHART_MUTED_COLOR }}
                        tickLine={{ stroke: CHART_MUTED_COLOR }}
                      />
                      <YAxis
                        type="category"
                        dataKey="route"
                        width={112}
                        tick={CHART_AXIS_STYLE}
                        axisLine={CHART_AXIS_LINE_STYLE}
                        tickLine={CHART_AXIS_LINE_STYLE}
                      />
                      <Tooltip
                        contentStyle={CHART_TOOLTIP_STYLE}
                        labelStyle={CHART_TOOLTIP_LABEL_STYLE}
                        itemStyle={CHART_TOOLTIP_ITEM_STYLE}
                      />
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
                        <CartesianGrid
                          stroke={CHART_GRID_COLOR}
                          strokeDasharray="3 3"
                          horizontal={false}
                          opacity={0.45}
                        />
                        <XAxis
                          type="number"
                          allowDecimals={false}
                          tick={{ fill: CHART_TEXT_COLOR, fontSize: 12 }}
                          axisLine={{ stroke: CHART_MUTED_COLOR }}
                          tickLine={{ stroke: CHART_MUTED_COLOR }}
                        />
                        <YAxis
                          type="category"
                          dataKey="route"
                          width={112}
                          tick={CHART_AXIS_STYLE}
                          axisLine={CHART_AXIS_LINE_STYLE}
                          tickLine={CHART_AXIS_LINE_STYLE}
                        />
                        <Tooltip
                          contentStyle={CHART_TOOLTIP_STYLE}
                          labelStyle={CHART_TOOLTIP_LABEL_STYLE}
                          itemStyle={CHART_TOOLTIP_ITEM_STYLE}
                        />
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
                        <CartesianGrid
                          stroke={CHART_GRID_COLOR}
                          strokeDasharray="3 3"
                          horizontal={false}
                          opacity={0.45}
                        />
                        <XAxis
                          type="number"
                          allowDecimals={false}
                          tick={{ fill: CHART_TEXT_COLOR, fontSize: 12 }}
                          axisLine={{ stroke: CHART_MUTED_COLOR }}
                          tickLine={{ stroke: CHART_MUTED_COLOR }}
                        />
                        <YAxis
                          type="category"
                          dataKey="airport"
                          width={48}
                          tick={CHART_AXIS_STYLE}
                          axisLine={CHART_AXIS_LINE_STYLE}
                          tickLine={CHART_AXIS_LINE_STYLE}
                        />
                        <Tooltip
                          contentStyle={CHART_TOOLTIP_STYLE}
                          labelStyle={CHART_TOOLTIP_LABEL_STYLE}
                          itemStyle={CHART_TOOLTIP_ITEM_STYLE}
                        />
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
                        <CartesianGrid
                          stroke={CHART_GRID_COLOR}
                          strokeDasharray="3 3"
                          horizontal={false}
                          opacity={0.45}
                        />
                        <XAxis
                          type="number"
                          allowDecimals={false}
                          tick={{ fill: CHART_TEXT_COLOR, fontSize: 12 }}
                          axisLine={{ stroke: CHART_MUTED_COLOR }}
                          tickLine={{ stroke: CHART_MUTED_COLOR }}
                        />
                        <YAxis
                          type="category"
                          dataKey="registration"
                          width={76}
                          tick={CHART_AXIS_STYLE}
                          axisLine={CHART_AXIS_LINE_STYLE}
                          tickLine={CHART_AXIS_LINE_STYLE}
                        />
                        <Tooltip
                          contentStyle={CHART_TOOLTIP_STYLE}
                          labelStyle={CHART_TOOLTIP_LABEL_STYLE}
                          itemStyle={CHART_TOOLTIP_ITEM_STYLE}
                        />
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
                          labelLine={false}
                          label={({ name, value }) => `${name}: ${value}`}
                        >
                          {aircraftStatusChartData.map((entry, index) => (
                            <Cell
                              key={entry.name}
                              fill={STATUS_COLORS[index % STATUS_COLORS.length]}
                            />
                          ))}
                        </Pie>
                        <Tooltip
                          contentStyle={CHART_TOOLTIP_STYLE}
                          labelStyle={CHART_TOOLTIP_LABEL_STYLE}
                          itemStyle={CHART_TOOLTIP_ITEM_STYLE}
                        />
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

            <SectionCard
              title="Historical flight activity"
              subtitle="Flight volume grouped by scheduled departure date"
            >
              {historicalFlightChartData.length > 0 ? (
                <div className="h-[360px] w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart
                      data={historicalFlightChartData}
                      margin={{
                        top: 8,
                        right: 12,
                        left: 8,
                        bottom: 8,
                      }}
                    >
                      <CartesianGrid
                        stroke={CHART_GRID_COLOR}
                        strokeDasharray="3 3"
                        horizontal={false}
                        opacity={0.45}
                      />
                      <XAxis
                        dataKey="date"
                        tick={{ fill: CHART_TEXT_COLOR, fontSize: 12 }}
                        axisLine={{ stroke: CHART_MUTED_COLOR }}
                        tickLine={{ stroke: CHART_MUTED_COLOR }}
                      />
                      <YAxis
                        allowDecimals={false}
                        tick={CHART_AXIS_STYLE}
                        axisLine={CHART_AXIS_LINE_STYLE}
                        tickLine={CHART_AXIS_LINE_STYLE}
                      />
                      <Tooltip
                        contentStyle={CHART_TOOLTIP_STYLE}
                        labelStyle={CHART_TOOLTIP_LABEL_STYLE}
                        itemStyle={CHART_TOOLTIP_ITEM_STYLE}
                      />
                      <Line
                        type="monotone"
                        dataKey="total"
                        name="Total flights"
                        stroke="hsl(var(--primary))"
                        strokeWidth={2}
                        dot={false}
                      />
                      <Line
                        type="monotone"
                        dataKey="delayed"
                        name="Delayed flights"
                        stroke="hsl(var(--destructive))"
                        strokeWidth={2}
                        dot={false}
                      />
                      <Line
                        type="monotone"
                        dataKey="cancelled"
                        name="Cancelled flights"
                        stroke="hsl(var(--chart-3))"
                        strokeWidth={2}
                        dot={false}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">
                  No historical flight activity available.
                </p>
              )}
            </SectionCard>
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
