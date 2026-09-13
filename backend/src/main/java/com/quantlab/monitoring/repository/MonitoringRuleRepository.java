package com.quantlab.monitoring.repository;

import com.quantlab.monitoring.entity.MonitoringRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitoringRuleRepository extends JpaRepository<MonitoringRuleEntity, String> {
    List<MonitoringRuleEntity> findByIsEnabledTrue();
    List<MonitoringRuleEntity> findByTargetComponent(String targetComponent);
}
