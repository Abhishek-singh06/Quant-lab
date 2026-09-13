package com.quantlab.broker.model;

import java.time.Instant;
import java.util.UUID;

public record PortfolioReconciliationDTO(
        UUID id,
        UUID brokerAccountId,
        Instant reconciliationTimestamp,
        String status, // MATCHED, MISMATCH, UNKNOWN
        int totalPositionsMatched,
        int totalDiscrepanciesCount,
        String discrepancyDetails,
        String checkedBy
) {}
