package com.quantlab.news.entity;

import com.quantlab.news.model.ArticleProcessingStatus;
import com.quantlab.news.model.SentimentLabel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "news_articles",
    schema = "market_data",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_article_content_hash", columnNames = {"content_hash"})
    },
    indexes = {
        @Index(name = "idx_news_info_avail", columnList = "information_available_at"),
        @Index(name = "idx_news_published_at", columnList = "published_at"),
        @Index(name = "idx_news_source_id", columnList = "source_id"),
        @Index(name = "idx_news_cluster_id", columnList = "cluster_id")
    }
)
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "source_name", nullable = false, length = 64)
    private String sourceName;

    @Column(nullable = false, length = 512)
    private String title;

    @Column(length = 2048)
    private String description;

    @Column(name = "url", length = 1024)
    private String url;

    @Column(name = "canonical_url", length = 1024)
    private String canonicalUrl;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "information_available_at", nullable = false)
    private Instant informationAvailableAt;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt = Instant.now();

    @Column(length = 16)
    private String language = "en";

    @Column(length = 128)
    private String author;

    @Column(name = "importance_score", precision = 5, scale = 4)
    private BigDecimal importanceScore = BigDecimal.valueOf(0.5);

    @Column(name = "sentiment_score", precision = 5, scale = 4)
    private BigDecimal sentimentScore = BigDecimal.ZERO;

    @Column(name = "financial_impact_score", precision = 5, scale = 4)
    private BigDecimal financialImpactScore = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment_label", length = 32)
    private SentimentLabel sentimentLabel = SentimentLabel.NEUTRAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ArticleProcessingStatus status = ArticleProcessingStatus.PROCESSED;

    @Column(name = "cluster_id", length = 64)
    private String clusterId;

    @Column(name = "duplicate_of_id")
    private Long duplicateOfId;

    public NewsArticle() {}

    public NewsArticle(Long sourceId, String sourceName, String title, String description, String url,
                       String contentHash, Instant publishedAt, Instant informationAvailableAt) {
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.title = title;
        this.description = description;
        this.url = url;
        this.contentHash = contentHash;
        this.publishedAt = publishedAt;
        this.informationAvailableAt = informationAvailableAt != null ? informationAvailableAt : publishedAt;
        this.ingestedAt = Instant.now();
        this.status = ArticleProcessingStatus.PROCESSED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getCanonicalUrl() { return canonicalUrl; }
    public void setCanonicalUrl(String canonicalUrl) { this.canonicalUrl = canonicalUrl; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }
    public Instant getIngestedAt() { return ingestedAt; }
    public void setIngestedAt(Instant ingestedAt) { this.ingestedAt = ingestedAt; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public BigDecimal getImportanceScore() { return importanceScore; }
    public void setImportanceScore(BigDecimal importanceScore) { this.importanceScore = importanceScore; }
    public BigDecimal getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(BigDecimal sentimentScore) { this.sentimentScore = sentimentScore; }
    public BigDecimal getFinancialImpactScore() { return financialImpactScore; }
    public void setFinancialImpactScore(BigDecimal financialImpactScore) { this.financialImpactScore = financialImpactScore; }
    public SentimentLabel getSentimentLabel() { return sentimentLabel; }
    public void setSentimentLabel(SentimentLabel sentimentLabel) { this.sentimentLabel = sentimentLabel; }
    public ArticleProcessingStatus getStatus() { return status; }
    public void setStatus(ArticleProcessingStatus status) { this.status = status; }
    public String getClusterId() { return clusterId; }
    public void setClusterId(String clusterId) { this.clusterId = clusterId; }
    public Long getDuplicateOfId() { return duplicateOfId; }
    public void setDuplicateOfId(Long duplicateOfId) { this.duplicateOfId = duplicateOfId; }
}
