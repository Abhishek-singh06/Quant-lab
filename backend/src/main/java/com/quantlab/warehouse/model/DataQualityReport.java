package com.quantlab.warehouse.model;

import java.time.LocalDate;
import java.util.List;

public record DataQualityReport(
    String symbol,
    LocalDate fromDate,
    LocalDate toDate,
    long expectedTradingDays,
    long actualTradingDays,
    long missingDaysCount,
    List<LocalDate> missingDates,
    long duplicateRecordsCount,
    long anomaliesCount,
    String status
) {}
