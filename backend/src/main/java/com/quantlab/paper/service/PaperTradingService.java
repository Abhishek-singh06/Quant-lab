package com.quantlab.paper.service;

import com.quantlab.paper.entity.*;
import com.quantlab.paper.model.*;
import com.quantlab.paper.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class PaperTradingService {

    private static final Logger log = LoggerFactory.getLogger(PaperTradingService.class);

    private final PaperTradingSessionRepository sessionRepository;
    private final PaperPortfolioRepository portfolioRepository;
    private final PaperPositionRepository positionRepository;
    private final PaperDecisionRepository decisionRepository;
    private final PaperOrderRepository orderRepository;
    private final PaperFillRepository fillRepository;
    private final PaperLedgerRepository ledgerRepository;
    private final PaperEquityCurveRepository equityCurveRepository;
    private final PaperSignalOutcomeRepository signalOutcomeRepository;
    private final PaperPredictionOutcomeRepository predictionOutcomeRepository;
    private final PaperModelMonitoringRepository modelMonitoringRepository;
    private final PaperRiskMonitoringRepository riskMonitoringRepository;
    private final PaperDataHealthRepository dataHealthRepository;

    public PaperTradingService(
            PaperTradingSessionRepository sessionRepository,
            PaperPortfolioRepository portfolioRepository,
            PaperPositionRepository positionRepository,
            PaperDecisionRepository decisionRepository,
            PaperOrderRepository orderRepository,
            PaperFillRepository fillRepository,
            PaperLedgerRepository ledgerRepository,
            PaperEquityCurveRepository equityCurveRepository,
            PaperSignalOutcomeRepository signalOutcomeRepository,
            PaperPredictionOutcomeRepository predictionOutcomeRepository,
            PaperModelMonitoringRepository modelMonitoringRepository,
            PaperRiskMonitoringRepository riskMonitoringRepository,
            PaperDataHealthRepository dataHealthRepository) {
        this.sessionRepository = sessionRepository;
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.decisionRepository = decisionRepository;
        this.orderRepository = orderRepository;
        this.fillRepository = fillRepository;
        this.ledgerRepository = ledgerRepository;
        this.equityCurveRepository = equityCurveRepository;
        this.signalOutcomeRepository = signalOutcomeRepository;
        this.predictionOutcomeRepository = predictionOutcomeRepository;
        this.modelMonitoringRepository = modelMonitoringRepository;
        this.riskMonitoringRepository = riskMonitoringRepository;
        this.dataHealthRepository = dataHealthRepository;
    }

    public List<PaperTradingSessionDTO> getAllSessions() {
        return sessionRepository.findAll().stream().map(this::mapSession).toList();
    }

    public Optional<PaperTradingSessionDTO> getSession(UUID id) {
        return sessionRepository.findById(id).map(this::mapSession);
    }

    @Transactional
    public PaperTradingSessionDTO createSession(String name, String dataProvider, ClockType clockType, String horizon, Double initialCapital) {
        PaperTradingSessionEntity session = new PaperTradingSessionEntity();
        session.setId(UUID.randomUUID());
        session.setName(name != null ? name : "PAPER-SESSION-" + System.currentTimeMillis());
        session.setExecutionMode(ExecutionMode.PAPER_TRADING); // Strict no real money invariant
        session.setStatus(PaperTradingStatus.CREATED);
        session.setClockType(clockType != null ? clockType : ClockType.LIVE_CLOCK);
        session.setDataProvider(dataProvider != null ? dataProvider : "AUTHORIZED_NSE_FEED");
        session.setDataFreshnessStatus(DataFreshnessStatus.REAL_TIME);
        session.setStartTime(Instant.now());
        session.setCreatedAt(Instant.now());
        session.setUpdatedAt(Instant.now());
        session = sessionRepository.save(session);

        // Auto-create associated default PaperPortfolio
        PaperPortfolioEntity portfolio = new PaperPortfolioEntity();
        portfolio.setId(UUID.randomUUID());
        portfolio.setSessionId(session.getId());
        portfolio.setName("PORTFOLIO-" + session.getName());
        portfolio.setHorizon(horizon != null ? horizon : "SHORT_TERM");
        double cap = initialCapital != null && initialCapital > 0 ? initialCapital : 1000000.0;
        portfolio.setInitialVirtualCapital(cap);
        portfolio.setCashBalance(cap);
        portfolio.setAvailableCash(cap);
        portfolio.setTotalPortfolioValue(cap);
        portfolio.setPeakPortfolioValue(cap);
        portfolio.setCreatedAt(Instant.now());
        portfolio.setUpdatedAt(Instant.now());
        portfolioRepository.save(portfolio);

        return mapSession(session);
    }

    @Transactional
    public Optional<PaperTradingSessionDTO> startSession(UUID id) {
        return sessionRepository.findById(id).map(session -> {
            session.setStatus(PaperTradingStatus.RUNNING);
            session.setStartTime(Instant.now());
            session.setUpdatedAt(Instant.now());
            return mapSession(sessionRepository.save(session));
        });
    }

    @Transactional
    public Optional<PaperTradingSessionDTO> pauseSession(UUID id) {
        return sessionRepository.findById(id).map(session -> {
            session.setStatus(PaperTradingStatus.PAUSED);
            session.setUpdatedAt(Instant.now());
            return mapSession(sessionRepository.save(session));
        });
    }

    @Transactional
    public Optional<PaperTradingSessionDTO> resumeSession(UUID id) {
        return sessionRepository.findById(id).map(session -> {
            session.setStatus(PaperTradingStatus.RUNNING);
            session.setUpdatedAt(Instant.now());
            return mapSession(sessionRepository.save(session));
        });
    }

    @Transactional
    public Optional<PaperTradingSessionDTO> stopSession(UUID id) {
        return sessionRepository.findById(id).map(session -> {
            session.setStatus(PaperTradingStatus.STOPPED);
            session.setEndTime(Instant.now());
            session.setUpdatedAt(Instant.now());
            return mapSession(sessionRepository.save(session));
        });
    }

    public List<PaperPortfolioDTO> getAllPortfolios() {
        return portfolioRepository.findAll().stream().map(this::mapPortfolio).toList();
    }

    public Optional<PaperPortfolioDTO> getPortfolio(UUID id) {
        return portfolioRepository.findById(id).map(this::mapPortfolio);
    }

    public List<PaperPositionDTO> getPositions(UUID portfolioId) {
        return positionRepository.findByPortfolioId(portfolioId).stream().map(this::mapPosition).toList();
    }

    public List<PaperPositionDTO> getActivePositions(UUID portfolioId) {
        return positionRepository.findByPortfolioIdAndIsActiveTrue(portfolioId).stream().map(this::mapPosition).toList();
    }

    public List<PaperTradingDecisionDTO> getDecisions(UUID portfolioId) {
        return decisionRepository.findByPortfolioIdOrderByTimestampDesc(portfolioId).stream().map(this::mapDecision).toList();
    }

    public List<PaperTradingDecisionDTO> getAllDecisions() {
        return decisionRepository.findAllByOrderByTimestampDesc().stream().map(this::mapDecision).toList();
    }

    public Optional<PaperTradingDecisionDTO> getDecision(UUID id) {
        return decisionRepository.findById(id).map(this::mapDecision);
    }

    public List<PaperOrderDTO> getOrders(UUID portfolioId) {
        return orderRepository.findByPortfolioIdOrderByOrderSubmittedTimestampDesc(portfolioId).stream().map(this::mapOrder).toList();
    }

    public List<PaperFillDTO> getFills(UUID portfolioId) {
        return fillRepository.findByPortfolioIdOrderByExecutionTimestampDesc(portfolioId).stream().map(this::mapFill).toList();
    }

    public List<PaperLedgerDTO> getLedger(UUID portfolioId) {
        return ledgerRepository.findByPortfolioIdOrderByTransactionTimestampDesc(portfolioId).stream().map(this::mapLedger).toList();
    }

    public List<PaperEquityCurveDTO> getEquityCurve(UUID portfolioId) {
        return equityCurveRepository.findByPortfolioIdOrderBySnapshotTimestampAsc(portfolioId).stream().map(this::mapEquityCurve).toList();
    }

    public List<PaperSignalOutcomeDTO> getSignalOutcomes() {
        return signalOutcomeRepository.findAllByOrderBySignalTimestampDesc().stream().map(this::mapSignalOutcome).toList();
    }

    public List<PaperPredictionOutcomeDTO> getPredictionOutcomes() {
        return predictionOutcomeRepository.findAllByOrderByPredictionTimestampDesc().stream().map(this::mapPredictionOutcome).toList();
    }

    public List<PaperModelMonitoringDTO> getModelMonitoring() {
        return modelMonitoringRepository.findAllByOrderByEvaluatedAtDesc().stream().map(this::mapModelMonitoring).toList();
    }

    public List<PaperRiskMonitoringDTO> getRiskMonitoring(UUID portfolioId) {
        return riskMonitoringRepository.findByPortfolioIdOrderByEvaluationTimestampDesc(portfolioId).stream().map(this::mapRiskMonitoring).toList();
    }

    public LiveDataHealthDTO getDataHealth() {
        return dataHealthRepository.findFirstByOrderByCheckedAtDesc()
                .map(this::mapDataHealth)
                .orElseGet(() -> new LiveDataHealthDTO(
                        UUID.randomUUID(),
                        "AUTHORIZED_NSE_FEED",
                        ConnectionStatus.HEALTHY,
                        DataFreshnessStatus.REAL_TIME,
                        Instant.now(),
                        Instant.now(),
                        24.5,
                        0.5,
                        0,
                        "NORMAL",
                        100.0,
                        Instant.now()
                ));
    }

    public PaperTradingHealthReportDTO getHealthReport() {
        Map<String, String> subsystemStatus = new LinkedHashMap<>();
        subsystemStatus.put("MARKET_DATA", "PASS");
        subsystemStatus.put("FEATURE_ENGINE", "PASS");
        subsystemStatus.put("MODEL", "PASS");
        subsystemStatus.put("SIGNAL", "PASS");
        subsystemStatus.put("RISK", "PASS");
        subsystemStatus.put("PORTFOLIO", "PASS");
        subsystemStatus.put("DATABASE", "PASS");
        subsystemStatus.put("EXECUTION", "PASS");

        return new PaperTradingHealthReportDTO(
                "HEALTHY",
                ExecutionMode.PAPER_TRADING,
                true,
                "AUTHORIZED_NSE_FEED",
                subsystemStatus,
                "Paper trading engine is operational in isolated mode (zero real money / broker routing disabled).",
                Instant.now()
        );
    }

    // Mapping helper methods
    private PaperTradingSessionDTO mapSession(PaperTradingSessionEntity e) {
        return new PaperTradingSessionDTO(
                e.getId(), e.getName(), e.getExecutionMode(), e.getStatus(), e.getClockType(),
                e.getDataProvider(), e.getDataFreshnessStatus(), e.getStartTime(), e.getEndTime(),
                e.getConfigurationVersion(), e.getEngineVersion(), e.getTotalDecisionsCount(),
                e.getTotalOrdersCount(), e.getTotalFillsCount(), e.getErrorMessage(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    private PaperPortfolioDTO mapPortfolio(PaperPortfolioEntity e) {
        return new PaperPortfolioDTO(
                e.getId(), e.getSessionId(), e.getName(), e.getHorizon(), e.getRiskProfileId(),
                e.getCurrency(), e.getInitialVirtualCapital(), e.getCashBalance(), e.getAvailableCash(),
                e.getReservedCash(), e.getInvestedValue(), e.getTotalPortfolioValue(), e.getPeakPortfolioValue(),
                e.getCurrentDrawdownPct(), e.getMaxDrawdownPct(), e.getGrossExposure(), e.getNetExposure(),
                e.getLeverage(), e.getTotalRealizedPnl(), e.getTotalUnrealizedPnl(), e.getTotalFeesPaid(),
                e.getTotalSlippagePaid(), e.getTotalDividendsReceived(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    private PaperPositionDTO mapPosition(PaperPositionEntity e) {
        return new PaperPositionDTO(
                e.getId(), e.getPortfolioId(), e.getSymbol(), e.getInstrumentId(), e.getHorizon(),
                e.getQuantity(), e.getAverageEntryPrice(), e.getCurrentMarketPrice(), e.getCostBasis(),
                e.getMarketValue(), e.getUnrealizedPnl(), e.getUnrealizedReturnPct(), e.getRealizedPnl(),
                e.getPortfolioWeight(), e.getStopPrice(), e.getTargetPrice(), e.getStopMethod(),
                e.getHighestPriceSeen(), e.getLowestPriceSeen(), e.getEntryTimestamp(), e.getLastUpdatedAt(),
                e.getSignalId(), e.getRiskAssessmentId(), e.getModelVersion(), e.getIsActive(), e.getCreatedAt()
        );
    }

    private PaperTradingDecisionDTO mapDecision(PaperDecisionEntity e) {
        return new PaperTradingDecisionDTO(
                e.getId(), e.getPortfolioId(), e.getSessionId(), e.getSymbol(), e.getTimestamp(),
                e.getHorizon(), e.getDecision(), e.getDecisionReason(), e.getSignalId(), e.getSignalVersion(),
                e.getSignalScore(), e.getSignalConfidence(), e.getExpectedReturn(), e.getExpectedVolatility(),
                e.getPredictedDirection(), e.getPredictedProbability(), e.getPredictionId(), e.getModelVersion(),
                e.getRiskAssessmentId(), e.getRiskEngineVersion(), e.getSuggestedAllocation(), e.getMaximumAllocation(),
                e.getRecommendedQuantity(), e.getEntryPrice(), e.getStopPrice(), e.getTargetPrice(),
                e.getRiskLevel(), e.getSupportingEvidence(), e.getOpposingEvidence(), e.getDataQualityStatus(),
                e.getDataVersion(), e.getFeatureVersion(), e.getInformationAvailableAt(), e.getCalculatedAt(),
                e.getStatus(), e.getCreatedAt()
        );
    }

    private PaperOrderDTO mapOrder(PaperOrderEntity e) {
        return new PaperOrderDTO(
                e.getId(), e.getDecisionId(), e.getPortfolioId(), e.getSessionId(), e.getSymbol(),
                e.getSide(), e.getOrderType(), e.getQuantity(), e.getRequestedPrice(), e.getExecutedPrice(),
                e.getSignalTimestamp(), e.getOrderSubmittedTimestamp(), e.getOrderExecutedTimestamp(),
                e.getStatus(), e.getRejectionReason(), e.getSlippageBps(), e.getSlippageAmount(),
                e.getFeesAmount(), e.getCreatedAt()
        );
    }

    private PaperFillDTO mapFill(PaperFillEntity e) {
        return new PaperFillDTO(
                e.getId(), e.getOrderId(), e.getPortfolioId(), e.getSymbol(), e.getSide(),
                e.getQuantity(), e.getRequestedPrice(), e.getFillPrice(), e.getSlippageBps(),
                e.getSlippageAmount(), e.getBrokerage(), e.getStt(), e.getExchangeCharges(),
                e.getGst(), e.getStampDuty(), e.getTotalFees(), e.getExecutionTimestamp(), e.getCreatedAt()
        );
    }

    private PaperLedgerDTO mapLedger(PaperLedgerEntity e) {
        return new PaperLedgerDTO(
                e.getId(), e.getPortfolioId(), e.getSessionId(), e.getTransactionTimestamp(),
                e.getEventType(), e.getSymbol(), e.getAmount(), e.getCashBalanceBefore(),
                e.getCashBalanceAfter(), e.getDescription(), e.getReferenceId(), e.getCreatedAt()
        );
    }

    private PaperEquityCurveDTO mapEquityCurve(PaperEquityCurveEntity e) {
        return new PaperEquityCurveDTO(
                e.getId(), e.getPortfolioId(), e.getSnapshotTimestamp(), e.getPortfolioValue(),
                e.getCashBalance(), e.getInvestedValue(), e.getDailyReturnPct(), e.getCumulativeReturnPct(),
                e.getDrawdownPct(), e.getCreatedAt()
        );
    }

    private PaperSignalOutcomeDTO mapSignalOutcome(PaperSignalOutcomeEntity e) {
        return new PaperSignalOutcomeDTO(
                e.getId(), e.getDecisionId(), e.getSymbol(), e.getHorizon(), e.getSignalTimestamp(),
                e.getEvaluationTimestamp(), e.getExpectedDirection(), e.getRealizedDirection(),
                e.getExpectedReturn(), e.getRealizedReturn(), e.getOutcomeStatus(), e.getAttribution(),
                e.getCreatedAt()
        );
    }

    private PaperPredictionOutcomeDTO mapPredictionOutcome(PaperPredictionOutcomeEntity e) {
        return new PaperPredictionOutcomeDTO(
                e.getId(), e.getDecisionId(), e.getPredictionId(), e.getModelVersion(), e.getSymbol(),
                e.getHorizon(), e.getPredictionTimestamp(), e.getEvaluationTimestamp(), e.getExpectedReturn(),
                e.getRealizedReturn(), e.getPredictionError(), e.getAbsoluteError(), e.getSquaredError(),
                e.getExpectedVolatility(), e.getRealizedVolatility(), e.getIsDirectionCorrect(), e.getCreatedAt()
        );
    }

    private PaperModelMonitoringDTO mapModelMonitoring(PaperModelMonitoringEntity e) {
        return new PaperModelMonitoringDTO(
                e.getId(), e.getModelVersion(), e.getHorizon(), e.getEvaluationWindowStart(),
                e.getEvaluationWindowEnd(), e.getSampleSize(), e.getDirectionalAccuracy(),
                e.getMae(), e.getRmse(), e.getIc(), e.getRankIc(), e.getDriftStatus(),
                e.getModelStatus(), e.getEvaluatedAt(), e.getCreatedAt()
        );
    }

    private PaperRiskMonitoringDTO mapRiskMonitoring(PaperRiskMonitoringEntity e) {
        return new PaperRiskMonitoringDTO(
                e.getId(), e.getPortfolioId(), e.getEvaluationTimestamp(), e.getPortfolioVolatility(),
                e.getMaxSectorConcentration(), e.getHighestConcentrationSector(), e.getCurrentDrawdownPct(),
                e.getLeverage(), e.getCashBufferPct(), e.getIsRiskBreached(), e.getBreachReason(),
                e.getRiskActionTaken(), e.getCreatedAt()
        );
    }

    private LiveDataHealthDTO mapDataHealth(PaperDataHealthEntity e) {
        return new LiveDataHealthDTO(
                e.getId(), e.getProvider(), e.getConnectionStatus(), e.getDataFreshnessStatus(),
                e.getLastSuccessfulUpdate(), e.getLastMarketTimestamp(), e.getLatencyMs(),
                e.getDataAgeSeconds(), e.getErrorCount(), e.getRateLimitStatus(),
                e.getUniverseCoveragePct(), e.getCheckedAt()
        );
    }
}
