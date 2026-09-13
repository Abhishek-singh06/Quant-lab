package com.quantlab.paper.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_prediction_outcomes")
public class PaperPredictionOutcomeEntity {

    @Id
    private UUID id;

    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(name = "prediction_id")
    private UUID predictionId;

    @Column(name = "model_version", nullable = false, length = 32)
    private String modelVersion;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 32)
    private String horizon;

    @Column(name = "prediction_timestamp", nullable = false)
    private Instant predictionTimestamp;

    @Column(name = "evaluation_timestamp", nullable = false)
    private Instant evaluationTimestamp;

    @Column(name = "expected_return", nullable = false)
    private Double expectedReturn;

    @Column(name = "realized_return", nullable = false)
    private Double realizedReturn;

    @Column(name = "prediction_error", nullable = false)
    private Double predictionError;

    @Column(name = "absolute_error", nullable = false)
    private Double absoluteError;

    @Column(name = "squared_error", nullable = false)
    private Double squaredError;

    @Column(name = "expected_volatility")
    private Double expectedVolatility;

    @Column(name = "realized_volatility")
    private Double realizedVolatility;

    @Column(name = "is_direction_correct", nullable = false)
    private Boolean isDirectionCorrect;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDecisionId() { return decisionId; }
    public void setDecisionId(UUID decisionId) { this.decisionId = decisionId; }

    public UUID getPredictionId() { return predictionId; }
    public void setPredictionId(UUID predictionId) { this.predictionId = predictionId; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getHorizon() { return horizon; }
    public void setHorizon(String horizon) { this.horizon = horizon; }

    public Instant getPredictionTimestamp() { return predictionTimestamp; }
    public void setPredictionTimestamp(Instant predictionTimestamp) { this.predictionTimestamp = predictionTimestamp; }

    public Instant getEvaluationTimestamp() { return evaluationTimestamp; }
    public void setEvaluationTimestamp(Instant evaluationTimestamp) { this.evaluationTimestamp = evaluationTimestamp; }

    public Double getExpectedReturn() { return expectedReturn; }
    public void setExpectedReturn(Double expectedReturn) { this.expectedReturn = expectedReturn; }

    public Double getRealizedReturn() { return realizedReturn; }
    public void setRealizedReturn(Double realizedReturn) { this.realizedReturn = realizedReturn; }

    public Double getPredictionError() { return predictionError; }
    public void setPredictionError(Double predictionError) { this.predictionError = predictionError; }

    public Double getAbsoluteError() { return absoluteError; }
    public void setAbsoluteError(Double absoluteError) { this.absoluteError = absoluteError; }

    public Double getSquaredError() { return squaredError; }
    public void setSquaredError(Double squaredError) { this.squaredError = squaredError; }

    public Double getExpectedVolatility() { return expectedVolatility; }
    public void setExpectedVolatility(Double expectedVolatility) { this.expectedVolatility = expectedVolatility; }

    public Double getRealizedVolatility() { return realizedVolatility; }
    public void setRealizedVolatility(Double realizedVolatility) { this.realizedVolatility = realizedVolatility; }

    public Boolean getIsDirectionCorrect() { return isDirectionCorrect; }
    public void setIsDirectionCorrect(Boolean isDirectionCorrect) { this.isDirectionCorrect = isDirectionCorrect; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
