package com.quantlab.warehouse.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "historical_data_quality",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_hist_dq_inst", columnList = "instrument_id"),
        @Index(name = "idx_hist_dq_run", columnList = "run_id")
    }
)
public class HistoricalDataQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", length = 64)
    private String runId;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "expected_days", nullable = false)
    private long expectedDays;

    @Column(name = "actual_days", nullable = false)
    private long actualDays;

    @Column(name = "missing_days", nullable = false)
    private long missingDays;

    @Column(name = "duplicate_records", nullable = false)
    private long duplicateRecords;

    @Column(nullable = false, length = 32)
    private String status; // "CLEAN", "WARNING", "GAPS_DETECTED"

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt = Instant.now();

    public HistoricalDataQuality() {}

    public HistoricalDataQuality(String runId, Long instrumentId, String symbol, LocalDate fromDate, LocalDate toDate,
                                 long expectedDays, long actualDays, long missingDays, long duplicateRecords, String status) {
        this.runId = runId;
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.expectedDays = expectedDays;
        this.actualDays = actualDays;
        this.missingDays = missingDays;
        this.duplicateRecords = duplicateRecords;
        this.status = status;
        this.checkedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public long getExpectedDays() { return expectedDays; }
    public void setExpectedDays(long expectedDays) { this.expectedDays = expectedDays; }
    public long getActualDays() { return actualDays; }
    public void setActualDays(long actualDays) { this.actualDays = actualDays; }
    public long getMissingDays() { return missingDays; }
    public void setMissingDays(long missingDays) { this.missingDays = missingDays; }
    public long getDuplicateRecords() { return duplicateRecords; }
    public void setDuplicateRecords(long duplicateRecords) { this.duplicateRecords = duplicateRecords; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}
