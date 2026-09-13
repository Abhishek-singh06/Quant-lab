package com.quantlab.paper.model;

import java.time.Instant;
import java.util.UUID;

public record PaperOrderDTO(
        UUID id,
        UUID decisionId,
        UUID portfolioId,
        UUID sessionId,
        String symbol,
        String side,
        String orderType,
        Integer quantity,
        Double requestedPrice,
        Double executedPrice,
        Instant signalTimestamp,
        Instant orderSubmittedTimestamp,
        Instant orderExecutedTimestamp,
        String status,
        String rejectionReason,
        Double slippageBps,
        Double slippageAmount,
        Double feesAmount,
        Instant createdAt
) {}
