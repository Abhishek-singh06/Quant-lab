package com.quantlab.risk.repository;

import com.quantlab.risk.entity.PortfolioSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshotEntity, UUID> {
    List<PortfolioSnapshotEntity> findByPortfolioIdOrderBySnapshotTimestampDesc(UUID portfolioId);
    Optional<PortfolioSnapshotEntity> findFirstByPortfolioIdOrderBySnapshotTimestampDesc(UUID portfolioId);
    List<PortfolioSnapshotEntity> findByPortfolioIdAndSnapshotTimestampBetweenOrderBySnapshotTimestampAsc(
        UUID portfolioId, Instant start, Instant end
    );
}
