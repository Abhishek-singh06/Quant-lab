package com.quantlab.warehouse.repository;

import com.quantlab.warehouse.entity.IndexMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndexMasterRepository extends JpaRepository<IndexMaster, Long> {
    Optional<IndexMaster> findByIndexSymbol(String indexSymbol);
}
