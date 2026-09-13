package com.quantlab.broker;

import com.quantlab.broker.service.OrderValidationService;
import com.quantlab.marketdata.detector.IndianTradingCalendar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class OrderValidationServiceTest {

    private OrderValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new OrderValidationService(new IndianTradingCalendar());
    }

    @Test
    void testValidBuyOrderParameters() {
        assertDoesNotThrow(() ->
                validationService.validateOrderParameters(
                        "RELIANCE", "BUY", "LIMIT", 10, 2850.50, 2800.00, 2950.00, 100000.0
                )
        );
    }

    @Test
    void testZeroOrNegativeQuantityThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                validationService.validateOrderParameters(
                        "RELIANCE", "BUY", "LIMIT", 0, 2850.00, null, null, 100000.0
                )
        );
    }

    @Test
    void testInvalidTickSizeThrows() {
        // 2850.13 violates tick size of 0.05
        assertThrows(IllegalArgumentException.class, () ->
                validationService.validateOrderParameters(
                        "RELIANCE", "BUY", "LIMIT", 10, 2850.13, null, null, 100000.0
                )
        );
    }

    @Test
    void testBuyStopLossAbovePriceThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                validationService.validateOrderParameters(
                        "RELIANCE", "BUY", "LIMIT", 10, 2850.00, 2900.00, 3000.00, 100000.0
                )
        );
    }

    @Test
    void testInsufficientFundsThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                validationService.validateOrderParameters(
                        "RELIANCE", "BUY", "LIMIT", 100, 2850.00, 2800.00, 3000.00, 10000.0
                )
        );
    }

    @Test
    void testMarketHoursDetection() {
        ZoneId ist = ZoneId.of("Asia/Kolkata");
        // Regular trading Wednesday at 10:30 AM
        ZonedDateTime openTime = ZonedDateTime.of(LocalDate.of(2026, 9, 16), LocalTime.of(10, 30), ist);
        assertTrue(validationService.isMarketSessionOpen(openTime));

        // Sunday at 10:30 AM
        ZonedDateTime weekend = ZonedDateTime.of(LocalDate.of(2026, 9, 13), LocalTime.of(10, 30), ist);
        assertFalse(validationService.isMarketSessionOpen(weekend));

        // Wednesday at 7:00 PM (market closed)
        ZonedDateTime afterHours = ZonedDateTime.of(LocalDate.of(2026, 9, 16), LocalTime.of(19, 0), ist);
        assertFalse(validationService.isMarketSessionOpen(afterHours));
    }
}
