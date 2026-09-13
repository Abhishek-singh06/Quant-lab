package com.quantlab.paper.model;

import java.time.Instant;
import java.util.UUID;

public record PaperLedgerDTO(
        UUID id,
        UUID portfolioId,
        UUID sessionId,
        Instant transactionTimestamp,
        String eventType,
        String symbol,
        Double amount,
        Double cashBalanceBefore,
        Double cashBalanceAfter,
        String description,
        UUID referenceId,
        Instant createdAt
) {}
