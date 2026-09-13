package com.quantlab.global.calendar;

import com.quantlab.global.model.GlobalMarket;
import com.quantlab.global.model.SessionStatus;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Timezone-aware global market session and trading calendar service.
 * Determines market open, closed, holiday, and weekend states across international financial centers.
 */
@Service
public class GlobalMarketCalendarService {

    private static final Map<GlobalMarket, ZoneId> MARKET_TIMEZONES = Map.of(
        GlobalMarket.US, ZoneId.of("America/New_York"),
        GlobalMarket.JAPAN, ZoneId.of("Asia/Tokyo"),
        GlobalMarket.HONG_KONG, ZoneId.of("Asia/Hong_Kong"),
        GlobalMarket.CHINA, ZoneId.of("Asia/Shanghai"),
        GlobalMarket.UK, ZoneId.of("Europe/London"),
        GlobalMarket.GERMANY, ZoneId.of("Europe/Berlin"),
        GlobalMarket.INDIA, ZoneId.of("Asia/Kolkata"),
        GlobalMarket.GLOBAL, ZoneId.of("America/New_York")
    );

    // Fixed key annual holidays (MM-DD)
    private static final Map<GlobalMarket, Set<MonthDay>> FIXED_HOLIDAYS = Map.of(
        GlobalMarket.US, Set.of(
            MonthDay.of(1, 1),   // New Year's Day
            MonthDay.of(7, 4),   // Independence Day
            MonthDay.of(12, 25)  // Christmas Day
        ),
        GlobalMarket.JAPAN, Set.of(
            MonthDay.of(1, 1),
            MonthDay.of(5, 3),
            MonthDay.of(11, 3)
        ),
        GlobalMarket.UK, Set.of(
            MonthDay.of(1, 1),
            MonthDay.of(12, 25),
            MonthDay.of(12, 26)
        ),
        GlobalMarket.INDIA, Set.of(
            MonthDay.of(1, 26),  // Republic Day
            MonthDay.of(8, 15),  // Independence Day
            MonthDay.of(10, 2),  // Gandhi Jayanti
            MonthDay.of(12, 25)
        )
    );

    public ZoneId getMarketZoneId(GlobalMarket market) {
        return MARKET_TIMEZONES.getOrDefault(market, ZoneId.of("UTC"));
    }

    public ZonedDateTime getLocalMarketTime(GlobalMarket market, Instant instant) {
        return instant.atZone(getMarketZoneId(market));
    }

    public boolean isTradingDay(GlobalMarket market, LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return false;
        }

        Set<MonthDay> holidays = FIXED_HOLIDAYS.get(market);
        if (holidays != null && holidays.contains(MonthDay.from(date))) {
            return false;
        }
        return true;
    }

    public SessionStatus evaluateSessionStatus(GlobalMarket market, Instant instant) {
        if (instant == null) {
            instant = Instant.now();
        }

        ZoneId zone = getMarketZoneId(market);
        ZonedDateTime localTime = instant.atZone(zone);
        LocalDate localDate = localTime.toLocalDate();
        LocalTime time = localTime.toLocalTime();
        DayOfWeek dow = localTime.getDayOfWeek();

        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return SessionStatus.WEEKEND;
        }

        if (!isTradingDay(market, localDate)) {
            return SessionStatus.HOLIDAY;
        }

        switch (market) {
            case US:
                // 09:30 to 16:00 EST
                if (time.isAfter(LocalTime.of(9, 29, 59)) && time.isBefore(LocalTime.of(16, 0, 1))) {
                    return SessionStatus.OPEN;
                }
                return SessionStatus.CLOSED;

            case JAPAN:
                // Morning: 09:00 - 11:30 JST, Afternoon: 12:30 - 15:30 JST
                if ((time.isAfter(LocalTime.of(8, 59, 59)) && time.isBefore(LocalTime.of(11, 30, 1))) ||
                    (time.isAfter(LocalTime.of(12, 29, 59)) && time.isBefore(LocalTime.of(15, 30, 1)))) {
                    return SessionStatus.OPEN;
                }
                return SessionStatus.CLOSED;

            case HONG_KONG:
                // 09:30 - 12:00 HKT, 13:00 - 16:00 HKT
                if ((time.isAfter(LocalTime.of(9, 29, 59)) && time.isBefore(LocalTime.of(12, 0, 1))) ||
                    (time.isAfter(LocalTime.of(12, 59, 59)) && time.isBefore(LocalTime.of(16, 0, 1)))) {
                    return SessionStatus.OPEN;
                }
                return SessionStatus.CLOSED;

            case CHINA:
                // 09:30 - 11:30 CST, 13:00 - 15:00 CST
                if ((time.isAfter(LocalTime.of(9, 29, 59)) && time.isBefore(LocalTime.of(11, 30, 1))) ||
                    (time.isAfter(LocalTime.of(12, 59, 59)) && time.isBefore(LocalTime.of(15, 0, 1)))) {
                    return SessionStatus.OPEN;
                }
                return SessionStatus.CLOSED;

            case UK:
                // 08:00 - 16:30 GMT/BST
                if (time.isAfter(LocalTime.of(7, 59, 59)) && time.isBefore(LocalTime.of(16, 30, 1))) {
                    return SessionStatus.OPEN;
                }
                return SessionStatus.CLOSED;

            case GERMANY:
                // 09:00 - 17:30 CET/CEST
                if (time.isAfter(LocalTime.of(8, 59, 59)) && time.isBefore(LocalTime.of(17, 30, 1))) {
                    return SessionStatus.OPEN;
                }
                return SessionStatus.CLOSED;

            case INDIA:
                // 09:15 - 15:30 IST
                if (time.isAfter(LocalTime.of(9, 14, 59)) && time.isBefore(LocalTime.of(15, 30, 1))) {
                    return SessionStatus.OPEN;
                }
                return SessionStatus.CLOSED;

            case GLOBAL:
                // FX and Commodity 24h market during weekdays
                return SessionStatus.OPEN;

            default:
                return SessionStatus.CLOSED;
        }
    }

    public String getMarketSessionHoursDescription(GlobalMarket market) {
        switch (market) {
            case US: return "09:30 - 16:00 EST";
            case JAPAN: return "09:00-11:30, 12:30-15:30 JST";
            case HONG_KONG: return "09:30-12:00, 13:00-16:00 HKT";
            case CHINA: return "09:30-11:30, 13:00-15:00 CST";
            case UK: return "08:00 - 16:30 GMT";
            case GERMANY: return "09:00 - 17:30 CET";
            case INDIA: return "09:15 - 15:30 IST";
            case GLOBAL: return "24/5 Global Trading";
            default: return "Standard Market Hours";
        }
    }
}
