package com.quantlab.warehouse.service;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.warehouse.entity.CorporateAction;
import com.quantlab.warehouse.entity.HistoricalPriceAdjusted;
import com.quantlab.warehouse.entity.HistoricalPriceRaw;
import com.quantlab.warehouse.model.AdjustmentMethodology;
import com.quantlab.warehouse.model.CorporateActionType;
import com.quantlab.warehouse.model.TimeGranularity;
import com.quantlab.warehouse.repository.CorporateActionRepository;
import com.quantlab.warehouse.repository.HistoricalPriceAdjustedRepository;
import com.quantlab.warehouse.repository.HistoricalPriceRawRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Corporate Actions Adjustment Engine.
 * 
 * CORE RULES:
 * 1. Raw historical prices are NEVER overwritten.
 * 2. Adjusted prices are computed and stored in a separate table: `historical_prices_adjusted`.
 * 3. Supports SPLIT_ADJUSTED and TOTAL_RETURN (dividend reinvestment) methodologies.
 * 4. Fully idempotent: Re-running adjustments produces identical deterministic results.
 */
@Service
public class CorporateActionAdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(CorporateActionAdjustmentService.class);

    private final HistoricalPriceRawRepository rawPriceRepository;
    private final HistoricalPriceAdjustedRepository adjustedPriceRepository;
    private final CorporateActionRepository corporateActionRepository;

    public CorporateActionAdjustmentService(
            HistoricalPriceRawRepository rawPriceRepository,
            HistoricalPriceAdjustedRepository adjustedPriceRepository,
            CorporateActionRepository corporateActionRepository) {
        this.rawPriceRepository = rawPriceRepository;
        this.adjustedPriceRepository = adjustedPriceRepository;
        this.corporateActionRepository = corporateActionRepository;
    }

    @Transactional
    public List<HistoricalPriceAdjusted> computeAndSaveAdjustments(Long instrumentId, Exchange exchange,
                                                                   LocalDate fromDate, LocalDate toDate) {
        List<HistoricalPriceRaw> rawPrices = rawPriceRepository.findPricesInRange(
            instrumentId, exchange, TimeGranularity.DAILY, fromDate, toDate
        );

        if (rawPrices.isEmpty()) {
            return List.of();
        }

        List<CorporateAction> actions = corporateActionRepository.findInDateRange(instrumentId, fromDate, toDate);
        List<CorporateAction> splits = actions.stream()
            .filter(a -> a.getActionType() == CorporateActionType.STOCK_SPLIT || a.getActionType() == CorporateActionType.BONUS_ISSUE)
            .sorted(Comparator.comparing(CorporateAction::getExDate))
            .toList();

        List<CorporateAction> dividends = actions.stream()
            .filter(a -> a.getActionType() == CorporateActionType.DIVIDEND && a.getDividendAmount() != null)
            .sorted(Comparator.comparing(CorporateAction::getExDate))
            .toList();

        List<HistoricalPriceAdjusted> adjustedRecords = new ArrayList<>();

        for (HistoricalPriceRaw raw : rawPrices) {
            LocalDate date = raw.getTradingDate();

            // Compute cumulative split factor for all splits occurring STRICTLY AFTER this trading date
            BigDecimal cumulativeSplitFactor = BigDecimal.ONE;
            for (CorporateAction split : splits) {
                if (split.getExDate().isAfter(date)) {
                    BigDecimal factor = split.getAdjustmentFactor() != null ? split.getAdjustmentFactor() : BigDecimal.ONE;
                    cumulativeSplitFactor = cumulativeSplitFactor.multiply(factor);
                }
            }

            BigDecimal adjOpen = raw.getOpen().multiply(cumulativeSplitFactor).setScale(4, RoundingMode.HALF_UP);
            BigDecimal adjHigh = raw.getHigh().multiply(cumulativeSplitFactor).setScale(4, RoundingMode.HALF_UP);
            BigDecimal adjLow = raw.getLow().multiply(cumulativeSplitFactor).setScale(4, RoundingMode.HALF_UP);
            BigDecimal adjClose = raw.getClose().multiply(cumulativeSplitFactor).setScale(4, RoundingMode.HALF_UP);

            // Compute cumulative dividend return factor
            BigDecimal cumulativeDivFactor = BigDecimal.ONE;
            for (CorporateAction div : dividends) {
                if (div.getExDate().isAfter(date) && raw.getClose().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal divYield = div.getDividendAmount().divide(raw.getClose(), 8, RoundingMode.HALF_UP);
                    BigDecimal divFactor = BigDecimal.ONE.subtract(divYield);
                    if (divFactor.compareTo(BigDecimal.ZERO) > 0) {
                        cumulativeDivFactor = cumulativeDivFactor.multiply(divFactor);
                    }
                }
            }
            BigDecimal totalReturnClose = adjClose.multiply(cumulativeDivFactor).setScale(4, RoundingMode.HALF_UP);

            // Idempotent upsert
            var existing = adjustedPriceRepository.findByInstrumentIdAndExchangeAndTradingDateAndGranularityAndMethodology(
                instrumentId, exchange, date, TimeGranularity.DAILY, AdjustmentMethodology.SPLIT_ADJUSTED
            );

            HistoricalPriceAdjusted entity = existing.orElseGet(HistoricalPriceAdjusted::new);
            entity.setRawPriceId(raw.getId());
            entity.setInstrumentId(instrumentId);
            entity.setSymbol(raw.getSymbol());
            entity.setExchange(exchange);
            entity.setTradingDate(date);
            entity.setTimestamp(raw.getTimestamp());
            entity.setAdjOpen(adjOpen);
            entity.setAdjHigh(adjHigh);
            entity.setAdjLow(adjLow);
            entity.setAdjClose(adjClose);
            entity.setTotalReturnClose(totalReturnClose);
            entity.setCumulativeSplitFactor(cumulativeSplitFactor);
            entity.setCumulativeDividendFactor(cumulativeDivFactor);
            entity.setMethodology(AdjustmentMethodology.SPLIT_ADJUSTED);
            entity.setGranularity(TimeGranularity.DAILY);

            adjustedRecords.add(adjustedPriceRepository.save(entity));
        }

        log.info("[CorporateActionEngine] Computed {} adjusted price records for instrument ID {}",
                adjustedRecords.size(), instrumentId);

        return adjustedRecords;
    }
}
