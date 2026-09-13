package com.quantlab.news.intelligence;

import com.quantlab.news.entity.NewsArticle;
import com.quantlab.news.repository.NewsArticleRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Article Deduplication and Clustering Engine.
 * 
 * Prevents duplicate news ingestion and groups syndicated articles into single event clusters
 * to avoid artificial sentiment and importance inflation.
 */
@Service
public class EventDeduplicationService {

    private final NewsArticleRepository articleRepository;

    public EventDeduplicationService(NewsArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public String computeContentHash(String url, String title) {
        String canonical = (url != null ? url.trim().toLowerCase() : "") + ":" +
                           (title != null ? title.trim().toLowerCase().replaceAll("\\s+", " ") : "");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            return String.valueOf(canonical.hashCode());
        }
    }

    public boolean isDuplicate(String contentHash) {
        return articleRepository.existsByContentHash(contentHash);
    }

    public Optional<NewsArticle> findDuplicate(String contentHash) {
        return articleRepository.findByContentHash(contentHash);
    }

    public String generateClusterId(String symbol, String eventType, String normalizedTitleKeyword) {
        String base = (symbol != null ? symbol : "GEN") + "_" + (eventType != null ? eventType : "EVT") + "_" + normalizedTitleKeyword;
        return Integer.toHexString(base.hashCode());
    }
}
