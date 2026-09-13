package com.quantlab.backtest.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BacktestOrderDTO(
    UUID id,
    UUID runId,
    String symbol,
    OrderSide side,
    String orderType,
    Integer quantity,
    Double requestedPrice,
    Double executedPrice,
    OffsetDateTime signalTimestamp,
    OffsetDateTime orderSubmittedTimestamp,
    OffsetDateTime orderExecutedTimestamp,
    OrderStatus status,
    String rejectionReason,
    Double slippageBps,
    Double slippageAmount,
    Double feesAmount,
    UUID tradeRefId
) {}
