package com.quantlab.fundamental.repository;

import com.quantlab.fundamental.entity.FundamentalFiling;
import com.quantlab.fundamental.model.PeriodType;
import com.quantlab.fundamental.model.ReportingBasis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundamentalFilingRepository extends JpaRepository<FundamentalFiling, Long> {

    Optional<FundamentalFiling> findByInstrumentIdAndPeriodEndAndPeriodTypeAndReportingBasisAndVersion(
        Long instrumentId, LocalDate periodEnd, PeriodType periodType, ReportingBasis reportingBasis, int version
    );

    @Query("SELECT f FROM FundamentalFiling f WHERE f.instrumentId = :instrumentId AND f.periodType = :periodType AND f.reportingBasis = :reportingBasis AND f.availableAt <= :asOfTime ORDER BY f.periodEnd DESC, f.version DESC LIMIT 1")
    Optional<FundamentalFiling> findLatestFilingAsOf(
        @Param("instrumentId") Long instrumentId,
        @Param("periodType") PeriodType periodType,
        @Param("reportingBasis") ReportingBasis reportingBasis,
        @Param("asOfTime") Instant asOfTime
    );

    @Query("SELECT f FROM FundamentalFiling f WHERE f.symbol = :symbol AND f.availableAt <= :asOfTime ORDER BY f.periodEnd DESC, f.version DESC")
    List<FundamentalFiling> findFilingsBySymbolAsOf(
        @Param("symbol") String symbol,
        @Param("asOfTime") Instant asOfTime
    );
}
