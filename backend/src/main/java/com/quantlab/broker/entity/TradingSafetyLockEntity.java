package com.quantlab.broker.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "trading_safety_locks")
public class TradingSafetyLockEntity {

    @Id
    private String id; // GLOBAL_SAFETY_LOCK, EMERGENCY_STOP

    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;

    @Column(name = "lock_reason", columnDefinition = "TEXT")
    private String lockReason;

    @Column(name = "activated_by", length = 64)
    private String activatedBy;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getLockReason() { return lockReason; }
    public void setLockReason(String lockReason) { this.lockReason = lockReason; }

    public String getActivatedBy() { return activatedBy; }
    public void setActivatedBy(String activatedBy) { this.activatedBy = activatedBy; }

    public Instant getActivatedAt() { return activatedAt; }
    public void setActivatedAt(Instant activatedAt) { this.activatedAt = activatedAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
