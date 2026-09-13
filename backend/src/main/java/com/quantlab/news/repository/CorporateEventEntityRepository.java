package com.quantlab.news.repository;

import com.quantlab.news.entity.CorporateEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorporateEventEntityRepository extends JpaRepository<CorporateEventEntity, Long> {

    List<CorporateEventEntity> findByEventId(Long eventId);

    List<CorporateEventEntity> findByInstrumentId(Long instrumentId);
}
