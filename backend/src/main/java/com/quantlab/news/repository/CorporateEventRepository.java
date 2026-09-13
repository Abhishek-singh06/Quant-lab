package com.quantlab.news.repository;

import com.quantlab.news.entity.CorporateEvent;
import com.quantlab.news.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CorporateEventRepository extends JpaRepository<CorporateEvent, Long> {

    @Query("""
        SELECT e FROM CorporateEvent e 
        WHERE e.instrumentId = :instrumentId 
          AND e.informationAvailableAt <= :asOfTime 
          AND e.isCancelled = false 
        ORDER BY e.informationAvailableAt DESC
    """)
    List<CorporateEvent> findEventsAvailableAt(
        @Param("instrumentId") Long instrumentId,
        @Param("asOfTime") Instant asOfTime
    );

    @Query("""
        SELECT e FROM CorporateEvent e 
        WHERE e.symbol = :symbol 
          AND e.informationAvailableAt <= :asOfTime 
          AND e.isCancelled = false 
        ORDER BY e.informationAvailableAt DESC
    """)
    List<CorporateEvent> findEventsBySymbolAvailableAt(
        @Param("symbol") String symbol,
        @Param("asOfTime") Instant asOfTime
    );

    @Query("""
        SELECT e FROM CorporateEvent e 
        WHERE e.instrumentId = :instrumentId 
          AND e.eventType = :eventType 
          AND e.informationAvailableAt <= :asOfTime 
          AND e.isCancelled = false 
        ORDER BY e.informationAvailableAt DESC
    """)
    List<CorporateEvent> findEventsByTypeAvailableAt(
        @Param("instrumentId") Long instrumentId,
        @Param("eventType") EventType eventType,
        @Param("asOfTime") Instant asOfTime
    );

    Page<CorporateEvent> findAllByOrderByInformationAvailableAtDesc(Pageable pageable);
}
