package com.quantlab.warehouse.service;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.warehouse.entity.CorporateAction;
import com.quantlab.warehouse.entity.HistoricalDataQuality;
import com.quantlab.warehouse.entity.HistoricalPriceAdjusted;
import com.quantlab.warehouse.entity.HistoricalPriceRaw;
import com.quantlab.warehouse.model.AdjustmentMethodology;
import com.quantlab.warehouse.model.PointInTimeUniverse;
import com.quantlab.warehouse.model.TimeGranularity;
import com.quantlab.warehouse.repository.CorporateActionRepository;
import com.quantlab.warehouse.repository.HistoricalDataQualityRepository;
import com.quantlab.warehouse.repository.HistoricalPriceAdjustedRepository;
import com.quantlab.warehouse.repository.HistoricalPriceRawRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Historical Data Warehouse Service.
 * Central access gateway for point-in-time historical datasets, adjustments, and corporate events.
 */
@Service
@Transactional(readOnly = true)
public class HistoricalDataWarehouseService {

    private final HistoricalPriceRawRepository rawPriceRepository;
    private final HistoricalPriceAdjustedRepository adjustedPriceRepository;
    private final CorporateActionRepository corporateActionRepository;
    private final PointInTimeUniverseService universeService;
    private final HistoricalDataQualityRepository dataQualityRepository;

    public HistoricalDataWarehouseService(
            HistoricalPriceRawRepository rawPriceRepository,
            HistoricalPriceAdjustedRepository adjustedPriceRepository,
            CorporateActionRepository corporateActionRepository,
            PointInTimeUniverseService universeService,
            HistoricalDataQualityRepository dataQualityRepository) {
        this.rawPriceRepository = rawPriceRepository;
        this.adjustedPriceRepository = adjustedPriceRepository;
        this.corporateActionRepository = corporateActionRepository;
        this.universeService = universeService;
        this.dataQualityRepository = dataQualityRepository;
    }

    public List<HistoricalPriceRaw> getRawPrices(String symbol, Exchange exchange, LocalDate from, LocalDate to) {
        return rawPriceRepository.findPricesBySymbolInRange(
            symbol.trim().toUpperCase(), exchange != null ? exchange : Exchange.NSE,
            TimeGranularity.DAILY, from, to
        );
    }

    public List<HistoricalPriceAdjusted> getAdjustedPrices(String symbol, Exchange exchange,
                                                          AdjustmentMethodology methodology, LocalDate from, LocalDate to) {
        return adjustedPriceRepository.findAdjustedPricesBySymbolInRange(
            symbol.trim().toUpperCase(), exchange != null ? exchange : Exchange.NSE,
            methodology != null ? methodology : AdjustmentMethodology.SPLIT_ADJUSTED,
            TimeGranularity.DAILY, from, to
        );
    }

    public PointInTimeUniverse getUniverseAsOf(String indexSymbol, LocalDate asOfDate) {
        return universeService.getUniverseAsOf(indexSymbol, asOfDate);
    }

    public List<CorporateAction> getCorporateActions(Long instrumentId, LocalDate from, LocalDate to) {
        return corporateActionRepository.findInDateRange(instrumentId, from, to);
    }

    public Page<HistoricalDataQuality> getDataQualityReports(int page, int size) {
        return dataQualityRepository.findAllByOrderByCheckedAtDesc(PageRequest.of(page, size));
    }
}
