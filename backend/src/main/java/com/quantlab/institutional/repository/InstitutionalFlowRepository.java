package com.quantlab.institutional.repository;

import com.quantlab.institutional.entity.InstitutionalFlow;
import com.quantlab.institutional.model.FlowFrequency;
import com.quantlab.institutional.model.InstitutionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionalFlowRepository extends JpaRepository<InstitutionalFlow, Long> {

    Optional<InstitutionalFlow> findByTradeDateAndInstitutionTypeAndFlowFrequency(
        LocalDate tradeDate, InstitutionType institutionType, FlowFrequency flowFrequency
    );

    @Query("SELECT f FROM InstitutionalFlow f WHERE f.tradeDate BETWEEN :fromDate AND :toDate AND f.availableAt <= :asOfTime ORDER BY f.tradeDate ASC")
    List<InstitutionalFlow> findFlowsInRangeAvailableAt(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("asOfTime") Instant asOfTime
    );

    @Query("SELECT f FROM InstitutionalFlow f WHERE f.institutionType = :institutionType AND f.tradeDate BETWEEN :fromDate AND :toDate AND f.availableAt <= :asOfTime ORDER BY f.tradeDate ASC")
    List<InstitutionalFlow> findFlowsByTypeAndRangeAvailableAt(
        @Param("institutionType") InstitutionType institutionType,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("asOfTime") Instant asOfTime
    );

    List<InstitutionalFlow> findByAvailableAtLessThanEqualOrderByTradeDateDesc(
        Instant asOfTime, org.springframework.data.domain.Pageable pageable
    );

    default List<InstitutionalFlow> findLatestFlowsAvailableAt(Instant asOfTime, int limit) {
        return findByAvailableAtLessThanEqualOrderByTradeDateDesc(asOfTime, org.springframework.data.domain.PageRequest.of(0, limit));
    }
}
