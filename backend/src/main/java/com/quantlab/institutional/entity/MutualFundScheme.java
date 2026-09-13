package com.quantlab.institutional.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "mutual_fund_schemes",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_mf_scheme_code", columnList = "scheme_code", unique = true),
        @Index(name = "idx_mf_amc_id", columnList = "amc_id"),
        @Index(name = "idx_mf_category", columnList = "category"),
        @Index(name = "idx_mf_isin", columnList = "isin")
    }
)
public class MutualFundScheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amc_id", nullable = false)
    private Long amcId;

    @Column(name = "amc_name", nullable = false, length = 128)
    private String amcName;

    @Column(name = "scheme_code", nullable = false, unique = true, length = 32)
    private String schemeCode; // e.g. AMFI scheme code "120503"

    @Column(name = "scheme_name", nullable = false, length = 255)
    private String schemeName;

    @Column(name = "scheme_type", length = 32)
    private String schemeType; // "Open Ended", "Close Ended"

    @Column(length = 64)
    private String category; // "Equity Scheme", "Debt Scheme", "Hybrid"

    @Column(length = 64)
    private String subcategory; // "Large Cap Fund", "Flexi Cap Fund", "Small Cap Fund"

    @Column(length = 32)
    private String plan; // "Direct", "Regular"

    @Column(length = 32)
    private String optionType; // "Growth", "IDCW"

    @Column(length = 128)
    private String benchmark;

    @Column(length = 16)
    private String isin;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "launch_date")
    private LocalDate launchDate;

    @Column(name = "closure_date")
    private LocalDate closureDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "aum_crores")
    private java.math.BigDecimal aumCrores;

    public MutualFundScheme() {}

    public MutualFundScheme(long amcId, String amcName, String schemeCode, String schemeName,
                            String category, String subcategory, String plan, String isin) {
        this.amcId = amcId;
        this.amcName = amcName;
        this.schemeCode = schemeCode;
        this.schemeName = schemeName;
        this.category = category;
        this.subcategory = subcategory;
        this.plan = plan;
        this.isin = isin;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }

    public MutualFundScheme(Long amcId, String amcName, String schemeCode, String schemeName,
                            String category, String subcategory, String plan, String optionType, String isin) {
        this.amcId = amcId;
        this.amcName = amcName;
        this.schemeCode = schemeCode;
        this.schemeName = schemeName;
        this.category = category;
        this.subcategory = subcategory;
        this.plan = plan;
        this.optionType = optionType;
        this.isin = isin;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAmcId() { return amcId; }
    public void setAmcId(Long amcId) { this.amcId = amcId; }
    public String getAmcName() { return amcName; }
    public void setAmcName(String amcName) { this.amcName = amcName; }
    public String getSchemeCode() { return schemeCode; }
    public void setSchemeCode(String schemeCode) { this.schemeCode = schemeCode; }
    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }
    public String getSchemeType() { return schemeType; }
    public void setSchemeType(String schemeType) { this.schemeType = schemeType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }
    public String getBenchmark() { return benchmark; }
    public void setBenchmark(String benchmark) { this.benchmark = benchmark; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getLaunchDate() { return launchDate; }
    public void setLaunchDate(LocalDate launchDate) { this.launchDate = launchDate; }
    public LocalDate getClosureDate() { return closureDate; }
    public void setClosureDate(LocalDate closureDate) { this.closureDate = closureDate; }
    public java.math.BigDecimal getAumCrores() { return aumCrores; }
    public void setAumCrores(java.math.BigDecimal aumCrores) { this.aumCrores = aumCrores; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
