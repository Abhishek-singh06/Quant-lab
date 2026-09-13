package com.quantlab.institutional.repository;

import com.quantlab.institutional.entity.FundHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface FundHoldingRepository extends JpaRepository<FundHolding, Long> {

    List<FundHolding> findByDisclosureId(Long disclosureId);

    @Query("SELECT h FROM FundHolding h WHERE h.disclosureId = :disclosureId ORDER BY h.portfolioWeight DESC")
    List<FundHolding> findByDisclosureIdOrderByPortfolioWeightDesc(@Param("disclosureId") Long disclosureId);

    @Query("SELECT h FROM FundHolding h WHERE h.symbol = :symbol AND h.availableAt <= :asOfTime ORDER BY h.dataAsOf DESC, h.portfolioWeight DESC")
    List<FundHolding> findHoldingsBySymbolAvailableAt(@Param("symbol") String symbol, @Param("asOfTime") Instant asOfTime);

    @Query("SELECT h FROM FundHolding h WHERE h.instrumentId = :instrumentId AND h.availableAt <= :asOfTime ORDER BY h.dataAsOf DESC, h.portfolioWeight DESC")
    List<FundHolding> findHoldingsByInstrumentIdAvailableAt(@Param("instrumentId") Long instrumentId, @Param("asOfTime") Instant asOfTime);

    void deleteByDisclosureId(Long disclosureId);
}
