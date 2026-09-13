package com.quantlab.institutional.entity;

import com.quantlab.institutional.model.PortfolioScope;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "fund_portfolio_disclosures",
    schema = "market_data",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_disc_scheme_as_of_version", columnNames = {"scheme_id", "data_as_of", "version"})
    },
    indexes = {
        @Index(name = "idx_disc_scheme_as_of", columnList = "scheme_id, data_as_of"),
        @Index(name = "idx_disc_available_at", columnList = "available_at"),
        @Index(name = "idx_disc_published_at", columnList = "published_at")
    }
)
public class FundPortfolioDisclosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheme_id", nullable = false)
    private Long schemeId;

    @Column(name = "data_as_of", nullable = false)
    private LocalDate dataAsOf; // Date describing the holding snapshot (e.g. 2026-07-31)

    @Column(name = "published_at", nullable = false)
    private LocalDate publishedAt; // Date officially published by AMC/AMFI (e.g. 2026-08-15)

    @Column(name = "available_at", nullable = false)
    private Instant availableAt; // Timestamp when model/market could have observed it (e.g. 2026-08-15T00:00:00Z)

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "portfolio_scope", nullable = false, length = 32)
    private PortfolioScope portfolioScope = PortfolioScope.FULL_PORTFOLIO;

    @Column(name = "total_aum", precision = 20, scale = 4)
    private BigDecimal totalAum;

    @Column(name = "equity_aum", precision = 20, scale = 4)
    private BigDecimal equityAum;

    @Column(name = "equity_holding_percent", precision = 8, scale = 4)
    private BigDecimal equityHoldingPercent;

    @Column(name = "debt_holding_percent", precision = 8, scale = 4)
    private BigDecimal debtHoldingPercent;

    @Column(name = "cash_holding_percent", precision = 8, scale = 4)
    private BigDecimal cashHoldingPercent;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "source_url", length = 1024)
    private String sourceUrl;

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "superseded_by_id")
    private Long supersededById;

    @Column(name = "is_complete", nullable = false)
    private boolean isComplete = true;

    public FundPortfolioDisclosure() {}

    public FundPortfolioDisclosure(Long schemeId, LocalDate dataAsOf, LocalDate publishedAt,
                                   Instant availableAt, PortfolioScope portfolioScope,
                                   BigDecimal totalAum, String source, boolean isComplete) {
        this.schemeId = schemeId;
        this.dataAsOf = dataAsOf;
        this.publishedAt = publishedAt;
        this.availableAt = availableAt != null ? availableAt : publishedAt.atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        this.portfolioScope = portfolioScope;
        this.totalAum = totalAum;
        this.source = source;
        this.isComplete = isComplete;
        this.ingestedAt = Instant.now();
        this.version = 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }
    public LocalDate getDataAsOf() { return dataAsOf; }
    public void setDataAsOf(LocalDate dataAsOf) { this.dataAsOf = dataAsOf; }
    public LocalDate getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDate publishedAt) { this.publishedAt = publishedAt; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
    public Instant getIngestedAt() { return ingestedAt; }
    public void setIngestedAt(Instant ingestedAt) { this.ingestedAt = ingestedAt; }
    public PortfolioScope getPortfolioScope() { return portfolioScope; }
    public void setPortfolioScope(PortfolioScope portfolioScope) { this.portfolioScope = portfolioScope; }
    public BigDecimal getTotalAum() { return totalAum; }
    public void setTotalAum(BigDecimal totalAum) { this.totalAum = totalAum; }
    public BigDecimal getEquityAum() { return equityAum; }
    public void setEquityAum(BigDecimal equityAum) { this.equityAum = equityAum; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public Long getSupersededById() { return supersededById; }
    public void setSupersededById(Long supersededById) { this.supersededById = supersededById; }
    public boolean isComplete() { return isComplete; }
    public void setComplete(boolean complete) { isComplete = complete; }

    public BigDecimal getEquityHoldingPercent() { return equityHoldingPercent; }
    public void setEquityHoldingPercent(BigDecimal equityHoldingPercent) { this.equityHoldingPercent = equityHoldingPercent; }
    public BigDecimal getDebtHoldingPercent() { return debtHoldingPercent; }
    public void setDebtHoldingPercent(BigDecimal debtHoldingPercent) { this.debtHoldingPercent = debtHoldingPercent; }
    public BigDecimal getCashHoldingPercent() { return cashHoldingPercent; }
    public void setCashHoldingPercent(BigDecimal cashHoldingPercent) { this.cashHoldingPercent = cashHoldingPercent; }
}
