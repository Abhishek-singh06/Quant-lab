package com.quantlab.news.intelligence;

import com.quantlab.news.entity.CorporateEvent;
import com.quantlab.news.entity.NewsArticle;
import com.quantlab.news.model.EntityMatch;
import com.quantlab.news.model.EventType;
import com.quantlab.news.model.SentimentLabel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Robust Rule-based and Lexicon Financial Intelligence Processor.
 * 
 * Separates TEXT SENTIMENT from FINANCIAL IMPACT SENTIMENT.
 * Evaluates market importance score [0.0 to 1.0] independently of sentiment.
 */
@Service
public class RuleBasedNewsProcessor implements NewsIntelligenceProcessor {

    private final EntityResolutionService entityResolutionService;
    private final EventDeduplicationService deduplicationService;

    // Financial sentiment lexicons
    private static final List<String> POSITIVE_FINANCIAL_TERMS = List.of(
        "beats estimate", "profit surges", "profit rises", "record high", "dividend increase",
        "contract win", "order win", "wins contract", "upgrade", "rating raised",
        "growth surges", "outperforms", "secures deal", "strategic deal", "acquisition"
    );

    private static final List<String> NEGATIVE_FINANCIAL_TERMS = List.of(
        "profit falls", "misses estimate", "downgrade", "penalty", "sebi penalty",
        "regulatory probe", "tax demand", "plant shutdown", "resigns", "cfo resigns",
        "fraud", "investigation", "default", "rating cut", "loss widens", "strike"
    );

    public RuleBasedNewsProcessor(
            EntityResolutionService entityResolutionService,
            EventDeduplicationService deduplicationService) {
        this.entityResolutionService = entityResolutionService;
        this.deduplicationService = deduplicationService;
    }

    @Override
    public String getProcessorName() {
        return "RuleBasedFinancialNLP";
    }

    @Override
    public String getProcessorVersion() {
        return "1.0.0";
    }

    @Override
    public List<EntityMatch> extractEntities(String text) {
        return entityResolutionService.resolveEntities(text);
    }

    @Override
    public void enrichArticle(NewsArticle article) {
        if (article == null) return;

        String combined = (article.getTitle() + " " + (article.getDescription() != null ? article.getDescription() : "")).toLowerCase();

        // 1. Text & Financial Impact Sentiment
        int posScore = 0;
        for (String term : POSITIVE_FINANCIAL_TERMS) {
            if (combined.contains(term)) posScore++;
        }

        int negScore = 0;
        for (String term : NEGATIVE_FINANCIAL_TERMS) {
            if (combined.contains(term)) negScore++;
        }

        double netSentiment = 0.0;
        if (posScore + negScore > 0) {
            netSentiment = (double) (posScore - negScore) / (posScore + negScore);
        }

        // Special case: "profit falls X% but beats expectations" is impact positive
        if (combined.contains("profit falls") && combined.contains("beats")) {
            netSentiment = 0.40;
        }

        article.setSentimentScore(BigDecimal.valueOf(netSentiment).setScale(4, RoundingMode.HALF_UP));
        article.setFinancialImpactScore(BigDecimal.valueOf(netSentiment * 1.1).min(BigDecimal.ONE).max(BigDecimal.valueOf(-1.0)).setScale(4, RoundingMode.HALF_UP));

        if (netSentiment > 0.35) {
            article.setSentimentLabel(netSentiment > 0.70 ? SentimentLabel.VERY_POSITIVE : SentimentLabel.POSITIVE);
        } else if (netSentiment < -0.35) {
            article.setSentimentLabel(netSentiment < -0.70 ? SentimentLabel.VERY_NEGATIVE : SentimentLabel.NEGATIVE);
        } else {
            article.setSentimentLabel(SentimentLabel.NEUTRAL);
        }

        // 2. Importance Scoring [0.0 to 1.0]
        double importance = 0.50;
        EventType eventType = classifyEventType(combined);
        switch (eventType) {
            case EARNINGS_RESULT, ACQUISITION, MERGER, REGULATORY_ACTION -> importance = 0.90;
            case DIVIDEND, BUYBACK, NEW_CONTRACT, ORDER_WIN -> importance = 0.75;
            case MANAGEMENT_CHANGE, CEO_CHANGE, CFO_CHANGE -> importance = 0.70;
            case BOARD_MEETING, CAPEX -> importance = 0.60;
            default -> importance = 0.45;
        }
        article.setImportanceScore(BigDecimal.valueOf(importance).setScale(4, RoundingMode.HALF_UP));

        // 3. Cluster ID for syndicated grouping
        List<EntityMatch> entities = extractEntities(combined);
        String sym = !entities.isEmpty() ? entities.get(0).symbol() : "GEN";
        article.setClusterId(deduplicationService.generateClusterId(sym, eventType.name(), Integer.toHexString(article.getTitle().hashCode())));
    }

    @Override
    public CorporateEvent extractCorporateEvent(NewsArticle article, EntityMatch primaryEntity) {
        if (article == null || primaryEntity == null) return null;

        String combined = article.getTitle() + " " + (article.getDescription() != null ? article.getDescription() : "");
        EventType eventType = classifyEventType(combined.toLowerCase());

        CorporateEvent event = new CorporateEvent(
            primaryEntity.instrumentId(),
            primaryEntity.symbol(),
            eventType,
            article.getTitle(),
            article.getDescription(),
            LocalDate.now(),
            article.getPublishedAt(),
            article.getInformationAvailableAt(),
            article.getSourceName(),
            article.getImportanceScore(),
            article.getFinancialImpactScore(),
            article.getSentimentLabel()
        );

        event.setSourceUrl(article.getUrl());

        // Extract structured dividend or contract facts
        if (eventType == EventType.DIVIDEND) {
            Pattern divPattern = Pattern.compile("rs\\.?\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
            Matcher m = divPattern.matcher(combined);
            if (m.find()) {
                event.setStructuredPayload("{\"dividendPerShare\": " + m.group(1) + "}");
            }
        }

        return event;
    }

    public EventType classifyEventType(String text) {
        if (text == null) return EventType.OTHER_MATERIAL;
        String t = text.toLowerCase();

        if (t.contains("q1") || t.contains("q2") || t.contains("q3") || t.contains("q4") || t.contains("results") || t.contains("quarterly profit") || t.contains("financial results")) {
            return EventType.EARNINGS_RESULT;
        }
        if (t.contains("dividend")) return EventType.DIVIDEND;
        if (t.contains("buyback")) return EventType.BUYBACK;
        if (t.contains("split") || t.contains("sub-division")) return EventType.STOCK_SPLIT;
        if (t.contains("bonus issue") || t.contains("bonus share")) return EventType.BONUS;
        if (t.contains("acquire") || t.contains("acquisition")) return EventType.ACQUISITION;
        if (t.contains("merger") || t.contains("amalgamation")) return EventType.MERGER;
        if (t.contains("demerger") || t.contains("spin-off")) return EventType.DEMERGER;
        if (t.contains("contract") || t.contains("order win") || t.contains("secures order") || t.contains("wins deal")) return EventType.NEW_CONTRACT;
        if (t.contains("ceo") || t.contains("chief executive")) return EventType.CEO_CHANGE;
        if (t.contains("cfo") || t.contains("chief financial")) return EventType.CFO_CHANGE;
        if (t.contains("resigns") || t.contains("appointed") || t.contains("management change")) return EventType.MANAGEMENT_CHANGE;
        if (t.contains("sebi") || t.contains("penalty") || t.contains("investigation") || t.contains("regulatory")) return EventType.REGULATORY_ACTION;
        if (t.contains("board meeting")) return EventType.BOARD_MEETING;
        if (t.contains("rating")) return EventType.CREDIT_RATING;

        return EventType.OTHER_MATERIAL;
    }
}
