package com.quantlab.backtest.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BacktestRejectedSignalDTO(
    UUID id,
    UUID runId,
    String symbol,
    OffsetDateTime signalTimestamp,
    String signalType,
    Double signalStrength,
    String rejectionReason,
    String details
) {}
