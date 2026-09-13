package com.quantlab.global.provider;

import com.quantlab.global.calendar.GlobalMarketCalendarService;
import com.quantlab.global.entity.GlobalInstrument;
import com.quantlab.global.entity.GlobalMarketSnapshot;
import com.quantlab.global.model.AssetClass;
import com.quantlab.global.model.DataFreshness;
import com.quantlab.global.model.SessionStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deterministic test & simulation provider for all 15 global asset coverage benchmarks.
 */
@Component("mockGlobalMarketDataProvider")
public class MockGlobalMarketDataProvider implements GlobalMarketDataProvider {

    private static final String PROVIDER_NAME = "MOCK_GLOBAL_SOURCE";

    private final GlobalMarketCalendarService calendarService;

    // Benchmark sample prices / yields
    private static final Map<String, BigDecimal> BASE_PRICES = Map.ofEntries(
        Map.entry("SPX", new BigDecimal("5864.67")),
        Map.entry("NASDAQ", new BigDecimal("18373.61")),
        Map.entry("DJI", new BigDecimal("42863.86")),
        Map.entry("RUT", new BigDecimal("2234.40")),
        Map.entry("VIX", new BigDecimal("18.24")),
        Map.entry("DXY", new BigDecimal("103.45")),
        Map.entry("US10Y", new BigDecimal("4.0850")),
        Map.entry("USDINR", new BigDecimal("84.0750")),
        Map.entry("N225", new BigDecimal("39605.80")),
        Map.entry("HSI", new BigDecimal("20637.24")),
        Map.entry("SSEC", new BigDecimal("3284.32")),
        Map.entry("FTSE", new BigDecimal("8253.65")),
        Map.entry("DAX", new BigDecimal("19373.83")),
        Map.entry("CRUDE_WTI", new BigDecimal("75.56")),
        Map.entry("GOLD", new BigDecimal("2657.40"))
    );

    public MockGlobalMarketDataProvider(GlobalMarketCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public GlobalMarketSnapshot fetchLatestSnapshot(GlobalInstrument instrument) {
        Instant now = Instant.now();
        SessionStatus sessionStatus = calendarService.evaluateSessionStatus(instrument.getMarket(), now);
        LocalDate tradingDate = LocalDate.now(ZoneId.of(instrument.getTimezone()));

        BigDecimal basePrice = BASE_PRICES.getOrDefault(instrument.getCanonicalSymbol(), new BigDecimal("100.00"));
        BigDecimal changePct = new BigDecimal("0.45");
        BigDecimal changeVal = basePrice.multiply(changePct).divide(new BigDecimal("100.00"), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal prevClose = basePrice.subtract(changeVal);

        GlobalMarketSnapshot snap = new GlobalMarketSnapshot();
        snap.setInstrumentId(instrument.getId());
        snap.setCanonicalSymbol(instrument.getCanonicalSymbol());
        snap.setTimestamp(now);
        snap.setTradingDate(tradingDate);
        snap.setClose(basePrice);
        snap.setPreviousClose(prevClose);
        snap.setChange(changeVal);
        snap.setChangePercent(changePct);
        snap.setCurrency(instrument.getCurrency());
        snap.setSource(PROVIDER_NAME);
        snap.setSourceTimestamp(now.minusSeconds(300)); // 5 mins ago
        snap.setIngestionTimestamp(now);
        snap.setDataFreshness(DataFreshness.DELAYED);
        snap.setSessionStatus(sessionStatus);
        snap.setDataStatus("VALID");

        if (instrument.getAssetClass() == AssetClass.BOND_YIELD) {
            snap.setYieldRate(basePrice);
        } else if (instrument.getAssetClass() == AssetClass.EQUITY_INDEX || instrument.getAssetClass() == AssetClass.COMMODITY) {
            snap.setOpen(prevClose.add(new BigDecimal("2.50")));
            snap.setHigh(basePrice.add(new BigDecimal("10.00")));
            snap.setLow(prevClose.subtract(new BigDecimal("5.00")));
            snap.setVolume(125000000L);
        }

        return snap;
    }

    @Override
    public List<GlobalMarketSnapshot> fetchHistoricalSnapshots(GlobalInstrument instrument, LocalDate fromDate, LocalDate toDate) {
        List<GlobalMarketSnapshot> list = new ArrayList<>();
        BigDecimal current = BASE_PRICES.getOrDefault(instrument.getCanonicalSymbol(), new BigDecimal("100.00"));

        LocalDate d = fromDate;
        while (!d.isAfter(toDate)) {
            if (calendarService.isTradingDay(instrument.getMarket(), d)) {
                ZoneId zone = ZoneId.of(instrument.getTimezone());
                Instant obsTime = d.atTime(16, 0).atZone(zone).toInstant();

                BigDecimal chgPct = new BigDecimal("0.25");
                BigDecimal chgVal = current.multiply(chgPct).divide(new BigDecimal("100.00"), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal prev = current.subtract(chgVal);

                GlobalMarketSnapshot snap = new GlobalMarketSnapshot();
                snap.setInstrumentId(instrument.getId());
                snap.setCanonicalSymbol(instrument.getCanonicalSymbol());
                snap.setTimestamp(obsTime);
                snap.setTradingDate(d);
                snap.setClose(current);
                snap.setPreviousClose(prev);
                snap.setChange(chgVal);
                snap.setChangePercent(chgPct);
                snap.setCurrency(instrument.getCurrency());
                snap.setSource(PROVIDER_NAME);
                snap.setSourceTimestamp(obsTime);
                snap.setIngestionTimestamp(obsTime.plusSeconds(3600));
                snap.setDataFreshness(DataFreshness.END_OF_DAY);
                snap.setSessionStatus(SessionStatus.CLOSED);
                snap.setDataStatus("VALID");

                if (instrument.getAssetClass() == AssetClass.BOND_YIELD) {
                    snap.setYieldRate(current);
                } else {
                    snap.setOpen(prev);
                    snap.setHigh(current.multiply(new BigDecimal("1.01")));
                    snap.setLow(prev.multiply(new BigDecimal("0.99")));
                    snap.setVolume(85000000L);
                }
                list.add(snap);
            }
            d = d.plusDays(1);
        }
        return list;
    }

    @Override
    public List<GlobalMarketSnapshot> fetchLatestSnapshots(List<GlobalInstrument> instruments) {
        List<GlobalMarketSnapshot> list = new ArrayList<>();
        for (GlobalInstrument inst : instruments) {
            list.add(fetchLatestSnapshot(inst));
        }
        return list;
    }
}
