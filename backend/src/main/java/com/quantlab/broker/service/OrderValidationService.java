package com.quantlab.broker.service;

import com.quantlab.marketdata.detector.IndianTradingCalendar;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class OrderValidationService {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final LocalTime MARKET_OPEN = LocalTime.of(9, 15);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

    private final IndianTradingCalendar tradingCalendar;

    public OrderValidationService(IndianTradingCalendar tradingCalendar) {
        this.tradingCalendar = tradingCalendar;
    }

    public void validateOrderParameters(
            String symbol, String side, String orderType, int quantity, double price,
            Double stopLossPrice, Double targetPrice, double availableFunds) {

        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be empty.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (price <= 0.0) {
            throw new IllegalArgumentException("Price must be strictly positive.");
        }
        if (!"BUY".equalsIgnoreCase(side) && !"SELL".equalsIgnoreCase(side)) {
            throw new IllegalArgumentException("Order side must be either BUY or SELL.");
        }
        if (!"LIMIT".equalsIgnoreCase(orderType) && !"MARKET".equalsIgnoreCase(orderType)
                && !"STOP_LOSS".equalsIgnoreCase(orderType) && !"STOP_LIMIT".equalsIgnoreCase(orderType)) {
            throw new IllegalArgumentException("Unsupported order type: " + orderType);
        }

        // Validate tick size (NSE tick size is 0.05)
        double tickRemainder = Math.abs((price * 100) % 5);
        if (tickRemainder > 0.01 && tickRemainder < 4.99) {
            throw new IllegalArgumentException(String.format("Price %.2f violates minimum NSE tick size of 0.05", price));
        }

        // Stop loss & target validations
        if ("BUY".equalsIgnoreCase(side)) {
            if (stopLossPrice != null && stopLossPrice >= price) {
                throw new IllegalArgumentException("Stop-loss price for a BUY order must be strictly below order price.");
            }
            if (targetPrice != null && targetPrice <= price) {
                throw new IllegalArgumentException("Target price for a BUY order must be strictly above order price.");
            }
            double estimatedCost = quantity * price;
            if (availableFunds > 0 && estimatedCost > availableFunds) {
                throw new IllegalArgumentException(String.format("Insufficient funds: Order value %.2f exceeds available cash %.2f", estimatedCost, availableFunds));
            }
        } else {
            if (stopLossPrice != null && stopLossPrice <= price) {
                throw new IllegalArgumentException("Stop-loss price for a SELL order must be strictly above order price.");
            }
            if (targetPrice != null && targetPrice >= price) {
                throw new IllegalArgumentException("Target price for a SELL order must be strictly below order price.");
            }
        }
    }

    public boolean isMarketSessionOpen(ZonedDateTime now) {
        ZonedDateTime ist = now != null ? now.withZoneSameInstant(IST_ZONE) : ZonedDateTime.now(IST_ZONE);
        LocalDate date = ist.toLocalDate();
        LocalTime time = ist.toLocalTime();

        if (tradingCalendar.isHoliday(date) || tradingCalendar.isWeekend(date)) {
            return false;
        }

        return !time.isBefore(MARKET_OPEN) && !time.isAfter(MARKET_CLOSE);
    }
}
