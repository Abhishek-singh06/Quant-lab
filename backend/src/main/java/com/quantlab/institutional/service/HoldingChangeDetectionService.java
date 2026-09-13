package com.quantlab.institutional.service;

import com.quantlab.institutional.entity.FundHolding;
import com.quantlab.institutional.entity.FundPortfolioDisclosure;
import com.quantlab.institutional.model.HoldingChangeType;
import com.quantlab.institutional.model.PortfolioScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Robust change detection between consecutive mutual fund portfolio disclosures.
 *
 * Rules:
 * 1. Percentage point change (pp) = currentWeight - previousWeight
 * 2. Relative % change = ((currentWeight - previousWeight) / previousWeight) * 100
 * 3. An instrument missing from previous disclosure that appears now is NEW_POSITION (only if prev was COMPLETE).
 * 4. An instrument present previously that is missing now is EXITED_POSITION ONLY if current disclosure is COMPLETE.
 *    If current or previous disclosure was partial (e.g. TOP_10_ONLY), missing stocks must NOT be falsely classified as exits.
 */
@Service
public class HoldingChangeDetectionService {

    private static final Logger log = LoggerFactory.getLogger(HoldingChangeDetectionService.class);
    private static final BigDecimal EPSILON = new BigDecimal("0.0001");

    public List<FundHolding> computeHoldingChanges(
            FundPortfolioDisclosure currentDisclosure,
            List<FundHolding> currentHoldings,
            FundPortfolioDisclosure previousDisclosure,
            List<FundHolding> previousHoldings) {

        if (currentHoldings == null || currentHoldings.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, FundHolding> prevHoldingMap = new HashMap<>();
        if (previousHoldings != null) {
            for (FundHolding prev : previousHoldings) {
                prevHoldingMap.put(prev.getInstrumentId(), prev);
            }
        }

        boolean prevIsComplete = previousDisclosure != null && previousDisclosure.getPortfolioScope() == PortfolioScope.COMPLETE;
        boolean currIsComplete = currentDisclosure != null && currentDisclosure.getPortfolioScope() == PortfolioScope.COMPLETE;

        List<FundHolding> processedHoldings = new ArrayList<>();

        for (FundHolding curr : currentHoldings) {
            FundHolding prev = prevHoldingMap.get(curr.getInstrumentId());

            if (prev == null) {
                // New position if previous disclosure was complete, otherwise indeterminate new entry
                curr.setWeightChangePp(curr.getPortfolioWeight());
                curr.setRelativeWeightChangePercent(new BigDecimal("100.00"));
                curr.setChangeType(prevIsComplete ? HoldingChangeType.NEW_POSITION : HoldingChangeType.INCREASED);
            } else {
                BigDecimal currWeight = curr.getPortfolioWeight() != null ? curr.getPortfolioWeight() : BigDecimal.ZERO;
                BigDecimal prevWeight = prev.getPortfolioWeight() != null ? prev.getPortfolioWeight() : BigDecimal.ZERO;

                BigDecimal weightDiff = currWeight.subtract(prevWeight);
                curr.setWeightChangePp(weightDiff);

                if (prevWeight.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal relChange = weightDiff
                            .divide(prevWeight, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100.00"));
                    curr.setRelativeWeightChangePercent(relChange);
                } else {
                    curr.setRelativeWeightChangePercent(new BigDecimal("100.00"));
                }

                if (weightDiff.abs().compareTo(EPSILON) < 0) {
                    curr.setChangeType(HoldingChangeType.UNCHANGED);
                } else if (weightDiff.compareTo(BigDecimal.ZERO) > 0) {
                    curr.setChangeType(HoldingChangeType.INCREASED);
                } else {
                    curr.setChangeType(HoldingChangeType.DECREASED);
                }
            }
            processedHoldings.add(curr);
        }

        // If current disclosure is COMPLETE, detect completely exited positions
        if (currIsComplete && previousHoldings != null) {
            Set<Long> currInstrumentIds = new HashSet<>();
            for (FundHolding ch : currentHoldings) {
                currInstrumentIds.add(ch.getInstrumentId());
            }

            for (FundHolding prev : previousHoldings) {
                if (!currInstrumentIds.contains(prev.getInstrumentId())) {
                    // Instrument completely exited
                    FundHolding exitHolding = new FundHolding();
                    exitHolding.setDisclosureId(currentDisclosure.getId());
                    exitHolding.setSchemeId(currentDisclosure.getSchemeId());
                    exitHolding.setInstrumentId(prev.getInstrumentId());
                    exitHolding.setSymbol(prev.getSymbol());
                    exitHolding.setCompanyName(prev.getCompanyName());
                    exitHolding.setHoldingDate(currentDisclosure.getDataAsOf());
                    exitHolding.setDataAsOf(currentDisclosure.getDataAsOf());
                    exitHolding.setPublishedAt(currentDisclosure.getPublishedAt());
                    exitHolding.setAvailableAt(currentDisclosure.getAvailableAt());
                    exitHolding.setQuantity(0L);
                    exitHolding.setMarketValue(BigDecimal.ZERO);
                    exitHolding.setPortfolioWeight(BigDecimal.ZERO);
                    exitHolding.setWeightChangePp(prev.getPortfolioWeight().negate());
                    exitHolding.setRelativeWeightChangePercent(new BigDecimal("-100.00"));
                    exitHolding.setChangeType(HoldingChangeType.EXITED_POSITION);
                    exitHolding.setAssetClass(prev.getAssetClass());
                    exitHolding.setSector(prev.getSector());
                    exitHolding.setSource(currentDisclosure.getSource());
                    processedHoldings.add(exitHolding);
                }
            }
        }

        return processedHoldings;
    }
}
