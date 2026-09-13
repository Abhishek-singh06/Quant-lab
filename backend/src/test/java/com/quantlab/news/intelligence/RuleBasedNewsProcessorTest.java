package com.quantlab.news.intelligence;

import com.quantlab.news.entity.CorporateEvent;
import com.quantlab.news.entity.NewsArticle;
import com.quantlab.news.model.EntityMatch;
import com.quantlab.news.model.EventType;
import com.quantlab.news.model.SentimentLabel;
import com.quantlab.warehouse.repository.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RuleBasedNewsProcessorTest {

    private RuleBasedNewsProcessor processor;

    @BeforeEach
    void setUp() {
        InstrumentRepository instrumentRepository = mock(InstrumentRepository.class);
        EntityResolutionService entityResolutionService = new EntityResolutionService(instrumentRepository);
        EventDeduplicationService deduplicationService = mock(EventDeduplicationService.class);

        processor = new RuleBasedNewsProcessor(entityResolutionService, deduplicationService);
    }

    @Test
    void classifiesEventTypesAccurately() {
        assertEquals(EventType.EARNINGS_RESULT, processor.classifyEventType("TCS reports Q3 financial results with 8% YoY revenue growth"));
        assertEquals(EventType.DIVIDEND, processor.classifyEventType("Board announces interim dividend of Rs 28 per share"));
        assertEquals(EventType.NEW_CONTRACT, processor.classifyEventType("Reliance bags major 5G contract deal"));
        assertEquals(EventType.CFO_CHANGE, processor.classifyEventType("CFO resigns due to personal reasons"));
        assertEquals(EventType.MANAGEMENT_CHANGE, processor.classifyEventType("Executive Director resigns from board"));
        assertEquals(EventType.REGULATORY_ACTION, processor.classifyEventType("SEBI issues regulatory penalty notice"));
    }

    @Test
    void separatesFinancialImpactFromGenericTextSentiment() {
        NewsArticle article = new NewsArticle(
            1L, "Mint", "Company profit falls 15% but beats street estimates",
            "Operating margins expand despite revenue contraction.",
            "http://example.com/1", "hash1", Instant.now(), Instant.now()
        );

        processor.enrichArticle(article);

        // "profit falls" + "beats" => Financial impact should remain positive
        assertTrue(article.getFinancialImpactScore().compareTo(BigDecimal.ZERO) > 0,
                "Financial impact score should be positive when beating expectations despite profit fall words");
    }

    @Test
    void extractsStructuredDividendFacts() {
        NewsArticle article = new NewsArticle(
            1L, "ET", "TCS announces dividend of Rs 28 per share",
            "Interim dividend declared by board.",
            "http://example.com/2", "hash2", Instant.now(), Instant.now()
        );
        processor.enrichArticle(article);

        EntityMatch match = new EntityMatch(1L, "TCS", "Tata Consultancy Services Limited", BigDecimal.valueOf(0.95), "COMPANY_NAME");
        CorporateEvent event = processor.extractCorporateEvent(article, match);

        assertNotNull(event);
        assertEquals(EventType.DIVIDEND, event.getEventType());
        assertNotNull(event.getStructuredPayload());
        assertTrue(event.getStructuredPayload().contains("28"));
    }
}
