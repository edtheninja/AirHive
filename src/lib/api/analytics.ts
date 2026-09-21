import { apiRequest } from "./client";

export type AnalyticsStatusCount = {
  status: string;
  count: number;
};

export type AnalyticsAircraftUtilization = {
  aircraftId: number;
  registrationNumber: string;
  aircraftTypeCode: string | null;
  status: string;
  assignedFlights: number;
};

export type AnalyticsActivityCount = {
  name: string;
  departures: number;
  arrivals: number;
  total: number;
};

export type AnalyticsHistoricalActivity = {
  date: string;
  total: number;
  delayed: number;
  cancelled: number;
};

export type AnalyticsOverview = {
  totalFlights: number;
  delayedFlights: number;
  cancelledFlights: number;
  airborneFlights: number;

  totalAircraft: number;
  activeAircraft: number;
  inactiveAircraft: number;
  maintenanceAircraft: number;

  flightStatusDistribution: AnalyticsStatusCount[];
  aircraftUtilization: AnalyticsAircraftUtilization[];
  airportActivity: AnalyticsActivityCount[];
  routeActivity: AnalyticsActivityCount[];
  historicalActivity: AnalyticsHistoricalActivity[];
};

export async function getAnalyticsOverview(): Promise<AnalyticsOverview> {
  return apiRequest<AnalyticsOverview>("/analytics/overview");
}
