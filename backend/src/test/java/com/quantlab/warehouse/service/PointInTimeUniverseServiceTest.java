package com.quantlab.warehouse.service;

import com.quantlab.warehouse.entity.IndexConstituent;
import com.quantlab.warehouse.entity.IndexMaster;
import com.quantlab.warehouse.model.PointInTimeUniverse;
import com.quantlab.warehouse.repository.IndexConstituentRepository;
import com.quantlab.warehouse.repository.IndexMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PointInTimeUniverseServiceTest {

    private IndexMasterRepository indexMasterRepository;
    private IndexConstituentRepository constituentRepository;
    private PointInTimeUniverseService universeService;

    @BeforeEach
    void setUp() {
        indexMasterRepository = mock(IndexMasterRepository.class);
        constituentRepository = mock(IndexConstituentRepository.class);

        IndexMaster nifty = new IndexMaster("NIFTY 50", "Nifty 50 Index", com.quantlab.marketdata.model.Exchange.NSE, "Broad", "Benchmark");
        nifty.setId(10L);

        when(indexMasterRepository.findByIndexSymbol("NIFTY 50")).thenReturn(Optional.of(nifty));

        universeService = new PointInTimeUniverseService(indexMasterRepository, constituentRepository);
    }

    @Test
    void returnsOnlyConstituentsActiveAtPointInTimeDate() {
        LocalDate asOfDate = LocalDate.of(2018, 6, 1);

        IndexConstituent c1 = new IndexConstituent(10L, 1L, "RELIANCE", new BigDecimal("10.0"), LocalDate.of(2005, 1, 1), null, "NSE");
        IndexConstituent c2 = new IndexConstituent(10L, 2L, "YESBANK", new BigDecimal("2.5"), LocalDate.of(2015, 1, 1), LocalDate.of(2020, 3, 27), "NSE");

        when(constituentRepository.findConstituentsAsOfDate(eq(10L), eq(asOfDate)))
            .thenReturn(List.of(c1, c2));

        PointInTimeUniverse universe = universeService.getUniverseAsOf("NIFTY 50", asOfDate);

        assertNotNull(universe);
        assertEquals(2, universe.totalConstituents());
        assertTrue(universe.constituentSymbols().contains("RELIANCE"));
        assertTrue(universe.constituentSymbols().contains("YESBANK"));
    }
}
