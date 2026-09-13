package com.quantlab.news.repository;

import com.quantlab.news.entity.NewsSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsSourceRepository extends JpaRepository<NewsSource, Long> {
    Optional<NewsSource> findBySourceName(String sourceName);
}
