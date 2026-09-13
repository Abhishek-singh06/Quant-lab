package com.quantlab.risk.model;

import java.time.Instant;
import java.util.UUID;

public class PortfolioPositionDTO {
    private UUID id;
    private UUID portfolioId;
    private Long instrumentId;
    private String symbol;
    private String sector;
    private String industry;
    private double quantity;
    private double averageEntryPrice;
    private double currentPrice;
    private double marketValue;
    private double weight;
    private Double currentStopPrice;
    private Double positionRiskAmount;
    private Double positionRiskPercent;
    private Instant entryTimestamp;
    private Instant lastUpdatedTimestamp;
    private boolean active;
    private Instant createdAt;

    public PortfolioPositionDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getAverageEntryPrice() { return averageEntryPrice; }
    public void setAverageEntryPrice(double averageEntryPrice) { this.averageEntryPrice = averageEntryPrice; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getMarketValue() { return marketValue; }
    public void setMarketValue(double marketValue) { this.marketValue = marketValue; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public Double getCurrentStopPrice() { return currentStopPrice; }
    public void setCurrentStopPrice(Double currentStopPrice) { this.currentStopPrice = currentStopPrice; }

    public Double getPositionRiskAmount() { return positionRiskAmount; }
    public void setPositionRiskAmount(Double positionRiskAmount) { this.positionRiskAmount = positionRiskAmount; }

    public Double getPositionRiskPercent() { return positionRiskPercent; }
    public void setPositionRiskPercent(Double positionRiskPercent) { this.positionRiskPercent = positionRiskPercent; }

    public Instant getEntryTimestamp() { return entryTimestamp; }
    public void setEntryTimestamp(Instant entryTimestamp) { this.entryTimestamp = entryTimestamp; }

    public Instant getLastUpdatedTimestamp() { return lastUpdatedTimestamp; }
    public void setLastUpdatedTimestamp(Instant lastUpdatedTimestamp) { this.lastUpdatedTimestamp = lastUpdatedTimestamp; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
