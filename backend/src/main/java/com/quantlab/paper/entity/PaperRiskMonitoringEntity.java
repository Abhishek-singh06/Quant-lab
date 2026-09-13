package com.quantlab.paper.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_risk_monitoring")
public class PaperRiskMonitoringEntity {

    @Id
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(name = "evaluation_timestamp", nullable = false)
    private Instant evaluationTimestamp;

    @Column(name = "portfolio_volatility", nullable = false)
    private Double portfolioVolatility;

    @Column(name = "max_sector_concentration", nullable = false)
    private Double maxSectorConcentration;

    @Column(name = "highest_concentration_sector", length = 64)
    private String highestConcentrationSector;

    @Column(name = "current_drawdown_pct", nullable = false)
    private Double currentDrawdownPct;

    @Column(nullable = false)
    private Double leverage = 1.0;

    @Column(name = "cash_buffer_pct", nullable = false)
    private Double cashBufferPct;

    @Column(name = "is_risk_breached", nullable = false)
    private Boolean isRiskBreached = false;

    @Column(name = "breach_reason", columnDefinition = "TEXT")
    private String breachReason;

    @Column(name = "risk_action_taken", length = 32)
    private String riskActionTaken = "NONE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public Instant getEvaluationTimestamp() { return evaluationTimestamp; }
    public void setEvaluationTimestamp(Instant evaluationTimestamp) { this.evaluationTimestamp = evaluationTimestamp; }

    public Double getPortfolioVolatility() { return portfolioVolatility; }
    public void setPortfolioVolatility(Double portfolioVolatility) { this.portfolioVolatility = portfolioVolatility; }

    public Double getMaxSectorConcentration() { return maxSectorConcentration; }
    public void setMaxSectorConcentration(Double maxSectorConcentration) { this.maxSectorConcentration = maxSectorConcentration; }

    public String getHighestConcentrationSector() { return highestConcentrationSector; }
    public void setHighestConcentrationSector(String highestConcentrationSector) { this.highestConcentrationSector = highestConcentrationSector; }

    public Double getCurrentDrawdownPct() { return currentDrawdownPct; }
    public void setCurrentDrawdownPct(Double currentDrawdownPct) { this.currentDrawdownPct = currentDrawdownPct; }

    public Double getLeverage() { return leverage; }
    public void setLeverage(Double leverage) { this.leverage = leverage; }

    public Double getCashBufferPct() { return cashBufferPct; }
    public void setCashBufferPct(Double cashBufferPct) { this.cashBufferPct = cashBufferPct; }

    public Boolean getIsRiskBreached() { return isRiskBreached; }
    public void setIsRiskBreached(Boolean isRiskBreached) { this.isRiskBreached = isRiskBreached; }

    public String getBreachReason() { return breachReason; }
    public void setBreachReason(String breachReason) { this.breachReason = breachReason; }

    public String getRiskActionTaken() { return riskActionTaken; }
    public void setRiskActionTaken(String riskActionTaken) { this.riskActionTaken = riskActionTaken; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
