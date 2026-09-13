package com.quantlab.marketdata.repository;

import com.quantlab.marketdata.entity.MarketDataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketDataSourceRepository extends JpaRepository<MarketDataSource, Long> {

    Optional<MarketDataSource> findBySourceName(String sourceName);

    Optional<MarketDataSource> findByIsPrimaryTrue();
}
