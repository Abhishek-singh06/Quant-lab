package com.quantlab.warehouse.service;

import com.quantlab.marketdata.detector.IndianTradingCalendar;
import com.quantlab.warehouse.entity.HistoricalDataQuality;
import com.quantlab.warehouse.model.DataQualityReport;
import com.quantlab.warehouse.repository.HistoricalDataQualityRepository;
import com.quantlab.warehouse.repository.HistoricalPriceRawRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Historical Data Gap & Quality Analysis Service.
 * Evaluates completeness of historical time series against the Indian trading calendar.
 */
@Service
public class HistoricalGapDetectionService {

    private static final Logger log = LoggerFactory.getLogger(HistoricalGapDetectionService.class);

    private final HistoricalPriceRawRepository rawPriceRepository;
    private final HistoricalDataQualityRepository dataQualityRepository;
    private final IndianTradingCalendar calendar;

    public HistoricalGapDetectionService(
            HistoricalPriceRawRepository rawPriceRepository,
            HistoricalDataQualityRepository dataQualityRepository,
            IndianTradingCalendar calendar) {
        this.rawPriceRepository = rawPriceRepository;
        this.dataQualityRepository = dataQualityRepository;
        this.calendar = calendar;
    }

    public DataQualityReport auditInstrumentHistory(String runId, Long instrumentId, String symbol,
                                                    LocalDate fromDate, LocalDate toDate) {
        List<LocalDate> expectedTradingDays = new ArrayList<>();
        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            if (calendar.isTradingDay(current)) {
                expectedTradingDays.add(current);
            }
            current = current.plusDays(1);
        }

        List<LocalDate> actualTradingDates = rawPriceRepository.findTradingDatesInRange(instrumentId, fromDate, toDate);
        Set<LocalDate> actualSet = new HashSet<>(actualTradingDates);

        List<LocalDate> missingDays = new ArrayList<>();
        for (LocalDate expected : expectedTradingDays) {
            if (!actualSet.contains(expected)) {
                missingDays.add(expected);
            }
        }

        long duplicates = actualTradingDates.size() - actualSet.size();
        String status = missingDays.isEmpty() ? "CLEAN" : (missingDays.size() > 5 ? "GAPS_DETECTED" : "WARNING");

        HistoricalDataQuality entity = new HistoricalDataQuality(
            runId, instrumentId, symbol, fromDate, toDate,
            expectedTradingDays.size(), actualSet.size(), missingDays.size(), duplicates, status
        );
        dataQualityRepository.save(entity);

        log.info("[GapDetection] Audit for {} ({}-{}): expected={}, actual={}, missing={}, duplicates={}, status={}",
                symbol, fromDate, toDate, expectedTradingDays.size(), actualSet.size(), missingDays.size(), duplicates, status);

        return new DataQualityReport(
            symbol, fromDate, toDate, expectedTradingDays.size(), actualSet.size(),
            missingDays.size(), missingDays, duplicates, 0L, status
        );
    }
}
