package com.quantlab.warehouse.service;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.HistoricalCandle;
import com.quantlab.marketdata.model.IngestionRunStatus;
import com.quantlab.marketdata.provider.MarketDataProvider;
import com.quantlab.warehouse.entity.HistoricalIngestionRun;
import com.quantlab.warehouse.entity.HistoricalPriceRaw;
import com.quantlab.warehouse.entity.Instrument;
import com.quantlab.warehouse.model.TimeGranularity;
import com.quantlab.warehouse.repository.HistoricalIngestionRunRepository;
import com.quantlab.warehouse.repository.HistoricalPriceRawRepository;
import com.quantlab.warehouse.repository.InstrumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Historical Data Backfill and Incremental Update Service.
 * 
 * Supports:
 * - Full historical backfills (e.g. 2005 to current date)
 * - Incremental daily updates (last_available_date to current date)
 * - Checkpointing & idempotency
 */
@Service
public class HistoricalBackfillService {

    private static final Logger log = LoggerFactory.getLogger(HistoricalBackfillService.class);

    private final MarketDataProvider provider;
    private final InstrumentRepository instrumentRepository;
    private final HistoricalPriceRawRepository rawPriceRepository;
    private final HistoricalIngestionRunRepository runRepository;
    private final CorporateActionAdjustmentService adjustmentService;
    private final HistoricalGapDetectionService gapDetectionService;

    public HistoricalBackfillService(
            MarketDataProvider provider,
            InstrumentRepository instrumentRepository,
            HistoricalPriceRawRepository rawPriceRepository,
            HistoricalIngestionRunRepository runRepository,
            CorporateActionAdjustmentService adjustmentService,
            HistoricalGapDetectionService gapDetectionService) {
        this.provider = provider;
        this.instrumentRepository = instrumentRepository;
        this.rawPriceRepository = rawPriceRepository;
        this.runRepository = runRepository;
        this.adjustmentService = adjustmentService;
        this.gapDetectionService = gapDetectionService;
    }

    @Transactional
    public HistoricalIngestionRun executeBackfill(List<String> symbols, Exchange exchange,
                                                 LocalDate fromDate, LocalDate toDate) {
        String runId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        String providerName = provider.getProviderName();

        HistoricalIngestionRun run = new HistoricalIngestionRun(runId, providerName, fromDate, toDate, startTime);
        run = runRepository.save(run);

        log.info("[HistoricalBackfill] Starting backfill run '{}' for {} symbols from {} to {}",
                runId, symbols.size(), fromDate, toDate);

        int inserted = 0;
        int duplicates = 0;
        int gaps = 0;

        try {
            for (String symbol : symbols) {
                run.setCheckpointSymbol(symbol);

                // 1. Resolve or register instrument in master
                Instrument instrument = instrumentRepository.findByCurrentSymbolAndExchange(symbol, exchange)
                    .orElseGet(() -> instrumentRepository.save(
                        new Instrument(null, exchange, symbol, symbol, fromDate, com.quantlab.warehouse.model.InstrumentStatus.ACTIVE, null, null, false)
                    ));

                // 2. Fetch candles from provider
                Instant fromInstant = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant toInstant = toDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();
                List<HistoricalCandle> candles = provider.getHistoricalData(symbol, exchange, fromInstant, toInstant, "1D");

                for (HistoricalCandle candle : candles) {
                    LocalDate candleDate = candle.timestamp().atZone(ZoneOffset.UTC).toLocalDate();

                    // Check duplicate / idempotency
                    boolean exists = rawPriceRepository.existsByInstrumentIdAndExchangeAndTradingDateAndGranularity(
                        instrument.getId(), exchange, candleDate, TimeGranularity.DAILY
                    );

                    if (exists) {
                        duplicates++;
                        continue;
                    }

                    HistoricalPriceRaw raw = new HistoricalPriceRaw(
                        instrument.getId(),
                        symbol,
                        exchange,
                        candleDate,
                        candle.timestamp(),
                        candle.open(),
                        candle.high(),
                        candle.low(),
                        candle.close(),
                        candle.volume(),
                        candle.close().multiply(java.math.BigDecimal.valueOf(candle.volume())),
                        TimeGranularity.DAILY,
                        providerName
                    );
                    rawPriceRepository.save(raw);
                    inserted++;
                }

                // 3. Compute corporate action adjustments
                adjustmentService.computeAndSaveAdjustments(instrument.getId(), exchange, fromDate, toDate);

                // 4. Run gap detection
                var report = gapDetectionService.auditInstrumentHistory(runId, instrument.getId(), symbol, fromDate, toDate);
                gaps += report.missingDaysCount();
            }

            run.setSymbolsCount(symbols.size());
            run.setRecordsInserted(inserted);
            run.setDuplicatesCount(duplicates);
            run.setGapsCount(gaps);
            run.complete(IngestionRunStatus.SUCCESS);

            log.info("[HistoricalBackfill] Finished run '{}' in {} ms: inserted={}, duplicates={}, gaps={}",
                    runId, run.getDurationMs(), inserted, duplicates, gaps);

        } catch (Exception e) {
            log.error("[HistoricalBackfill] Backfill run '{}' failed: {}", runId, e.getMessage(), e);
            run.setErrorMessage(e.getMessage());
            run.complete(IngestionRunStatus.FAILED);
        }

        return runRepository.save(run);
    }
}
