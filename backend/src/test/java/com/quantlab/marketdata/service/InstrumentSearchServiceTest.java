package com.quantlab.marketdata.service;

import com.quantlab.global.entity.GlobalInstrument;
import com.quantlab.global.model.AssetClass;
import com.quantlab.global.model.GlobalMarket;
import com.quantlab.global.repository.GlobalInstrumentRepository;
import com.quantlab.marketdata.model.InstrumentSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class InstrumentSearchServiceTest {

    private GlobalInstrumentRepository globalInstrumentRepository;
    private InstrumentSearchService searchService;

    @BeforeEach
    void setUp() {
        globalInstrumentRepository = Mockito.mock(GlobalInstrumentRepository.class);
        searchService = new InstrumentSearchService(globalInstrumentRepository);
    }

    @Test
    @DisplayName("Should return default core universe when query is empty")
    void testEmptySearchQuery() {
        List<InstrumentSearchResult> results = searchService.search("", 5);
        assertNotNull(results);
        assertEquals(5, results.size());
        assertEquals("NIFTY 50", results.get(0).symbol());
    }

    @Test
    @DisplayName("Should match Indian equity by symbol prefix")
    void testSearchBySymbolPrefix() {
        List<InstrumentSearchResult> results = searchService.search("REL", 10);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.symbol().equals("RELIANCE")));
    }

    @Test
    @DisplayName("Should match Indian equity by company name")
    void testSearchByCompanyName() {
        List<InstrumentSearchResult> results = searchService.search("Tata Consultancy", 10);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.symbol().equals("TCS")));
    }

    @Test
    @DisplayName("Should match Indian equity by sector")
    void testSearchBySector() {
        List<InstrumentSearchResult> results = searchService.search("Banking", 10);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.symbol().equals("HDFCBANK") || r.symbol().equals("ICICIBANK")));
    }

    @Test
    @DisplayName("Should include global instruments when matched")
    void testSearchGlobalInstruments() {
        GlobalInstrument gi = new GlobalInstrument(
                "SPX", "SPX", "S&P 500 Index",
                AssetClass.EQUITY_INDEX, GlobalMarket.US, "USA", "CBOE", "USD", "America/New_York", "INDEX"
        );
        when(globalInstrumentRepository.findByActiveTrue()).thenReturn(List.of(gi));

        List<InstrumentSearchResult> results = searchService.search("SPX", 10);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.symbol().equals("SPX")));
    }
}
