package com.quantlab.warehouse.repository;

import com.quantlab.warehouse.entity.CorporateAction;
import com.quantlab.warehouse.model.CorporateActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CorporateActionRepository extends JpaRepository<CorporateAction, Long> {

    List<CorporateAction> findByInstrumentIdOrderByExDateAsc(Long instrumentId);

    List<CorporateAction> findByInstrumentIdAndActionTypeOrderByExDateAsc(Long instrumentId, CorporateActionType actionType);

    @Query("SELECT ca FROM CorporateAction ca WHERE ca.instrumentId = :instrumentId AND ca.exDate <= :asOfDate AND ca.informationAvailableAt <= :availableAt ORDER BY ca.exDate ASC")
    List<CorporateAction> findPointInTimeCorporateActions(
        @Param("instrumentId") Long instrumentId,
        @Param("asOfDate") LocalDate asOfDate,
        @Param("availableAt") Instant availableAt
    );

    @Query("SELECT ca FROM CorporateAction ca WHERE ca.instrumentId = :instrumentId AND ca.exDate BETWEEN :from AND :to ORDER BY ca.exDate ASC")
    List<CorporateAction> findInDateRange(
        @Param("instrumentId") Long instrumentId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );
}
