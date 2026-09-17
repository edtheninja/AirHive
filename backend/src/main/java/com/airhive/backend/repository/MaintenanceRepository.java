package com.airhive.backend.repository;

import com.airhive.backend.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    boolean existsByOrderNumber(String orderNumber);
}
