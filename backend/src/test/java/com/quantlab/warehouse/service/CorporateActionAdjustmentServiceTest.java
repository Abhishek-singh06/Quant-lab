package com.quantlab.warehouse.service;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.warehouse.entity.CorporateAction;
import com.quantlab.warehouse.entity.HistoricalPriceAdjusted;
import com.quantlab.warehouse.entity.HistoricalPriceRaw;
import com.quantlab.warehouse.model.CorporateActionType;
import com.quantlab.warehouse.model.TimeGranularity;
import com.quantlab.warehouse.repository.CorporateActionRepository;
import com.quantlab.warehouse.repository.HistoricalPriceAdjustedRepository;
import com.quantlab.warehouse.repository.HistoricalPriceRawRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CorporateActionAdjustmentServiceTest {

    private HistoricalPriceRawRepository rawPriceRepository;
    private HistoricalPriceAdjustedRepository adjustedPriceRepository;
    private CorporateActionRepository corporateActionRepository;
    private CorporateActionAdjustmentService adjustmentService;

    @BeforeEach
    void setUp() {
        rawPriceRepository = mock(HistoricalPriceRawRepository.class);
        adjustedPriceRepository = mock(HistoricalPriceAdjustedRepository.class);
        corporateActionRepository = mock(CorporateActionRepository.class);

        when(adjustedPriceRepository.save(any(HistoricalPriceAdjusted.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adjustmentService = new CorporateActionAdjustmentService(
            rawPriceRepository, adjustedPriceRepository, corporateActionRepository
        );
    }

    @Test
    void appliesSplitAdjustmentToHistoricalPricesPriorToExDate() {
        LocalDate d1 = LocalDate.of(2024, 2, 28);
        LocalDate d2 = LocalDate.of(2024, 3, 1); // Ex-date for 2:1 split (factor 0.5)

        HistoricalPriceRaw raw1 = new HistoricalPriceRaw(
            1L, "RELIANCE", Exchange.NSE, d1, Instant.now(),
            new BigDecimal("2000.00"), new BigDecimal("2020.00"),
            new BigDecimal("1990.00"), new BigDecimal("2000.00"),
            100_000L, new BigDecimal("200000000.00"), TimeGranularity.DAILY, "NSE"
        );
        raw1.setId(101L);

        HistoricalPriceRaw raw2 = new HistoricalPriceRaw(
            1L, "RELIANCE", Exchange.NSE, d2, Instant.now(),
            new BigDecimal("1000.00"), new BigDecimal("1010.00"),
            new BigDecimal("995.00"), new BigDecimal("1005.00"),
            150_000L, new BigDecimal("150750000.00"), TimeGranularity.DAILY, "NSE"
        );
        raw2.setId(102L);

        when(rawPriceRepository.findPricesInRange(eq(1L), eq(Exchange.NSE), eq(TimeGranularity.DAILY), any(), any()))
            .thenReturn(List.of(raw1, raw2));

        CorporateAction split = new CorporateAction(
            1L, "RELIANCE", CorporateActionType.STOCK_SPLIT,
            LocalDate.of(2024, 2, 15), d2, d2, Instant.now(),
            new BigDecimal("0.50000000"), null, "NSE"
        );

        when(corporateActionRepository.findInDateRange(eq(1L), any(), any()))
            .thenReturn(List.of(split));

        List<HistoricalPriceAdjusted> adjusted = adjustmentService.computeAndSaveAdjustments(
            1L, Exchange.NSE, d1, d2
        );

        assertEquals(2, adjusted.size());

        // d1 price (before ex-date) must be scaled by 0.5: 2000 * 0.5 = 1000
        HistoricalPriceAdjusted adj1 = adjusted.get(0);
        assertEquals(0, new BigDecimal("1000.0000").compareTo(adj1.getAdjClose()));
        assertEquals(0, new BigDecimal("0.50000000").compareTo(adj1.getCumulativeSplitFactor()));

        // d2 price (on ex-date) has factor 1.0: 1005 * 1.0 = 1005
        HistoricalPriceAdjusted adj2 = adjusted.get(1);
        assertEquals(0, new BigDecimal("1005.0000").compareTo(adj2.getAdjClose()));
        assertEquals(0, BigDecimal.ONE.compareTo(adj2.getCumulativeSplitFactor()));
    }
}
