package com.quantlab.news.entity;

import com.quantlab.news.model.SourceCredibility;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "news_sources",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_news_src_name", columnList = "source_name", unique = true)
    }
)
public class NewsSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, unique = true, length = 64)
    private String sourceName;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "credibility_level", nullable = false, length = 32)
    private SourceCredibility credibility = SourceCredibility.GENERAL_NEWS;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "base_url", length = 255)
    private String baseUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public NewsSource() {}

    public NewsSource(String sourceName, String sourceType, SourceCredibility credibility, boolean enabled, String baseUrl) {
        this.sourceName = sourceName;
        this.sourceType = sourceType;
        this.credibility = credibility;
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public SourceCredibility getCredibility() { return credibility; }
    public void setCredibility(SourceCredibility credibility) { this.credibility = credibility; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
