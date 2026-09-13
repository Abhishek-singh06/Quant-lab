package com.quantlab.global.repository;

import com.quantlab.global.entity.GlobalMarketSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalMarketSourceRepository extends JpaRepository<GlobalMarketSource, Long> {
    Optional<GlobalMarketSource> findBySourceName(String sourceName);
}
