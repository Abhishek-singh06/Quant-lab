package com.quantlab.paper.model;

import java.time.Instant;
import java.util.UUID;

public record PaperFillDTO(
        UUID id,
        UUID orderId,
        UUID portfolioId,
        String symbol,
        String side,
        Integer quantity,
        Double requestedPrice,
        Double fillPrice,
        Double slippageBps,
        Double slippageAmount,
        Double brokerage,
        Double stt,
        Double exchangeCharges,
        Double gst,
        Double stampDuty,
        Double totalFees,
        Instant executionTimestamp,
        Instant createdAt
) {}
