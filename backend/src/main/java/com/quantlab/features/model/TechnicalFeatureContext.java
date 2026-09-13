package com.quantlab.features.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable context containing strictly historical price series and parameters up to calculation time T.
 */
public class TechnicalFeatureContext {

    private final Long instrumentId;
    private final String symbol;
    private final LocalDate targetDate;
    private final Instant targetTimestamp;
    private final Timeframe timeframe;
    private final List<PriceBar> priceBars; // Chronologically ordered: oldest to newest (where last bar is target bar)
    private final List<PriceBar> benchmarkBars; // Optional benchmark series (e.g. NIFTY 50)
    private final Map<String, Object> parameters;

    public TechnicalFeatureContext(Long instrumentId, String symbol, LocalDate targetDate, Instant targetTimestamp,
                                  Timeframe timeframe, List<PriceBar> priceBars, List<PriceBar> benchmarkBars,
                                  Map<String, Object> parameters) {
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.targetDate = targetDate;
        this.targetTimestamp = targetTimestamp;
        this.timeframe = timeframe;
        this.priceBars = priceBars != null ? Collections.unmodifiableList(priceBars) : Collections.emptyList();
        this.benchmarkBars = benchmarkBars != null ? Collections.unmodifiableList(benchmarkBars) : Collections.emptyList();
        this.parameters = parameters != null ? Collections.unmodifiableMap(parameters) : Collections.emptyMap();
    }

    public Long getInstrumentId() { return instrumentId; }
    public String getSymbol() { return symbol; }
    public LocalDate getTargetDate() { return targetDate; }
    public Instant getTargetTimestamp() { return targetTimestamp; }
    public Timeframe getTimeframe() { return timeframe; }
    public List<PriceBar> getPriceBars() { return priceBars; }
    public List<PriceBar> getBenchmarkBars() { return benchmarkBars; }
    public Map<String, Object> getParameters() { return parameters; }
    public int getAvailableBarCount() { return priceBars.size(); }
    public PriceBar getLatestBar() { return priceBars.isEmpty() ? null : priceBars.get(priceBars.size() - 1); }
}
