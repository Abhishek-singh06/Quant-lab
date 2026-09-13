package com.quantlab.fundamental.repository;

import com.quantlab.fundamental.entity.FinancialStatement;
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
public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {

    Optional<FinancialStatement> findByFilingId(Long filingId);

    Optional<FinancialStatement> findFirstByInstrumentIdAndPeriodTypeAndReportingBasisAndAvailableAtLessThanEqualOrderByPeriodEndDesc(
        Long instrumentId, PeriodType periodType, ReportingBasis reportingBasis, Instant asOfTime
    );

    default Optional<FinancialStatement> findLatestStatementAsOf(
        Long instrumentId, PeriodType periodType, ReportingBasis reportingBasis, Instant asOfTime
    ) {
        return findFirstByInstrumentIdAndPeriodTypeAndReportingBasisAndAvailableAtLessThanEqualOrderByPeriodEndDesc(
            instrumentId, periodType, reportingBasis, asOfTime
        );
    }

    @Query("SELECT s FROM FinancialStatement s WHERE s.symbol = :symbol AND s.periodType = :periodType AND s.reportingBasis = :reportingBasis AND s.availableAt <= :asOfTime ORDER BY s.periodEnd DESC")
    List<FinancialStatement> findStatementsBySymbolAndPeriodTypeAsOf(
        @Param("symbol") String symbol,
        @Param("periodType") PeriodType periodType,
        @Param("reportingBasis") ReportingBasis reportingBasis,
        @Param("asOfTime") Instant asOfTime
    );

    List<FinancialStatement> findByInstrumentIdAndPeriodTypeAndReportingBasisAndPeriodEndLessThanEqualAndAvailableAtLessThanEqualOrderByPeriodEndDesc(
        Long instrumentId, PeriodType periodType, ReportingBasis reportingBasis, LocalDate periodEnd, Instant asOfTime, org.springframework.data.domain.Pageable pageable
    );

    default List<FinancialStatement> findPriorStatementsForTtm(
        Long instrumentId, PeriodType periodType, ReportingBasis reportingBasis, LocalDate periodEnd, Instant asOfTime, int limit
    ) {
        return findByInstrumentIdAndPeriodTypeAndReportingBasisAndPeriodEndLessThanEqualAndAvailableAtLessThanEqualOrderByPeriodEndDesc(
            instrumentId, periodType, reportingBasis, periodEnd, asOfTime, org.springframework.data.domain.PageRequest.of(0, limit)
        );
    }
}
