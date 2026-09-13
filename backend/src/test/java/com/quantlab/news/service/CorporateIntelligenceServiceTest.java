package com.quantlab.news.service;

import com.quantlab.news.entity.CorporateEvent;
import com.quantlab.news.intelligence.EventDecayService;
import com.quantlab.news.model.EventType;
import com.quantlab.news.model.PointInTimeTimeline;
import com.quantlab.news.model.SentimentLabel;
import com.quantlab.news.repository.CorporateEventRepository;
import com.quantlab.news.repository.NewsArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CorporateIntelligenceServiceTest {

    private NewsArticleRepository articleRepository;
    private CorporateEventRepository corporateEventRepository;
    private CorporateIntelligenceService intelligenceService;

    @BeforeEach
    void setUp() {
        articleRepository = mock(NewsArticleRepository.class);
        corporateEventRepository = mock(CorporateEventRepository.class);
        EventDecayService decayService = new EventDecayService();

        intelligenceService = new CorporateIntelligenceService(
            articleRepository, corporateEventRepository, decayService
        );
    }

    @Test
    void pointInTimeTimelineFiltersOutFutureEvents() {
        Instant asOfTime = Instant.parse("2024-03-01T12:00:00Z");

        CorporateEvent e1 = new CorporateEvent(
            1L, "RELIANCE", EventType.NEW_CONTRACT, "5G Network Deal", "Summary",
            LocalDate.of(2024, 2, 15), Instant.parse("2024-02-15T10:00:00Z"),
            Instant.parse("2024-02-15T10:00:00Z"), "NSE_FILINGS",
            BigDecimal.valueOf(0.85), BigDecimal.valueOf(0.70), SentimentLabel.POSITIVE
        );

        when(corporateEventRepository.findEventsBySymbolAvailableAt(eq("RELIANCE"), eq(asOfTime)))
            .thenReturn(List.of(e1));

        PointInTimeTimeline timeline = intelligenceService.getCompanyTimeline("RELIANCE", asOfTime);

        assertNotNull(timeline);
        assertEquals("RELIANCE", timeline.symbol());
        assertEquals(1, timeline.totalEvents());
        assertEquals(EventType.NEW_CONTRACT, timeline.events().get(0).eventType());
    }
}
