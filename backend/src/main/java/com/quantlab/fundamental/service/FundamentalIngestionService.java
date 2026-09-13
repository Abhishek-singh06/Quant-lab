package com.quantlab.fundamental.service;

import com.quantlab.fundamental.entity.FinancialRatio;
import com.quantlab.fundamental.entity.FinancialStatement;
import com.quantlab.fundamental.entity.FundamentalFiling;
import com.quantlab.fundamental.entity.FundamentalIngestionRun;
import com.quantlab.fundamental.model.PeriodType;
import com.quantlab.fundamental.model.ReportingBasis;
import com.quantlab.fundamental.provider.FundamentalDataProvider;
import com.quantlab.fundamental.repository.FinancialRatioRepository;
import com.quantlab.fundamental.repository.FinancialStatementRepository;
import com.quantlab.fundamental.repository.FundamentalFilingRepository;
import com.quantlab.fundamental.repository.FundamentalIngestionRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FundamentalIngestionService {

    private static final Logger log = LoggerFactory.getLogger(FundamentalIngestionService.class);

    private final FundamentalDataProvider primaryProvider;
    private final FundamentalDataProvider mockProvider;
    private final FundamentalFilingRepository filingRepository;
    private final FinancialStatementRepository statementRepository;
    private final FinancialRatioRepository ratioRepository;
    private final FundamentalIngestionRunRepository runRepository;
    private final FinancialStatementNormalizerService normalizerService;
    private final FinancialRatioCalculatorService ratioCalculatorService;

    public FundamentalIngestionService(
            @Qualifier("nseFundamentalDataProvider") FundamentalDataProvider primaryProvider,
            @Qualifier("mockFundamentalDataProvider") FundamentalDataProvider mockProvider,
            FundamentalFilingRepository filingRepository,
            FinancialStatementRepository statementRepository,
            FinancialRatioRepository ratioRepository,
            FundamentalIngestionRunRepository runRepository,
            FinancialStatementNormalizerService normalizerService,
            FinancialRatioCalculatorService ratioCalculatorService) {
        this.primaryProvider = primaryProvider;
        this.mockProvider = mockProvider;
        this.filingRepository = filingRepository;
        this.statementRepository = statementRepository;
        this.ratioRepository = ratioRepository;
        this.runRepository = runRepository;
        this.normalizerService = normalizerService;
        this.ratioCalculatorService = ratioCalculatorService;
    }

    public FundamentalDataProvider getActiveProvider() {
        return primaryProvider.isAvailable() ? primaryProvider : mockProvider;
    }

    @Transactional
    public FundamentalIngestionRun ingestForSymbol(String symbol) {
        FundamentalDataProvider provider = getActiveProvider();
        Instant startTime = Instant.now();
        FundamentalIngestionRun run = new FundamentalIngestionRun();
        run.setProvider(provider.getProviderName());
        run.setCompaniesRequested(1);
        run.setStartTime(startTime);
        run.setStatus("RUNNING");

        int processed = 0;
        int failed = 0;

        try {
            List<FundamentalFiling> filings = provider.fetchCompanyFilings(symbol);
            for (FundamentalFiling filing : filings) {
                try {
                    processFiling(provider, filing);
                    processed++;
                } catch (Exception ex) {
                    log.error("Failed to process filing for symbol {}: {}", symbol, ex.getMessage(), ex);
                    failed++;
                }
            }

            run.setFilingsInserted(processed);
            run.setRejectedCount(failed);
            run.setStatus(failed == 0 ? "SUCCESS" : (processed > 0 ? "PARTIAL_SUCCESS" : "FAILED"));
        } catch (Exception e) {
            log.error("Ingestion failed for symbol {}: {}", symbol, e.getMessage(), e);
            run.setStatus("FAILED");
            run.setErrorMessage(e.getMessage());
        } finally {
            Instant endTime = Instant.now();
            run.setEndTime(endTime);
            run.setDurationMs(Duration.between(startTime, endTime).toMillis());
            run = runRepository.save(run);
        }

        return run;
    }

    @Transactional
    public FundamentalIngestionRun ingestAllUniverse(List<String> symbols) {
        FundamentalDataProvider provider = getActiveProvider();
        Instant startTime = Instant.now();
        FundamentalIngestionRun run = new FundamentalIngestionRun();
        run.setProvider(provider.getProviderName());
        run.setCompaniesRequested(symbols.size());
        run.setStartTime(startTime);
        run.setStatus("RUNNING");

        int totalProcessed = 0;
        int totalFailed = 0;

        try {
            for (String symbol : symbols) {
                try {
                    List<FundamentalFiling> filings = provider.fetchCompanyFilings(symbol);
                    for (FundamentalFiling filing : filings) {
                        processFiling(provider, filing);
                        totalProcessed++;
                    }
                } catch (Exception ex) {
                    log.error("Error ingesting filings for symbol {}: {}", symbol, ex.getMessage(), ex);
                    totalFailed++;
                }
            }

            run.setFilingsInserted(totalProcessed);
            run.setRejectedCount(totalFailed);
            run.setStatus(totalFailed == 0 ? "SUCCESS" : "PARTIAL_SUCCESS");
        } catch (Exception e) {
            run.setStatus("FAILED");
            run.setErrorMessage(e.getMessage());
        } finally {
            Instant endTime = Instant.now();
            run.setEndTime(endTime);
            run.setDurationMs(Duration.between(startTime, endTime).toMillis());
            run = runRepository.save(run);
        }

        return run;
    }

    private void processFiling(FundamentalDataProvider provider, FundamentalFiling filing) {
        // 1. Deduplicate check
        Optional<FundamentalFiling> existing = filingRepository.findByInstrumentIdAndPeriodEndAndPeriodTypeAndReportingBasisAndVersion(
                filing.getInstrumentId(), filing.getPeriodEnd(), filing.getPeriodType(), filing.getReportingBasis(), filing.getVersion()
        );

        FundamentalFiling savedFiling;
        if (existing.isPresent()) {
            savedFiling = existing.get();
        } else {
            savedFiling = filingRepository.save(filing);
        }

        // 2. Fetch Statement
        FinancialStatement statement = provider.fetchFinancialStatement(savedFiling);
        statement.setFilingId(savedFiling.getId());

        // 3. Normalization & Quality Score
        String quality = normalizerService.validateAndAssignQuality(savedFiling, statement);
        savedFiling.setDataQualityScore(quality);
        filingRepository.save(savedFiling);

        Optional<FinancialStatement> existingStatement = statementRepository.findByFilingId(savedFiling.getId());
        FinancialStatement savedStatement;
        if (existingStatement.isPresent()) {
            savedStatement = existingStatement.get();
        } else {
            savedStatement = statementRepository.save(statement);
        }

        // 4. Calculate Financial Ratios (Point-in-Time)
        // Find prior year statement for YoY growth metrics
        FinancialStatement priorStatement = null;
        if (savedFiling.getPeriodType() == PeriodType.ANNUAL) {
            List<FinancialStatement> historical = statementRepository.findPriorStatementsForTtm(
                    savedFiling.getInstrumentId(), PeriodType.ANNUAL, savedFiling.getReportingBasis(),
                    savedFiling.getPeriodEnd().minusYears(1).plusDays(15), savedFiling.getAvailableAt(), 1
            );
            if (!historical.isEmpty()) {
                priorStatement = historical.get(0);
            }
        }

        BigDecimal dummyPrice = new BigDecimal("2850.00");
        BigDecimal dummyMcap = new BigDecimal("1925000.00"); // 19.25 Lakh Cr
        Long dummyShares = 6750000000L;

        FinancialRatio ratios = ratioCalculatorService.computeRatios(
                savedStatement, priorStatement, dummyPrice, dummyShares, dummyMcap
        );

        Optional<FinancialRatio> existingRatio = ratioRepository.findByFilingId(savedFiling.getId());
        if (existingRatio.isEmpty()) {
            ratioRepository.save(ratios);
        }
    }
}
