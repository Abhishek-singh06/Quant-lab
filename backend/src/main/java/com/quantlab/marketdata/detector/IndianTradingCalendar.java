package com.quantlab.marketdata.detector;

import org.springframework.stereotype.Component;

import java.time.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Trading Calendar for Indian Capital Markets (NSE / BSE).
 * Respects standard Indian Standard Time (IST, UTC+05:30), normal market hours (09:15 - 15:30 IST),
 * weekends (Saturday/Sunday), and official Indian stock market holidays.
 */
@Component
public class IndianTradingCalendar {

    public static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    public static final LocalTime MARKET_OPEN = LocalTime.of(9, 15);
    public static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

    // Standard NSE holidays for Indian markets (multi-year sample registry)
    private final Set<LocalDate> holidays = new HashSet<>();

    public IndianTradingCalendar() {
        initializeHolidays();
    }

    private void initializeHolidays() {
        // 2024 NSE Holidays
        holidays.add(LocalDate.of(2024, 1, 22)); // Special Holiday
        holidays.add(LocalDate.of(2024, 1, 26)); // Republic Day
        holidays.add(LocalDate.of(2024, 3, 8));  // Mahashivratri
        holidays.add(LocalDate.of(2024, 3, 25)); // Holi
        holidays.add(LocalDate.of(2024, 3, 29)); // Good Friday
        holidays.add(LocalDate.of(2024, 4, 11)); // Id-Ul-Fitr
        holidays.add(LocalDate.of(2024, 4, 17)); // Ram Navami
        holidays.add(LocalDate.of(2024, 5, 1));  // Maharashtra Day
        holidays.add(LocalDate.of(2024, 6, 17)); // Bakri Id
        holidays.add(LocalDate.of(2024, 7, 17)); // Muharram
        holidays.add(LocalDate.of(2024, 8, 15)); // Independence Day
        holidays.add(LocalDate.of(2024, 10, 2)); // Mahatma Gandhi Jayanti
        holidays.add(LocalDate.of(2024, 11, 1)); // Diwali Laxmi Pujan
        holidays.add(LocalDate.of(2024, 11, 15));// Gurunanak Jayanti
        holidays.add(LocalDate.of(2024, 12, 25));// Christmas

        // 2025 NSE Holidays
        holidays.add(LocalDate.of(2025, 2, 26)); // Mahashivratri
        holidays.add(LocalDate.of(2025, 3, 14)); // Holi
        holidays.add(LocalDate.of(2025, 3, 31)); // Id-Ul-Fitr
        holidays.add(LocalDate.of(2025, 4, 10)); // Mahavir Jayanti
        holidays.add(LocalDate.of(2025, 4, 14)); // Dr. Ambedkar Jayanti
        holidays.add(LocalDate.of(2025, 4, 18)); // Good Friday
        holidays.add(LocalDate.of(2025, 5, 1));  // Maharashtra Day
        holidays.add(LocalDate.of(2025, 8, 15)); // Independence Day
        holidays.add(LocalDate.of(2025, 8, 27)); // Ganesh Chaturthi
        holidays.add(LocalDate.of(2025, 10, 2)); // Mahatma Gandhi Jayanti
        holidays.add(LocalDate.of(2025, 10, 21));// Diwali
        holidays.add(LocalDate.of(2025, 11, 5)); // Prakash Gurpurb
        holidays.add(LocalDate.of(2025, 12, 25));// Christmas

        // 2026 NSE Holidays
        holidays.add(LocalDate.of(2026, 1, 26)); // Republic Day
        holidays.add(LocalDate.of(2026, 3, 3));  // Holi
        holidays.add(LocalDate.of(2026, 3, 20)); // Id-Ul-Fitr
        holidays.add(LocalDate.of(2026, 4, 3));  // Good Friday
        holidays.add(LocalDate.of(2026, 4, 14)); // Dr. Ambedkar Jayanti
        holidays.add(LocalDate.of(2026, 5, 1));  // Maharashtra Day
        holidays.add(LocalDate.of(2026, 10, 2)); // Mahatma Gandhi Jayanti
        holidays.add(LocalDate.of(2026, 11, 9)); // Diwali
        holidays.add(LocalDate.of(2026, 12, 25));// Christmas
    }

    public boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public boolean isHoliday(LocalDate date) {
        return holidays.contains(date);
    }

    public boolean isTradingDay(LocalDate date) {
        if (isWeekend(date)) {
            return false;
        }
        return !isHoliday(date);
    }

    public boolean isMarketOpen(Instant instant) {
        ZonedDateTime istTime = instant.atZone(IST_ZONE);
        LocalDate date = istTime.toLocalDate();
        if (!isTradingDay(date)) {
            return false;
        }
        LocalTime time = istTime.toLocalTime();
        return !time.isBefore(MARKET_OPEN) && !time.isAfter(MARKET_CLOSE);
    }

    public void addHoliday(LocalDate date) {
        this.holidays.add(date);
    }

    public Set<LocalDate> getHolidays() {
        return Set.copyOf(holidays);
    }
}
