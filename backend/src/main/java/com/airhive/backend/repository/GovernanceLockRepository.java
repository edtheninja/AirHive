package com.airhive.backend.repository;

import com.airhive.backend.entity.GovernanceLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GovernanceLockRepository
        extends JpaRepository<GovernanceLock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GovernanceLock g WHERE g.id = :id")
    Optional<GovernanceLock> lockById(@Param("id") Long id);
}