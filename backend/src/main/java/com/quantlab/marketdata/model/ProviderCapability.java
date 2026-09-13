package com.quantlab.marketdata.model;

/**
 * Explicit capabilities supported by market data and broker execution providers.
 * Inspired by CCXT unified capability declaration model, adapted for Indian Equities.
 */
public enum ProviderCapability {
    MARKET_DATA,
    HISTORICAL_DATA,
    REALTIME_QUOTES,
    WEBSOCKET,
    NEWS,
    CORPORATE_ACTIONS,
    PLACE_ORDER,
    CANCEL_ORDER,
    ORDER_STATUS,
    POSITIONS,
    HOLDINGS,
    MARGINS,
    RECONCILIATION
}
