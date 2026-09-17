package com.airhive.backend.dto;

public class MaintenanceResponseDTO {

    private Long id;
    private String orderNumber;
    private String aircraft;
    private String type;
    private String severity;
    private String due;
    private Integer progress;
    private String engineer;

    public MaintenanceResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getAircraft() {
        return aircraft;
    }

    public void setAircraft(String aircraft) {
        this.aircraft = aircraft;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDue() {
        return due;
    }

    public void setDue(String due) {
        this.due = due;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getEngineer() {
        return engineer;
    }

    public void setEngineer(String engineer) {
        this.engineer = engineer;
    }
}
