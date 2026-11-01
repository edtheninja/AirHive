package com.airhive.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_audit_log")
public class UserAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(name = "previous_value", length = 100)
    private String previousValue;

    @Column(name = "new_value", length = 100)
    private String newValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UserAuditLog() {
    }

    public UserAuditLog(
            Long actorUserId,
            Long targetUserId,
            String action,
            String previousValue,
            String newValue
    ) {
        this.actorUserId = actorUserId;
        this.targetUserId = targetUserId;
        this.action = action;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public String getAction() {
        return action;
    }

    public String getPreviousValue() {
        return previousValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}