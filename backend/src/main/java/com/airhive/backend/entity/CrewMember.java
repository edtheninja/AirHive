package com.airhive.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "crew_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_crew_member_code",
                        columnNames = "crew_code"
                )
        }
)
public class CrewMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crew_code", nullable = false, length = 20)
    private String crewCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 60)
    private String role;

    @Column(nullable = false, length = 10)
    private String base;

    @Column(nullable = false, length = 30)
    private String availability;

    @Column(nullable = false)
    private Integer restHours;

    @Column(nullable = false, length = 20)
    private String nextFlight;

    @Column(nullable = false, length = 80)
    private String medical;

    @Column(nullable = false, length = 10)
    private String initials;

    public CrewMember() {
    }

    public Long getId() {
        return id;
    }

    public String getCrewCode() {
        return crewCode;
    }

    public void setCrewCode(String crewCode) {
        this.crewCode = crewCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public Integer getRestHours() {
        return restHours;
    }

    public void setRestHours(Integer restHours) {
        this.restHours = restHours;
    }

    public String getNextFlight() {
        return nextFlight;
    }

    public void setNextFlight(String nextFlight) {
        this.nextFlight = nextFlight;
    }

    public String getMedical() {
        return medical;
    }

    public void setMedical(String medical) {
        this.medical = medical;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }
}
