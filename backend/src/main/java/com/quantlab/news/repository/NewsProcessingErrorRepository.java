package com.quantlab.news.repository;

import com.quantlab.news.entity.NewsProcessingError;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsProcessingErrorRepository extends JpaRepository<NewsProcessingError, Long> {

    Page<NewsProcessingError> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
