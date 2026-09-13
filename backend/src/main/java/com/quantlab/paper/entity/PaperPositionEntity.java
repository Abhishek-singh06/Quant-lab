package com.quantlab.paper.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_positions")
public class PaperPositionEntity {

    @Id
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(name = "instrument_id")
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String horizon;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "average_entry_price", nullable = false)
    private Double averageEntryPrice;

    @Column(name = "current_market_price", nullable = false)
    private Double currentMarketPrice;

    @Column(name = "cost_basis", nullable = false)
    private Double costBasis;

    @Column(name = "market_value", nullable = false)
    private Double marketValue;

    @Column(name = "unrealized_pnl", nullable = false)
    private Double unrealizedPnl;

    @Column(name = "unrealized_return_pct", nullable = false)
    private Double unrealizedReturnPct;

    @Column(name = "realized_pnl", nullable = false)
    private Double realizedPnl = 0.0;

    @Column(name = "portfolio_weight", nullable = false)
    private Double portfolioWeight;

    @Column(name = "stop_price")
    private Double stopPrice;

    @Column(name = "target_price")
    private Double targetPrice;

    @Column(name = "stop_method", length = 32)
    private String stopMethod;

    @Column(name = "highest_price_seen", nullable = false)
    private Double highestPriceSeen;

    @Column(name = "lowest_price_seen", nullable = false)
    private Double lowestPriceSeen;

    @Column(name = "entry_timestamp", nullable = false)
    private Instant entryTimestamp;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;

    @Column(name = "signal_id")
    private UUID signalId;

    @Column(name = "risk_assessment_id")
    private UUID riskAssessmentId;

    @Column(name = "model_version", length = 32)
    private String modelVersion;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }

    public String getHorizon() { return horizon; }
    public void setHorizon(String horizon) { this.horizon = horizon; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getAverageEntryPrice() { return averageEntryPrice; }
    public void setAverageEntryPrice(Double averageEntryPrice) { this.averageEntryPrice = averageEntryPrice; }

    public Double getCurrentMarketPrice() { return currentMarketPrice; }
    public void setCurrentMarketPrice(Double currentMarketPrice) { this.currentMarketPrice = currentMarketPrice; }

    public Double getCostBasis() { return costBasis; }
    public void setCostBasis(Double costBasis) { this.costBasis = costBasis; }

    public Double getMarketValue() { return marketValue; }
    public void setMarketValue(Double marketValue) { this.marketValue = marketValue; }

    public Double getUnrealizedPnl() { return unrealizedPnl; }
    public void setUnrealizedPnl(Double unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; }

    public Double getUnrealizedReturnPct() { return unrealizedReturnPct; }
    public void setUnrealizedReturnPct(Double unrealizedReturnPct) { this.unrealizedReturnPct = unrealizedReturnPct; }

    public Double getRealizedPnl() { return realizedPnl; }
    public void setRealizedPnl(Double realizedPnl) { this.realizedPnl = realizedPnl; }

    public Double getPortfolioWeight() { return portfolioWeight; }
    public void setPortfolioWeight(Double portfolioWeight) { this.portfolioWeight = portfolioWeight; }

    public Double getStopPrice() { return stopPrice; }
    public void setStopPrice(Double stopPrice) { this.stopPrice = stopPrice; }

    public Double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(Double targetPrice) { this.targetPrice = targetPrice; }

    public String getStopMethod() { return stopMethod; }
    public void setStopMethod(String stopMethod) { this.stopMethod = stopMethod; }

    public Double getHighestPriceSeen() { return highestPriceSeen; }
    public void setHighestPriceSeen(Double highestPriceSeen) { this.highestPriceSeen = highestPriceSeen; }

    public Double getLowestPriceSeen() { return lowestPriceSeen; }
    public void setLowestPriceSeen(Double lowestPriceSeen) { this.lowestPriceSeen = lowestPriceSeen; }

    public Instant getEntryTimestamp() { return entryTimestamp; }
    public void setEntryTimestamp(Instant entryTimestamp) { this.entryTimestamp = entryTimestamp; }

    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(Instant lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }

    public UUID getSignalId() { return signalId; }
    public void setSignalId(UUID signalId) { this.signalId = signalId; }

    public UUID getRiskAssessmentId() { return riskAssessmentId; }
    public void setRiskAssessmentId(UUID riskAssessmentId) { this.riskAssessmentId = riskAssessmentId; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
