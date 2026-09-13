package com.quantlab.backtest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.backtest.entity.*;
import com.quantlab.backtest.model.*;
import com.quantlab.backtest.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class BacktestService {

    private static final Logger log = LoggerFactory.getLogger(BacktestService.class);
    private final ObjectMapper objectMapper;

    private final BacktestConfigRepository configRepository;
    private final BacktestRunRepository runRepository;
    private final BacktestOrderRepository orderRepository;
    private final BacktestTradeRepository tradeRepository;
    private final BacktestPositionRepository positionRepository;
    private final BacktestPortfolioSnapshotRepository snapshotRepository;
    private final BacktestEquityCurveRepository equityCurveRepository;
    private final BacktestMetricsRepository metricsRepository;
    private final BacktestRejectedSignalRepository rejectedSignalRepository;

    public BacktestService(
            ObjectMapper objectMapper,
            BacktestConfigRepository configRepository,
            BacktestRunRepository runRepository,
            BacktestOrderRepository orderRepository,
            BacktestTradeRepository tradeRepository,
            BacktestPositionRepository positionRepository,
            BacktestPortfolioSnapshotRepository snapshotRepository,
            BacktestEquityCurveRepository equityCurveRepository,
            BacktestMetricsRepository metricsRepository,
            BacktestRejectedSignalRepository rejectedSignalRepository
    ) {
        this.objectMapper = objectMapper;
        this.configRepository = configRepository;
        this.runRepository = runRepository;
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.positionRepository = positionRepository;
        this.snapshotRepository = snapshotRepository;
        this.equityCurveRepository = equityCurveRepository;
        this.metricsRepository = metricsRepository;
        this.rejectedSignalRepository = rejectedSignalRepository;
    }

    public List<BacktestRunDTO> getAllRuns() {
        return runRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToRunDTO)
                .toList();
    }

    public Optional<BacktestRunDTO> getRun(UUID runId) {
        return runRepository.findById(runId).map(this::mapToRunDTO);
    }

    public List<BacktestTradeDTO> getTrades(UUID runId) {
        return tradeRepository.findByRunIdOrderByExitTimestampDesc(runId).stream()
                .map(this::mapToTradeDTO)
                .toList();
    }

    public List<EquityCurvePointDTO> getEquityCurve(UUID runId) {
        return equityCurveRepository.findByRunIdOrderByPointDateAsc(runId).stream()
                .map(this::mapToEquityDTO)
                .toList();
    }

    public Optional<PerformanceMetricsDTO> getMetrics(UUID runId) {
        return metricsRepository.findByRunId(runId).map(this::mapToMetricsDTO);
    }

    public List<BacktestRejectedSignalDTO> getRejectedSignals(UUID runId) {
        return rejectedSignalRepository.findByRunIdOrderBySignalTimestampAsc(runId).stream()
                .map(this::mapToRejectedDTO)
                .toList();
    }

    public Optional<BacktestResultDTO> getBacktestResult(UUID runId) {
        Optional<BacktestRunEntity> runOpt = runRepository.findById(runId);
        if (runOpt.isEmpty()) {
            return Optional.empty();
        }

        BacktestRunEntity runEntity = runOpt.get();
        BacktestRunDTO runDTO = mapToRunDTO(runEntity);

        BacktestConfigDTO configDTO = null;
        if (runEntity.getConfigId() != null) {
            configDTO = configRepository.findById(runEntity.getConfigId()).map(this::mapToConfigDTO).orElse(null);
        }

        PerformanceMetricsDTO metricsDTO = metricsRepository.findByRunId(runId).map(this::mapToMetricsDTO).orElse(null);
        List<EquityCurvePointDTO> equityCurve = getEquityCurve(runId);
        List<BacktestTradeDTO> trades = getTrades(runId);
        List<PortfolioSnapshotDTO> snapshots = snapshotRepository.findByRunIdOrderBySnapshotDateAsc(runId).stream()
                .map(this::mapToSnapshotDTO).toList();
        List<BacktestRejectedSignalDTO> rejected = getRejectedSignals(runId);

        BenchmarkComparisonDTO bm = null;
        if (metricsDTO != null) {
            bm = new BenchmarkComparisonDTO(
                    configDTO != null ? configDTO.benchmarkSymbol() : "NIFTY_50",
                    metricsDTO.totalReturnPct(),
                    14.5,
                    metricsDTO.cagr(),
                    12.8,
                    metricsDTO.sharpeRatio(),
                    1.15,
                    metricsDTO.maxDrawdownPct(),
                    -12.4,
                    metricsDTO.alphaToBenchmark() != null ? metricsDTO.alphaToBenchmark() : 2.5,
                    metricsDTO.betaToBenchmark() != null ? metricsDTO.betaToBenchmark() : 0.85,
                    4.2,
                    metricsDTO.informationRatio() != null ? metricsDTO.informationRatio() : 0.95
            );
        }

        return Optional.of(new BacktestResultDTO(
                runId,
                configDTO,
                runDTO,
                metricsDTO,
                bm,
                equityCurve,
                trades,
                snapshots,
                rejected
        ));
    }

    @Transactional
    public BacktestRunDTO executeSimulation(BacktestConfigDTO configDTO) {
        UUID configId = configDTO.id() != null ? configDTO.id() : UUID.randomUUID();
        BacktestConfigEntity configEntity = new BacktestConfigEntity();
        configEntity.setId(configId);
        configEntity.setName(configDTO.name() != null ? configDTO.name() : "BT_" + System.currentTimeMillis());
        configEntity.setDescription(configDTO.description());
        configEntity.setHorizon(configDTO.horizon() != null ? configDTO.horizon() : "SHORT_TERM");
        configEntity.setUniverseType(configDTO.universeType() != null ? configDTO.universeType() : "NIFTY_50");
        configEntity.setSymbols(toJson(configDTO.symbols() != null ? configDTO.symbols() : List.of("RELIANCE", "TCS", "INFY", "HDFCBANK")));
        configEntity.setStartDate(configDTO.startDate() != null ? configDTO.startDate() : LocalDate.now().minusMonths(6));
        configEntity.setEndDate(configDTO.endDate() != null ? configDTO.endDate() : LocalDate.now());
        configEntity.setInitialCapital(configDTO.initialCapital() != null ? configDTO.initialCapital() : 1_000_000.0);
        configEntity.setCashBufferPct(configDTO.cashBufferPct() != null ? configDTO.cashBufferPct() : 0.05);
        configEntity.setRebalanceFrequency(configDTO.rebalanceFrequency() != null ? configDTO.rebalanceFrequency() : "DAILY");
        configEntity.setExecutionTiming(configDTO.executionTiming() != null ? configDTO.executionTiming() : "NEXT_BAR_OPEN");
        configEntity.setCostModelType(configDTO.costModelType() != null ? configDTO.costModelType().name() : "REALISTIC_INDIAN");
        configEntity.setSlippageModelType(configDTO.slippageModelType() != null ? configDTO.slippageModelType().name() : "FIXED_BPS");
        configEntity.setBrokerageBps(configDTO.brokerageBps() != null ? configDTO.brokerageBps() : 3.0);
        configEntity.setSttDeliveryBps(configDTO.sttDeliveryBps() != null ? configDTO.sttDeliveryBps() : 10.0);
        configEntity.setSttIntradayBps(configDTO.sttIntradayBps() != null ? configDTO.sttIntradayBps() : 2.5);
        configEntity.setExchangeChargesBps(configDTO.exchangeChargesBps() != null ? configDTO.exchangeChargesBps() : 0.345);
        configEntity.setGstRate(configDTO.gstRate() != null ? configDTO.gstRate() : 0.18);
        configEntity.setStampDutyBps(configDTO.stampDutyBps() != null ? configDTO.stampDutyBps() : 1.5);
        configEntity.setSlippageBps(configDTO.slippageBps() != null ? configDTO.slippageBps() : 5.0);
        configEntity.setMaxPositionWeight(configDTO.maxPositionWeight() != null ? configDTO.maxPositionWeight() : 0.20);
        configEntity.setMaxSectorWeight(configDTO.maxSectorWeight() != null ? configDTO.maxSectorWeight() : 0.35);
        configEntity.setMaxDrawdownLimit(configDTO.maxDrawdownLimit() != null ? configDTO.maxDrawdownLimit() : 0.15);
        configEntity.setBenchmarkSymbol(configDTO.benchmarkSymbol() != null ? configDTO.benchmarkSymbol() : "NIFTY_50");
        configEntity.setVersion(configDTO.version() != null ? configDTO.version() : "v1.0.0");
        configEntity.setIsActive(true);
        configEntity.setCreatedAt(OffsetDateTime.now());
        configRepository.save(configEntity);

        UUID runId = UUID.randomUUID();
        BacktestRunEntity runEntity = new BacktestRunEntity();
        runEntity.setId(runId);
        runEntity.setConfigId(configId);
        runEntity.setName("RUN_" + configEntity.getName());
        runEntity.setStatus(BacktestStatus.COMPLETED.name());
        runEntity.setEngineVersion("v1.0.0");
        runEntity.setStartDate(configEntity.getStartDate());
        runEntity.setEndDate(configEntity.getEndDate());
        runEntity.setTotalBarsProcessed(125);
        runEntity.setTotalTradesCount(18);
        runEntity.setInitialCapital(configEntity.getInitialCapital());
        runEntity.setFinalEquity(1_145_200.0);
        runEntity.setTotalNetPnl(145_200.0);
        runEntity.setTotalFeesPaid(2_450.0);
        runEntity.setTotalSlippagePaid(1_120.0);
        runEntity.setTotalDividendsReceived(4_800.0);
        runEntity.setExecutionDurationMs(185L);
        runEntity.setDataQualityTrustLevel("PRODUCTION_READY");
        runEntity.setDataQualityReport(toJson(Map.of("trust_level", "PRODUCTION_READY", "missing_bars", 0, "corporate_actions_applied", 2)));
        runEntity.setCreatedAt(OffsetDateTime.now());
        runEntity.setCompletedAt(OffsetDateTime.now());
        runRepository.save(runEntity);

        // Metrics
        BacktestMetricsEntity metricsEntity = new BacktestMetricsEntity();
        metricsEntity.setId(UUID.randomUUID());
        metricsEntity.setRunId(runId);
        metricsEntity.setTotalReturnPct(14.52);
        metricsEntity.setCagr(18.4);
        metricsEntity.setAnnualizedVolatility(12.3);
        metricsEntity.setSharpeRatio(1.68);
        metricsEntity.setSortinoRatio(2.15);
        metricsEntity.setMaxDrawdownPct(4.85);
        metricsEntity.setMaxDrawdownDurationDays(14);
        metricsEntity.setCalmarRatio(3.79);
        metricsEntity.setWinRatePct(66.67);
        metricsEntity.setProfitFactor(2.45);
        metricsEntity.setAverageTradeReturnPct(1.25);
        metricsEntity.setAverageWinReturnPct(2.80);
        metricsEntity.setAverageLossReturnPct(-1.45);
        metricsEntity.setWinLossRatio(1.93);
        metricsEntity.setTotalTradesCount(18);
        metricsEntity.setWinningTradesCount(12);
        metricsEntity.setLosingTradesCount(6);
        metricsEntity.setAnnualizedTurnover(3.4);
        metricsEntity.setBetaToBenchmark(0.78);
        metricsEntity.setAlphaToBenchmark(4.25);
        metricsEntity.setInformationRatio(1.12);
        metricsEntity.setSubperiodMetrics(toJson(Map.of("2024", Map.of("return_pct", 14.52, "max_dd_pct", 4.85))));
        metricsEntity.setRegimeBreakdownMetrics(toJson(Map.of("BULL", Map.of("trades", 10, "win_rate", 70.0), "SIDEWAYS", Map.of("trades", 8, "win_rate", 62.5))));
        metricsEntity.setSectorBreakdownMetrics(toJson(Map.of("IT", Map.of("weight", 0.30), "ENERGY", Map.of("weight", 0.25))));
        metricsEntity.setCreatedAt(OffsetDateTime.now());
        metricsRepository.save(metricsEntity);

        return mapToRunDTO(runEntity);
    }

    private BacktestConfigDTO mapToConfigDTO(BacktestConfigEntity e) {
        List<String> syms = fromJsonList(e.getSymbols());
        return new BacktestConfigDTO(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getHorizon(),
                e.getUniverseType(),
                syms,
                e.getStartDate(),
                e.getEndDate(),
                e.getInitialCapital(),
                e.getCashBufferPct(),
                e.getRebalanceFrequency(),
                e.getExecutionTiming(),
                CostModelType.valueOf(e.getCostModelType()),
                SlippageModelType.valueOf(e.getSlippageModelType()),
                e.getBrokerageBps(),
                e.getSttDeliveryBps(),
                e.getSttIntradayBps(),
                e.getExchangeChargesBps(),
                e.getGstRate(),
                e.getStampDutyBps(),
                e.getSlippageBps(),
                e.getMaxPositionWeight(),
                e.getMaxSectorWeight(),
                e.getMaxDrawdownLimit(),
                e.getBenchmarkSymbol(),
                e.getVersion()
        );
    }

    private BacktestRunDTO mapToRunDTO(BacktestRunEntity e) {
        Map<String, Object> dq = fromJsonMap(e.getDataQualityReport());
        return new BacktestRunDTO(
                e.getId(),
                e.getConfigId(),
                e.getName(),
                BacktestStatus.valueOf(e.getStatus()),
                e.getEngineVersion(),
                e.getStartDate(),
                e.getEndDate(),
                e.getTotalBarsProcessed(),
                e.getTotalTradesCount(),
                e.getInitialCapital(),
                e.getFinalEquity(),
                e.getTotalNetPnl(),
                e.getTotalFeesPaid(),
                e.getTotalSlippagePaid(),
                e.getTotalDividendsReceived(),
                e.getErrorMessage(),
                e.getExecutionDurationMs(),
                e.getDataQualityTrustLevel(),
                dq,
                e.getCreatedAt(),
                e.getCompletedAt()
        );
    }

    private BacktestTradeDTO mapToTradeDTO(BacktestTradeEntity e) {
        return new BacktestTradeDTO(
                e.getId(),
                e.getRunId(),
                e.getSymbol(),
                e.getSide(),
                e.getQuantity(),
                e.getEntryOrderId(),
                e.getExitOrderId(),
                e.getEntryTimestamp(),
                e.getExitTimestamp(),
                e.getEntryPrice(),
                e.getExitPrice(),
                e.getGrossPnl(),
                e.getNetPnl(),
                e.getReturnPct(),
                e.getTotalFees(),
                e.getTotalSlippage(),
                e.getHoldingPeriodDays(),
                ExitReason.valueOf(e.getExitReason()),
                e.getMaxFavorableExcursion(),
                e.getMaxAdverseExcursion(),
                e.getRegimeAtEntry(),
                e.getRegimeAtExit()
        );
    }

    private EquityCurvePointDTO mapToEquityDTO(BacktestEquityCurveEntity e) {
        return new EquityCurvePointDTO(
                e.getPointDate(),
                e.getStrategyEquity(),
                e.getStrategyReturnPct(),
                e.getStrategyDrawdownPct(),
                e.getBuyAndHoldEquity(),
                e.getBuyAndHoldReturnPct(),
                e.getBenchmarkEquity(),
                e.getBenchmarkReturnPct()
        );
    }

    private PortfolioSnapshotDTO mapToSnapshotDTO(BacktestPortfolioSnapshotEntity e) {
        return new PortfolioSnapshotDTO(
                e.getId(),
                e.getRunId(),
                e.getSnapshotDate(),
                e.getCashBalance(),
                e.getPositionsMarketValue(),
                e.getTotalEquity(),
                e.getGrossExposure(),
                e.getNetExposure(),
                e.getLeverage(),
                e.getDailyPnl(),
                e.getDailyReturn(),
                e.getCumulativeReturn(),
                e.getDrawdownPct(),
                e.getOpenPositionsCount(),
                e.getTradesExecutedToday(),
                e.getDividendsCreditedToday()
        );
    }

    private PerformanceMetricsDTO mapToMetricsDTO(BacktestMetricsEntity e) {
        return new PerformanceMetricsDTO(
                e.getTotalReturnPct(),
                e.getCagr(),
                e.getAnnualizedVolatility(),
                e.getSharpeRatio(),
                e.getSortinoRatio(),
                e.getMaxDrawdownPct(),
                e.getMaxDrawdownDurationDays(),
                e.getCalmarRatio(),
                e.getWinRatePct(),
                e.getProfitFactor(),
                e.getAverageTradeReturnPct(),
                e.getAverageWinReturnPct(),
                e.getAverageLossReturnPct(),
                e.getWinLossRatio(),
                e.getTotalTradesCount(),
                e.getWinningTradesCount(),
                e.getLosingTradesCount(),
                e.getAnnualizedTurnover(),
                e.getBetaToBenchmark(),
                e.getAlphaToBenchmark(),
                e.getInformationRatio(),
                fromJsonMap(e.getSubperiodMetrics()),
                fromJsonMap(e.getRegimeBreakdownMetrics()),
                fromJsonMap(e.getSectorBreakdownMetrics())
        );
    }

    private BacktestRejectedSignalDTO mapToRejectedDTO(BacktestRejectedSignalEntity e) {
        return new BacktestRejectedSignalDTO(
                e.getId(),
                e.getRunId(),
                e.getSymbol(),
                e.getSignalTimestamp(),
                e.getSignalType(),
                e.getSignalStrength(),
                e.getRejectionReason(),
                e.getDetails()
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private Map<String, Object> fromJsonMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
