package com.quantlab.news.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "corporate_event_entities",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_cee_event_id", columnList = "event_id"),
        @Index(name = "idx_cee_instrument_id", columnList = "instrument_id")
    }
)
public class CorporateEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(length = 32)
    private String role; // "PRIMARY_ISSUER", "ACQUIRER", "TARGET", "COUNTERPARTY"

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public CorporateEventEntity() {}

    public CorporateEventEntity(Long eventId, Long instrumentId, String symbol, String role) {
        this.eventId = eventId;
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
