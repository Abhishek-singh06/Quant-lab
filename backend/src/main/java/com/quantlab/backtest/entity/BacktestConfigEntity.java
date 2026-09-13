package com.quantlab.backtest.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "backtest_configs")
public class BacktestConfigEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 32)
    private String horizon;

    @Column(name = "universe_type", nullable = false, length = 32)
    private String universeType;

    @Column(columnDefinition = "JSONB", nullable = false)
    private String symbols;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "initial_capital", nullable = false)
    private Double initialCapital;

    @Column(name = "cash_buffer_pct", nullable = false)
    private Double cashBufferPct;

    @Column(name = "rebalance_frequency", nullable = false, length = 32)
    private String rebalanceFrequency;

    @Column(name = "execution_timing", nullable = false, length = 32)
    private String executionTiming;

    @Column(name = "cost_model_type", nullable = false, length = 32)
    private String costModelType;

    @Column(name = "slippage_model_type", nullable = false, length = 32)
    private String slippageModelType;

    @Column(name = "brokerage_bps", nullable = false)
    private Double brokerageBps;

    @Column(name = "stt_delivery_bps", nullable = false)
    private Double sttDeliveryBps;

    @Column(name = "stt_intraday_bps", nullable = false)
    private Double sttIntradayBps;

    @Column(name = "exchange_charges_bps", nullable = false)
    private Double exchangeChargesBps;

    @Column(name = "gst_rate", nullable = false)
    private Double gstRate;

    @Column(name = "stamp_duty_bps", nullable = false)
    private Double stampDutyBps;

    @Column(name = "slippage_bps", nullable = false)
    private Double slippageBps;

    @Column(name = "max_position_weight", nullable = false)
    private Double maxPositionWeight;

    @Column(name = "max_sector_weight", nullable = false)
    private Double maxSectorWeight;

    @Column(name = "max_drawdown_limit", nullable = false)
    private Double maxDrawdownLimit;

    @Column(name = "benchmark_symbol", nullable = false, length = 32)
    private String benchmarkSymbol;

    @Column(nullable = false, length = 32)
    private String version;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public BacktestConfigEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHorizon() { return horizon; }
    public void setHorizon(String horizon) { this.horizon = horizon; }
    public String getUniverseType() { return universeType; }
    public void setUniverseType(String universeType) { this.universeType = universeType; }
    public String getSymbols() { return symbols; }
    public void setSymbols(String symbols) { this.symbols = symbols; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Double getInitialCapital() { return initialCapital; }
    public void setInitialCapital(Double initialCapital) { this.initialCapital = initialCapital; }
    public Double getCashBufferPct() { return cashBufferPct; }
    public void setCashBufferPct(Double cashBufferPct) { this.cashBufferPct = cashBufferPct; }
    public String getRebalanceFrequency() { return rebalanceFrequency; }
    public void setRebalanceFrequency(String rebalanceFrequency) { this.rebalanceFrequency = rebalanceFrequency; }
    public String getExecutionTiming() { return executionTiming; }
    public void setExecutionTiming(String executionTiming) { this.executionTiming = executionTiming; }
    public String getCostModelType() { return costModelType; }
    public void setCostModelType(String costModelType) { this.costModelType = costModelType; }
    public String getSlippageModelType() { return slippageModelType; }
    public void setSlippageModelType(String slippageModelType) { this.slippageModelType = slippageModelType; }
    public Double getBrokerageBps() { return brokerageBps; }
    public void setBrokerageBps(Double brokerageBps) { this.brokerageBps = brokerageBps; }
    public Double getSttDeliveryBps() { return sttDeliveryBps; }
    public void setSttDeliveryBps(Double sttDeliveryBps) { this.sttDeliveryBps = sttDeliveryBps; }
    public Double getSttIntradayBps() { return sttIntradayBps; }
    public void setSttIntradayBps(Double sttIntradayBps) { this.sttIntradayBps = sttIntradayBps; }
    public Double getExchangeChargesBps() { return exchangeChargesBps; }
    public void setExchangeChargesBps(Double exchangeChargesBps) { this.exchangeChargesBps = exchangeChargesBps; }
    public Double getGstRate() { return gstRate; }
    public void setGstRate(Double gstRate) { this.gstRate = gstRate; }
    public Double getStampDutyBps() { return stampDutyBps; }
    public void setStampDutyBps(Double stampDutyBps) { this.stampDutyBps = stampDutyBps; }
    public Double getSlippageBps() { return slippageBps; }
    public void setSlippageBps(Double slippageBps) { this.slippageBps = slippageBps; }
    public Double getMaxPositionWeight() { return maxPositionWeight; }
    public void setMaxPositionWeight(Double maxPositionWeight) { this.maxPositionWeight = maxPositionWeight; }
    public Double getMaxSectorWeight() { return maxSectorWeight; }
    public void setMaxSectorWeight(Double maxSectorWeight) { this.maxSectorWeight = maxSectorWeight; }
    public Double getMaxDrawdownLimit() { return maxDrawdownLimit; }
    public void setMaxDrawdownLimit(Double maxDrawdownLimit) { this.maxDrawdownLimit = maxDrawdownLimit; }
    public String getBenchmarkSymbol() { return benchmarkSymbol; }
    public void setBenchmarkSymbol(String benchmarkSymbol) { this.benchmarkSymbol = benchmarkSymbol; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
