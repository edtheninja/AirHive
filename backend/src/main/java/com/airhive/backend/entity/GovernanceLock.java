package com.airhive.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "governance_lock")
public class GovernanceLock {

    @Id
    private Long id;

    protected GovernanceLock() {
    }

    public static GovernanceLock of(Long id) {
        GovernanceLock lock = new GovernanceLock();
        lock.id = id;
        return lock;
    }

    public Long getId() {
        return id;
    }
}