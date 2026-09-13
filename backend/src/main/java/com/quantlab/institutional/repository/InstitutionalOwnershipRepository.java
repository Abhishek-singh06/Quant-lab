package com.quantlab.institutional.repository;

import com.quantlab.institutional.entity.InstitutionalOwnership;
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
public interface InstitutionalOwnershipRepository extends JpaRepository<InstitutionalOwnership, Long> {

    Optional<InstitutionalOwnership> findByInstrumentIdAndPeriodEndAndInstitutionType(
        Long instrumentId, LocalDate periodEnd, InstitutionType institutionType
    );

    @Query("SELECT o FROM InstitutionalOwnership o WHERE o.symbol = :symbol AND o.availableAt <= :asOfTime ORDER BY o.periodEnd DESC")
    List<InstitutionalOwnership> findOwnershipBySymbolAvailableAt(
        @Param("symbol") String symbol,
        @Param("asOfTime") Instant asOfTime
    );

    @Query("SELECT o FROM InstitutionalOwnership o WHERE o.instrumentId = :instrumentId AND o.availableAt <= :asOfTime ORDER BY o.periodEnd DESC")
    List<InstitutionalOwnership> findOwnershipByInstrumentIdAvailableAt(
        @Param("instrumentId") Long instrumentId,
        @Param("asOfTime") Instant asOfTime
    );

    @Query("SELECT o FROM InstitutionalOwnership o WHERE o.symbol = :symbol AND o.periodEnd = :periodEnd AND o.availableAt <= :asOfTime")
    List<InstitutionalOwnership> findOwnershipBySymbolAndPeriodEndAvailableAt(
        @Param("symbol") String symbol,
        @Param("periodEnd") LocalDate periodEnd,
        @Param("asOfTime") Instant asOfTime
    );
}
