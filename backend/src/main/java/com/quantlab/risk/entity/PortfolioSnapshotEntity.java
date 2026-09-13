package com.quantlab.risk.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portfolio_snapshots")
public class PortfolioSnapshotEntity {

    @Id
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(name = "snapshot_timestamp", nullable = false)
    private Instant snapshotTimestamp;

    @Column(name = "portfolio_value", nullable = false)
    private double portfolioValue;

    @Column(name = "cash", nullable = false)
    private double cash;

    @Column(name = "total_positions_value", nullable = false)
    private double totalPositionsValue;

    @Column(name = "position_count", nullable = false)
    private int positionCount;

    @Column(name = "positions_json", nullable = false, columnDefinition = "jsonb")
    private String positionsJson;

    @Column(name = "sector_exposure_json", nullable = false, columnDefinition = "jsonb")
    private String sectorExposureJson;

    @Column(name = "consumed_risk_budget", nullable = false)
    private double consumedRiskBudget;

    @Column(name = "remaining_risk_budget", nullable = false)
    private double remainingRiskBudget;

    @Column(name = "portfolio_volatility")
    private Double portfolioVolatility;

    @Column(name = "current_drawdown", nullable = false)
    private double currentDrawdown;

    @Column(name = "data_version", nullable = false, length = 32)
    private String dataVersion = "1";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public PortfolioSnapshotEntity() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (snapshotTimestamp == null) snapshotTimestamp = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public Instant getSnapshotTimestamp() { return snapshotTimestamp; }
    public void setSnapshotTimestamp(Instant snapshotTimestamp) { this.snapshotTimestamp = snapshotTimestamp; }

    public double getPortfolioValue() { return portfolioValue; }
    public void setPortfolioValue(double portfolioValue) { this.portfolioValue = portfolioValue; }

    public double getCash() { return cash; }
    public void setCash(double cash) { this.cash = cash; }

    public double getTotalPositionsValue() { return totalPositionsValue; }
    public void setTotalPositionsValue(double totalPositionsValue) { this.totalPositionsValue = totalPositionsValue; }

    public int getPositionCount() { return positionCount; }
    public void setPositionCount(int positionCount) { this.positionCount = positionCount; }

    public String getPositionsJson() { return positionsJson; }
    public void setPositionsJson(String positionsJson) { this.positionsJson = positionsJson; }

    public String getSectorExposureJson() { return sectorExposureJson; }
    public void setSectorExposureJson(String sectorExposureJson) { this.sectorExposureJson = sectorExposureJson; }

    public double getConsumedRiskBudget() { return consumedRiskBudget; }
    public void setConsumedRiskBudget(double consumedRiskBudget) { this.consumedRiskBudget = consumedRiskBudget; }

    public double getRemainingRiskBudget() { return remainingRiskBudget; }
    public void setRemainingRiskBudget(double remainingRiskBudget) { this.remainingRiskBudget = remainingRiskBudget; }

    public Double getPortfolioVolatility() { return portfolioVolatility; }
    public void setPortfolioVolatility(Double portfolioVolatility) { this.portfolioVolatility = portfolioVolatility; }

    public double getCurrentDrawdown() { return currentDrawdown; }
    public void setCurrentDrawdown(double currentDrawdown) { this.currentDrawdown = currentDrawdown; }

    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
