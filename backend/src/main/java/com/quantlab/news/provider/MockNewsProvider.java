package com.quantlab.news.provider;

import com.quantlab.news.entity.NewsArticle;
import com.quantlab.news.model.ArticleProcessingStatus;
import com.quantlab.news.model.SentimentLabel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * Mock News Data Provider.
 * STRICTLY FOR UNIT/INTEGRATION TESTS. Marked source="MOCK".
 */
@Component("mockNewsProvider")
public class MockNewsProvider implements NewsDataProvider {

    @Override
    public String getProviderName() {
        return "MOCK_NEWS";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public List<NewsArticle> fetchLatestNews(Instant since, int limit) {
        List<NewsArticle> list = new ArrayList<>();
        Instant now = Instant.now();

        list.add(createMockArticle(
            1L, "Economic Times", "Reliance secures major 5G enterprise network contract",
            "Reliance Industries digital division signs long term multi-year telecom infrastructure contract.",
            "https://economictimes.example.com/reliance-contract", now.minusSeconds(3600), now.minusSeconds(3500)
        ));

        list.add(createMockArticle(
            1L, "Mint", "TCS reports 8.5% YoY revenue growth in Q3, declares interim dividend",
            "Tata Consultancy Services reports quarterly profit beating street estimates and announces dividend of Rs 28 per share.",
            "https://livemint.example.com/tcs-q3-results", now.minusSeconds(7200), now.minusSeconds(7100)
        ));

        list.add(createMockArticle(
            1L, "Business Standard", "Infosys signs AI deal with European automotive major",
            "Infosys announces strategic collaboration to transform digital cloud infrastructure.",
            "https://business-standard.example.com/infy-ai-deal", now.minusSeconds(10800), now.minusSeconds(10700)
        ));

        return list;
    }

    @Override
    public List<NewsArticle> fetchNewsForSymbol(String symbol, Instant from, Instant to) {
        return fetchLatestNews(from, 10);
    }

    private NewsArticle createMockArticle(Long sourceId, String srcName, String title, String desc, String url, Instant pub, Instant avail) {
        String hash = computeHash(url + ":" + title);
        NewsArticle a = new NewsArticle(sourceId, srcName, title, desc, url, hash, pub, avail);
        a.setImportanceScore(BigDecimal.valueOf(0.85));
        a.setSentimentScore(BigDecimal.valueOf(0.70));
        a.setFinancialImpactScore(BigDecimal.valueOf(0.80));
        a.setSentimentLabel(SentimentLabel.POSITIVE);
        a.setStatus(ArticleProcessingStatus.PROCESSED);
        return a;
    }

    private String computeHash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            return String.valueOf(text.hashCode());
        }
    }
}
