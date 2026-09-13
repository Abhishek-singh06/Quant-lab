package com.quantlab.institutional.repository;

import com.quantlab.institutional.entity.FundPortfolioDisclosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundPortfolioDisclosureRepository extends JpaRepository<FundPortfolioDisclosure, Long> {

    Optional<FundPortfolioDisclosure> findBySchemeIdAndDataAsOf(Long schemeId, LocalDate dataAsOf);

    @Query("SELECT d FROM FundPortfolioDisclosure d WHERE d.schemeId = :schemeId AND d.availableAt <= :asOfTime ORDER BY d.dataAsOf DESC, d.availableAt DESC LIMIT 1")
    Optional<FundPortfolioDisclosure> findLatestDisclosureAsOf(@Param("schemeId") Long schemeId, @Param("asOfTime") Instant asOfTime);

    @Query("SELECT d FROM FundPortfolioDisclosure d WHERE d.schemeId = :schemeId AND d.dataAsOf < :currentDataAsOf AND d.availableAt <= :asOfTime ORDER BY d.dataAsOf DESC LIMIT 1")
    Optional<FundPortfolioDisclosure> findPreviousDisclosureAsOf(@Param("schemeId") Long schemeId, @Param("currentDataAsOf") LocalDate currentDataAsOf, @Param("asOfTime") Instant asOfTime);

    @Query("SELECT d FROM FundPortfolioDisclosure d WHERE d.schemeId = :schemeId ORDER BY d.dataAsOf DESC")
    List<FundPortfolioDisclosure> findAllBySchemeIdOrderByDataAsOfDesc(@Param("schemeId") Long schemeId);
}
