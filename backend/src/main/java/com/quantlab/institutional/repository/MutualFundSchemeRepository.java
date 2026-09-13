package com.quantlab.institutional.repository;

import com.quantlab.institutional.entity.MutualFundScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MutualFundSchemeRepository extends JpaRepository<MutualFundScheme, Long> {
    Optional<MutualFundScheme> findBySchemeCode(String schemeCode);
    Optional<MutualFundScheme> findByIsin(String isin);
    List<MutualFundScheme> findByAmcId(Long amcId);
    List<MutualFundScheme> findByCategory(String category);
}
