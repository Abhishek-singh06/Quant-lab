package com.quantlab.global.entity;

import com.quantlab.global.model.DataFreshness;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "global_market_sources", schema = "market_data")
public class GlobalMarketSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, unique = true, length = 64)
    private String sourceName;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_freshness", nullable = false, length = 32)
    private DataFreshness defaultFreshness = DataFreshness.DELAYED;

    @Column(name = "delay_minutes")
    private Integer delayMinutes = 15;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public GlobalMarketSource() {}

    public GlobalMarketSource(String sourceName, String sourceType, DataFreshness defaultFreshness,
                              Integer delayMinutes, boolean active, String baseUrl) {
        this.sourceName = sourceName;
        this.sourceType = sourceType;
        this.defaultFreshness = defaultFreshness;
        this.delayMinutes = delayMinutes;
        this.active = active;
        this.baseUrl = baseUrl;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public DataFreshness getDefaultFreshness() { return defaultFreshness; }
    public void setDefaultFreshness(DataFreshness defaultFreshness) { this.defaultFreshness = defaultFreshness; }
    public Integer getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(Integer delayMinutes) { this.delayMinutes = delayMinutes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
