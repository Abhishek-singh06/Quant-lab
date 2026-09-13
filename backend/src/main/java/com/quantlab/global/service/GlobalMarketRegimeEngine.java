package com.quantlab.global.service;

import com.quantlab.global.entity.GlobalMarketRegime;
import com.quantlab.global.entity.GlobalMarketSnapshot;
import com.quantlab.global.model.RegimeConfidence;
import com.quantlab.global.model.RegimeLabel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transparent, deterministic quantitative engine for classifying Global Market Regime.
 * Produces composite score (-100 to +100), sub-scores, confidence, and human-interpretable rationale.
 */
@Service
public class GlobalMarketRegimeEngine {

    public static final String METHODOLOGY_VERSION = "1.0.0";

    public GlobalMarketRegime calculateRegime(List<GlobalMarketSnapshot> snapshots, Instant evaluationTime) {
        if (snapshots == null || snapshots.isEmpty()) {
            GlobalMarketRegime emptyRegime = new GlobalMarketRegime();
            emptyRegime.setTimestamp(evaluationTime);
            emptyRegime.setRegimeLabel(RegimeLabel.NEUTRAL);
            emptyRegime.setCompositeScore(BigDecimal.ZERO);
            emptyRegime.setEquityScore(BigDecimal.ZERO);
            emptyRegime.setVolatilityScore(BigDecimal.ZERO);
            emptyRegime.setRatesScore(BigDecimal.ZERO);
            emptyRegime.setDollarScore(BigDecimal.ZERO);
            emptyRegime.setCommodityScore(BigDecimal.ZERO);
            emptyRegime.setAsiaScore(BigDecimal.ZERO);
            emptyRegime.setEuropeScore(BigDecimal.ZERO);
            emptyRegime.setConfidence(RegimeConfidence.LOW);
            emptyRegime.setExplanation("Insufficient snapshot data to evaluate global market regime.");
            emptyRegime.setMethodologyVersion(METHODOLOGY_VERSION);
            emptyRegime.setSourceSnapshotCount(0);
            return emptyRegime;
        }

        Map<String, GlobalMarketSnapshot> snapMap = new HashMap<>();
        for (GlobalMarketSnapshot snap : snapshots) {
            snapMap.put(snap.getCanonicalSymbol(), snap);
        }

        // 1. Equity Score (US Indices: SPX, NASDAQ, DJI, RUT) - Weight 25%
        double usEquitySum = 0.0;
        int usEquityCount = 0;
        for (String sym : List.of("SPX", "NASDAQ", "DJI", "RUT")) {
            GlobalMarketSnapshot s = snapMap.get(sym);
            if (s != null && s.getChangePercent() != null) {
                usEquitySum += s.getChangePercent().doubleValue();
                usEquityCount++;
            }
        }
        double avgUsEquityPct = usEquityCount > 0 ? (usEquitySum / usEquityCount) : 0.0;
        double equityScoreVal = clamp(avgUsEquityPct * 50.0, -100.0, 100.0);

        // 2. Volatility Score (VIX) - Weight 20%
        double volScoreVal = 0.0;
        GlobalMarketSnapshot vixSnap = snapMap.get("VIX");
        double vixLevel = 18.0;
        if (vixSnap != null && vixSnap.getClose() != null) {
            vixLevel = vixSnap.getClose().doubleValue();
            if (vixLevel < 14.0) volScoreVal = 75.0;
            else if (vixLevel < 18.0) volScoreVal = 35.0;
            else if (vixLevel < 22.0) volScoreVal = -15.0;
            else if (vixLevel < 30.0) volScoreVal = -60.0;
            else volScoreVal = -95.0;

            if (vixSnap.getChangePercent() != null) {
                volScoreVal -= clamp(vixSnap.getChangePercent().doubleValue() * 3.0, -25.0, 25.0);
            }
        }
        volScoreVal = clamp(volScoreVal, -100.0, 100.0);

        // 3. Dollar Score (DXY & USDINR) - Weight 15% (Strengthening Dollar = Risk-off, Softening = Risk-on)
        double dollarScoreVal = 0.0;
        GlobalMarketSnapshot dxySnap = snapMap.get("DXY");
        if (dxySnap != null && dxySnap.getChangePercent() != null) {
            dollarScoreVal = clamp(-dxySnap.getChangePercent().doubleValue() * 60.0, -100.0, 100.0);
        }

        // 4. Rates Score (US10Y) - Weight 10%
        double ratesScoreVal = 0.0;
        GlobalMarketSnapshot us10ySnap = snapMap.get("US10Y");
        if (us10ySnap != null && us10ySnap.getChange() != null) {
            double yieldChangeBps = us10ySnap.getChange().doubleValue() * 100.0;
            ratesScoreVal = clamp(-yieldChangeBps * 4.0, -100.0, 100.0);
        }

        // 5. Commodities Score (CRUDE_WTI, GOLD) - Weight 10%
        double commodityScoreVal = 0.0;
        GlobalMarketSnapshot crudeSnap = snapMap.get("CRUDE_WTI");
        GlobalMarketSnapshot goldSnap = snapMap.get("GOLD");
        if (crudeSnap != null && crudeSnap.getChangePercent() != null) {
            commodityScoreVal += crudeSnap.getChangePercent().doubleValue() * 20.0;
        }
        if (goldSnap != null && goldSnap.getChangePercent() != null) {
            commodityScoreVal += goldSnap.getChangePercent().doubleValue() * 15.0;
        }
        commodityScoreVal = clamp(commodityScoreVal, -100.0, 100.0);

        // 6. Asia Score (N225, HSI, SSEC) - Weight 10%
        double asiaSum = 0.0;
        int asiaCount = 0;
        for (String sym : List.of("N225", "HSI", "SSEC")) {
            GlobalMarketSnapshot s = snapMap.get(sym);
            if (s != null && s.getChangePercent() != null) {
                asiaSum += s.getChangePercent().doubleValue();
                asiaCount++;
            }
        }
        double avgAsiaPct = asiaCount > 0 ? (asiaSum / asiaCount) : 0.0;
        double asiaScoreVal = clamp(avgAsiaPct * 50.0, -100.0, 100.0);

        // 7. Europe Score (FTSE, DAX) - Weight 10%
        double europeSum = 0.0;
        int europeCount = 0;
        for (String sym : List.of("FTSE", "DAX")) {
            GlobalMarketSnapshot s = snapMap.get(sym);
            if (s != null && s.getChangePercent() != null) {
                europeSum += s.getChangePercent().doubleValue();
                europeCount++;
            }
        }
        double avgEuropePct = europeCount > 0 ? (europeSum / europeCount) : 0.0;
        double europeScoreVal = clamp(avgEuropePct * 50.0, -100.0, 100.0);

        // Composite Score (Weighted: 0.25 Eq + 0.20 Vol + 0.15 Dollar + 0.10 Rates + 0.10 Comm + 0.10 Asia + 0.10 Europe)
        double compositeVal = (0.25 * equityScoreVal) +
                              (0.20 * volScoreVal) +
                              (0.15 * dollarScoreVal) +
                              (0.10 * ratesScoreVal) +
                              (0.10 * commodityScoreVal) +
                              (0.10 * asiaScoreVal) +
                              (0.10 * europeScoreVal);
        compositeVal = clamp(compositeVal, -100.0, 100.0);

        // Regime Classification
        RegimeLabel label;
        if (compositeVal > 25.0) {
            label = RegimeLabel.RISK_ON;
        } else if (compositeVal < -25.0) {
            label = RegimeLabel.RISK_OFF;
        } else if (vixLevel > 24.0) {
            label = RegimeLabel.HIGH_VOLATILITY;
        } else if (vixLevel < 14.0) {
            label = RegimeLabel.LOW_VOLATILITY;
        } else if (Math.abs(compositeVal) < 10.0) {
            label = RegimeLabel.NEUTRAL;
        } else {
            label = RegimeLabel.TRANSITION;
        }

        // Confidence Evaluation
        int totalAvailableAssets = snapMap.size();
        RegimeConfidence confidence;
        if (totalAvailableAssets >= 12) {
            confidence = RegimeConfidence.HIGH;
        } else if (totalAvailableAssets >= 7) {
            confidence = RegimeConfidence.MEDIUM;
        } else {
            confidence = RegimeConfidence.LOW;
        }

        // Explanation text
        StringBuilder exp = new StringBuilder();
        exp.append(String.format("Global Regime is %s (Composite Score: %.2f, Confidence: %s). ", label, compositeVal, confidence));
        exp.append(String.format("US Equities (%.1f%% avg, Score: %.1f). ", avgUsEquityPct, equityScoreVal));
        exp.append(String.format("VIX level at %.2f (Score: %.1f). ", vixLevel, volScoreVal));
        if (dxySnap != null && dxySnap.getClose() != null) {
            exp.append(String.format("DXY at %.2f (Score: %.1f). ", dxySnap.getClose().doubleValue(), dollarScoreVal));
        }
        exp.append(String.format("Regional Breadth: Asia Score %.1f, Europe Score %.1f.", asiaScoreVal, europeScoreVal));

        GlobalMarketRegime regime = new GlobalMarketRegime();
        regime.setTimestamp(evaluationTime);
        regime.setRegimeLabel(label);
        regime.setCompositeScore(BigDecimal.valueOf(compositeVal).setScale(4, RoundingMode.HALF_UP));
        regime.setEquityScore(BigDecimal.valueOf(equityScoreVal).setScale(4, RoundingMode.HALF_UP));
        regime.setVolatilityScore(BigDecimal.valueOf(volScoreVal).setScale(4, RoundingMode.HALF_UP));
        regime.setRatesScore(BigDecimal.valueOf(ratesScoreVal).setScale(4, RoundingMode.HALF_UP));
        regime.setDollarScore(BigDecimal.valueOf(dollarScoreVal).setScale(4, RoundingMode.HALF_UP));
        regime.setCommodityScore(BigDecimal.valueOf(commodityScoreVal).setScale(4, RoundingMode.HALF_UP));
        regime.setAsiaScore(BigDecimal.valueOf(asiaScoreVal).setScale(4, RoundingMode.HALF_UP));
        regime.setEuropeScore(BigDecimal.valueOf(europeScoreVal).setScale(4, RoundingMode.HALF_UP));
        regime.setConfidence(confidence);
        regime.setExplanation(exp.toString());
        regime.setMethodologyVersion(METHODOLOGY_VERSION);
        regime.setSourceSnapshotCount(totalAvailableAssets);
        regime.setCalculatedAt(Instant.now());

        return regime;
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
