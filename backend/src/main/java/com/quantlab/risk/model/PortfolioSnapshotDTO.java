package com.quantlab.risk.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class PortfolioSnapshotDTO {
    private UUID id;
    private UUID portfolioId;
    private Instant snapshotTimestamp;
    private double portfolioValue;
    private double cash;
    private double totalPositionsValue;
    private int positionCount;
    private String positionsJson;
    private String sectorExposureJson;
    private double consumedRiskBudget;
    private double remainingRiskBudget;
    private Double portfolioVolatility;
    private double currentDrawdown;
    private String dataVersion;
    private Instant createdAt;

    public PortfolioSnapshotDTO() {}

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
