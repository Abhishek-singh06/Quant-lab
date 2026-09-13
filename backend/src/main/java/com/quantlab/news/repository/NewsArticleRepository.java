package com.quantlab.news.repository;

import com.quantlab.news.entity.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    Optional<NewsArticle> findByContentHash(String contentHash);

    boolean existsByContentHash(String contentHash);

    @Query("""
        SELECT a FROM NewsArticle a 
        JOIN NewsArticleEntity e ON a.id = e.articleId 
        WHERE e.instrumentId = :instrumentId 
          AND a.informationAvailableAt <= :asOfTime 
        ORDER BY a.informationAvailableAt DESC
    """)
    List<NewsArticle> findArticlesAvailableAt(
        @Param("instrumentId") Long instrumentId,
        @Param("asOfTime") Instant asOfTime
    );

    @Query("""
        SELECT a FROM NewsArticle a 
        JOIN NewsArticleEntity e ON a.id = e.articleId 
        WHERE e.symbol = :symbol 
          AND a.informationAvailableAt <= :asOfTime 
        ORDER BY a.informationAvailableAt DESC
    """)
    List<NewsArticle> findArticlesBySymbolAvailableAt(
        @Param("symbol") String symbol,
        @Param("asOfTime") Instant asOfTime
    );

    Page<NewsArticle> findAllByOrderByInformationAvailableAtDesc(Pageable pageable);
}
