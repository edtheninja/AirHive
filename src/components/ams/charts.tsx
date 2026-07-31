import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

const axis = {
  stroke: "var(--color-muted-foreground)",
  fontSize: 11,
  tickLine: false,
  axisLine: false,
};

const tooltipStyle = {
  contentStyle: {
    background: "var(--color-popover)",
    border: "1px solid var(--color-border)",
    borderRadius: "14px",
    boxShadow: "var(--elev-2)",
    fontSize: "12px",
    color: "var(--color-popover-foreground)",
    backdropFilter: "blur(20px)",
  },
  cursor: { stroke: "var(--color-muted-foreground)", strokeOpacity: 0.2 },
};

export function RevenueChart({ data }: { data: { label: string; revenue: number; target: number }[] }) {
  return (
    <ResponsiveContainer width="100%" height={240}>
      <AreaChart data={data} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
        <defs>
          <linearGradient id="rev" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="var(--color-chart-1)" stopOpacity={0.28} />
            <stop offset="100%" stopColor="var(--color-chart-1)" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 6" stroke="var(--color-border)" vertical={false} />
        <XAxis dataKey="label" {...axis} />
        <YAxis {...axis} />
        <Tooltip {...tooltipStyle} />
        <Area type="monotone" dataKey="revenue" stroke="var(--color-chart-1)" strokeWidth={1.6} fill="url(#rev)" />
        <Line type="monotone" dataKey="target" stroke="var(--color-muted-foreground)" strokeWidth={1} strokeDasharray="4 4" dot={false} />
      </AreaChart>
    </ResponsiveContainer>
  );
}

export function OccupancyChart({ data }: { data: { label: string; value: number }[] }) {
  return (
    <ResponsiveContainer width="100%" height={240}>
      <BarChart data={data} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 6" stroke="var(--color-border)" vertical={false} />
        <XAxis dataKey="label" {...axis} />
        <YAxis {...axis} />
        <Tooltip {...tooltipStyle} cursor={{ fill: "var(--color-foreground)", fillOpacity: 0.04 }} />
        <Bar dataKey="value" radius={[8, 8, 8, 8]} barSize={26}>
          {data.map((_, i) => (
            <Cell key={i} fill={`var(--color-chart-${[1, 2, 3, 5][i % 4]})`} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}

export function CompletionChart({ data }: { data: { label: string; completion: number }[] }) {
  return (
    <ResponsiveContainer width="100%" height={240}>
      <LineChart data={data} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 6" stroke="var(--color-border)" vertical={false} />
        <XAxis dataKey="label" {...axis} />
        <YAxis domain={[80, 100]} {...axis} />
        <Tooltip {...tooltipStyle} />
        <Line type="monotone" dataKey="completion" stroke="var(--color-chart-2)" strokeWidth={1.6} dot={false} />
      </LineChart>
    </ResponsiveContainer>
  );
}

export function FuelChart({ data }: { data: { label: string; burn: number; planned: number }[] }) {
  return (
    <ResponsiveContainer width="100%" height={240}>
      <LineChart data={data} margin={{ top: 8, right: 8, left: -18, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 6" stroke="var(--color-border)" vertical={false} />
        <XAxis dataKey="label" {...axis} />
        <YAxis {...axis} />
        <Tooltip {...tooltipStyle} />
        <Line type="monotone" dataKey="burn" stroke="var(--color-chart-3)" strokeWidth={1.6} dot={false} />
        <Line type="monotone" dataKey="planned" stroke="var(--color-muted-foreground)" strokeWidth={1} strokeDasharray="4 4" dot={false} />
      </LineChart>
    </ResponsiveContainer>
  );
}

export function DelayChart({ data }: { data: { label: string; value: number }[] }) {
  return (
    <ResponsiveContainer width="100%" height={240}>
      <BarChart layout="vertical" data={data} margin={{ top: 8, right: 16, left: 24, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 6" stroke="var(--color-border)" horizontal={false} />
        <XAxis type="number" {...axis} />
        <YAxis type="category" dataKey="label" width={80} {...axis} />
        <Tooltip {...tooltipStyle} cursor={{ fill: "var(--color-foreground)", fillOpacity: 0.04 }} />
        <Bar dataKey="value" radius={[8, 8, 8, 8]} barSize={16} fill="var(--color-chart-5)" />
      </BarChart>
    </ResponsiveContainer>
  );
}
