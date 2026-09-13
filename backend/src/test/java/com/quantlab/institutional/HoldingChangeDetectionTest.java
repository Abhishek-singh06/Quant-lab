package com.quantlab.institutional;

import com.quantlab.institutional.entity.FundHolding;
import com.quantlab.institutional.entity.FundPortfolioDisclosure;
import com.quantlab.institutional.model.HoldingChangeType;
import com.quantlab.institutional.model.PortfolioScope;
import com.quantlab.institutional.service.HoldingChangeDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HoldingChangeDetectionTest {

    private HoldingChangeDetectionService changeService;

    @BeforeEach
    void setUp() {
        changeService = new HoldingChangeDetectionService();
    }

    @Test
    void testWeightChangeAndNewPositionDetection() {
        LocalDate prevDate = LocalDate.of(2026, 6, 30);
        LocalDate currDate = LocalDate.of(2026, 7, 31);

        FundPortfolioDisclosure prevDisc = new FundPortfolioDisclosure();
        prevDisc.setId(1L);
        prevDisc.setPortfolioScope(PortfolioScope.COMPLETE);
        prevDisc.setDataAsOf(prevDate);

        FundPortfolioDisclosure currDisc = new FundPortfolioDisclosure();
        currDisc.setId(2L);
        currDisc.setPortfolioScope(PortfolioScope.COMPLETE);
        currDisc.setDataAsOf(currDate);
        currDisc.setPublishedAt(LocalDate.of(2026, 8, 15));
        currDisc.setAvailableAt(Instant.now());
        currDisc.setSource("AMFI");

        FundHolding h1Prev = new FundHolding();
        h1Prev.setInstrumentId(10L);
        h1Prev.setSymbol("HDFCBANK");
        h1Prev.setPortfolioWeight(new BigDecimal("8.0000"));

        FundHolding h1Curr = new FundHolding();
        h1Curr.setInstrumentId(10L);
        h1Curr.setSymbol("HDFCBANK");
        h1Curr.setPortfolioWeight(new BigDecimal("9.5000"));

        FundHolding h2CurrNew = new FundHolding();
        h2CurrNew.setInstrumentId(20L);
        h2CurrNew.setSymbol("INFY");
        h2CurrNew.setPortfolioWeight(new BigDecimal("4.2000"));

        List<FundHolding> result = changeService.computeHoldingChanges(
            currDisc, List.of(h1Curr, h2CurrNew), prevDisc, List.of(h1Prev)
        );

        assertEquals(2, result.size());

        // HDFCBANK: increased by +1.5 pp
        FundHolding hdfc = result.stream().filter(h -> h.getSymbol().equals("HDFCBANK")).findFirst().orElseThrow();
        assertEquals(new BigDecimal("1.5000"), hdfc.getWeightChangePp());
        assertEquals(HoldingChangeType.INCREASED, hdfc.getChangeType());

        // INFY: new position (+4.2 pp)
        FundHolding infy = result.stream().filter(h -> h.getSymbol().equals("INFY")).findFirst().orElseThrow();
        assertEquals(HoldingChangeType.NEW_POSITION, infy.getChangeType());
        assertEquals(new BigDecimal("4.2000"), infy.getWeightChangePp());
    }

    @Test
    void testExitedPositionDetectionForCompletePortfolio() {
        FundPortfolioDisclosure prevDisc = new FundPortfolioDisclosure();
        prevDisc.setId(1L);
        prevDisc.setPortfolioScope(PortfolioScope.COMPLETE);

        FundPortfolioDisclosure currDisc = new FundPortfolioDisclosure();
        currDisc.setId(2L);
        currDisc.setPortfolioScope(PortfolioScope.COMPLETE);
        currDisc.setDataAsOf(LocalDate.of(2026, 7, 31));
        currDisc.setPublishedAt(LocalDate.of(2026, 8, 15));
        currDisc.setAvailableAt(Instant.now());
        currDisc.setSource("AMFI");

        FundHolding h1Prev = new FundHolding();
        h1Prev.setInstrumentId(10L);
        h1Prev.setSymbol("TCS");
        h1Prev.setPortfolioWeight(new BigDecimal("5.0000"));

        FundHolding h2Curr = new FundHolding();
        h2Curr.setInstrumentId(20L);
        h2Curr.setSymbol("WIPRO");
        h2Curr.setPortfolioWeight(new BigDecimal("3.0000"));

        // TCS is not present in current complete disclosure -> must be marked EXITED_POSITION
        List<FundHolding> result = changeService.computeHoldingChanges(
            currDisc, List.of(h2Curr), prevDisc, List.of(h1Prev)
        );

        assertEquals(2, result.size());
        FundHolding tcs = result.stream().filter(h -> h.getSymbol().equals("TCS")).findFirst().orElseThrow();
        assertEquals(HoldingChangeType.EXITED_POSITION, tcs.getChangeType());
        assertEquals(new BigDecimal("-5.0000"), tcs.getWeightChangePp());
        assertEquals(BigDecimal.ZERO, tcs.getPortfolioWeight());
    }

    @Test
    void testPartialPortfolioDoesNotFalselyFlagExits() {
        FundPortfolioDisclosure prevDisc = new FundPortfolioDisclosure();
        prevDisc.setId(1L);
        prevDisc.setPortfolioScope(PortfolioScope.TOP_10_ONLY); // Partial!

        FundPortfolioDisclosure currDisc = new FundPortfolioDisclosure();
        currDisc.setId(2L);
        currDisc.setPortfolioScope(PortfolioScope.TOP_10_ONLY); // Partial!
        currDisc.setDataAsOf(LocalDate.of(2026, 7, 31));
        currDisc.setPublishedAt(LocalDate.of(2026, 8, 15));
        currDisc.setAvailableAt(Instant.now());
        currDisc.setSource("AMFI");

        FundHolding h1Prev = new FundHolding();
        h1Prev.setInstrumentId(10L);
        h1Prev.setSymbol("TCS");
        h1Prev.setPortfolioWeight(new BigDecimal("3.0000"));

        FundHolding h2Curr = new FundHolding();
        h2Curr.setInstrumentId(20L);
        h2Curr.setSymbol("WIPRO");
        h2Curr.setPortfolioWeight(new BigDecimal("4.0000"));

        // Because currDisc is TOP_10_ONLY, missing TCS must NOT be tagged as EXITED_POSITION
        List<FundHolding> result = changeService.computeHoldingChanges(
            currDisc, List.of(h2Curr), prevDisc, List.of(h1Prev)
        );

        assertEquals(1, result.size());
        assertEquals("WIPRO", result.get(0).getSymbol());
    }
}
