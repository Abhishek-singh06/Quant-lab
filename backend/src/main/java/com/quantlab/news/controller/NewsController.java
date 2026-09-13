package com.quantlab.news.controller;

import com.quantlab.news.entity.CorporateEvent;
import com.quantlab.news.entity.NewsArticle;
import com.quantlab.news.entity.NewsIngestionRun;
import com.quantlab.news.model.PointInTimeTimeline;
import com.quantlab.news.service.CorporateIntelligenceService;
import com.quantlab.news.service.NewsIngestionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
public class NewsController {

    private final CorporateIntelligenceService intelligenceService;
    private final NewsIngestionService ingestionService;

    public NewsController(
            CorporateIntelligenceService intelligenceService,
            NewsIngestionService ingestionService) {
        this.intelligenceService = intelligenceService;
        this.ingestionService = ingestionService;
    }

    @GetMapping("/timeline/{symbol}")
    public ResponseEntity<PointInTimeTimeline> getTimeline(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOfTime) {
        Instant effectiveTime = asOfTime != null ? asOfTime : Instant.now();
        return ResponseEntity.ok(intelligenceService.getCompanyTimeline(symbol, effectiveTime));
    }

    @GetMapping("/articles/{symbol}")
    public ResponseEntity<List<NewsArticle>> getArticles(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOfTime) {
        Instant effectiveTime = asOfTime != null ? asOfTime : Instant.now();
        return ResponseEntity.ok(intelligenceService.getNewsAvailableAt(symbol, effectiveTime));
    }

    @GetMapping("/events/{symbol}")
    public ResponseEntity<List<CorporateEvent>> getEvents(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOfTime) {
        Instant effectiveTime = asOfTime != null ? asOfTime : Instant.now();
        return ResponseEntity.ok(intelligenceService.getEventsAvailableAt(symbol, effectiveTime));
    }

    @PostMapping("/ingest")
    public ResponseEntity<NewsIngestionRun> triggerIngestion() {
        return ResponseEntity.ok(ingestionService.ingestNewsAndFilings());
    }
}
