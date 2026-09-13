package com.quantlab.risk.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "portfolios")
public class PortfolioEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "INR";

    @Column(name = "risk_profile_id")
    private UUID riskProfileId;

    @Column(name = "current_cash", nullable = false)
    private double currentCash;

    @Column(name = "current_portfolio_value", nullable = false)
    private double currentPortfolioValue;

    @Column(name = "peak_portfolio_value", nullable = false)
    private double peakPortfolioValue;

    @Column(name = "peak_timestamp", nullable = false)
    private Instant peakTimestamp;

    @Column(name = "current_drawdown", nullable = false)
    private double currentDrawdown;

    @Column(name = "max_drawdown", nullable = false)
    private double maxDrawdown;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PortfolioPositionEntity> positions = new ArrayList<>();

    public PortfolioEntity() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (peakTimestamp == null) peakTimestamp = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public UUID getRiskProfileId() { return riskProfileId; }
    public void setRiskProfileId(UUID riskProfileId) { this.riskProfileId = riskProfileId; }

    public double getCurrentCash() { return currentCash; }
    public void setCurrentCash(double currentCash) { this.currentCash = currentCash; }

    public double getCurrentPortfolioValue() { return currentPortfolioValue; }
    public void setCurrentPortfolioValue(double currentPortfolioValue) { this.currentPortfolioValue = currentPortfolioValue; }

    public double getPeakPortfolioValue() { return peakPortfolioValue; }
    public void setPeakPortfolioValue(double peakPortfolioValue) { this.peakPortfolioValue = peakPortfolioValue; }

    public Instant getPeakTimestamp() { return peakTimestamp; }
    public void setPeakTimestamp(Instant peakTimestamp) { this.peakTimestamp = peakTimestamp; }

    public double getCurrentDrawdown() { return currentDrawdown; }
    public void setCurrentDrawdown(double currentDrawdown) { this.currentDrawdown = currentDrawdown; }

    public double getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(double maxDrawdown) { this.maxDrawdown = maxDrawdown; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<PortfolioPositionEntity> getPositions() { return positions; }
    public void setPositions(List<PortfolioPositionEntity> positions) { this.positions = positions; }
}
