package com.quantlab.global.provider;

import com.quantlab.global.entity.GlobalInstrument;
import com.quantlab.global.entity.GlobalMarketSnapshot;

import java.time.LocalDate;
import java.util.List;

/**
 * Provider interface contract for ingesting legitimate global market data.
 */
public interface GlobalMarketDataProvider {

    String getProviderName();

    boolean isAvailable();

    GlobalMarketSnapshot fetchLatestSnapshot(GlobalInstrument instrument);

    List<GlobalMarketSnapshot> fetchHistoricalSnapshots(GlobalInstrument instrument, LocalDate fromDate, LocalDate toDate);

    List<GlobalMarketSnapshot> fetchLatestSnapshots(List<GlobalInstrument> instruments);
}
