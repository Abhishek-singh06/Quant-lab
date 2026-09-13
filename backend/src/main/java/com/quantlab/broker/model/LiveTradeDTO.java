package com.quantlab.broker.model;

import java.time.Instant;
import java.util.UUID;

public record LiveTradeDTO(
        UUID id,
        UUID liveOrderId,
        String brokerTradeId,
        String symbol,
        String side,
        int quantity,
        double executionPrice,
        double brokerage,
        double stt,
        double exchangeCharges,
        double gst,
        double stampDuty,
        double totalFees,
        Instant executionTimestamp
) {}
