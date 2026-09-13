package com.quantlab.news.service;

import com.quantlab.marketdata.alert.AlertService;
import com.quantlab.marketdata.model.AlertEvent;
import com.quantlab.marketdata.model.AlertSeverity;
import com.quantlab.marketdata.model.IngestionRunStatus;
import com.quantlab.news.entity.*;
import com.quantlab.news.intelligence.EventDeduplicationService;
import com.quantlab.news.intelligence.NewsIntelligenceProcessor;
import com.quantlab.news.model.ArticleProcessingStatus;
import com.quantlab.news.model.EntityMatch;
import com.quantlab.news.provider.CorporateFilingsProvider;
import com.quantlab.news.provider.NewsDataProvider;
import com.quantlab.news.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * News and Corporate Intelligence Ingestion Pipeline.
 * 
 * Pipeline Flow:
 * Provider -> Deduplication -> Entity Recognition -> Event Classification -> Sentiment/Importance -> Persistence with information_available_at
 */
@Service
public class NewsIngestionService {

    private static final Logger log = LoggerFactory.getLogger(NewsIngestionService.class);

    private final NewsDataProvider newsProvider;
    private final CorporateFilingsProvider filingsProvider;
    private final NewsIntelligenceProcessor nlpProcessor;
    private final EventDeduplicationService deduplicationService;
    private final NewsArticleRepository articleRepository;
    private final NewsArticleEntityRepository articleEntityRepository;
    private final CorporateEventRepository corporateEventRepository;
    private final NewsIngestionRunRepository runRepository;
    private final AlertService alertService;

    public NewsIngestionService(
            NewsDataProvider newsProvider,
            CorporateFilingsProvider filingsProvider,
            NewsIntelligenceProcessor nlpProcessor,
            EventDeduplicationService deduplicationService,
            NewsArticleRepository articleRepository,
            NewsArticleEntityRepository articleEntityRepository,
            CorporateEventRepository corporateEventRepository,
            NewsIngestionRunRepository runRepository,
            AlertService alertService) {
        this.newsProvider = newsProvider;
        this.filingsProvider = filingsProvider;
        this.nlpProcessor = nlpProcessor;
        this.deduplicationService = deduplicationService;
        this.articleRepository = articleRepository;
        this.articleEntityRepository = articleEntityRepository;
        this.corporateEventRepository = corporateEventRepository;
        this.runRepository = runRepository;
        this.alertService = alertService;
    }

    @Transactional
    public NewsIngestionRun ingestNewsAndFilings() {
        String runId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        String providerName = newsProvider.getProviderName();

        NewsIngestionRun run = new NewsIngestionRun(runId, providerName, startTime);
        run = runRepository.save(run);

        log.info("[NewsIngestion] Starting ingestion run '{}' for provider '{}'", runId, providerName);

        int articlesReceived = 0;
        int articlesInserted = 0;
        int duplicates = 0;
        int eventsExtracted = 0;
        int entityMatches = 0;

        try {
            // 1. Fetch news articles
            List<NewsArticle> articles = newsProvider.fetchLatestNews(startTime.minusSeconds(86400), 50);
            articlesReceived = articles.size();

            for (NewsArticle article : articles) {
                String hash = article.getContentHash() != null
                    ? article.getContentHash()
                    : deduplicationService.computeContentHash(article.getUrl(), article.getTitle());
                article.setContentHash(hash);

                // Deduplication check
                if (deduplicationService.isDuplicate(hash)) {
                    duplicates++;
                    continue;
                }

                // NLP Enrichment (Sentiment, Importance, Classification)
                nlpProcessor.enrichArticle(article);
                NewsArticle savedArticle = articleRepository.save(article);
                articlesInserted++;

                // Entity Extraction
                List<EntityMatch> matches = nlpProcessor.extractEntities(article.getTitle() + " " + article.getDescription());
                entityMatches += matches.size();

                for (EntityMatch match : matches) {
                    NewsArticleEntity entityLink = new NewsArticleEntity(
                        savedArticle.getId(), match.instrumentId(), match.symbol(), match.confidence(), match.matchType()
                    );
                    articleEntityRepository.save(entityLink);

                    // Extract and persist structured corporate event
                    CorporateEvent event = nlpProcessor.extractCorporateEvent(savedArticle, match);
                    if (event != null) {
                        corporateEventRepository.save(event);
                        eventsExtracted++;
                    }
                }
            }

            // 2. Fetch official corporate announcements/filings
            List<CorporateEvent> filings = filingsProvider.fetchLatestFilings(startTime.minusSeconds(86400), 20);
            for (CorporateEvent filing : filings) {
                corporateEventRepository.save(filing);
                eventsExtracted++;
            }

            run.setArticlesReceived(articlesReceived);
            run.setArticlesInserted(articlesInserted);
            run.setDuplicatesCount(duplicates);
            run.setEventsExtracted(eventsExtracted);
            run.setEntityMatches(entityMatches);
            run.complete(IngestionRunStatus.SUCCESS);

            log.info("[NewsIngestion] Finished run '{}' in {} ms: articles={}/{}, duplicates={}, events={}, entities={}",
                    runId, run.getDurationMs(), articlesInserted, articlesReceived, duplicates, eventsExtracted, entityMatches);

        } catch (Exception e) {
            log.error("[NewsIngestion] Run '{}' failed: {}", runId, e.getMessage(), e);
            run.setErrorMessage(e.getMessage());
            run.complete(IngestionRunStatus.FAILED);

            alertService.sendAlert(AlertEvent.of(
                "NEWS_INGESTION_FAILED",
                AlertSeverity.ERROR,
                "News and Corporate intelligence ingestion run failed",
                e.getMessage(),
                providerName,
                runId
            ));
        }

        return runRepository.save(run);
    }
}
