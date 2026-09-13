package com.quantlab.news.model;

public enum SourceCredibility {
    OFFICIAL_CORPORATE_DISCLOSURE, // Direct company announcements (Highest credibility)
    EXCHANGE_DISCLOSURE,           // NSE/BSE filing
    REGULATORY_DISCLOSURE,         // SEBI, RBI, CCI
    LICENSED_NEWS,                 // Reuters, Bloomberg, Mint, Economic Times
    GENERAL_NEWS,                  // Secondary press
    SOCIAL_MISC                    // Unverified web/social
}
