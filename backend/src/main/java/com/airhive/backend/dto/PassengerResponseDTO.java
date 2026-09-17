package com.airhive.backend.dto;

public class PassengerResponseDTO {

    private Long id;
    private String passengerCode;
    private String name;
    private String tier;
    private String flight;
    private String route;
    private String seat;
    private Integer bags;
    private boolean checkedIn;

    public PassengerResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassengerCode() {
        return passengerCode;
    }

    public void setPassengerCode(String passengerCode) {
        this.passengerCode = passengerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getFlight() {
        return flight;
    }

    public void setFlight(String flight) {
        this.flight = flight;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public Integer getBags() {
        return bags;
    }

    public void setBags(Integer bags) {
        this.bags = bags;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}
