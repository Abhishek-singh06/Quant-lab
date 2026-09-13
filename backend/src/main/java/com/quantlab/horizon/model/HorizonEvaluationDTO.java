package com.quantlab.horizon.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class HorizonEvaluationDTO {
    private UUID id;
    private UUID modelVersionId;
    private TradingHorizon horizon;
    private String targetPeriod;
    private String evaluationType;
    private int sampleSize;
    private Double mae;
    private Double rmse;
    private Double r2;
    private Double directionalAccuracy;
    private Double ic;
    private Double rankIc;
    private Double rocAuc;
    private Double brierScore;
    private Double logLoss;
    private Double hitRate;
    private Double maxDrawdown;
    private Map<String, Object> baselineMetrics;
    private Map<String, Object> regimeMetrics;
    private Instant evaluationTimestamp;

    public HorizonEvaluationDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getModelVersionId() { return modelVersionId; }
    public void setModelVersionId(UUID modelVersionId) { this.modelVersionId = modelVersionId; }

    public TradingHorizon getHorizon() { return horizon; }
    public void setHorizon(TradingHorizon horizon) { this.horizon = horizon; }

    public String getTargetPeriod() { return targetPeriod; }
    public void setTargetPeriod(String targetPeriod) { this.targetPeriod = targetPeriod; }

    public String getEvaluationType() { return evaluationType; }
    public void setEvaluationType(String evaluationType) { this.evaluationType = evaluationType; }

    public int getSampleSize() { return sampleSize; }
    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }

    public Double getMae() { return mae; }
    public void setMae(Double mae) { this.mae = mae; }

    public Double getRmse() { return rmse; }
    public void setRmse(Double rmse) { this.rmse = rmse; }

    public Double getR2() { return r2; }
    public void setR2(Double r2) { this.r2 = r2; }

    public Double getDirectionalAccuracy() { return directionalAccuracy; }
    public void setDirectionalAccuracy(Double directionalAccuracy) { this.directionalAccuracy = directionalAccuracy; }

    public Double getIc() { return ic; }
    public void setIc(Double ic) { this.ic = ic; }

    public Double getRankIc() { return rankIc; }
    public void setRankIc(Double rankIc) { this.rankIc = rankIc; }

    public Double getRocAuc() { return rocAuc; }
    public void setRocAuc(Double rocAuc) { this.rocAuc = rocAuc; }

    public Double getBrierScore() { return brierScore; }
    public void setBrierScore(Double brierScore) { this.brierScore = brierScore; }

    public Double getLogLoss() { return logLoss; }
    public void setLogLoss(Double logLoss) { this.logLoss = logLoss; }

    public Double getHitRate() { return hitRate; }
    public void setHitRate(Double hitRate) { this.hitRate = hitRate; }

    public Double getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(Double maxDrawdown) { this.maxDrawdown = maxDrawdown; }

    public Map<String, Object> getBaselineMetrics() { return baselineMetrics; }
    public void setBaselineMetrics(Map<String, Object> baselineMetrics) { this.baselineMetrics = baselineMetrics; }

    public Map<String, Object> getRegimeMetrics() { return regimeMetrics; }
    public void setRegimeMetrics(Map<String, Object> regimeMetrics) { this.regimeMetrics = regimeMetrics; }

    public Instant getEvaluationTimestamp() { return evaluationTimestamp; }
    public void setEvaluationTimestamp(Instant evaluationTimestamp) { this.evaluationTimestamp = evaluationTimestamp; }
}
