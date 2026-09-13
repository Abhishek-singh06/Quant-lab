package com.quantlab.warehouse.repository;

import com.quantlab.warehouse.entity.HistoricalDataQuality;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricalDataQualityRepository extends JpaRepository<HistoricalDataQuality, Long> {

    List<HistoricalDataQuality> findByInstrumentId(Long instrumentId);

    Page<HistoricalDataQuality> findAllByOrderByCheckedAtDesc(Pageable pageable);
}
