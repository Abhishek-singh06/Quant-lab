package com.quantlab.monitoring.repository;

import com.quantlab.monitoring.entity.MonitoringIncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonitoringIncidentRepository extends JpaRepository<MonitoringIncidentEntity, UUID> {
    List<MonitoringIncidentEntity> findAllByOrderByCreatedAtDesc();
}
