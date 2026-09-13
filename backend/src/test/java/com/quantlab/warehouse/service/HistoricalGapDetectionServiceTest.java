package com.quantlab.warehouse.service;

import com.quantlab.marketdata.detector.IndianTradingCalendar;
import com.quantlab.warehouse.model.DataQualityReport;
import com.quantlab.warehouse.repository.HistoricalDataQualityRepository;
import com.quantlab.warehouse.repository.HistoricalPriceRawRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HistoricalGapDetectionServiceTest {

    private HistoricalPriceRawRepository rawPriceRepository;
    private HistoricalDataQualityRepository dataQualityRepository;
    private IndianTradingCalendar calendar;
    private HistoricalGapDetectionService gapDetectionService;

    @BeforeEach
    void setUp() {
        rawPriceRepository = mock(HistoricalPriceRawRepository.class);
        dataQualityRepository = mock(HistoricalDataQualityRepository.class);
        calendar = new IndianTradingCalendar();

        gapDetectionService = new HistoricalGapDetectionService(
            rawPriceRepository, dataQualityRepository, calendar
        );
    }

    @Test
    void detectsMissingTradingDaysAccurately() {
        // Range: Monday 2026-09-14 to Friday 2026-09-18 (5 trading days)
        LocalDate from = LocalDate.of(2026, 9, 14);
        LocalDate to = LocalDate.of(2026, 9, 18);

        // Database only has Monday, Tuesday, Thursday, Friday (Wednesday 2026-09-16 is missing)
        List<LocalDate> actualDates = List.of(
            LocalDate.of(2026, 9, 14),
            LocalDate.of(2026, 9, 15),
            LocalDate.of(2026, 9, 17),
            LocalDate.of(2026, 9, 18)
        );

        when(rawPriceRepository.findTradingDatesInRange(eq(1L), eq(from), eq(to)))
            .thenReturn(actualDates);

        DataQualityReport report = gapDetectionService.auditInstrumentHistory("run-1", 1L, "TCS", from, to);

        assertNotNull(report);
        assertEquals(5, report.expectedTradingDays());
        assertEquals(4, report.actualTradingDays());
        assertEquals(1, report.missingDaysCount());
        assertTrue(report.missingDates().contains(LocalDate.of(2026, 9, 16)));

        verify(dataQualityRepository, times(1)).save(any());
    }
}
