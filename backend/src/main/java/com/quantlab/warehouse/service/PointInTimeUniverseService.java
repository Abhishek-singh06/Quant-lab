package com.quantlab.warehouse.service;

import com.quantlab.warehouse.entity.IndexConstituent;
import com.quantlab.warehouse.entity.IndexMaster;
import com.quantlab.warehouse.model.PointInTimeUniverse;
import com.quantlab.warehouse.repository.IndexConstituentRepository;
import com.quantlab.warehouse.repository.IndexMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Survivorship-Bias Safe Point-in-Time Universe Engine.
 * Reconstructs exact index membership as of any historical timestamp T.
 */
@Service
public class PointInTimeUniverseService {

    private static final Logger log = LoggerFactory.getLogger(PointInTimeUniverseService.class);

    private final IndexMasterRepository indexMasterRepository;
    private final IndexConstituentRepository constituentRepository;

    public PointInTimeUniverseService(
            IndexMasterRepository indexMasterRepository,
            IndexConstituentRepository constituentRepository) {
        this.indexMasterRepository = indexMasterRepository;
        this.constituentRepository = constituentRepository;
    }

    public PointInTimeUniverse getUniverseAsOf(String indexSymbol, LocalDate asOfDate) {
        if (indexSymbol == null || asOfDate == null) {
            return new PointInTimeUniverse(indexSymbol, asOfDate, Collections.emptyList(), Collections.emptyList(), 0);
        }

        Optional<IndexMaster> indexOpt = indexMasterRepository.findByIndexSymbol(indexSymbol.trim().toUpperCase());
        if (indexOpt.isEmpty()) {
            log.warn("[PointInTimeUniverse] Index '{}' not found in master registry", indexSymbol);
            return new PointInTimeUniverse(indexSymbol, asOfDate, Collections.emptyList(), Collections.emptyList(), 0);
        }

        Long indexId = indexOpt.get().getId();
        List<IndexConstituent> constituents = constituentRepository.findConstituentsAsOfDate(indexId, asOfDate);

        List<String> symbols = constituents.stream().map(IndexConstituent::getSymbol).sorted().toList();
        List<Long> instrumentIds = constituents.stream().map(IndexConstituent::getInstrumentId).toList();

        log.debug("[PointInTimeUniverse] Index '{}' had {} constituent(s) on {}", indexSymbol, symbols.size(), asOfDate);

        return new PointInTimeUniverse(indexSymbol, asOfDate, symbols, instrumentIds, symbols.size());
    }
}
