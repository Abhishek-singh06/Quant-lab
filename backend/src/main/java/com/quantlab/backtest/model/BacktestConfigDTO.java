package com.quantlab.backtest.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BacktestConfigDTO(
    UUID id,
    String name,
    String description,
    String horizon,
    String universeType,
    List<String> symbols,
    LocalDate startDate,
    LocalDate endDate,
    Double initialCapital,
    Double cashBufferPct,
    String rebalanceFrequency,
    String executionTiming,
    CostModelType costModelType,
    SlippageModelType slippageModelType,
    Double brokerageBps,
    Double sttDeliveryBps,
    Double sttIntradayBps,
    Double exchangeChargesBps,
    Double gstRate,
    Double stampDutyBps,
    Double slippageBps,
    Double maxPositionWeight,
    Double maxSectorWeight,
    Double maxDrawdownLimit,
    String benchmarkSymbol,
    String version
) {}
