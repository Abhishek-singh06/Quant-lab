package com.quantlab.backtest.model;

import java.time.LocalDate;

public record EquityCurvePointDTO(
    LocalDate pointDate,
    Double strategyEquity,
    Double strategyReturnPct,
    Double strategyDrawdownPct,
    Double buyAndHoldEquity,
    Double buyAndHoldReturnPct,
    Double benchmarkEquity,
    Double benchmarkReturnPct
) {}
