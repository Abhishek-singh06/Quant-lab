package com.quantlab.fundamental.entity;

import com.quantlab.fundamental.model.AuditStatus;
import com.quantlab.fundamental.model.FilingType;
import com.quantlab.fundamental.model.PeriodType;
import com.quantlab.fundamental.model.ReportingBasis;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "fundamental_filings",
    schema = "market_data",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_filing_inst_period_basis_ver", columnNames = {"instrument_id", "period_end", "period_type", "reporting_basis", "version"})
    },
    indexes = {
        @Index(name = "idx_ff_inst_id", columnList = "instrument_id"),
        @Index(name = "idx_ff_symbol", columnList = "symbol"),
        @Index(name = "idx_ff_period_end", columnList = "period_end"),
        @Index(name = "idx_ff_available_at", columnList = "available_at")
    }
)
public class FundamentalFiling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "filing_type", nullable = false, length = 32)
    private FilingType filingType = FilingType.FINANCIAL_RESULT;

    @Column(name = "fiscal_year", nullable = false, length = 16)
    private String fiscalYear;

    @Column(name = "fiscal_quarter", length = 8)
    private String fiscalQuarter;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 32)
    private PeriodType periodType = PeriodType.QUARTERLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "reporting_basis", nullable = false, length = 32)
    private ReportingBasis reportingBasis = ReportingBasis.CONSOLIDATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_status", nullable = false, length = 32)
    private AuditStatus auditStatus = AuditStatus.UNAUDITED;

    @Column(name = "announced_at")
    private Instant announcedAt;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt; // UTC timestamp for Point-in-Time queries

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt = Instant.now();

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "source_document_url", length = 1024)
    private String sourceDocumentUrl;

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "superseded_by_id")
    private Long supersededById;

    @Column(name = "is_restatement", nullable = false)
    private boolean restatement = false;

    @Column(name = "restatement_reason", length = 512)
    private String restatementReason;

    @Column(name = "data_quality_score", nullable = false, length = 16)
    private String dataQualityScore = "HIGH";

    public FundamentalFiling() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public FilingType getFilingType() { return filingType; }
    public void setFilingType(FilingType filingType) { this.filingType = filingType; }
    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }
    public String getFiscalQuarter() { return fiscalQuarter; }
    public void setFiscalQuarter(String fiscalQuarter) { this.fiscalQuarter = fiscalQuarter; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public PeriodType getPeriodType() { return periodType; }
    public void setPeriodType(PeriodType periodType) { this.periodType = periodType; }
    public ReportingBasis getReportingBasis() { return reportingBasis; }
    public void setReportingBasis(ReportingBasis reportingBasis) { this.reportingBasis = reportingBasis; }
    public AuditStatus getAuditStatus() { return auditStatus; }
    public void setAuditStatus(AuditStatus auditStatus) { this.auditStatus = auditStatus; }
    public Instant getAnnouncedAt() { return announcedAt; }
    public void setAnnouncedAt(Instant announcedAt) { this.announcedAt = announcedAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
    public Instant getIngestedAt() { return ingestedAt; }
    public void setIngestedAt(Instant ingestedAt) { this.ingestedAt = ingestedAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceDocumentUrl() { return sourceDocumentUrl; }
    public void setSourceDocumentUrl(String sourceDocumentUrl) { this.sourceDocumentUrl = sourceDocumentUrl; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public Long getSupersededById() { return supersededById; }
    public void setSupersededById(Long supersededById) { this.supersededById = supersededById; }
    public boolean isRestatement() { return restatement; }
    public void setRestatement(boolean restatement) { this.restatement = restatement; }
    public String getRestatementReason() { return restatementReason; }
    public void setRestatementReason(String restatementReason) { this.restatementReason = restatementReason; }
    public String getDataQualityScore() { return dataQualityScore; }
    public void setDataQualityScore(String dataQualityScore) { this.dataQualityScore = dataQualityScore; }
}
