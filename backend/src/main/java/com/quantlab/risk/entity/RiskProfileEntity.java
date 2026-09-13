package com.quantlab.risk.entity;

import com.quantlab.risk.model.PositionSizingMethod;
import com.quantlab.risk.model.RiskProfileType;
import com.quantlab.risk.model.StopMethod;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_profiles")
public class RiskProfileEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false, length = 32)
    private RiskProfileType profileType;

    @Column(name = "max_portfolio_risk", nullable = false)
    private double maxPortfolioRisk;

    @Column(name = "max_position_risk", nullable = false)
    private double maxPositionRisk;

    @Column(name = "max_position_allocation", nullable = false)
    private double maxPositionAllocation;

    @Column(name = "max_sector_allocation", nullable = false)
    private double maxSectorAllocation;

    @Column(name = "max_industry_allocation", nullable = false)
    private double maxIndustryAllocation;

    @Column(name = "max_single_security_allocation", nullable = false)
    private double maxSingleSecurityAllocation;

    @Column(name = "max_correlation_exposure", nullable = false)
    private double maxCorrelationExposure;

    @Column(name = "max_drawdown_tolerance", nullable = false)
    private double maxDrawdownTolerance;

    @Column(name = "max_portfolio_volatility", nullable = false)
    private double maxPortfolioVolatility;

    @Column(name = "minimum_liquidity_requirement", nullable = false)
    private double minimumLiquidityRequirement;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_stop_method", nullable = false, length = 32)
    private StopMethod defaultStopMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_position_sizing_method", nullable = false, length = 32)
    private PositionSizingMethod defaultPositionSizingMethod;

    @Column(name = "allow_short_selling", nullable = false)
    private boolean allowShortSelling;

    @Column(name = "allow_leverage", nullable = false)
    private boolean allowLeverage;

    @Column(name = "max_leverage", nullable = false)
    private double maxLeverage;

    @Column(name = "cash_buffer", nullable = false)
    private double cashBuffer;

    @Column(name = "minimum_confidence", nullable = false)
    private double minimumConfidence;

    @Column(name = "minimum_signal_score", nullable = false)
    private double minimumSignalScore;

    @Column(name = "risk_budget_method", nullable = false, length = 32)
    private String riskBudgetMethod;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "version", nullable = false, length = 32)
    private String version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public RiskProfileEntity() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RiskProfileType getProfileType() { return profileType; }
    public void setProfileType(RiskProfileType profileType) { this.profileType = profileType; }

    public double getMaxPortfolioRisk() { return maxPortfolioRisk; }
    public void setMaxPortfolioRisk(double maxPortfolioRisk) { this.maxPortfolioRisk = maxPortfolioRisk; }

    public double getMaxPositionRisk() { return maxPositionRisk; }
    public void setMaxPositionRisk(double maxPositionRisk) { this.maxPositionRisk = maxPositionRisk; }

    public double getMaxPositionAllocation() { return maxPositionAllocation; }
    public void setMaxPositionAllocation(double maxPositionAllocation) { this.maxPositionAllocation = maxPositionAllocation; }

    public double getMaxSectorAllocation() { return maxSectorAllocation; }
    public void setMaxSectorAllocation(double maxSectorAllocation) { this.maxSectorAllocation = maxSectorAllocation; }

    public double getMaxIndustryAllocation() { return maxIndustryAllocation; }
    public void setMaxIndustryAllocation(double maxIndustryAllocation) { this.maxIndustryAllocation = maxIndustryAllocation; }

    public double getMaxSingleSecurityAllocation() { return maxSingleSecurityAllocation; }
    public void setMaxSingleSecurityAllocation(double maxSingleSecurityAllocation) { this.maxSingleSecurityAllocation = maxSingleSecurityAllocation; }

    public double getMaxCorrelationExposure() { return maxCorrelationExposure; }
    public void setMaxCorrelationExposure(double maxCorrelationExposure) { this.maxCorrelationExposure = maxCorrelationExposure; }

    public double getMaxDrawdownTolerance() { return maxDrawdownTolerance; }
    public void setMaxDrawdownTolerance(double maxDrawdownTolerance) { this.maxDrawdownTolerance = maxDrawdownTolerance; }

    public double getMaxPortfolioVolatility() { return maxPortfolioVolatility; }
    public void setMaxPortfolioVolatility(double maxPortfolioVolatility) { this.maxPortfolioVolatility = maxPortfolioVolatility; }

    public double getMinimumLiquidityRequirement() { return minimumLiquidityRequirement; }
    public void setMinimumLiquidityRequirement(double minimumLiquidityRequirement) { this.minimumLiquidityRequirement = minimumLiquidityRequirement; }

    public StopMethod getDefaultStopMethod() { return defaultStopMethod; }
    public void setDefaultStopMethod(StopMethod defaultStopMethod) { this.defaultStopMethod = defaultStopMethod; }

    public PositionSizingMethod getDefaultPositionSizingMethod() { return defaultPositionSizingMethod; }
    public void setDefaultPositionSizingMethod(PositionSizingMethod defaultPositionSizingMethod) { this.defaultPositionSizingMethod = defaultPositionSizingMethod; }

    public boolean isAllowShortSelling() { return allowShortSelling; }
    public void setAllowShortSelling(boolean allowShortSelling) { this.allowShortSelling = allowShortSelling; }

    public boolean isAllowLeverage() { return allowLeverage; }
    public void setAllowLeverage(boolean allowLeverage) { this.allowLeverage = allowLeverage; }

    public double getMaxLeverage() { return maxLeverage; }
    public void setMaxLeverage(double maxLeverage) { this.maxLeverage = maxLeverage; }

    public double getCashBuffer() { return cashBuffer; }
    public void setCashBuffer(double cashBuffer) { this.cashBuffer = cashBuffer; }

    public double getMinimumConfidence() { return minimumConfidence; }
    public void setMinimumConfidence(double minimumConfidence) { this.minimumConfidence = minimumConfidence; }

    public double getMinimumSignalScore() { return minimumSignalScore; }
    public void setMinimumSignalScore(double minimumSignalScore) { this.minimumSignalScore = minimumSignalScore; }

    public String getRiskBudgetMethod() { return riskBudgetMethod; }
    public void setRiskBudgetMethod(String riskBudgetMethod) { this.riskBudgetMethod = riskBudgetMethod; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
