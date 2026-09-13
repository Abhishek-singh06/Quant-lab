package com.quantlab.fundamental.repository;

import com.quantlab.fundamental.entity.FundamentalSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FundamentalSourceRepository extends JpaRepository<FundamentalSource, Long> {
    Optional<FundamentalSource> findBySourceName(String sourceName);
}
