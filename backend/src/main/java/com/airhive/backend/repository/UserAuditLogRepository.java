package com.airhive.backend.repository;

import com.airhive.backend.entity.UserAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuditLogRepository
        extends JpaRepository<UserAuditLog, Long> {
}