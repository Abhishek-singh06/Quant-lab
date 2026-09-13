package com.quantlab.warehouse.repository;

import com.quantlab.warehouse.entity.IndexConstituent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IndexConstituentRepository extends JpaRepository<IndexConstituent, Long> {

    @Query("SELECT ic FROM IndexConstituent ic WHERE ic.indexId = :indexId AND ic.effectiveFrom <= :asOfDate AND (ic.effectiveTo IS NULL OR ic.effectiveTo >= :asOfDate)")
    List<IndexConstituent> findConstituentsAsOfDate(@Param("indexId") Long indexId, @Param("asOfDate") LocalDate asOfDate);

    List<IndexConstituent> findByIndexId(Long indexId);
}
