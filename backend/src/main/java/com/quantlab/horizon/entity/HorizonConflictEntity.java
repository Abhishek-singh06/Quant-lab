package com.quantlab.horizon.entity;

import com.quantlab.horizon.model.HorizonOutlook;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "horizon_conflicts", schema = "market_data")
public class HorizonConflictEntity {

    @Id
    private UUID id;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "short_term_outlook", nullable = false, length = 20)
    private HorizonOutlook shortTermOutlook;

    @Enumerated(EnumType.STRING)
    @Column(name = "medium_term_outlook", nullable = false, length = 20)
    private HorizonOutlook mediumTermOutlook;

    @Enumerated(EnumType.STRING)
    @Column(name = "long_term_outlook", nullable = false, length = 20)
    private HorizonOutlook longTermOutlook;

    @Column(name = "conflict_detected", nullable = false)
    private boolean conflictDetected;

    @Column(name = "conflict_severity", nullable = false, length = 20)
    private String conflictSeverity;

    @Column(name = "explanation", nullable = false, columnDefinition = "text")
    private String explanation;

    @Column(name = "short_term_pred_id")
    private UUID shortTermPredId;

    @Column(name = "medium_term_pred_id")
    private UUID mediumTermPredId;

    @Column(name = "long_term_pred_id")
    private UUID longTermPredId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public HorizonConflictEntity() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (timestamp == null) timestamp = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public HorizonOutlook getShortTermOutlook() { return shortTermOutlook; }
    public void setShortTermOutlook(HorizonOutlook shortTermOutlook) { this.shortTermOutlook = shortTermOutlook; }

    public HorizonOutlook getMediumTermOutlook() { return mediumTermOutlook; }
    public void setMediumTermOutlook(HorizonOutlook mediumTermOutlook) { this.mediumTermOutlook = mediumTermOutlook; }

    public HorizonOutlook getLongTermOutlook() { return longTermOutlook; }
    public void setLongTermOutlook(HorizonOutlook longTermOutlook) { this.longTermOutlook = longTermOutlook; }

    public boolean isConflictDetected() { return conflictDetected; }
    public void setConflictDetected(boolean conflictDetected) { this.conflictDetected = conflictDetected; }

    public String getConflictSeverity() { return conflictSeverity; }
    public void setConflictSeverity(String conflictSeverity) { this.conflictSeverity = conflictSeverity; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public UUID getShortTermPredId() { return shortTermPredId; }
    public void setShortTermPredId(UUID shortTermPredId) { this.shortTermPredId = shortTermPredId; }

    public UUID getMediumTermPredId() { return mediumTermPredId; }
    public void setMediumTermPredId(UUID mediumTermPredId) { this.mediumTermPredId = mediumTermPredId; }

    public UUID getLongTermPredId() { return longTermPredId; }
    public void setLongTermPredId(UUID longTermPredId) { this.longTermPredId = longTermPredId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
