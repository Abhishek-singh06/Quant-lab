package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperDataHealthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaperDataHealthRepository extends JpaRepository<PaperDataHealthEntity, UUID> {
    Optional<PaperDataHealthEntity> findFirstByProviderOrderByCheckedAtDesc(String provider);
    Optional<PaperDataHealthEntity> findFirstByOrderByCheckedAtDesc();
}
