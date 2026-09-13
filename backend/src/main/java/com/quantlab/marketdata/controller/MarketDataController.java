package com.quantlab.marketdata.controller;

import com.quantlab.marketdata.entity.MarketDataIngestionRun;
import com.quantlab.marketdata.entity.MarketDataRecord;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.HistoricalCandle;
import com.quantlab.marketdata.model.MarketStatusInfo;
import com.quantlab.marketdata.service.MarketDataIngestionService;
import com.quantlab.marketdata.service.MarketDataQueryService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Standardized Market Data REST Controller.
 * Decouples frontend and consumers from provider-specific implementations.
 */
@RestController
@RequestMapping("/api/v1/market-data")
public class MarketDataController {

    private final MarketDataQueryService queryService;
    private final MarketDataIngestionService ingestionService;

    public MarketDataController(MarketDataQueryService queryService, MarketDataIngestionService ingestionService) {
        this.queryService = queryService;
        this.ingestionService = ingestionService;
    }

    @GetMapping("/quotes/{symbol}")
    public ResponseEntity<?> getQuote(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "NSE") Exchange exchange) {
        Optional<MarketDataRecord> record = queryService.getLatestQuote(symbol, exchange);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        }
        return ResponseEntity.status(404).body(Map.of("error", "No quote found for symbol " + symbol));
    }

    @GetMapping("/quotes")
    public ResponseEntity<List<MarketDataRecord>> getQuotes(
            @RequestParam String symbols,
            @RequestParam(defaultValue = "NSE") Exchange exchange) {
        List<String> symbolList = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        return ResponseEntity.ok(queryService.getLatestQuotes(symbolList, exchange));
    }

    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<HistoricalCandle>> getHistorical(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "NSE") Exchange exchange,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "1D") String interval) {
        List<HistoricalCandle> candles = queryService.fetchHistoricalFromProvider(symbol, exchange, from, to, interval);
        return ResponseEntity.ok(candles);
    }

    @GetMapping("/status")
    public ResponseEntity<MarketStatusInfo> getMarketStatus(@RequestParam(defaultValue = "NSE") Exchange exchange) {
        return ResponseEntity.ok(queryService.getMarketStatus(exchange));
    }

    @PostMapping("/ingest")
    public ResponseEntity<MarketDataIngestionRun> triggerIngestion(
            @RequestBody(required = false) Map<String, Object> payload) {
        List<String> symbols = null;
        Exchange exchange = Exchange.NSE;

        if (payload != null) {
            if (payload.containsKey("symbols")) {
                symbols = (List<String>) payload.get("symbols");
            }
            if (payload.containsKey("exchange")) {
                exchange = Exchange.valueOf(payload.get("exchange").toString().toUpperCase());
            }
        }

        MarketDataIngestionRun run = ingestionService.ingestQuotes(symbols, exchange);
        return ResponseEntity.ok(run);
    }

    @GetMapping("/runs")
    public ResponseEntity<Page<MarketDataIngestionRun>> getRuns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(queryService.getRecentIngestionRuns(page, size));
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<?> getRunDetails(@PathVariable String runId) {
        Optional<MarketDataIngestionRun> run = queryService.getIngestionRun(runId);
        if (run.isPresent()) {
            return ResponseEntity.ok(run.get());
        }
        return ResponseEntity.status(404).body(Map.of("error", "Ingestion run not found: " + runId));
    }

    @GetMapping("/provider/health")
    public ResponseEntity<Map<String, Object>> getProviderHealth() {
        return ResponseEntity.ok(Map.of(
            "provider", queryService.getActiveProviderName(),
            "healthState", queryService.getProviderHealthState().name(),
            "isConfigured", queryService.getProviderHealthState() != com.quantlab.marketdata.model.ProviderHealthState.NOT_CONFIGURED,
            "timestamp", Instant.now()
        ));
    }

    @GetMapping("/provider/smoke-test")
    public ResponseEntity<com.quantlab.marketdata.model.ProviderSmokeTestResult> runProviderSmokeTestGet() {
        return ResponseEntity.ok(queryService.runProviderSmokeTest());
    }

    @PostMapping("/provider/smoke-test")
    public ResponseEntity<com.quantlab.marketdata.model.ProviderSmokeTestResult> runProviderSmokeTestPost() {
        return ResponseEntity.ok(queryService.runProviderSmokeTest());
    }
}
