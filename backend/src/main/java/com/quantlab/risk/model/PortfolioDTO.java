package com.quantlab.risk.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PortfolioDTO {
    private UUID id;
    private String name;
    private String currency;
    private UUID riskProfileId;
    private double currentCash;
    private double currentPortfolioValue;
    private double peakPortfolioValue;
    private Instant peakTimestamp;
    private double currentDrawdown;
    private double maxDrawdown;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PortfolioPositionDTO> positions;

    public PortfolioDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public UUID getRiskProfileId() { return riskProfileId; }
    public void setRiskProfileId(UUID riskProfileId) { this.riskProfileId = riskProfileId; }

    public double getCurrentCash() { return currentCash; }
    public void setCurrentCash(double currentCash) { this.currentCash = currentCash; }

    public double getCurrentPortfolioValue() { return currentPortfolioValue; }
    public void setCurrentPortfolioValue(double currentPortfolioValue) { this.currentPortfolioValue = currentPortfolioValue; }

    public double getPeakPortfolioValue() { return peakPortfolioValue; }
    public void setPeakPortfolioValue(double peakPortfolioValue) { this.peakPortfolioValue = peakPortfolioValue; }

    public Instant getPeakTimestamp() { return peakTimestamp; }
    public void setPeakTimestamp(Instant peakTimestamp) { this.peakTimestamp = peakTimestamp; }

    public double getCurrentDrawdown() { return currentDrawdown; }
    public void setCurrentDrawdown(double currentDrawdown) { this.currentDrawdown = currentDrawdown; }

    public double getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(double maxDrawdown) { this.maxDrawdown = maxDrawdown; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<PortfolioPositionDTO> getPositions() { return positions; }
    public void setPositions(List<PortfolioPositionDTO> positions) { this.positions = positions; }
}
