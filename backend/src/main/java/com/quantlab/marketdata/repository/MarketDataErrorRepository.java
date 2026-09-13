package com.quantlab.marketdata.repository;

import com.quantlab.marketdata.entity.MarketDataError;
import com.quantlab.marketdata.model.ErrorCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MarketDataErrorRepository extends JpaRepository<MarketDataError, Long> {

    List<MarketDataError> findByRunId(String runId);

    Page<MarketDataError> findByErrorCategoryOrderByCreatedAtDesc(ErrorCategory category, Pageable pageable);

    Page<MarketDataError> findBySymbolOrderByCreatedAtDesc(String symbol, Pageable pageable);

    long countByRunId(String runId);

    List<MarketDataError> findByCreatedAtAfter(Instant after);
}
