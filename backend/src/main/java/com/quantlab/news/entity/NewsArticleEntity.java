package com.quantlab.news.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "news_article_entities",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_nae_article_id", columnList = "article_id"),
        @Index(name = "idx_nae_instrument_id", columnList = "instrument_id"),
        @Index(name = "idx_nae_symbol", columnList = "symbol")
    }
)
public class NewsArticleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidence = BigDecimal.valueOf(1.0);

    @Column(name = "match_type", length = 32)
    private String matchType; // "EXACT_TICKER", "COMPANY_NAME", "HISTORICAL_ALIAS"

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public NewsArticleEntity() {}

    public NewsArticleEntity(Long articleId, Long instrumentId, String symbol, BigDecimal confidence, String matchType) {
        this.articleId = articleId;
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.confidence = confidence;
        this.matchType = matchType;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
