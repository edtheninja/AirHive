package com.airhive.backend.dto;

public class CrewMemberResponseDTO {

    private Long id;
    private String crewCode;
    private String name;
    private String role;
    private String base;
    private String availability;
    private Integer restHours;
    private String nextFlight;
    private String medical;
    private String initials;

    public CrewMemberResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
