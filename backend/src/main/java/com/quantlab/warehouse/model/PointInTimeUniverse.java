package com.quantlab.warehouse.model;

import java.time.LocalDate;
import java.util.List;

public record PointInTimeUniverse(
    String indexSymbol,
    LocalDate asOfDate,
    List<String> constituentSymbols,
    List<Long> instrumentIds,
    int totalConstituents
) {}
