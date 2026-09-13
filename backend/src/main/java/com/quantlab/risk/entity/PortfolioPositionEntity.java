package com.quantlab.risk.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portfolio_positions")
public class PortfolioPositionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private PortfolioEntity portfolio;

    @Column(name = "instrument_id")
    private Long instrumentId;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "sector", length = 64)
    private String sector;

    @Column(name = "industry", length = 64)
    private String industry;

    @Column(name = "quantity", nullable = false)
    private double quantity;

    @Column(name = "average_entry_price", nullable = false)
    private double averageEntryPrice;

    @Column(name = "current_price", nullable = false)
    private double currentPrice;

    @Column(name = "market_value", nullable = false)
    private double marketValue;

    @Column(name = "weight", nullable = false)
    private double weight;

    @Column(name = "current_stop_price")
    private Double currentStopPrice;

    @Column(name = "position_risk_amount")
    private Double positionRiskAmount;

    @Column(name = "position_risk_percent")
    private Double positionRiskPercent;

    @Column(name = "entry_timestamp", nullable = false)
    private Instant entryTimestamp;

    @Column(name = "last_updated_timestamp", nullable = false)
    private Instant lastUpdatedTimestamp;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public PortfolioPositionEntity() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (entryTimestamp == null) entryTimestamp = Instant.now();
        if (lastUpdatedTimestamp == null) lastUpdatedTimestamp = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdatedTimestamp = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public PortfolioEntity getPortfolio() { return portfolio; }
    public void setPortfolio(PortfolioEntity portfolio) { this.portfolio = portfolio; }

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
