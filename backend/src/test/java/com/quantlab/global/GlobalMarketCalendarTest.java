package com.quantlab.global;

import com.quantlab.global.calendar.GlobalMarketCalendarService;
import com.quantlab.global.model.GlobalMarket;
import com.quantlab.global.model.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class GlobalMarketCalendarTest {

    private GlobalMarketCalendarService calendarService;

    @BeforeEach
    void setUp() {
        calendarService = new GlobalMarketCalendarService();
    }

    @Test
    void testWeekendDetection() {
        // Saturday 2026-01-10 12:00 UTC
        Instant saturday = Instant.parse("2026-01-10T12:00:00Z");
        assertEquals(SessionStatus.WEEKEND, calendarService.evaluateSessionStatus(GlobalMarket.US, saturday));
        assertEquals(SessionStatus.WEEKEND, calendarService.evaluateSessionStatus(GlobalMarket.JAPAN, saturday));
    }

    @Test
    void testUSMarketTradingHours() {
        // Wednesday 2026-01-14 15:00 UTC (10:00 EST) -> OPEN
        Instant usTradingTime = Instant.parse("2026-01-14T15:00:00Z");
        assertEquals(SessionStatus.OPEN, calendarService.evaluateSessionStatus(GlobalMarket.US, usTradingTime));

        // Wednesday 2026-01-14 22:00 UTC (17:00 EST) -> CLOSED
        Instant usPostMarket = Instant.parse("2026-01-14T22:00:00Z");
        assertEquals(SessionStatus.CLOSED, calendarService.evaluateSessionStatus(GlobalMarket.US, usPostMarket));
    }

    @Test
    void testJapanMarketTradingHours() {
        // Wednesday 2026-01-14 01:00 UTC (10:00 JST) -> OPEN
        Instant tokyoMorning = Instant.parse("2026-01-14T01:00:00Z");
        assertEquals(SessionStatus.OPEN, calendarService.evaluateSessionStatus(GlobalMarket.JAPAN, tokyoMorning));

        // Wednesday 2026-01-14 09:00 UTC (18:00 JST) -> CLOSED
        Instant tokyoEvening = Instant.parse("2026-01-14T09:00:00Z");
        assertEquals(SessionStatus.CLOSED, calendarService.evaluateSessionStatus(GlobalMarket.JAPAN, tokyoEvening));
    }

    @Test
    void testHolidayDetection() {
        // US Independence Day: July 4 (if weekday)
        LocalDate july4 = LocalDate.of(2026, 7, 4);
        assertFalse(calendarService.isTradingDay(GlobalMarket.US, july4));
    }
}
