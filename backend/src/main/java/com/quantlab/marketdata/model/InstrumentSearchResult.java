package com.quantlab.marketdata.model;

public record InstrumentSearchResult(
        String symbol,
        String name,
        String exchange,
        String sector,
        String assetClass,
        String isin
) {}
