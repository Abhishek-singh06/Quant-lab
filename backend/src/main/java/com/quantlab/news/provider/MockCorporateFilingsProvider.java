package com.quantlab.news.provider;

import com.quantlab.news.entity.CorporateEvent;
import com.quantlab.news.model.EventType;
import com.quantlab.news.model.SentimentLabel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock Corporate Filings Provider for test suites.
 */
@Component("mockCorporateFilingsProvider")
public class MockCorporateFilingsProvider implements CorporateFilingsProvider {

    @Override
    public String getProviderName() {
        return "MOCK_FILINGS";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public List<CorporateEvent> fetchLatestFilings(Instant since, int limit) {
        List<CorporateEvent> list = new ArrayList<>();
        Instant now = Instant.now();

        list.add(new CorporateEvent(
            1L, "RELIANCE", EventType.NEW_CONTRACT,
            "Major enterprise network contract award",
            "Execution of definitive contract for enterprise network infrastructure.",
            LocalDate.now(), now.minusSeconds(1800), now.minusSeconds(1800),
            "MOCK_FILINGS", BigDecimal.valueOf(0.85), BigDecimal.valueOf(0.75), SentimentLabel.POSITIVE
        ));

        list.add(new CorporateEvent(
            2L, "TCS", EventType.DIVIDEND,
            "Declaration of third interim dividend of Rs 28 per equity share",
            "Board of directors declared interim dividend with record date.",
            LocalDate.now(), now.minusSeconds(3600), now.minusSeconds(3600),
            "MOCK_FILINGS", BigDecimal.valueOf(0.70), BigDecimal.valueOf(0.60), SentimentLabel.POSITIVE
        ));

        return list;
    }

    @Override
    public List<CorporateEvent> fetchFilingsForSymbol(String symbol, Instant from, Instant to) {
        return fetchLatestFilings(from, 10);
    }
}
