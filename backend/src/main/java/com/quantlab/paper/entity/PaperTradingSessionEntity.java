package com.quantlab.paper.entity;

import com.quantlab.paper.model.ClockType;
import com.quantlab.paper.model.DataFreshnessStatus;
import com.quantlab.paper.model.ExecutionMode;
import com.quantlab.paper.model.PaperTradingStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_trading_sessions")
public class PaperTradingSessionEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_mode", nullable = false, length = 32)
    private ExecutionMode executionMode = ExecutionMode.PAPER_TRADING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaperTradingStatus status = PaperTradingStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "clock_type", nullable = false, length = 32)
    private ClockType clockType = ClockType.LIVE_CLOCK;

    @Column(name = "data_provider", nullable = false, length = 64)
    private String dataProvider = "AUTHORIZED_FEED";

    @Enumerated(EnumType.STRING)
    @Column(name = "data_freshness_status", nullable = false, length = 32)
    private DataFreshnessStatus dataFreshnessStatus = DataFreshnessStatus.UNKNOWN;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "configuration_version", nullable = false, length = 32)
    private String configurationVersion = "v1.0.0";

    @Column(name = "engine_version", nullable = false, length = 32)
    private String engineVersion = "v1.0.0";

    @Column(name = "total_decisions_count", nullable = false)
    private Integer totalDecisionsCount = 0;

    @Column(name = "total_orders_count", nullable = false)
    private Integer totalOrdersCount = 0;

    @Column(name = "total_fills_count", nullable = false)
    private Integer totalFillsCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ExecutionMode getExecutionMode() { return executionMode; }
    public void setExecutionMode(ExecutionMode executionMode) { this.executionMode = executionMode; }

    public PaperTradingStatus getStatus() { return status; }
    public void setStatus(PaperTradingStatus status) { this.status = status; }

    public ClockType getClockType() { return clockType; }
    public void setClockType(ClockType clockType) { this.clockType = clockType; }

    public String getDataProvider() { return dataProvider; }
    public void setDataProvider(String dataProvider) { this.dataProvider = dataProvider; }

    public DataFreshnessStatus getDataFreshnessStatus() { return dataFreshnessStatus; }
    public void setDataFreshnessStatus(DataFreshnessStatus dataFreshnessStatus) { this.dataFreshnessStatus = dataFreshnessStatus; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public String getConfigurationVersion() { return configurationVersion; }
    public void setConfigurationVersion(String configurationVersion) { this.configurationVersion = configurationVersion; }

    public String getEngineVersion() { return engineVersion; }
    public void setEngineVersion(String engineVersion) { this.engineVersion = engineVersion; }

    public Integer getTotalDecisionsCount() { return totalDecisionsCount; }
    public void setTotalDecisionsCount(Integer totalDecisionsCount) { this.totalDecisionsCount = totalDecisionsCount; }

    public Integer getTotalOrdersCount() { return totalOrdersCount; }
    public void setTotalOrdersCount(Integer totalOrdersCount) { this.totalOrdersCount = totalOrdersCount; }

    public Integer getTotalFillsCount() { return totalFillsCount; }
    public void setTotalFillsCount(Integer totalFillsCount) { this.totalFillsCount = totalFillsCount; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
