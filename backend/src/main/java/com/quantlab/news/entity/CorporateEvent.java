package com.quantlab.news.entity;

import com.quantlab.news.model.EventType;
import com.quantlab.news.model.SentimentLabel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "corporate_events",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_corp_evt_inst", columnList = "instrument_id"),
        @Index(name = "idx_corp_evt_symbol", columnList = "symbol"),
        @Index(name = "idx_corp_evt_info_avail", columnList = "information_available_at"),
        @Index(name = "idx_corp_evt_type", columnList = "event_type"),
        @Index(name = "idx_corp_evt_date", columnList = "event_date")
    }
)
public class CorporateEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private EventType eventType;

    @Column(name = "event_subtype", length = 64)
    private String eventSubtype;

    @Column(nullable = false, length = 512)
    private String title;

    @Column(length = 2048)
    private String description;

    @Column(name = "event_date")
    private LocalDate eventDate; // The economic date (e.g. Q1 period end: March 31)

    @Column(length = 32)
    private String period; // e.g. "Q1 2024", "FY2024"

    @Column(name = "announced_at")
    private Instant announcedAt; // When officially disclosed (e.g. May 15 16:30)

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "information_available_at", nullable = false)
    private Instant informationAvailableAt; // MUST be used for historical feature generation!

    @Column(name = "effective_at")
    private Instant effectiveAt;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "source_url", length = 1024)
    private String sourceUrl;

    @Column(name = "importance_score", precision = 5, scale = 4)
    private BigDecimal importanceScore = BigDecimal.valueOf(0.5);

    @Column(name = "financial_impact_score", precision = 5, scale = 4)
    private BigDecimal financialImpactScore = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment_label", length = 32)
    private SentimentLabel sentimentLabel = SentimentLabel.NEUTRAL;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidence = BigDecimal.valueOf(1.0);

    @Column(name = "structured_payload", columnDefinition = "TEXT")
    private String structuredPayload; // JSON string of extracted contract value, revenue, etc.

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "superseded_by_id")
    private Long supersededById;

    @Column(name = "is_cancelled", nullable = false)
    private boolean isCancelled = false;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt = Instant.now();

    public CorporateEvent() {}

    public CorporateEvent(Long instrumentId, String symbol, EventType eventType, String title,
                          String description, LocalDate eventDate, Instant announcedAt,
                          Instant informationAvailableAt, String source, BigDecimal importanceScore,
                          BigDecimal financialImpactScore, SentimentLabel sentimentLabel) {
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.eventType = eventType;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.announcedAt = announcedAt;
        this.informationAvailableAt = informationAvailableAt != null ? informationAvailableAt : (announcedAt != null ? announcedAt : Instant.now());
        this.source = source;
        this.importanceScore = importanceScore;
        this.financialImpactScore = financialImpactScore;
        this.sentimentLabel = sentimentLabel;
        this.ingestedAt = Instant.now();
        this.version = 1;
        this.isCancelled = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public String getEventSubtype() { return eventSubtype; }
    public void setEventSubtype(String eventSubtype) { this.eventSubtype = eventSubtype; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Instant getAnnouncedAt() { return announcedAt; }
    public void setAnnouncedAt(Instant announcedAt) { this.announcedAt = announcedAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }
    public Instant getEffectiveAt() { return effectiveAt; }
    public void setEffectiveAt(Instant effectiveAt) { this.effectiveAt = effectiveAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public BigDecimal getImportanceScore() { return importanceScore; }
    public void setImportanceScore(BigDecimal importanceScore) { this.importanceScore = importanceScore; }
    public BigDecimal getFinancialImpactScore() { return financialImpactScore; }
    public void setFinancialImpactScore(BigDecimal financialImpactScore) { this.financialImpactScore = financialImpactScore; }
    public SentimentLabel getSentimentLabel() { return sentimentLabel; }
    public void setSentimentLabel(SentimentLabel sentimentLabel) { this.sentimentLabel = sentimentLabel; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getStructuredPayload() { return structuredPayload; }
    public void setStructuredPayload(String structuredPayload) { this.structuredPayload = structuredPayload; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public Long getSupersededById() { return supersededById; }
    public void setSupersededById(Long supersededById) { this.supersededById = supersededById; }
    public boolean isCancelled() { return isCancelled; }
    public void setCancelled(boolean cancelled) { isCancelled = cancelled; }
    public Instant getIngestedAt() { return ingestedAt; }
    public void setIngestedAt(Instant ingestedAt) { this.ingestedAt = ingestedAt; }
}
