package com.quantlab.institutional.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record FundPortfolioDTO(
    Long schemeId,
    String schemeName,
    String schemeCode,
    String amcName,
    String category,
    LocalDate dataAsOf,
    LocalDate publishedAt,
    Instant availableAt,
    String source,
    long ageDays,
    BigDecimal totalAum,
    PortfolioScope portfolioScope,
    List<DisclosedStockPosition> topHoldings,
    Map<String, BigDecimal> sectorAllocations,
    List<DisclosedStockPosition> newPositions,
    List<DisclosedStockPosition> exitedPositions,
    List<DisclosedStockPosition> increasedPositions,
    List<DisclosedStockPosition> decreasedPositions
) {
    public record DisclosedStockPosition(
        Long instrumentId,
        String symbol,
        String companyName,
        String sector,
        BigDecimal portfolioWeight,
        BigDecimal weightChangePp,
        BigDecimal relativeWeightChangePercent,
        HoldingChangeType changeType,
        Long quantity,
        BigDecimal marketValue
    ) {}
}
