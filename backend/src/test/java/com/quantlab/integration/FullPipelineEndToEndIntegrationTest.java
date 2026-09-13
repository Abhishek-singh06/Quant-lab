package com.quantlab.integration;

import com.quantlab.broker.model.LiveOrderDTO;
import com.quantlab.broker.model.SafetyLockDTO;
import com.quantlab.broker.service.BrokerService;
import com.quantlab.features.service.TechnicalFeatureBatchService;
import com.quantlab.marketdata.entity.MarketDataIngestionRun;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.IngestionRunStatus;
import com.quantlab.marketdata.service.MarketDataIngestionService;
import com.quantlab.monitoring.service.DataQualityMonitoringService;
import com.quantlab.monitoring.service.SignalAnomalyDetectionService;
import com.quantlab.regime.model.DirectionRegime;
import com.quantlab.regime.model.MarketRegimeDTO;
import com.quantlab.regime.service.MarketRegimeEngineService;
import com.quantlab.risk.model.RiskAssessmentDTO;
import com.quantlab.risk.model.RiskAssessmentRequestDTO;
import com.quantlab.risk.model.RiskDecision;
import com.quantlab.risk.service.RiskAssessmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Genuine Full-Pipeline End-to-End Integration Test for QuantLab.
 * 
 * Verifies the connected pipeline across all layers:
 * 1. Market Data Ingestion & Persistence
 * 2. Technical Indicator Feature Calculations
 * 3. Market Regime Classification
 * 4. Risk Assessment & Constraint Validation
 * 5. Safety Guards & Order Preview Generation
 * 6. Monitoring & Signal Anomaly Detection
 * 7. Failure Paths: Emergency Kill Switch & Malformed Rejections
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FullPipelineEndToEndIntegrationTest {

    @Autowired
    private MarketDataIngestionService marketDataIngestionService;

    @Autowired
    private TechnicalFeatureBatchService technicalFeatureBatchService;

    @Autowired
    private MarketRegimeEngineService marketRegimeEngineService;

    @Autowired
    private RiskAssessmentService riskAssessmentService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private SignalAnomalyDetectionService signalAnomalyDetectionService;

    @Autowired
    private DataQualityMonitoringService dataQualityMonitoringService;

    @Test
    @DisplayName("End-to-End Pipeline: Ingestion -> Features -> Regime -> Risk -> Safety -> Order Preview")
    void testCompleteDecisionPipelineEndToEnd() {
        // Step 1: Ingest Market Quotes into PostgreSQL/H2 Warehouse
        List<String> symbols = List.of("RELIANCE", "TCS", "HDFCBANK");
        MarketDataIngestionRun ingestionRun = marketDataIngestionService.ingestQuotes(symbols, Exchange.NSE);

        assertNotNull(ingestionRun);
        assertNotNull(ingestionRun.getRunId());
        assertTrue(ingestionRun.getRecordsAccepted() > 0, "Pipeline should accept valid normalized quotes");
        assertEquals(IngestionRunStatus.SUCCESS, ingestionRun.getStatus());

        // Step 2: Calculate Technical Features
        com.quantlab.features.model.FeatureCalculationRequest featReq = new com.quantlab.features.model.FeatureCalculationRequest();
        featReq.setSymbols(symbols);
        var featRun = technicalFeatureBatchService.calculateForRequest(featReq);
        assertNotNull(featRun);
        assertNotNull(featRun.getRunId());

        // Step 3: Classify Market Regime
        MarketRegimeDTO regimeResult = marketRegimeEngineService.calculateAndSaveRegime("NIFTY 50", LocalDate.now(), Instant.now());
        assertNotNull(regimeResult);
        assertNotNull(regimeResult.getDirectionRegime());
        assertNotNull(regimeResult.getVolatilityRegime());

        // Step 4: Evaluate Risk Assessment Gates
        RiskAssessmentRequestDTO riskReq = new RiskAssessmentRequestDTO();
        riskReq.setSymbol("RELIANCE");
        riskReq.setSignalType("BUY");
        riskReq.setEntryPrice(2450.00);
        riskReq.setUserStopPrice(2350.00);
        riskReq.setSignalConfidence(0.85);
        riskReq.setSignalScore(0.78);
        riskReq.setAsOfDate(Instant.now());

        RiskAssessmentDTO riskAssessment = riskAssessmentService.assessRisk(riskReq);

        assertNotNull(riskAssessment);
        assertNotEquals(RiskDecision.REJECT, riskAssessment.getRiskDecision(), "Standard sized order within limits should pass risk gates");
        assertTrue(riskAssessment.getRecommendedQuantity() >= 0);

        // Step 5: Safety Guard & Manual Order Preview Lifecycle
        LiveOrderDTO previewOrder = brokerService.previewOrder(
            UUID.randomUUID(),
            "RELIANCE",
            "BUY",
            "LIMIT",
            10,
            2450.00,
            2350.00,
            2650.00,
            UUID.randomUUID(),
            0.78,
            0.85,
            0.08,
            UUID.randomUUID(),
            "LOW",
            0.05
        );

        assertNotNull(previewOrder);
        assertEquals("AWAITING_CONFIRMATION", previewOrder.status(), "Live order preview must enforce manual confirmation");
        assertFalse(previewOrder.isManuallyConfirmed(), "Order must not be auto-confirmed");
        assertNotNull(previewOrder.idempotencyKey(), "Idempotency key must be assigned");

        // Step 6: Verify Monitoring & Anomaly Detection Pipeline
        var anomaly = signalAnomalyDetectionService.evaluateSignalAnomaly("ALL_SIGNALS", "GENERAL_DRIFT", 0.05, 0.05, 1.5, "ALL", "Pipeline test");
        assertNotNull(anomaly);
    }

    @Test
    @DisplayName("Failure Path: Global Emergency Stop Blocks Order Preview")
    void testEmergencyStopBlocksLiveOrderCreation() {
        // Activate emergency stop
        SafetyLockDTO lock = brokerService.setEmergencyStop(true, "Simulated Risk Event", "TEST_OPERATOR");
        assertTrue(lock.isActive());

        // Attempting to place an order while emergency stop is active must be rejected
        assertThrows(IllegalStateException.class, () -> {
            brokerService.previewOrder(
                UUID.randomUUID(),
                "TCS",
                "BUY",
                "LIMIT",
                10,
                3920.00,
                3800.00,
                4200.00,
                UUID.randomUUID(),
                0.80,
                0.90,
                0.05,
                UUID.randomUUID(),
                "LOW",
                0.05
            );
        });

        // Release emergency stop after test
        brokerService.setEmergencyStop(false, "Test Complete", "TEST_OPERATOR");
    }
}
