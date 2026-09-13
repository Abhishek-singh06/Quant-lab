package com.quantlab.backtest.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "backtest_trades")
public class BacktestTradeEntity {

    @Id
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 10)
    private String side;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "entry_order_id")
    private UUID entryOrderId;

    @Column(name = "exit_order_id")
    private UUID exitOrderId;

    @Column(name = "entry_timestamp", nullable = false)
    private OffsetDateTime entryTimestamp;

    @Column(name = "exit_timestamp", nullable = false)
    private OffsetDateTime exitTimestamp;

    @Column(name = "entry_price", nullable = false)
    private Double entryPrice;

    @Column(name = "exit_price", nullable = false)
    private Double exitPrice;

    @Column(name = "gross_pnl", nullable = false)
    private Double grossPnl;

    @Column(name = "net_pnl", nullable = false)
    private Double netPnl;

    @Column(name = "return_pct", nullable = false)
    private Double returnPct;

    @Column(name = "total_fees", nullable = false)
    private Double totalFees;

    @Column(name = "total_slippage", nullable = false)
    private Double totalSlippage;

    @Column(name = "holding_period_days", nullable = false)
    private Integer holdingPeriodDays;

    @Column(name = "exit_reason", nullable = false, length = 32)
    private String exitReason;

    @Column(name = "max_favorable_excursion")
    private Double maxFavorableExcursion;

    @Column(name = "max_adverse_excursion")
    private Double maxAdverseExcursion;

    @Column(name = "regime_at_entry", length = 32)
    private String regimeAtEntry;

    @Column(name = "regime_at_exit", length = 32)
    private String regimeAtExit;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public BacktestTradeEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public UUID getEntryOrderId() { return entryOrderId; }
    public void setEntryOrderId(UUID entryOrderId) { this.entryOrderId = entryOrderId; }
    public UUID getExitOrderId() { return exitOrderId; }
    public void setExitOrderId(UUID exitOrderId) { this.exitOrderId = exitOrderId; }
    public OffsetDateTime getEntryTimestamp() { return entryTimestamp; }
    public void setEntryTimestamp(OffsetDateTime entryTimestamp) { this.entryTimestamp = entryTimestamp; }
    public OffsetDateTime getExitTimestamp() { return exitTimestamp; }
    public void setExitTimestamp(OffsetDateTime exitTimestamp) { this.exitTimestamp = exitTimestamp; }
    public Double getEntryPrice() { return entryPrice; }
    public void setEntryPrice(Double entryPrice) { this.entryPrice = entryPrice; }
    public Double getExitPrice() { return exitPrice; }
    public void setExitPrice(Double exitPrice) { this.exitPrice = exitPrice; }
    public Double getGrossPnl() { return grossPnl; }
    public void setGrossPnl(Double grossPnl) { this.grossPnl = grossPnl; }
    public Double getNetPnl() { return netPnl; }
    public void setNetPnl(Double netPnl) { this.netPnl = netPnl; }
    public Double getReturnPct() { return returnPct; }
    public void setReturnPct(Double returnPct) { this.returnPct = returnPct; }
    public Double getTotalFees() { return totalFees; }
    public void setTotalFees(Double totalFees) { this.totalFees = totalFees; }
    public Double getTotalSlippage() { return totalSlippage; }
    public void setTotalSlippage(Double totalSlippage) { this.totalSlippage = totalSlippage; }
    public Integer getHoldingPeriodDays() { return holdingPeriodDays; }
    public void setHoldingPeriodDays(Integer holdingPeriodDays) { this.holdingPeriodDays = holdingPeriodDays; }
    public String getExitReason() { return exitReason; }
    public void setExitReason(String exitReason) { this.exitReason = exitReason; }
    public Double getMaxFavorableExcursion() { return maxFavorableExcursion; }
    public void setMaxFavorableExcursion(Double maxFavorableExcursion) { this.maxFavorableExcursion = maxFavorableExcursion; }
    public Double getMaxAdverseExcursion() { return maxAdverseExcursion; }
    public void setMaxAdverseExcursion(Double maxAdverseExcursion) { this.maxAdverseExcursion = maxAdverseExcursion; }
    public String getRegimeAtEntry() { return regimeAtEntry; }
    public void setRegimeAtEntry(String regimeAtEntry) { this.regimeAtEntry = regimeAtEntry; }
    public String getRegimeAtExit() { return regimeAtExit; }
    public void setRegimeAtExit(String regimeAtExit) { this.regimeAtExit = regimeAtExit; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
