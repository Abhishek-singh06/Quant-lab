package com.quantlab.news.intelligence;

import com.quantlab.news.entity.CorporateEvent;
import com.quantlab.news.entity.NewsArticle;
import com.quantlab.news.model.EntityMatch;

import java.util.List;

/**
 * News Intelligence Processor Interface.
 * Allows pluggable NLP implementations (Rule-based, Classical ML, or LLM)
 * while maintaining version tracking and structured output guarantees.
 */
public interface NewsIntelligenceProcessor {

    String getProcessorName();

    String getProcessorVersion();

    List<EntityMatch> extractEntities(String text);

    void enrichArticle(NewsArticle article);

    CorporateEvent extractCorporateEvent(NewsArticle article, EntityMatch primaryEntity);
}
