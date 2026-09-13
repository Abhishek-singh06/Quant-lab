package com.quantlab.regime.service;

import com.quantlab.regime.model.RegimeComponentScoreDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class RegimeNormalizationService {

    /**
     * Normalizes component scores, bounds raw values to [-100.0, +100.0],
     * and dynamically renormalizes effective weights across available non-null signals.
     */
    public List<RegimeComponentScoreDTO> normalizeAndWeightComponents(List<RegimeComponentScoreDTO> rawComponents) {
        if (rawComponents == null || rawComponents.isEmpty()) {
            return List.of();
        }

        List<RegimeComponentScoreDTO> validComponents = new ArrayList<>();
        BigDecimal totalConfiguredWeight = BigDecimal.ZERO;

        for (RegimeComponentScoreDTO comp : rawComponents) {
            if (comp.getRawValue() != null) {
                // Bound normalized score [-100, +100]
                BigDecimal norm = clamp(comp.getNormalizedValue() != null ? comp.getNormalizedValue() : comp.getRawValue());
                comp.setNormalizedValue(norm.setScale(4, RoundingMode.HALF_UP));
                comp.setComponentScore(norm.setScale(4, RoundingMode.HALF_UP));

                BigDecimal weight = comp.getConfiguredWeight() != null ? comp.getConfiguredWeight() : BigDecimal.ZERO;
                totalConfiguredWeight = totalConfiguredWeight.add(weight);
                validComponents.add(comp);
            }
        }

        if (totalConfiguredWeight.compareTo(BigDecimal.ZERO) <= 0) {
            totalConfiguredWeight = BigDecimal.valueOf(validComponents.size());
        }

        // Dynamically renormalize effective weights so sum = 1.0
        for (RegimeComponentScoreDTO comp : validComponents) {
            BigDecimal confWeight = comp.getConfiguredWeight() != null ? comp.getConfiguredWeight() : BigDecimal.ONE;
            BigDecimal effectiveWeight = confWeight.divide(totalConfiguredWeight, 6, RoundingMode.HALF_UP);
            comp.setEffectiveWeight(effectiveWeight.setScale(4, RoundingMode.HALF_UP));
        }

        return validComponents;
    }

    private BigDecimal clamp(BigDecimal val) {
        if (val == null) return BigDecimal.ZERO;
        if (val.compareTo(BigDecimal.valueOf(100.0)) > 0) return BigDecimal.valueOf(100.0);
        if (val.compareTo(BigDecimal.valueOf(-100.0)) < 0) return BigDecimal.valueOf(-100.0);
        return val;
    }
}
