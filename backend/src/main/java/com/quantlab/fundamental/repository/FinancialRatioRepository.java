package com.quantlab.fundamental.repository;

import com.quantlab.fundamental.entity.FinancialRatio;
import com.quantlab.fundamental.model.PeriodType;
import com.quantlab.fundamental.model.ReportingBasis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRatioRepository extends JpaRepository<FinancialRatio, Long> {

    Optional<FinancialRatio> findByFilingId(Long filingId);

    Optional<FinancialRatio> findFirstByInstrumentIdAndPeriodTypeAndReportingBasisAndAvailableAtLessThanEqualOrderByPeriodEndDesc(
        Long instrumentId, PeriodType periodType, ReportingBasis reportingBasis, Instant asOfTime
    );

    default Optional<FinancialRatio> findLatestRatiosAsOf(
        Long instrumentId, PeriodType periodType, ReportingBasis reportingBasis, Instant asOfTime
    ) {
        return findFirstByInstrumentIdAndPeriodTypeAndReportingBasisAndAvailableAtLessThanEqualOrderByPeriodEndDesc(
            instrumentId, periodType, reportingBasis, asOfTime
        );
    }

    @Query("SELECT r FROM FinancialRatio r WHERE r.symbol = :symbol AND r.availableAt <= :asOfTime ORDER BY r.periodEnd DESC")
    List<FinancialRatio> findRatiosBySymbolAsOf(
        @Param("symbol") String symbol,
        @Param("asOfTime") Instant asOfTime
    );
}
