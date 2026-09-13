package com.quantlab.paper.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_portfolios")
public class PaperPortfolioEntity {

    @Id
    private UUID id;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 32)
    private String horizon = "SHORT_TERM";

    @Column(name = "risk_profile_id")
    private UUID riskProfileId;

    @Column(nullable = false, length = 10)
    private String currency = "INR";

    @Column(name = "initial_virtual_capital", nullable = false)
    private Double initialVirtualCapital = 1000000.0;

    @Column(name = "cash_balance", nullable = false)
    private Double cashBalance;

    @Column(name = "available_cash", nullable = false)
    private Double availableCash;

    @Column(name = "reserved_cash", nullable = false)
    private Double reservedCash = 0.0;

    @Column(name = "invested_value", nullable = false)
    private Double investedValue = 0.0;

    @Column(name = "total_portfolio_value", nullable = false)
    private Double totalPortfolioValue;

    @Column(name = "peak_portfolio_value", nullable = false)
    private Double peakPortfolioValue;

    @Column(name = "current_drawdown_pct", nullable = false)
    private Double currentDrawdownPct = 0.0;

    @Column(name = "max_drawdown_pct", nullable = false)
    private Double maxDrawdownPct = 0.0;

    @Column(name = "gross_exposure", nullable = false)
    private Double grossExposure = 0.0;

    @Column(name = "net_exposure", nullable = false)
    private Double netExposure = 0.0;

    @Column(nullable = false)
    private Double leverage = 1.0;

    @Column(name = "total_realized_pnl", nullable = false)
    private Double totalRealizedPnl = 0.0;

    @Column(name = "total_unrealized_pnl", nullable = false)
    private Double totalUnrealizedPnl = 0.0;

    @Column(name = "total_fees_paid", nullable = false)
    private Double totalFeesPaid = 0.0;

    @Column(name = "total_slippage_paid", nullable = false)
    private Double totalSlippagePaid = 0.0;

    @Column(name = "total_dividends_received", nullable = false)
    private Double totalDividendsReceived = 0.0;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHorizon() { return horizon; }
    public void setHorizon(String horizon) { this.horizon = horizon; }

    public UUID getRiskProfileId() { return riskProfileId; }
    public void setRiskProfileId(UUID riskProfileId) { this.riskProfileId = riskProfileId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Double getInitialVirtualCapital() { return initialVirtualCapital; }
    public void setInitialVirtualCapital(Double initialVirtualCapital) { this.initialVirtualCapital = initialVirtualCapital; }

    public Double getCashBalance() { return cashBalance; }
    public void setCashBalance(Double cashBalance) { this.cashBalance = cashBalance; }

    public Double getAvailableCash() { return availableCash; }
    public void setAvailableCash(Double availableCash) { this.availableCash = availableCash; }

    public Double getReservedCash() { return reservedCash; }
    public void setReservedCash(Double reservedCash) { this.reservedCash = reservedCash; }

    public Double getInvestedValue() { return investedValue; }
    public void setInvestedValue(Double investedValue) { this.investedValue = investedValue; }

    public Double getTotalPortfolioValue() { return totalPortfolioValue; }
    public void setTotalPortfolioValue(Double totalPortfolioValue) { this.totalPortfolioValue = totalPortfolioValue; }

    public Double getPeakPortfolioValue() { return peakPortfolioValue; }
    public void setPeakPortfolioValue(Double peakPortfolioValue) { this.peakPortfolioValue = peakPortfolioValue; }

    public Double getCurrentDrawdownPct() { return currentDrawdownPct; }
    public void setCurrentDrawdownPct(Double currentDrawdownPct) { this.currentDrawdownPct = currentDrawdownPct; }

    public Double getMaxDrawdownPct() { return maxDrawdownPct; }
    public void setMaxDrawdownPct(Double maxDrawdownPct) { this.maxDrawdownPct = maxDrawdownPct; }

    public Double getGrossExposure() { return grossExposure; }
    public void setGrossExposure(Double grossExposure) { this.grossExposure = grossExposure; }

    public Double getNetExposure() { return netExposure; }
    public void setNetExposure(Double netExposure) { this.netExposure = netExposure; }

    public Double getLeverage() { return leverage; }
    public void setLeverage(Double leverage) { this.leverage = leverage; }

    public Double getTotalRealizedPnl() { return totalRealizedPnl; }
    public void setTotalRealizedPnl(Double totalRealizedPnl) { this.totalRealizedPnl = totalRealizedPnl; }

    public Double getTotalUnrealizedPnl() { return totalUnrealizedPnl; }
    public void setTotalUnrealizedPnl(Double totalUnrealizedPnl) { this.totalUnrealizedPnl = totalUnrealizedPnl; }

    public Double getTotalFeesPaid() { return totalFeesPaid; }
    public void setTotalFeesPaid(Double totalFeesPaid) { this.totalFeesPaid = totalFeesPaid; }

    public Double getTotalSlippagePaid() { return totalSlippagePaid; }
    public void setTotalSlippagePaid(Double totalSlippagePaid) { this.totalSlippagePaid = totalSlippagePaid; }

    public Double getTotalDividendsReceived() { return totalDividendsReceived; }
    public void setTotalDividendsReceived(Double totalDividendsReceived) { this.totalDividendsReceived = totalDividendsReceived; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
