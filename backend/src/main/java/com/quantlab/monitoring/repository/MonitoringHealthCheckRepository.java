package com.quantlab.monitoring.repository;

import com.quantlab.monitoring.entity.MonitoringHealthCheckEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonitoringHealthCheckRepository extends JpaRepository<MonitoringHealthCheckEntity, UUID> {
    List<MonitoringHealthCheckEntity> findAllByOrderByCheckedAtDesc();
    List<MonitoringHealthCheckEntity> findByComponentOrderByCheckedAtDesc(String component);
}
