package com.quantlab.signal.entity;

import com.quantlab.signal.model.SignalType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signal_transitions", schema = "market_data")
public class SignalTransitionEntity {

    @Id
    private UUID id;

    @Column(name = "instrument_id")
    private Long instrumentId;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_signal", length = 20)
    private SignalType previousSignal;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_signal", nullable = false, length = 20)
    private SignalType newSignal;

    @Column(name = "previous_score")
    private Double previousScore;

    @Column(name = "new_score", nullable = false)
    private Double newScore;

    @Column(name = "transition_timestamp", nullable = false)
    private Instant transitionTimestamp;

    @Column(name = "transition_reason", nullable = false, columnDefinition = "TEXT")
    private String transitionReason;

    @Column(name = "signal_id")
    private UUID signalId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SignalTransitionEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public SignalType getPreviousSignal() { return previousSignal; }
    public void setPreviousSignal(SignalType previousSignal) { this.previousSignal = previousSignal; }

    public SignalType getNewSignal() { return newSignal; }
    public void setNewSignal(SignalType newSignal) { this.newSignal = newSignal; }

    public Double getPreviousScore() { return previousScore; }
    public void setPreviousScore(Double previousScore) { this.previousScore = previousScore; }

    public Double getNewScore() { return newScore; }
    public void setNewScore(Double newScore) { this.newScore = newScore; }

    public Instant getTransitionTimestamp() { return transitionTimestamp; }
    public void setTransitionTimestamp(Instant transitionTimestamp) { this.transitionTimestamp = transitionTimestamp; }

    public String getTransitionReason() { return transitionReason; }
    public void setTransitionReason(String transitionReason) { this.transitionReason = transitionReason; }

    public UUID getSignalId() { return signalId; }
    public void setSignalId(UUID signalId) { this.signalId = signalId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
