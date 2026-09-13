package com.quantlab.news.model;

import java.math.BigDecimal;

public record EntityMatch(
    Long instrumentId,
    String symbol,
    String entityName,
    BigDecimal confidence,
    String matchType // "EXACT_TICKER", "COMPANY_NAME", "HISTORICAL_ALIAS", "CONTEXTUAL"
) {}
