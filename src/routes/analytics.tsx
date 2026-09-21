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
import { getAnalyticsOverview } from "@/lib/api/analytics";
import type { AnalyticsOverview } from "@/lib/api/analytics";
import { useEffect, useState } from "react";

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
  const [analytics, setAnalytics] = useState<AnalyticsOverview | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    getAnalyticsOverview()
      .then((data) => {
        if (!mounted) {
          return;
        }

        setAnalytics(data);
      })
      .catch((requestError) => {
        if (!mounted) {
          return;
        }

        setError(
          requestError instanceof Error ? requestError.message : "Unable to load analytics.",
        );
      })
      .finally(() => {
        if (mounted) {
          setLoading(false);
        }
      });

    return () => {
      mounted = false;
    };
  }, []);

  const flightStatusCounts = analytics?.flightStatusDistribution ?? [];

  const statusRows = [
    "SCHEDULED",
    "BOARDING",
    "TAXIING",
    "DEPARTED",
    "IN AIR",
    "LANDING",
    "LANDED",
    "DELAYED",
    "CANCELLED",
  ].map((status) => ({
    status,
    count: flightStatusCounts.find((item) => item.status === status)?.count ?? 0,
  }));

  const statusChartData = statusRows.map((item) => ({
    name: item.status,
    value: item.count,
  }));

  const airportActivity = analytics?.airportActivity ?? [];

  const airportChartData = airportActivity.slice(0, 10).map((item) => ({
    airport: item.name,
    departures: item.departures,
    arrivals: item.arrivals,
    total: item.total,
  }));

  const routeActivity = analytics?.routeActivity ?? [];

  const routeChartData = routeActivity.slice(0, 10).map((item) => ({
    route: item.name,
    count: item.total,
  }));

  /*
   * The current analytics API does not expose delayed flights grouped
   * by route or airport. Keep these sections empty rather than
   * presenting total activity as delayed activity.
   */
  const delayedFlights = analytics?.delayedFlights ?? 0;

  const delayedRouteChartData: Array<{
    route: string;
    count: number;
  }> = [];

  const delayedAirportChartData: Array<{
    airport: string;
    count: number;
  }> = [];

  const aircraftUtilizationData = (analytics?.aircraftUtilization ?? []).map((item) => ({
    aircraftId: item.aircraftId,
    registration: item.registrationNumber,
    aircraftTypeCode: item.aircraftTypeCode,
    status: item.status,
    assignedFlights: item.assignedFlights,
  }));

  const aircraftStatusCounts = aircraftUtilizationData.reduce<Record<string, number>>(
    (counts, aircraft) => {
      counts[aircraft.status] = (counts[aircraft.status] ?? 0) + 1;
      return counts;
    },
    {},
  );

  const aircraftStatusChartData = Object.entries(aircraftStatusCounts).map(([name, value]) => ({
    name,
    value,
  }));

  const historicalFlightActivity = analytics?.historicalActivity ?? [];

  const historicalFlightChartData = historicalFlightActivity;

  const delayedShare =
    analytics && analytics.totalFlights > 0
      ? Math.round((analytics.delayedFlights / analytics.totalFlights) * 100)
      : 0;

  const aircraftUtilizationShare =
    analytics && analytics.totalAircraft > 0
      ? Math.round(
          (aircraftUtilizationData.filter((aircraft) => aircraft.assignedFlights > 0).length /
            analytics.totalAircraft) *
            100,
        )
      : 0;

  const airborneShare =
    analytics && analytics.totalFlights > 0
      ? Math.round((analytics.airborneFlights / analytics.totalFlights) * 100)
      : 0;

  const fleetAvailability =
    analytics && analytics.totalAircraft > 0
      ? Math.round((analytics.activeAircraft / analytics.totalAircraft) * 100)
      : 0;

  const exportAnalyticsReport = () => {
    if (!analytics) {
      return;
    }

    const rows = [
      ["AirHive Analytics Report"],
      [],
      ["Operational Summary"],
      ["Total Flights", analytics.totalFlights],
      ["Delayed Flights", analytics.delayedFlights],
      ["Cancelled Flights", analytics.cancelledFlights],
      ["Airborne Flights", analytics.airborneFlights],
      [],
      ["Fleet Summary"],
      ["Total Aircraft", analytics.totalAircraft],
      ["Active Aircraft", analytics.activeAircraft],
      ["Inactive Aircraft", analytics.inactiveAircraft],
      ["Maintenance Aircraft", analytics.maintenanceAircraft],
      [],
      ["Flight Status Distribution"],
      ["Status", "Count"],
      ...analytics.flightStatusDistribution.map((item) => [item.status, item.count]),
      [],
      ["Aircraft Utilization"],
      ["Aircraft ID", "Registration", "Aircraft Type", "Status", "Assigned Flights"],
      ...analytics.aircraftUtilization.map((item) => [
        item.aircraftId,
        item.registrationNumber,
        item.aircraftTypeCode ?? "",
        item.status,
        item.assignedFlights,
      ]),
      [],
      ["Airport Activity"],
      ["Airport", "Departures", "Arrivals", "Total"],
      ...analytics.airportActivity.map((item) => [
        item.name,
        item.departures,
        item.arrivals,
        item.total,
      ]),
      [],
      ["Route Activity"],
      ["Route", "Total Flights"],
      ...analytics.routeActivity.map((item) => [item.name, item.total]),
      [],
      ["Historical Activity"],
      ["Date", "Total", "Delayed", "Cancelled"],
      ...analytics.historicalActivity.map((item) => [
        item.date,
        item.total,
        item.delayed,
        item.cancelled,
      ]),
    ];

    const csv = rows
      .map((row) => row.map((value) => `"${String(value).replace(/"/g, '""')}"`).join(","))
      .join("\n");

    const blob = new Blob([csv], {
      type: "text/csv;charset=utf-8;",
    });

    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");

    link.href = url;
    link.download = "airhive-analytics-report.csv";
    link.click();

    URL.revokeObjectURL(url);
  };

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
              <p className="num mt-4 text-3xl font-semibold">{analytics?.totalFlights ?? 0}</p>
            </SectionCard>

            <SectionCard title="Currently airborne" subtitle="Flights in air">
              <p className="num mt-4 text-3xl font-semibold">{analytics?.airborneFlights ?? 0}</p>
            </SectionCard>

            <SectionCard title="Currently delayed" subtitle="Active delays">
              <p className="num mt-4 text-3xl font-semibold">{analytics?.delayedFlights ?? 0}</p>
            </SectionCard>

            <SectionCard title="Fleet availability" subtitle="Based on available fleet">
              <p className="num mt-4 text-3xl font-semibold">{fleetAvailability}%</p>
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
                {statusRows.map((item) => (
                  <div key={item.status} className="rounded-lg border border-border/60 px-3 py-2">
                    <p className="text-muted-foreground">{item.status}</p>
                    <p className="num mt-1 text-lg font-semibold">{item.count}</p>
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
                  <dd className="num font-medium">{analytics?.totalAircraft ?? 0}</dd>
                </div>

                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Delayed flights tracked</dt>
                  <dd className="num font-medium">{delayedFlights}</dd>
                </div>

                <div className="flex items-center justify-between">
                  <dt className="text-muted-foreground">Flights tracked</dt>
                  <dd className="num font-medium">{analytics?.totalFlights ?? 0}</dd>
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
                  <dd className="num font-medium">{fleetAvailability}%</dd>
                </div>
              </dl>
            </SectionCard>
          </div>
        </>
      )}
    </motion.div>
  );
}
