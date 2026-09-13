package com.quantlab.fundamental.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "fundamental_sources", schema = "market_data")
public class FundamentalSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, unique = true, length = 64)
    private String sourceName;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public FundamentalSource() {}

    public FundamentalSource(String sourceName, String sourceType, boolean active, String baseUrl) {
        this.sourceName = sourceName;
        this.sourceType = sourceType;
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
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
