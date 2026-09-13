package com.quantlab.global;

import com.quantlab.global.entity.GlobalMarketRegime;
import com.quantlab.global.entity.GlobalMarketSnapshot;
import com.quantlab.global.model.RegimeConfidence;
import com.quantlab.global.model.RegimeLabel;
import com.quantlab.global.service.GlobalMarketRegimeEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobalMarketRegimeEngineTest {

    private GlobalMarketRegimeEngine regimeEngine;

    @BeforeEach
    void setUp() {
        regimeEngine = new GlobalMarketRegimeEngine();
    }

    @Test
    void testRiskOnRegimeClassification() {
        List<GlobalMarketSnapshot> snapshots = new ArrayList<>();

        // Strong US Equities rally
        snapshots.add(createSnapshot("SPX", new BigDecimal("5800.00"), new BigDecimal("1.20")));
        snapshots.add(createSnapshot("NASDAQ", new BigDecimal("18200.00"), new BigDecimal("1.60")));
        snapshots.add(createSnapshot("DJI", new BigDecimal("42000.00"), new BigDecimal("0.80")));
        snapshots.add(createSnapshot("RUT", new BigDecimal("2200.00"), new BigDecimal("1.40")));

        // Low Volatility
        snapshots.add(createSnapshot("VIX", new BigDecimal("13.20"), new BigDecimal("-4.50")));

        // Softening Dollar
        snapshots.add(createSnapshot("DXY", new BigDecimal("102.50"), new BigDecimal("-0.40")));

        // Strong Asia & Europe
        snapshots.add(createSnapshot("N225", new BigDecimal("39000.00"), new BigDecimal("1.10")));
        snapshots.add(createSnapshot("HSI", new BigDecimal("20500.00"), new BigDecimal("1.50")));
        snapshots.add(createSnapshot("FTSE", new BigDecimal("8200.00"), new BigDecimal("0.70")));
        snapshots.add(createSnapshot("DAX", new BigDecimal("19200.00"), new BigDecimal("0.90")));

        GlobalMarketRegime regime = regimeEngine.calculateRegime(snapshots, Instant.now());

        assertEquals(RegimeLabel.RISK_ON, regime.getRegimeLabel());
        assertTrue(regime.getCompositeScore().compareTo(BigDecimal.valueOf(25.0)) > 0);
        assertTrue(regime.getEquityScore().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(regime.getVolatilityScore().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(regime.getExplanation());
    }

    @Test
    void testRiskOffRegimeClassification() {
        List<GlobalMarketSnapshot> snapshots = new ArrayList<>();

        // Sharp selloff
        snapshots.add(createSnapshot("SPX", new BigDecimal("5600.00"), new BigDecimal("-2.10")));
        snapshots.add(createSnapshot("NASDAQ", new BigDecimal("17500.00"), new BigDecimal("-2.80")));
        snapshots.add(createSnapshot("DJI", new BigDecimal("41000.00"), new BigDecimal("-1.50")));
        snapshots.add(createSnapshot("RUT", new BigDecimal("2100.00"), new BigDecimal("-2.40")));

        // VIX spike > 25
        snapshots.add(createSnapshot("VIX", new BigDecimal("28.50"), new BigDecimal("25.00")));

        // Surging Dollar
        snapshots.add(createSnapshot("DXY", new BigDecimal("105.20"), new BigDecimal("0.85")));

        GlobalMarketRegime regime = regimeEngine.calculateRegime(snapshots, Instant.now());

        assertEquals(RegimeLabel.RISK_OFF, regime.getRegimeLabel());
        assertTrue(regime.getCompositeScore().compareTo(BigDecimal.valueOf(-25.0)) < 0);
        assertTrue(regime.getEquityScore().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(regime.getVolatilityScore().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void testConfidenceDegradesWithSparseData() {
        List<GlobalMarketSnapshot> sparseSnapshots = List.of(
            createSnapshot("SPX", new BigDecimal("5800.00"), new BigDecimal("0.10")),
            createSnapshot("VIX", new BigDecimal("17.00"), new BigDecimal("0.00"))
        );

        GlobalMarketRegime regime = regimeEngine.calculateRegime(sparseSnapshots, Instant.now());
        assertEquals(RegimeConfidence.LOW, regime.getConfidence());
        assertEquals(2, regime.getSourceSnapshotCount());
    }

    private GlobalMarketSnapshot createSnapshot(String symbol, BigDecimal close, BigDecimal changePercent) {
        GlobalMarketSnapshot s = new GlobalMarketSnapshot();
        s.setCanonicalSymbol(symbol);
        s.setClose(close);
        s.setChangePercent(changePercent);
        s.setChange(close.multiply(changePercent).divide(BigDecimal.valueOf(100)));
        s.setSource("MOCK");
        s.setTimestamp(Instant.now());
        s.setSourceTimestamp(Instant.now());
        return s;
    }
}
