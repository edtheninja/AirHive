export type FlightStatus =
  | "Scheduled"
  | "Boarding"
  | "Taxiing"
  | "Departed"
  | "In Air"
  | "Landing"
  | "Landed"
  | "Delayed"
  | "Cancelled";

export const FLIGHT_STATUS_FLOW: FlightStatus[] = [
  "Scheduled",
  "Boarding",
  "Taxiing",
  "Departed",
  "In Air",
  "Landing",
  "Landed",
];

export type Flight = {
  id: string;
  number: string;
  aircraft: string;
  aircraftModel: string;
  origin: string;
  destination: string;
  gate: string;
  boarding: number;
  departure: string;
  delay: number;
  crew: string;
  crewCount: number;
  status: FlightStatus;
  pax: number;
};

export type Aircraft = {
  id: string;
  registration: string;
  model: string;
  capacity: number;
  health: number;
  fuel: number;
  maintenance: string;
  location: string;
  status: "In Service" | "Grounded" | "Maintenance" | "Standby";
  image: string;
};

export type CrewMember = {
  id: string;
  name: string;
  role: "Captain" | "First Officer" | "Purser" | "Cabin Crew" | "Engineer";
  base: string;
  availability: "Available" | "On Duty" | "Resting" | "Leave";
  restHours: number;
  nextFlight: string;
  medical: string;
  initials: string;
};

export type Booking = {
  id: string;
  passenger: string;
  flight: string;
  route: string;
  cabin: "Economy" | "Premium" | "Business" | "First";
  seat: string;
  status: "Confirmed" | "Checked In" | "Pending" | "Cancelled";
  amount: number;
  date: string;
};

export type OpsNotification = {
  id: string;
  title: string;
  detail: string;
  tone: "info" | "success" | "warning" | "danger";
  time: string;
};

const airports = ["DEL", "BOM", "DXB", "SIN", "LHR", "FRA", "JFK", "CDG", "HKG", "SYD", "BLR", "DOH"];

export const AIRPORTS = [
  { code: "DEL", city: "Delhi", country: "India", terminals: 3, flights: 148, status: "Normal" },
  { code: "BOM", city: "Mumbai", country: "India", terminals: 2, flights: 122, status: "Normal" },
  { code: "DXB", city: "Dubai", country: "UAE", terminals: 3, flights: 176, status: "Congested" },
  { code: "SIN", city: "Singapore", country: "Singapore", terminals: 4, flights: 134, status: "Normal" },
  { code: "LHR", city: "London", country: "United Kingdom", terminals: 4, flights: 158, status: "Weather" },
  { code: "FRA", city: "Frankfurt", country: "Germany", terminals: 2, flights: 141, status: "Normal" },
  { code: "JFK", city: "New York", country: "USA", terminals: 6, flights: 165, status: "Normal" },
  { code: "SYD", city: "Sydney", country: "Australia", terminals: 3, flights: 98, status: "Normal" },
];

export const ROUTES = [
  { id: "R-101", code: "DEL → DXB", distance: 2190, duration: "3h 45m", frequency: "14 / week", load: 88, revenue: 412000 },
  { id: "R-102", code: "BOM → SIN", distance: 3910, duration: "5h 30m", frequency: "10 / week", load: 81, revenue: 385000 },
  { id: "R-103", code: "DEL → LHR", distance: 6710, duration: "9h 05m", frequency: "7 / week", load: 92, revenue: 704000 },
  { id: "R-104", code: "BLR → FRA", distance: 7480, duration: "10h 15m", frequency: "5 / week", load: 76, revenue: 512000 },
  { id: "R-105", code: "DEL → JFK", distance: 11760, duration: "15h 40m", frequency: "7 / week", load: 94, revenue: 968000 },
  { id: "R-106", code: "BOM → DOH", distance: 1930, duration: "3h 20m", frequency: "12 / week", load: 79, revenue: 298000 },
];

export const AIRCRAFT: Aircraft[] = [
  { id: "1", registration: "VT-AXN", model: "Airbus A320neo", capacity: 186, health: 96, fuel: 82, maintenance: "12 Aug 2026", location: "DEL — Bay 14", status: "In Service", image: "a320" },
  { id: "2", registration: "VT-BQR", model: "Boeing 787-9", capacity: 296, health: 91, fuel: 64, maintenance: "03 Sep 2026", location: "BOM — Bay 07", status: "In Service", image: "b787" },
  { id: "3", registration: "VT-CLM", model: "Airbus A350-900", capacity: 324, health: 88, fuel: 45, maintenance: "22 Aug 2026", location: "DXB — Gate C2", status: "Standby", image: "a350" },
  { id: "4", registration: "VT-DKS", model: "Boeing 737 MAX 8", capacity: 189, health: 72, fuel: 30, maintenance: "In progress", location: "BLR — Hangar 2", status: "Maintenance", image: "b737" },
  { id: "5", registration: "VT-ERU", model: "Airbus A321neo", capacity: 222, health: 94, fuel: 77, maintenance: "18 Sep 2026", location: "SIN — Bay 21", status: "In Service", image: "a321" },
  { id: "6", registration: "VT-FTL", model: "Boeing 777-300ER", capacity: 342, health: 61, fuel: 12, maintenance: "Overdue", location: "DEL — Hangar 1", status: "Grounded", image: "b777" },
];

export const CREW: CrewMember[] = [
  { id: "C1", name: "Capt. Arjun Mehta", role: "Captain", base: "DEL", availability: "On Duty", restHours: 11, nextFlight: "AI302", medical: "Valid — Mar 2027", initials: "AM" },
  { id: "C2", name: "Capt. Lena Fischer", role: "Captain", base: "FRA", availability: "Resting", restHours: 4, nextFlight: "AI914", medical: "Valid — Nov 2026", initials: "LF" },
  { id: "C3", name: "F/O Rahul Nair", role: "First Officer", base: "BOM", availability: "Available", restHours: 22, nextFlight: "AI458", medical: "Valid — Jun 2027", initials: "RN" },
  { id: "C4", name: "Sofia Alvarez", role: "Purser", base: "DXB", availability: "On Duty", restHours: 9, nextFlight: "AI302", medical: "Valid — Jan 2027", initials: "SA" },
  { id: "C5", name: "Chen Wei", role: "Cabin Crew", base: "SIN", availability: "Available", restHours: 18, nextFlight: "AI770", medical: "Renew — Sep 2026", initials: "CW" },
  { id: "C6", name: "Priya Sharma", role: "Cabin Crew", base: "DEL", availability: "Leave", restHours: 48, nextFlight: "—", medical: "Valid — Aug 2027", initials: "PS" },
  { id: "C7", name: "Tomas Novak", role: "Engineer", base: "LHR", availability: "On Duty", restHours: 7, nextFlight: "Ground", medical: "Valid — Feb 2027", initials: "TN" },
  { id: "C8", name: "Aisha Rahman", role: "First Officer", base: "DOH", availability: "Available", restHours: 26, nextFlight: "AI221", medical: "Valid — Dec 2026", initials: "AR" },
];

const firstNames = ["Ananya", "Marcus", "Yuki", "Elena", "Omar", "Grace", "Daniel", "Priya", "Lukas", "Sara", "Ibrahim", "Mei"];
const lastNames = ["Kapoor", "Bennett", "Tanaka", "Rossi", "Haddad", "Okafor", "Muller", "Iyer", "Berg", "Lindqvist", "Khan", "Zhang"];

function pick<T>(arr: T[], i: number) {
  return arr[i % arr.length];
}

export const BOOKINGS: Booking[] = Array.from({ length: 48 }, (_, i) => {
  const cabins = ["Economy", "Premium", "Business", "First"] as const;
  const statuses = ["Confirmed", "Checked In", "Pending", "Cancelled"] as const;
  return {
    id: `BK-${(10248 + i * 7).toString()}`,
    passenger: `${pick(firstNames, i * 3)} ${pick(lastNames, i * 5)}`,
    flight: `AI${300 + ((i * 13) % 600)}`,
    route: `${pick(airports, i)} → ${pick(airports, i + 4)}`,
    cabin: cabins[i % 4],
    seat: `${(i % 32) + 1}${"ABCDEF"[i % 6]}`,
    status: statuses[i % 7 === 0 ? 3 : i % 3],
    amount: 210 + ((i * 137) % 2400),
    date: `${(i % 28) + 1} Aug 2026`,
  };
});

export const PASSENGERS = BOOKINGS.slice(0, 24).map((b, i) => ({
  id: `PX-${9000 + i}`,
  name: b.passenger,
  tier: (["Blue", "Silver", "Gold", "Platinum"] as const)[i % 4],
  flight: b.flight,
  route: b.route,
  checkedIn: i % 3 !== 0,
  bags: i % 3,
  seat: b.seat,
}));

export const MAINTENANCE = [
  { id: "MX-4412", aircraft: "VT-DKS", type: "A-Check", severity: "Routine", due: "In progress", progress: 62, engineer: "T. Novak" },
  { id: "MX-4413", aircraft: "VT-FTL", type: "Engine Inspection", severity: "Critical", due: "Overdue 2d", progress: 24, engineer: "M. Farouk" },
  { id: "MX-4414", aircraft: "VT-CLM", type: "Avionics Update", severity: "Routine", due: "22 Aug", progress: 0, engineer: "S. Kapoor" },
  { id: "MX-4415", aircraft: "VT-BQR", type: "Landing Gear", severity: "Major", due: "03 Sep", progress: 8, engineer: "L. Bianchi" },
  { id: "MX-4416", aircraft: "VT-AXN", type: "Cabin Refit", severity: "Minor", due: "12 Aug", progress: 88, engineer: "R. Nair" },
];

export const REVENUE_SERIES = [
  { label: "Mon", revenue: 412, target: 380 },
  { label: "Tue", revenue: 458, target: 400 },
  { label: "Wed", revenue: 396, target: 410 },
  { label: "Thu", revenue: 512, target: 430 },
  { label: "Fri", revenue: 604, target: 470 },
  { label: "Sat", revenue: 688, target: 520 },
  { label: "Sun", revenue: 622, target: 500 },
];

export const OCCUPANCY_SERIES = [
  { label: "Economy", value: 92 },
  { label: "Premium", value: 78 },
  { label: "Business", value: 84 },
  { label: "First", value: 61 },
];

export const DELAY_SERIES = [
  { label: "Weather", value: 34 },
  { label: "Technical", value: 21 },
  { label: "ATC", value: 18 },
  { label: "Crew", value: 12 },
  { label: "Ground Ops", value: 15 },
];

export const FUEL_SERIES = Array.from({ length: 12 }, (_, i) => ({
  label: `${i + 1}`,
  burn: 240 + Math.round(Math.sin(i / 1.6) * 40) + i * 4,
  planned: 250 + i * 4,
}));

export const COMPLETION_SERIES = Array.from({ length: 12 }, (_, i) => ({
  label: `W${i + 1}`,
  completion: 92 + Math.round(Math.sin(i / 2) * 4),
}));

function flightSeed(i: number): Flight {
  const statuses: FlightStatus[] = ["Scheduled", "Boarding", "Taxiing", "Departed", "In Air", "Landing", "Landed", "Delayed"];
  const status = statuses[i % statuses.length];
  const ac = AIRCRAFT[i % AIRCRAFT.length];
  return {
    id: `F${i}`,
    number: `AI${300 + i * 17}`,
    aircraft: ac.registration,
    aircraftModel: ac.model,
    origin: pick(airports, i),
    destination: pick(airports, i + 5),
    gate: `${"ABCD"[i % 4]}${(i % 20) + 1}`,
    boarding: status === "Boarding" ? 20 + ((i * 9) % 60) : status === "Scheduled" ? 0 : 100,
    departure: `${String(6 + (i % 16)).padStart(2, "0")}:${(i % 6) * 10 || "05"}`.slice(0, 5),
    delay: status === "Delayed" ? 15 + ((i * 5) % 45) : 0,
    crew: `${pick(CREW, i).name.split(" ").slice(-1)[0]} +${4 + (i % 5)}`,
    crewCount: 5 + (i % 5),
    status,
    pax: 96 + ((i * 37) % 240),
  };
}

export const FLIGHTS: Flight[] = Array.from({ length: 14 }, (_, i) => flightSeed(i));

export const SEED_NOTIFICATIONS: OpsNotification[] = [
  { id: "N1", title: "Flight AI302 delayed", detail: "Departure pushed by 15 minutes — ATC slot", tone: "warning", time: "just now" },
  { id: "N2", title: "Gate changed to A12", detail: "AI458 reassigned from B04", tone: "info", time: "2m ago" },
  { id: "N3", title: "Aircraft VT-AXN ready", detail: "Pre-flight checks completed", tone: "success", time: "6m ago" },
  { id: "N4", title: "Crew assigned", detail: "Capt. Mehta rostered to AI302", tone: "info", time: "11m ago" },
  { id: "N5", title: "Passenger checked in", detail: "218 of 240 checked in for AI914", tone: "success", time: "14m ago" },
];

export const NOTIFICATION_POOL: Omit<OpsNotification, "id" | "time">[] = [
  { title: "Flight AI770 boarding", detail: "Gate C4 — 62% boarded", tone: "info" },
  { title: "De-icing requested", detail: "VT-BQR at FRA stand 21", tone: "warning" },
  { title: "Slot confirmed", detail: "AI221 cleared for 14:20 departure", tone: "success" },
  { title: "Medical clearance expiring", detail: "Chen Wei — renew before 30 Sep", tone: "warning" },
  { title: "Emergency drill logged", detail: "DEL ops centre — quarterly compliance", tone: "info" },
  { title: "Fuel uplift complete", detail: "VT-ERU 18,400 kg loaded", tone: "success" },
  { title: "Baggage belt fault", detail: "BOM T2 belt 4 offline", tone: "danger" },
  { title: "Weather advisory", detail: "LHR low visibility procedures active", tone: "warning" },
];
