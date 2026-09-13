package com.quantlab.institutional.repository;

import com.quantlab.institutional.entity.AmcMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmcMasterRepository extends JpaRepository<AmcMaster, Long> {
    Optional<AmcMaster> findByAmcCode(String amcCode);
}
