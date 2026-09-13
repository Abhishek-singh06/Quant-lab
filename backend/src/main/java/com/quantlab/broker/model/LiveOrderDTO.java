package com.quantlab.broker.model;

import java.time.Instant;
import java.util.UUID;

public record LiveOrderDTO(
        UUID id,
        UUID brokerAccountId,
        String brokerOrderId,
        UUID orderIntentId,
        String symbol,
        String exchange,
        String side, // BUY, SELL
        String orderType, // MARKET, LIMIT, STOP_LOSS, STOP_LIMIT
        String productType, // CASH, INTRADAY
        int quantity,
        double price,
        Double triggerPrice,
        Double stopLossPrice,
        Double targetPrice,
        String status, // AWAITING_CONFIRMATION, CONFIRMED, SUBMITTING, SUBMITTED, OPEN, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED
        int filledQuantity,
        Double averageFillPrice,
        String rejectionReason,
        UUID signalId,
        Double signalScore,
        Double signalConfidence,
        Double expectedReturn,
        UUID riskAssessmentId,
        String riskLevel,
        Double suggestedAllocation,
        boolean isManuallyConfirmed,
        String confirmedByUser,
        Instant confirmedAt,
        Instant orderSubmittedAt,
        Instant orderExecutedAt,
        String idempotencyKey,
        Instant createdAt
) {}
