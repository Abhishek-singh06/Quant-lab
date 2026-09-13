package com.quantlab.global.service;

import com.quantlab.global.entity.*;
import com.quantlab.global.provider.GlobalMarketDataProvider;
import com.quantlab.global.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GlobalMarketIngestionService {

    private static final Logger log = LoggerFactory.getLogger(GlobalMarketIngestionService.class);

    private final GlobalMarketDataProvider defaultProvider;
    private final GlobalInstrumentRepository instrumentRepository;
    private final GlobalMarketSnapshotRepository snapshotRepository;
    private final GlobalMarketRegimeRepository regimeRepository;
    private final GlobalMarketIngestionRunRepository runRepository;
    private final GlobalMarketRegimeEngine regimeEngine;

    public GlobalMarketIngestionService(
            @Qualifier("mockGlobalMarketDataProvider") GlobalMarketDataProvider defaultProvider,
            GlobalInstrumentRepository instrumentRepository,
            GlobalMarketSnapshotRepository snapshotRepository,
            GlobalMarketRegimeRepository regimeRepository,
            GlobalMarketIngestionRunRepository runRepository,
            GlobalMarketRegimeEngine regimeEngine) {
        this.defaultProvider = defaultProvider;
        this.instrumentRepository = instrumentRepository;
        this.snapshotRepository = snapshotRepository;
        this.regimeRepository = regimeRepository;
        this.runRepository = runRepository;
        this.regimeEngine = regimeEngine;
    }

    @Transactional
    public GlobalMarketIngestionRun ingestLatestGlobalData() {
        String runUuid = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        GlobalMarketIngestionRun run = new GlobalMarketIngestionRun(runUuid, defaultProvider.getProviderName(), startTime, "RUNNING");
        run = runRepository.save(run);

        int requested = 0;
        int received = 0;
        int inserted = 0;
        int duplicates = 0;
        int rejected = 0;

        try {
            List<GlobalInstrument> instruments = instrumentRepository.findByActiveTrue();
            requested = instruments.size();
            run.setInstrumentsRequested(requested);

            List<GlobalMarketSnapshot> snapshots = defaultProvider.fetchLatestSnapshots(instruments);
            received = snapshots.size();

            for (GlobalMarketSnapshot snap : snapshots) {
                if (snap.getClose() == null) {
                    rejected++;
                    continue;
                }

                Optional<GlobalMarketSnapshot> existing = snapshotRepository.findByInstrumentIdAndTimestampAndSource(
                    snap.getInstrumentId(), snap.getTimestamp(), snap.getSource()
                );

                if (existing.isEmpty()) {
                    snapshotRepository.save(snap);
                    inserted++;
                } else {
                    duplicates++;
                }
            }

            // Derive and save latest Global Market Regime
            GlobalMarketRegime regime = regimeEngine.calculateRegime(snapshots, startTime);
            regimeRepository.save(regime);

            run.setStatus("SUCCESS");
            run.setObservationsReceived(received);
            run.setObservationsInserted(inserted);
            run.setDuplicatesCount(duplicates);
            run.setRejectedCount(rejected);
            run.setEndTime(Instant.now());
            run.setDurationMs(run.getEndTime().toEpochMilli() - startTime.toEpochMilli());
            return runRepository.save(run);

        } catch (Exception e) {
            log.error("Global market ingestion failed: {}", e.getMessage(), e);
            run.setStatus("FAILED");
            run.setErrorMessage(e.getMessage());
            run.setEndTime(Instant.now());
            run.setDurationMs(run.getEndTime().toEpochMilli() - startTime.toEpochMilli());
            return runRepository.save(run);
        }
    }

    @Transactional
    public void ingestHistoricalData(LocalDate fromDate, LocalDate toDate) {
        List<GlobalInstrument> instruments = instrumentRepository.findByActiveTrue();
        for (GlobalInstrument inst : instruments) {
            List<GlobalMarketSnapshot> history = defaultProvider.fetchHistoricalSnapshots(inst, fromDate, toDate);
            for (GlobalMarketSnapshot snap : history) {
                Optional<GlobalMarketSnapshot> existing = snapshotRepository.findByInstrumentIdAndTimestampAndSource(
                    snap.getInstrumentId(), snap.getTimestamp(), snap.getSource()
                );
                if (existing.isEmpty()) {
                    snapshotRepository.save(snap);
                }
            }
        }
    }
}
