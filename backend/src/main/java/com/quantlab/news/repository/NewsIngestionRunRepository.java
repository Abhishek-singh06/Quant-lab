package com.quantlab.news.repository;

import com.quantlab.news.entity.NewsIngestionRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsIngestionRunRepository extends JpaRepository<NewsIngestionRun, Long> {

    Optional<NewsIngestionRun> findByRunId(String runId);

    Page<NewsIngestionRun> findAllByOrderByStartTimeDesc(Pageable pageable);
}
