package com.quantlab.marketdata.service;

import com.quantlab.marketdata.alert.AlertService;
import com.quantlab.marketdata.config.MarketDataProperties;
import com.quantlab.marketdata.detector.DuplicateDetector;
import com.quantlab.marketdata.detector.MissingDataDetector;
import com.quantlab.marketdata.detector.StaleDataDetector;
import com.quantlab.marketdata.entity.MarketDataIngestionRun;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.IngestionRunStatus;
import com.quantlab.marketdata.model.MarketQuote;
import com.quantlab.marketdata.normalizer.MarketDataNormalizer;
import com.quantlab.marketdata.provider.MarketDataProvider;
import com.quantlab.marketdata.repository.MarketDataErrorRepository;
import com.quantlab.marketdata.repository.MarketDataIngestionRunRepository;
import com.quantlab.marketdata.repository.MarketDataRecordRepository;
import com.quantlab.marketdata.validator.MarketDataValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MarketDataIngestionServiceTest {

    private MarketDataProvider provider;
    private MarketDataRecordRepository recordRepository;
    private MarketDataIngestionRunRepository runRepository;
    private MarketDataErrorRepository errorRepository;
    private AlertService alertService;
    private MarketDataProperties properties;

    private MarketDataIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        provider = mock(MarketDataProvider.class);
        recordRepository = mock(MarketDataRecordRepository.class);
        runRepository = mock(MarketDataIngestionRunRepository.class);
        errorRepository = mock(MarketDataErrorRepository.class);
        alertService = mock(AlertService.class);
        properties = new MarketDataProperties();

        MarketDataNormalizer normalizer = new MarketDataNormalizer();
        MarketDataValidator validator = new MarketDataValidator(properties);
        DuplicateDetector duplicateDetector = new DuplicateDetector(recordRepository);
        StaleDataDetector staleDataDetector = mock(StaleDataDetector.class);
        MissingDataDetector missingDataDetector = mock(MissingDataDetector.class);

        when(runRepository.save(any(MarketDataIngestionRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ingestionService = new MarketDataIngestionService(
            provider,
            normalizer,
            validator,
            duplicateDetector,
            staleDataDetector,
            missingDataDetector,
            recordRepository,
            runRepository,
            errorRepository,
            alertService,
            properties
        );
    }

    @Test
    void ingestionRunSuccessfullyIngestsValidQuotes() {
        when(provider.getProviderName()).thenReturn("MOCK");

        MarketQuote quote1 = MarketQuote.builder()
            .symbol("RELIANCE")
            .exchange(Exchange.NSE)
            .lastPrice(new BigDecimal("2450.00"))
            .openPrice(new BigDecimal("2440.00"))
            .highPrice(new BigDecimal("2460.00"))
            .lowPrice(new BigDecimal("2430.00"))
            .closePrice(new BigDecimal("2450.00"))
            .prevClosePrice(new BigDecimal("2445.00"))
            .volume(100_000L)
            .timestamp(Instant.now())
            .build();

        when(provider.getQuotes(anyList(), eq(Exchange.NSE))).thenReturn(List.of(quote1));

        MarketDataIngestionRun run = ingestionService.ingestQuotes(List.of("RELIANCE"), Exchange.NSE);

        assertNotNull(run);
        assertEquals(IngestionRunStatus.SUCCESS, run.getStatus());
        assertEquals(1, run.getRecordsReceived());
        assertEquals(1, run.getRecordsAccepted());
        assertEquals(0, run.getRecordsRejected());
        assertEquals(0, run.getDuplicatesCount());

        verify(recordRepository, times(1)).save(any());
    }

    @Test
    void rejectsInvalidQuotesAndLogsErrors() {
        when(provider.getProviderName()).thenReturn("MOCK");

        // Invalid quote with High < Low
        MarketQuote badQuote = MarketQuote.builder()
            .symbol("TCS")
            .exchange(Exchange.NSE)
            .lastPrice(new BigDecimal("3500.00"))
            .highPrice(new BigDecimal("3400.00")) // Invalid high
            .lowPrice(new BigDecimal("3600.00"))
            .timestamp(Instant.now())
            .build();

        when(provider.getQuotes(anyList(), eq(Exchange.NSE))).thenReturn(List.of(badQuote));

        MarketDataIngestionRun run = ingestionService.ingestQuotes(List.of("TCS"), Exchange.NSE);

        assertEquals(IngestionRunStatus.FAILED, run.getStatus());
        assertEquals(1, run.getRecordsReceived());
        assertEquals(0, run.getRecordsAccepted());
        assertEquals(1, run.getRecordsRejected());

        verify(recordRepository, never()).save(any());
        verify(errorRepository, times(1)).save(any());
    }
}
