package com.quantlab.global.service;

import com.quantlab.global.calendar.GlobalMarketCalendarService;
import com.quantlab.global.entity.GlobalInstrument;
import com.quantlab.global.entity.GlobalMarketRegime;
import com.quantlab.global.entity.GlobalMarketSnapshot;
import com.quantlab.global.model.*;
import com.quantlab.global.repository.GlobalInstrumentRepository;
import com.quantlab.global.repository.GlobalMarketRegimeRepository;
import com.quantlab.global.repository.GlobalMarketSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class GlobalMarketAnalyticsService {

    private final GlobalInstrumentRepository instrumentRepository;
    private final GlobalMarketSnapshotRepository snapshotRepository;
    private final GlobalMarketRegimeRepository regimeRepository;
    private final GlobalMarketCalendarService calendarService;
    private final GlobalMarketRegimeEngine regimeEngine;

    public GlobalMarketAnalyticsService(
            GlobalInstrumentRepository instrumentRepository,
            GlobalMarketSnapshotRepository snapshotRepository,
            GlobalMarketRegimeRepository regimeRepository,
            GlobalMarketCalendarService calendarService,
            GlobalMarketRegimeEngine regimeEngine) {
        this.instrumentRepository = instrumentRepository;
        this.snapshotRepository = snapshotRepository;
        this.regimeRepository = regimeRepository;
        this.calendarService = calendarService;
        this.regimeEngine = regimeEngine;
    }

    public List<GlobalInstrument> getGlobalInstruments() {
        return instrumentRepository.findByActiveTrue();
    }

    public List<GlobalMarketSnapshotDTO> getLatestSnapshots(Instant asOfTime) {
        if (asOfTime == null) {
            asOfTime = Instant.now();
        }

        List<GlobalInstrument> instruments = instrumentRepository.findByActiveTrue();
        List<GlobalMarketSnapshotDTO> dtos = new ArrayList<>();

        for (GlobalInstrument inst : instruments) {
            Optional<GlobalMarketSnapshot> snapOpt = snapshotRepository.findLatestSnapshotAsOf(inst.getCanonicalSymbol(), asOfTime);
            if (snapOpt.isPresent()) {
                dtos.add(toDTO(inst, snapOpt.get()));
            }
        }
        return dtos;
    }

    public List<GlobalMarketSnapshotDTO> getInstrumentHistory(String symbol, LocalDate fromDate, LocalDate toDate, Instant asOfTime) {
        if (asOfTime == null) {
            asOfTime = Instant.now();
        }
        Optional<GlobalInstrument> instOpt = instrumentRepository.findByCanonicalSymbol(symbol);
        if (instOpt.isEmpty()) {
            return Collections.emptyList();
        }

        GlobalInstrument inst = instOpt.get();
        List<GlobalMarketSnapshot> history = snapshotRepository.findHistoryBySymbolInRangeAsOf(symbol, fromDate, toDate, asOfTime);
        List<GlobalMarketSnapshotDTO> dtos = new ArrayList<>();
        for (GlobalMarketSnapshot s : history) {
            dtos.add(toDTO(inst, s));
        }
        return dtos;
    }

    public GlobalMarketRegimeDTO getLatestRegime(Instant asOfTime) {
        if (asOfTime == null) {
            asOfTime = Instant.now();
        }

        Optional<GlobalMarketRegime> regimeOpt = regimeRepository.findLatestRegimeAsOf(asOfTime);
        if (regimeOpt.isPresent()) {
            return toRegimeDTO(regimeOpt.get());
        }

        // Compute dynamically if no persisted record exists
        List<GlobalMarketSnapshotDTO> snapshots = getLatestSnapshots(asOfTime);
        List<GlobalMarketSnapshot> snapEntities = new ArrayList<>();
        for (GlobalMarketSnapshotDTO d : snapshots) {
            GlobalMarketSnapshot s = new GlobalMarketSnapshot();
            s.setCanonicalSymbol(d.getCanonicalSymbol());
            s.setClose(d.getClose());
            s.setChange(d.getChange());
            s.setChangePercent(d.getChangePercent());
            snapEntities.add(s);
        }
        GlobalMarketRegime calculated = regimeEngine.calculateRegime(snapEntities, asOfTime);
        return toRegimeDTO(calculated);
    }

    public List<GlobalMarketRegimeDTO> getRegimeHistory(Instant fromTime, Instant toTime) {
        List<GlobalMarketRegime> history = regimeRepository.findRegimesInRange(fromTime, toTime);
        List<GlobalMarketRegimeDTO> dtos = new ArrayList<>();
        for (GlobalMarketRegime r : history) {
            dtos.add(toRegimeDTO(r));
        }
        return dtos;
    }

    public List<GlobalMarketStatusDTO> getGlobalMarketStatuses(Instant evaluatedAt) {
        if (evaluatedAt == null) {
            evaluatedAt = Instant.now();
        }

        List<GlobalMarketStatusDTO> statuses = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

        for (GlobalMarket market : GlobalMarket.values()) {
            SessionStatus status = calendarService.evaluateSessionStatus(market, evaluatedAt);
            var zdt = calendarService.getLocalMarketTime(market, evaluatedAt);
            boolean isTrading = calendarService.isTradingDay(market, zdt.toLocalDate());
            String hours = calendarService.getMarketSessionHoursDescription(market);

            statuses.add(new GlobalMarketStatusDTO(
                market,
                getMarketCountry(market),
                calendarService.getMarketZoneId(market).getId(),
                status,
                evaluatedAt,
                zdt.format(fmt),
                hours,
                isTrading
            ));
        }
        return statuses;
    }

    private GlobalMarketSnapshotDTO toDTO(GlobalInstrument inst, GlobalMarketSnapshot snap) {
        GlobalMarketSnapshotDTO dto = new GlobalMarketSnapshotDTO();
        dto.setCanonicalSymbol(inst.getCanonicalSymbol());
        dto.setProviderSymbol(inst.getProviderSymbol());
        dto.setInstrumentName(inst.getInstrumentName());
        dto.setAssetClass(inst.getAssetClass());
        dto.setMarket(inst.getMarket());
        dto.setCountry(inst.getCountry());
        dto.setCurrency(inst.getCurrency());
        dto.setTimezone(inst.getTimezone());
        dto.setTimestamp(snap.getTimestamp());
        dto.setTradingDate(snap.getTradingDate());
        dto.setOpen(snap.getOpen());
        dto.setHigh(snap.getHigh());
        dto.setLow(snap.getLow());
        dto.setClose(snap.getClose());
        dto.setPreviousClose(snap.getPreviousClose());
        dto.setChange(snap.getChange());
        dto.setChangePercent(snap.getChangePercent());
        dto.setVolume(snap.getVolume());
        dto.setYieldRate(snap.getYieldRate());
        dto.setSource(snap.getSource());
        dto.setSourceTimestamp(snap.getSourceTimestamp());
        dto.setIngestionTimestamp(snap.getIngestionTimestamp());
        dto.setDataFreshness(snap.getDataFreshness());
        dto.setSessionStatus(snap.getSessionStatus());
        dto.setAgeMinutes(Duration.between(snap.getSourceTimestamp(), Instant.now()).toMinutes());
        return dto;
    }

    private GlobalMarketRegimeDTO toRegimeDTO(GlobalMarketRegime r) {
        GlobalMarketRegimeDTO dto = new GlobalMarketRegimeDTO();
        dto.setTimestamp(r.getTimestamp());
        dto.setRegimeLabel(r.getRegimeLabel());
        dto.setCompositeScore(r.getCompositeScore());
        dto.setEquityScore(r.getEquityScore());
        dto.setVolatilityScore(r.getVolatilityScore());
        dto.setRatesScore(r.getRatesScore());
        dto.setDollarScore(r.getDollarScore());
        dto.setCommodityScore(r.getCommodityScore());
        dto.setAsiaScore(r.getAsiaScore());
        dto.setEuropeScore(r.getEuropeScore());
        dto.setConfidence(r.getConfidence());
        dto.setExplanation(r.getExplanation());
        dto.setMethodologyVersion(r.getMethodologyVersion());
        dto.setSourceSnapshotCount(r.getSourceSnapshotCount());
        dto.setCalculatedAt(r.getCalculatedAt());

        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        breakdown.put("Equity", r.getEquityScore());
        breakdown.put("Volatility", r.getVolatilityScore());
        breakdown.put("Dollar", r.getDollarScore());
        breakdown.put("Rates", r.getRatesScore());
        breakdown.put("Commodities", r.getCommodityScore());
        breakdown.put("Asia", r.getAsiaScore());
        breakdown.put("Europe", r.getEuropeScore());
        dto.setComponentBreakdown(breakdown);

        return dto;
    }

    private String getMarketCountry(GlobalMarket m) {
        switch (m) {
            case US: return "United States";
            case JAPAN: return "Japan";
            case HONG_KONG: return "Hong Kong";
            case CHINA: return "China";
            case UK: return "United Kingdom";
            case GERMANY: return "Germany";
            case INDIA: return "India";
            default: return "Global";
        }
    }
}
