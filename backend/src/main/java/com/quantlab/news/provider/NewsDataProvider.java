package com.quantlab.news.provider;

import com.quantlab.news.entity.NewsArticle;
import java.time.Instant;
import java.util.List;

/**
 * Generic News Data Provider Interface.
 * The application depends strictly on this abstraction, enabling pluggable news vendors.
 */
public interface NewsDataProvider {

    String getProviderName();

    boolean isAvailable();

    List<NewsArticle> fetchLatestNews(Instant since, int limit);

    List<NewsArticle> fetchNewsForSymbol(String symbol, Instant from, Instant to);
}
