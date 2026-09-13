package com.quantlab.broker.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "live_portfolio_reconciliations")
public class LivePortfolioReconciliationEntity {

    @Id
    private UUID id;

    @Column(name = "broker_account_id", nullable = false)
    private UUID brokerAccountId;

    @Column(name = "reconciliation_timestamp", nullable = false)
    private Instant reconciliationTimestamp = Instant.now();

    @Column(nullable = false, length = 32)
    private String status = "MATCHED";

    @Column(name = "total_positions_matched", nullable = false)
    private int totalPositionsMatched = 0;

    @Column(name = "total_discrepancies_count", nullable = false)
    private int totalDiscrepanciesCount = 0;

    @Column(name = "discrepancy_details", columnDefinition = "TEXT")
    private String discrepancyDetails;

    @Column(name = "checked_by", nullable = false, length = 64)
    private String checkedBy = "RECONCILIATION_ENGINE";

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBrokerAccountId() { return brokerAccountId; }
    public void setBrokerAccountId(UUID brokerAccountId) { this.brokerAccountId = brokerAccountId; }

    public Instant getReconciliationTimestamp() { return reconciliationTimestamp; }
    public void setReconciliationTimestamp(Instant reconciliationTimestamp) { this.reconciliationTimestamp = reconciliationTimestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalPositionsMatched() { return totalPositionsMatched; }
    public void setTotalPositionsMatched(int totalPositionsMatched) { this.totalPositionsMatched = totalPositionsMatched; }

    public int getTotalDiscrepanciesCount() { return totalDiscrepanciesCount; }
    public void setTotalDiscrepanciesCount(int totalDiscrepanciesCount) { this.totalDiscrepanciesCount = totalDiscrepanciesCount; }

    public String getDiscrepancyDetails() { return discrepancyDetails; }
    public void setDiscrepancyDetails(String discrepancyDetails) { this.discrepancyDetails = discrepancyDetails; }

    public String getCheckedBy() { return checkedBy; }
    public void setCheckedBy(String checkedBy) { this.checkedBy = checkedBy; }
}
