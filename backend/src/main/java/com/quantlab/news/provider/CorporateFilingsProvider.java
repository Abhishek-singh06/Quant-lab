package com.quantlab.news.provider;

import com.quantlab.news.entity.CorporateEvent;
import java.time.Instant;
import java.util.List;

/**
 * Generic Corporate Filings and Disclosures Provider Interface.
 */
public interface CorporateFilingsProvider {

    String getProviderName();

    boolean isAvailable();

    List<CorporateEvent> fetchLatestFilings(Instant since, int limit);

    List<CorporateEvent> fetchFilingsForSymbol(String symbol, Instant from, Instant to);
}
