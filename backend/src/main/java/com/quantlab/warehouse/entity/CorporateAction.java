package com.quantlab.warehouse.entity;

import com.quantlab.warehouse.model.CorporateActionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "corporate_actions",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_corp_act_inst", columnList = "instrument_id"),
        @Index(name = "idx_corp_act_ex_date", columnList = "ex_date"),
        @Index(name = "idx_corp_act_info_avail", columnList = "information_available_at"),
        @Index(name = "idx_corp_act_type", columnList = "action_type")
    }
)
public class CorporateAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 32)
    private CorporateActionType actionType;

    @Column(name = "announcement_date")
    private LocalDate announcementDate;

    @Column(name = "ex_date", nullable = false)
    private LocalDate exDate;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "information_available_at", nullable = false)
    private Instant informationAvailableAt;

    @Column(name = "ratio_numerator", precision = 10, scale = 4)
    private BigDecimal ratioNumerator;

    @Column(name = "ratio_denominator", precision = 10, scale = 4)
    private BigDecimal ratioDenominator;

    @Column(name = "adjustment_factor", precision = 18, scale = 8)
    private BigDecimal adjustmentFactor;

    @Column(name = "dividend_amount", precision = 18, scale = 4)
    private BigDecimal dividendAmount;

    @Column(name = "old_symbol", length = 32)
    private String oldSymbol;

    @Column(name = "new_symbol", length = 32)
    private String newSymbol;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "ingestion_timestamp", nullable = false)
    private Instant ingestionTimestamp = Instant.now();

    public CorporateAction() {}

    public CorporateAction(Long instrumentId, String symbol, CorporateActionType actionType,
                           LocalDate announcementDate, LocalDate exDate, LocalDate recordDate,
                           Instant informationAvailableAt, BigDecimal adjustmentFactor,
                           BigDecimal dividendAmount, String source) {
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.actionType = actionType;
        this.announcementDate = announcementDate;
        this.exDate = exDate;
        this.recordDate = recordDate;
        this.informationAvailableAt = informationAvailableAt != null ? informationAvailableAt : Instant.now();
        this.adjustmentFactor = adjustmentFactor;
        this.dividendAmount = dividendAmount;
        this.source = source;
        this.ingestionTimestamp = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public CorporateActionType getActionType() { return actionType; }
    public void setActionType(CorporateActionType actionType) { this.actionType = actionType; }
    public LocalDate getAnnouncementDate() { return announcementDate; }
    public void setAnnouncementDate(LocalDate announcementDate) { this.announcementDate = announcementDate; }
    public LocalDate getExDate() { return exDate; }
    public void setExDate(LocalDate exDate) { this.exDate = exDate; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }
    public BigDecimal getRatioNumerator() { return ratioNumerator; }
    public void setRatioNumerator(BigDecimal ratioNumerator) { this.ratioNumerator = ratioNumerator; }
    public BigDecimal getRatioDenominator() { return ratioDenominator; }
    public void setRatioDenominator(BigDecimal ratioDenominator) { this.ratioDenominator = ratioDenominator; }
    public BigDecimal getAdjustmentFactor() { return adjustmentFactor; }
    public void setAdjustmentFactor(BigDecimal adjustmentFactor) { this.adjustmentFactor = adjustmentFactor; }
    public BigDecimal getDividendAmount() { return dividendAmount; }
    public void setDividendAmount(BigDecimal dividendAmount) { this.dividendAmount = dividendAmount; }
    public String getOldSymbol() { return oldSymbol; }
    public void setOldSymbol(String oldSymbol) { this.oldSymbol = oldSymbol; }
    public String getNewSymbol() { return newSymbol; }
    public void setNewSymbol(String newSymbol) { this.newSymbol = newSymbol; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getIngestionTimestamp() { return ingestionTimestamp; }
    public void setIngestionTimestamp(Instant ingestionTimestamp) { this.ingestionTimestamp = ingestionTimestamp; }
}
