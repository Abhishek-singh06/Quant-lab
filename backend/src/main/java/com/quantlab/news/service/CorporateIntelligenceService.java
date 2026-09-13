package com.quantlab.news.service;

import com.quantlab.news.entity.CorporateEvent;
import com.quantlab.news.entity.NewsArticle;
import com.quantlab.news.intelligence.EventDecayService;
import com.quantlab.news.model.PointInTimeTimeline;
import com.quantlab.news.repository.CorporateEventRepository;
import com.quantlab.news.repository.NewsArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Point-in-Time Corporate Intelligence & News Access Service.
 * 
 * Strict filter: All queries enforce `information_available_at <= asOfTime`.
 */
@Service
@Transactional(readOnly = true)
public class CorporateIntelligenceService {

    private final NewsArticleRepository articleRepository;
    private final CorporateEventRepository corporateEventRepository;
    private final EventDecayService decayService;

    public CorporateIntelligenceService(
            NewsArticleRepository articleRepository,
            CorporateEventRepository corporateEventRepository,
            EventDecayService decayService) {
        this.articleRepository = articleRepository;
        this.corporateEventRepository = corporateEventRepository;
        this.decayService = decayService;
    }

    public List<NewsArticle> getNewsAvailableAt(String symbol, Instant asOfTime) {
        return articleRepository.findArticlesBySymbolAvailableAt(symbol.trim().toUpperCase(), asOfTime);
    }

    public List<CorporateEvent> getEventsAvailableAt(String symbol, Instant asOfTime) {
        return corporateEventRepository.findEventsBySymbolAvailableAt(symbol.trim().toUpperCase(), asOfTime);
    }

    public PointInTimeTimeline getCompanyTimeline(String symbol, Instant asOfTime) {
        String cleanSymbol = symbol.trim().toUpperCase();
        List<CorporateEvent> events = corporateEventRepository.findEventsBySymbolAvailableAt(cleanSymbol, asOfTime);

        List<PointInTimeTimeline.TimelineEvent> timelineEvents = new ArrayList<>();
        BigDecimal totalSentiment = BigDecimal.ZERO;
        long highImportanceCount = 0;

        for (CorporateEvent e : events) {
            timelineEvents.add(new PointInTimeTimeline.TimelineEvent(
                e.getId(),
                e.getEventType(),
                e.getTitle(),
                e.getDescription(),
                e.getEventDate(),
                e.getAnnouncedAt(),
                e.getInformationAvailableAt(),
                e.getSource(),
                e.getImportanceScore(),
                e.getFinancialImpactScore(),
                e.getSentimentLabel(),
                e.getConfidence()
            ));

            if (e.getFinancialImpactScore() != null) {
                totalSentiment = totalSentiment.add(e.getFinancialImpactScore());
            }
            if (e.getImportanceScore() != null && e.getImportanceScore().compareTo(BigDecimal.valueOf(0.70)) >= 0) {
                highImportanceCount++;
            }
        }

        BigDecimal avgSentiment = !events.isEmpty()
            ? totalSentiment.divide(BigDecimal.valueOf(events.size()), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        return new PointInTimeTimeline(
            cleanSymbol,
            asOfTime,
            timelineEvents,
            timelineEvents.size(),
            avgSentiment,
            BigDecimal.valueOf(highImportanceCount)
        );
    }
}
