package com.airhive.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "passengers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_passenger_code", columnNames = "passenger_code")
})
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "passenger_code", nullable = false, length = 20)
    private String passengerCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 20)
    private String tier;

    @Column(nullable = false, length = 20)
    private String flight;

    @Column(nullable = false, length = 30)
    private String route;

    @Column(nullable = false, length = 10)
    private String seat;

    @Column(nullable = false)
    private Integer bags;

    @Column(name = "checked_in", nullable = false)
    private boolean checkedIn;

    public Passenger() {
    }

    public Long getId() {
        return id;
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
