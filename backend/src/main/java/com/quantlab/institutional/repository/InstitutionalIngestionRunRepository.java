package com.quantlab.institutional.repository;

import com.quantlab.institutional.entity.InstitutionalIngestionRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionalIngestionRunRepository extends JpaRepository<InstitutionalIngestionRun, Long> {
    Optional<InstitutionalIngestionRun> findByRunUuid(String runUuid);
}
