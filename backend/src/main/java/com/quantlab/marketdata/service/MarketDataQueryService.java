package com.quantlab.marketdata.service;

import com.quantlab.marketdata.entity.MarketDataIngestionRun;
import com.quantlab.marketdata.entity.MarketDataRecord;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.HistoricalCandle;
import com.quantlab.marketdata.model.MarketQuote;
import com.quantlab.marketdata.model.MarketStatusInfo;
import com.quantlab.marketdata.provider.MarketDataProvider;
import com.quantlab.marketdata.repository.MarketDataIngestionRunRepository;
import com.quantlab.marketdata.repository.MarketDataRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Query Service for Application and Frontend layers.
 * Abstracts provider interactions and reads directly from normalized PostgreSQL storage.
 */
@Service
@Transactional(readOnly = true)
public class MarketDataQueryService {

    private final MarketDataProvider provider;
    private final MarketDataRecordRepository recordRepository;
    private final MarketDataIngestionRunRepository runRepository;

    public MarketDataQueryService(
            MarketDataProvider provider,
            MarketDataRecordRepository recordRepository,
            MarketDataIngestionRunRepository runRepository) {
        this.provider = provider;
        this.recordRepository = recordRepository;
        this.runRepository = runRepository;
    }

    public Optional<MarketDataRecord> getLatestQuote(String symbol, Exchange exchange) {
        return recordRepository.findLatestBySymbolAndExchange(symbol, exchange != null ? exchange : Exchange.NSE);
    }

    public List<MarketDataRecord> getLatestQuotes(List<String> symbols, Exchange exchange) {
        return recordRepository.findLatestBySymbolsAndExchange(symbols, exchange != null ? exchange : Exchange.NSE);
    }

    public List<MarketDataRecord> getHistoricalQuotes(String symbol, Exchange exchange, Instant from, Instant to) {
        return recordRepository.findRange(symbol, exchange != null ? exchange : Exchange.NSE, from, to);
    }

    public List<HistoricalCandle> fetchHistoricalFromProvider(String symbol, Exchange exchange, Instant from, Instant to, String interval) {
        return provider.getHistoricalData(symbol, exchange != null ? exchange : Exchange.NSE, from, to, interval);
    }

    public MarketStatusInfo getMarketStatus(Exchange exchange) {
        return provider.getMarketStatus(exchange != null ? exchange : Exchange.NSE);
    }

    public Page<MarketDataIngestionRun> getRecentIngestionRuns(int page, int size) {
        return runRepository.findAllByOrderByStartTimeDesc(PageRequest.of(page, size));
    }

    public Optional<MarketDataIngestionRun> getIngestionRun(String runId) {
        return runRepository.findByRunId(runId);
    }

    public com.quantlab.marketdata.model.ProviderHealthState getProviderHealthState() {
        return provider.getHealthState();
    }

    public com.quantlab.marketdata.model.ProviderSmokeTestResult runProviderSmokeTest() {
        return provider.runSmokeTest();
    }

    public String getActiveProviderName() {
        return provider.getProviderName();
    }
}
