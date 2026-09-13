package com.quantlab.marketdata.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "market_data_sources",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_source_name", columnList = "source_name", unique = true)
    }
)
public class MarketDataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, unique = true, length = 64)
    private String sourceName;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType; // "EXCHANGE", "VENDOR", "MOCK"

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;

    @Column(name = "rate_limit_per_min")
    private Integer rateLimitPerMinute = 60;

    @Column(name = "base_url", length = 255)
    private String baseUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    public MarketDataSource() {}

    public MarketDataSource(String sourceName, String sourceType, boolean enabled, boolean isPrimary, Integer rateLimitPerMinute, String baseUrl) {
        this.sourceName = sourceName;
        this.sourceType = sourceType;
        this.enabled = enabled;
        this.isPrimary = isPrimary;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.baseUrl = baseUrl;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }
    public Integer getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(Integer rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
