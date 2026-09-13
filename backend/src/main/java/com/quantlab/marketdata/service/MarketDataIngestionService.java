package com.quantlab.marketdata.service;

import com.quantlab.marketdata.alert.AlertService;
import com.quantlab.marketdata.config.MarketDataProperties;
import com.quantlab.marketdata.detector.DuplicateDetector;
import com.quantlab.marketdata.detector.MissingDataDetector;
import com.quantlab.marketdata.detector.StaleDataDetector;
import com.quantlab.marketdata.entity.MarketDataError;
import com.quantlab.marketdata.entity.MarketDataIngestionRun;
import com.quantlab.marketdata.entity.MarketDataRecord;
import com.quantlab.marketdata.model.*;
import com.quantlab.marketdata.normalizer.MarketDataNormalizer;
import com.quantlab.marketdata.provider.MarketDataProvider;
import com.quantlab.marketdata.repository.MarketDataErrorRepository;
import com.quantlab.marketdata.repository.MarketDataIngestionRunRepository;
import com.quantlab.marketdata.repository.MarketDataRecordRepository;
import com.quantlab.marketdata.validator.MarketDataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core Market Data Ingestion Pipeline.
 * 
 * Pipeline Flow:
 * Provider -> Normalizer -> Validator -> Stale Detection -> Duplicate Check -> Missing Check -> PostgreSQL -> Alerting
 */
@Service
public class MarketDataIngestionService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataIngestionService.class);

    private final MarketDataProvider provider;
    private final MarketDataNormalizer normalizer;
    private final MarketDataValidator validator;
    private final DuplicateDetector duplicateDetector;
    private final StaleDataDetector staleDataDetector;
    private final MissingDataDetector missingDataDetector;
    private final MarketDataRecordRepository recordRepository;
    private final MarketDataIngestionRunRepository runRepository;
    private final MarketDataErrorRepository errorRepository;
    private final AlertService alertService;
    private final MarketDataProperties properties;

    public MarketDataIngestionService(
            MarketDataProvider provider,
            MarketDataNormalizer normalizer,
            MarketDataValidator validator,
            DuplicateDetector duplicateDetector,
            StaleDataDetector staleDataDetector,
            MissingDataDetector missingDataDetector,
            MarketDataRecordRepository recordRepository,
            MarketDataIngestionRunRepository runRepository,
            MarketDataErrorRepository errorRepository,
            AlertService alertService,
            MarketDataProperties properties) {
        this.provider = provider;
        this.normalizer = normalizer;
        this.validator = validator;
        this.duplicateDetector = duplicateDetector;
        this.staleDataDetector = staleDataDetector;
        this.missingDataDetector = missingDataDetector;
        this.recordRepository = recordRepository;
        this.runRepository = runRepository;
        this.errorRepository = errorRepository;
        this.alertService = alertService;
        this.properties = properties;
    }

    @Transactional
    public MarketDataIngestionRun ingestQuotes(List<String> symbols, Exchange exchange) {
        String runId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        String providerName = provider.getProviderName();

        MarketDataIngestionRun run = new MarketDataIngestionRun(runId, providerName, startTime);
        run = runRepository.save(run);

        log.info("[IngestionPipeline] Starting ingestion run '{}' for provider '{}' with {} symbols",
                runId, providerName, symbols != null ? symbols.size() : 0);

        List<String> targetSymbols = (symbols != null && !symbols.isEmpty())
                ? symbols
                : properties.getDefaultSymbols();

        List<MarketQuote> receivedQuotes = new ArrayList<>();
        int accepted = 0;
        int rejected = 0;
        int duplicates = 0;
        int staleCount = 0;

        try {
            // 1. Fetch from provider
            receivedQuotes = provider.getQuotes(targetSymbols, exchange);
            run.setRecordsReceived(receivedQuotes.size());

            // 2. Process each quote through pipeline
            for (MarketQuote rawQuote : receivedQuotes) {
                if (rawQuote == null) {
                    rejected++;
                    continue;
                }

                // Normalization
                MarketQuote normalized = normalizer.normalize(rawQuote, runId);

                // Validation
                ValidationResult validation = validator.validate(normalized);
                if (!validation.isValid()) {
                    rejected++;
                    saveError(runId, providerName, normalized.symbol(), normalized.sourceTimestamp(),
                            ErrorCategory.VALIDATION_FAILURE, String.join("; ", validation.errors()), normalized.toString());
                    continue;
                }

                // Stale detection
                if (staleDataDetector.isStale(normalized)) {
                    staleCount++;
                }

                // Duplicate detection
                if (duplicateDetector.isDuplicate(normalized, "QUOTE")) {
                    duplicates++;
                    duplicateDetector.registerKey(normalized, "QUOTE");
                    continue;
                }

                // Persist valid record to PostgreSQL
                MarketDataRecord entity = toEntity(normalized, runId);
                recordRepository.save(entity);
                duplicateDetector.registerKey(normalized, "QUOTE");
                accepted++;
            }

            // 3. Missing Data Detection (respects Indian trading calendar)
            List<String> missing = missingDataDetector.detectMissingSymbols(targetSymbols, receivedQuotes, startTime);
            int missingCount = missing.size();
            run.setMissingCount(missingCount);

            if (!missing.isEmpty()) {
                alertService.sendAlert(AlertEvent.of(
                    "MISSING_DATA",
                    AlertSeverity.WARNING,
                    "Missing market symbols in ingestion run",
                    "Missing: " + String.join(", ", missing),
                    providerName,
                    runId
                ));
            }

            // 4. Update run metrics
            run.setRecordsAccepted(accepted);
            run.setRecordsRejected(rejected);
            run.setDuplicatesCount(duplicates);
            run.setStaleCount(staleCount);

            IngestionRunStatus finalStatus = IngestionRunStatus.SUCCESS;
            if (rejected > 0 || missingCount > 0) {
                finalStatus = accepted > 0 ? IngestionRunStatus.PARTIAL_SUCCESS : IngestionRunStatus.FAILED;
            }
            run.complete(finalStatus);

            log.info("[IngestionPipeline] Completed run '{}' in {} ms: status={}, received={}, accepted={}, rejected={}, duplicates={}, missing={}, stale={}",
                    runId, run.getDurationMs(), finalStatus, run.getRecordsReceived(), accepted, rejected, duplicates, missingCount, staleCount);

        } catch (Exception ex) {
            log.error("[IngestionPipeline] Ingestion run '{}' failed catastrophically: {}", runId, ex.getMessage(), ex);
            run.setErrorMessage(ex.getMessage());
            run.setErrorCount(run.getErrorCount() + 1);
            run.complete(IngestionRunStatus.FAILED);

            saveError(runId, providerName, "ALL", startTime, ErrorCategory.PROVIDER_UNAVAILABLE, ex.getMessage(), null);

            alertService.sendAlert(AlertEvent.of(
                "INGESTION_FAILED",
                AlertSeverity.CRITICAL,
                "Market data ingestion run failed",
                ex.getMessage(),
                providerName,
                runId
            ));
        }

        return runRepository.save(run);
    }

    private void saveError(String runId, String provider, String symbol, Instant sourceTs,
                           ErrorCategory category, String reason, String payload) {
        try {
            MarketDataError error = new MarketDataError(runId, provider, symbol, sourceTs, category, reason, payload);
            errorRepository.save(error);
        } catch (Exception e) {
            log.error("[IngestionPipeline] Failed to log error record to database: {}", e.getMessage());
        }
    }

    private MarketDataRecord toEntity(MarketQuote quote, String runId) {
        MarketDataRecord record = new MarketDataRecord();
        record.setSymbol(quote.symbol());
        record.setExchange(quote.exchange());
        record.setIsin(quote.isin());
        record.setDataType("QUOTE");
        record.setLastPrice(quote.lastPrice());
        record.setOpenPrice(quote.openPrice());
        record.setHighPrice(quote.highPrice());
        record.setLowPrice(quote.lowPrice());
        record.setClosePrice(quote.closePrice());
        record.setPrevClosePrice(quote.prevClosePrice());
        record.setChange(quote.change());
        record.setChangePercent(quote.changePercent());
        record.setVolume(quote.volume());
        record.setTotalTradedValue(quote.totalTradedValue());
        record.setOpenInterest(quote.openInterest());
        record.setTimestamp(quote.timestamp());
        record.setSourceTimestamp(quote.sourceTimestamp());
        record.setIngestionTimestamp(quote.ingestionTimestamp());
        record.setSource(quote.source());
        record.setRunId(runId);
        record.setStatus(quote.status());
        return record;
    }
}
