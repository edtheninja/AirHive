package com.airhive.backend.dto.analytics;

import java.util.List;

public class AnalyticsOverviewResponseDTO {

    private long totalFlights;
    private long delayedFlights;
    private long cancelledFlights;
    private long airborneFlights;

    private long totalAircraft;
    private long activeAircraft;
    private long inactiveAircraft;
    private long maintenanceAircraft;

    private List<StatusCountDTO> flightStatusDistribution;
    private List<AircraftUtilizationDTO> aircraftUtilization;
    private List<ActivityCountDTO> airportActivity;
    private List<ActivityCountDTO> routeActivity;
    private List<HistoricalActivityDTO> historicalActivity;

    public AnalyticsOverviewResponseDTO() {
    }

    public long getMaintenanceAircraft() {
    return maintenanceAircraft;
}

public void setMaintenanceAircraft(long maintenanceAircraft) {
    this.maintenanceAircraft = maintenanceAircraft;
}

    public long getTotalFlights() {
        return totalFlights;
    }

    public void setTotalFlights(long totalFlights) {
        this.totalFlights = totalFlights;
    }

    public long getDelayedFlights() {
        return delayedFlights;
    }

    public void setDelayedFlights(long delayedFlights) {
        this.delayedFlights = delayedFlights;
    }

    public long getCancelledFlights() {
        return cancelledFlights;
    }

    public void setCancelledFlights(long cancelledFlights) {
        this.cancelledFlights = cancelledFlights;
    }

    public long getAirborneFlights() {
        return airborneFlights;
    }

    public void setAirborneFlights(long airborneFlights) {
        this.airborneFlights = airborneFlights;
    }

    public long getTotalAircraft() {
        return totalAircraft;
    }

    public void setTotalAircraft(long totalAircraft) {
        this.totalAircraft = totalAircraft;
    }

    public long getActiveAircraft() {
        return activeAircraft;
    }

    public void setActiveAircraft(long activeAircraft) {
        this.activeAircraft = activeAircraft;
    }

    public long getInactiveAircraft() {
        return inactiveAircraft;
    }

    public void setInactiveAircraft(long inactiveAircraft) {
        this.inactiveAircraft = inactiveAircraft;
    }

    public List<StatusCountDTO> getFlightStatusDistribution() {
        return flightStatusDistribution;
    }

    public void setFlightStatusDistribution(
            List<StatusCountDTO> flightStatusDistribution) {
        this.flightStatusDistribution = flightStatusDistribution;
    }

    public List<AircraftUtilizationDTO> getAircraftUtilization() {
        return aircraftUtilization;
    }

    public void setAircraftUtilization(
            List<AircraftUtilizationDTO> aircraftUtilization) {
        this.aircraftUtilization = aircraftUtilization;
    }

    public List<ActivityCountDTO> getAirportActivity() {
        return airportActivity;
    }

    public void setAirportActivity(
            List<ActivityCountDTO> airportActivity) {
        this.airportActivity = airportActivity;
    }

    public List<ActivityCountDTO> getRouteActivity() {
        return routeActivity;
    }

    public void setRouteActivity(
            List<ActivityCountDTO> routeActivity) {
        this.routeActivity = routeActivity;
    }

    public List<HistoricalActivityDTO> getHistoricalActivity() {
        return historicalActivity;
    }

    public void setHistoricalActivity(
            List<HistoricalActivityDTO> historicalActivity) {
        this.historicalActivity = historicalActivity;
    }

    public static class StatusCountDTO {

        private String status;
        private long count;

        public StatusCountDTO() {
        }

        public StatusCountDTO(String status, long count) {
            this.status = status;
            this.count = count;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    public static class AircraftUtilizationDTO {

        private Long aircraftId;
        private String registrationNumber;
        private String aircraftTypeCode;
        private String status;
        private long assignedFlights;

        public AircraftUtilizationDTO() {
        }

        public AircraftUtilizationDTO(
                Long aircraftId,
                String registrationNumber,
                String aircraftTypeCode,
                String status,
                long assignedFlights) {

            this.aircraftId = aircraftId;
            this.registrationNumber = registrationNumber;
            this.aircraftTypeCode = aircraftTypeCode;
            this.status = status;
            this.assignedFlights = assignedFlights;
        }

        public Long getAircraftId() {
            return aircraftId;
        }

        public void setAircraftId(Long aircraftId) {
            this.aircraftId = aircraftId;
        }

        public String getRegistrationNumber() {
            return registrationNumber;
        }

        public void setRegistrationNumber(String registrationNumber) {
            this.registrationNumber = registrationNumber;
        }

        public String getAircraftTypeCode() {
            return aircraftTypeCode;
        }

        public void setAircraftTypeCode(String aircraftTypeCode) {
            this.aircraftTypeCode = aircraftTypeCode;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getAssignedFlights() {
            return assignedFlights;
        }

        public void setAssignedFlights(long assignedFlights) {
            this.assignedFlights = assignedFlights;
        }
    }
    
    /**
 * Activity metrics for airports and routes.
 *
 * For airports:
 * - departures = flights departing from the airport
 * - arrivals = flights arriving at the airport
 * - total = departures + arrivals
 *
 * For routes:
 * - departures = 0
 * - arrivals = 0
 * - total = number of flights assigned to the route
 */
    public static class ActivityCountDTO {

        private String name;
        private long departures;
        private long arrivals;
        private long total;

        public ActivityCountDTO() {
        }

        public ActivityCountDTO(
                String name,
                long departures,
                long arrivals,
                long total) {

            this.name = name;
            this.departures = departures;
            this.arrivals = arrivals;
            this.total = total;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getDepartures() {
            return departures;
        }

        public void setDepartures(long departures) {
            this.departures = departures;
        }

        public long getArrivals() {
            return arrivals;
        }

        public void setArrivals(long arrivals) {
            this.arrivals = arrivals;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }
    }

    public static class HistoricalActivityDTO {

        private String date;
        private long total;
        private long delayed;
        private long cancelled;

        public HistoricalActivityDTO() {
        }

        public HistoricalActivityDTO(
                String date,
                long total,
                long delayed,
                long cancelled) {

            this.date = date;
            this.total = total;
            this.delayed = delayed;
            this.cancelled = cancelled;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getDelayed() {
            return delayed;
        }

        public void setDelayed(long delayed) {
            this.delayed = delayed;
        }

        public long getCancelled() {
            return cancelled;
        }

        public void setCancelled(long cancelled) {
            this.cancelled = cancelled;
        }
    }
}
