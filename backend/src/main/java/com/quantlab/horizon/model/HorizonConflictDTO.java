package com.quantlab.horizon.model;

import java.time.Instant;
import java.util.UUID;

public class HorizonConflictDTO {
    private UUID id;
    private String symbol;
    private Instant timestamp;
    private HorizonOutlook shortTermOutlook;
    private HorizonOutlook mediumTermOutlook;
    private HorizonOutlook longTermOutlook;
    private boolean conflictDetected;
    private String conflictSeverity;
    private String explanation;
    private UUID shortTermPredId;
    private UUID mediumTermPredId;
    private UUID longTermPredId;
    private Instant createdAt;

    public HorizonConflictDTO() {}

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
