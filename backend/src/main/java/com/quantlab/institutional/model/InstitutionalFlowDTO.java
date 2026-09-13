package com.quantlab.institutional.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record InstitutionalFlowDTO(
    LocalDate tradeDate,
    String market,
    InstitutionType institutionType,
    BigDecimal buyValueCrores,
    BigDecimal sellValueCrores,
    BigDecimal netValueCrores,
    FlowFrequency frequency,
    LocalDate dataAsOf,
    LocalDate publishedAt,
    Instant availableAt,
    String source
) {}
