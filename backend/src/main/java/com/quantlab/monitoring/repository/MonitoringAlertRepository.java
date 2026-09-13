package com.quantlab.monitoring.repository;

import com.quantlab.monitoring.entity.MonitoringAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonitoringAlertRepository extends JpaRepository<MonitoringAlertEntity, UUID> {
    List<MonitoringAlertEntity> findAllByOrderByCreatedAtDesc();
    List<MonitoringAlertEntity> findByStatusOrderByCreatedAtDesc(String status);
    List<MonitoringAlertEntity> findBySeverityOrderByCreatedAtDesc(String severity);
}
