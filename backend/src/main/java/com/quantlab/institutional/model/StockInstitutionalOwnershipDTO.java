package com.quantlab.institutional.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record StockInstitutionalOwnershipDTO(
    String symbol,
    LocalDate dataAsOf,
    LocalDate publishedAt,
    Instant availableAt,
    String source,
    long ageDays,
    BigDecimal totalDisclosedMfWeight,
    int totalFundsHolding,
    int newFundsCount,
    int exitedFundsCount,
    BigDecimal aggregateWeightChangePp,
    List<DisclosedFundHolding> topFunds
) {
    public record DisclosedFundHolding(
        Long schemeId,
        String schemeName,
        String amcName,
        BigDecimal portfolioWeight,
        BigDecimal weightChangePp,
        BigDecimal relativeWeightChangePercent,
        HoldingChangeType changeType,
        Long quantity,
        BigDecimal marketValue
    ) {}
}
