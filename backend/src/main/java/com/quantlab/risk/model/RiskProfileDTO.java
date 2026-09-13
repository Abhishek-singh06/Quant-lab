package com.quantlab.risk.model;

import java.time.Instant;
import java.util.UUID;

public record RiskProfileDTO(
    UUID id,
    String name,
    RiskProfileType profileType,
    Double maxPortfolioRisk,
    Double maxPositionRisk,
    Double maxPositionAllocation,
    Double maxSectorAllocation,
    Double maxIndustryAllocation,
    Double maxSingleSecurityAllocation,
    Double maxCorrelationExposure,
    Double maxDrawdownTolerance,
    Double maxPortfolioVolatility,
    Double minimumLiquidityRequirement,
    StopMethod defaultStopMethod,
    PositionSizingMethod defaultPositionSizingMethod,
    Boolean allowShortSelling,
    Boolean allowLeverage,
    Double maxLeverage,
    Double cashBuffer,
    Double minimumConfidence,
    Double minimumSignalScore,
    String riskBudgetMethod,
    Boolean isActive,
    String version,
    Instant createdAt,
    Instant updatedAt
) {}
