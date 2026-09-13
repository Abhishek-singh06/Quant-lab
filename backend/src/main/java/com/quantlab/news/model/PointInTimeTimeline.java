package com.quantlab.news.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PointInTimeTimeline(
    String symbol,
    Instant asOfTimestamp,
    List<TimelineEvent> events,
    int totalEvents,
    BigDecimal aggregateSentimentScore,
    BigDecimal highImportanceEventCount
) {
    public record TimelineEvent(
        Long eventId,
        EventType eventType,
        String title,
        String summary,
        LocalDate eventDate,
        Instant announcedAt,
        Instant informationAvailableAt,
        String source,
        BigDecimal importanceScore,
        BigDecimal financialImpactScore,
        SentimentLabel sentimentLabel,
        BigDecimal confidence
    ) {}
}
