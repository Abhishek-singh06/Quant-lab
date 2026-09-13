package com.quantlab.news.repository;

import com.quantlab.news.entity.NewsArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsArticleEntityRepository extends JpaRepository<NewsArticleEntity, Long> {

    List<NewsArticleEntity> findByArticleId(Long articleId);

    List<NewsArticleEntity> findByInstrumentId(Long instrumentId);
}
