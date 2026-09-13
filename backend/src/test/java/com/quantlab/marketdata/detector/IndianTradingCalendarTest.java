package com.quantlab.marketdata.detector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IndianTradingCalendarTest {

    private IndianTradingCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new IndianTradingCalendar();
    }

    @Test
    void detectsWeekendsAsNonTradingDays() {
        // Saturday & Sunday
        LocalDate saturday = LocalDate.of(2026, 9, 12);
        LocalDate sunday = LocalDate.of(2026, 9, 13);
        LocalDate monday = LocalDate.of(2026, 9, 14);

        assertFalse(calendar.isTradingDay(saturday));
        assertFalse(calendar.isTradingDay(sunday));
        assertTrue(calendar.isTradingDay(monday));
    }

    @Test
    void detectsOfficialNSEHolidays() {
        // Republic Day
        LocalDate republicDay = LocalDate.of(2026, 1, 26);
        // Christmas
        LocalDate christmas = LocalDate.of(2026, 12, 25);

        assertFalse(calendar.isTradingDay(republicDay));
        assertFalse(calendar.isTradingDay(christmas));
    }

    @Test
    void checksMarketOpenHoursInIST() {
        ZoneId ist = ZoneId.of("Asia/Kolkata");

        // Monday 10:30 AM IST (Open)
        ZonedDateTime openTime = ZonedDateTime.of(2026, 9, 14, 10, 30, 0, 0, ist);
        assertTrue(calendar.isMarketOpen(openTime.toInstant()));

        // Monday 08:30 AM IST (Before open)
        ZonedDateTime preOpen = ZonedDateTime.of(2026, 9, 14, 8, 30, 0, 0, ist);
        assertFalse(calendar.isMarketOpen(preOpen.toInstant()));

        // Monday 16:00 PM IST (After close)
        ZonedDateTime postClose = ZonedDateTime.of(2026, 9, 14, 16, 0, 0, 0, ist);
        assertFalse(calendar.isMarketOpen(postClose.toInstant()));
    }
}
