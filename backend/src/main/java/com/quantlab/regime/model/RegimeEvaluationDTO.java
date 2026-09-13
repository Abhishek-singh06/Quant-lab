package com.quantlab.regime.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class RegimeEvaluationDTO {

    private String runId;
    private String modelVersion;
    private String evaluationType;
    private LocalDate trainStart;
    private LocalDate trainEnd;
    private LocalDate testStart;
    private LocalDate testEnd;

    private BigDecimal bullForwardReturn20d;
    private BigDecimal bearForwardReturn20d;
    private BigDecimal sidewaysForwardReturn20d;

    private BigDecimal bullSharpe;
    private BigDecimal bearSharpe;
    private BigDecimal regimePersistence;

    private BigDecimal baseline1Sma50Sharpe;
    private BigDecimal baseline2Sma200Sharpe;
    private BigDecimal baseline3MomentumSharpe;
    private BigDecimal baseline4VixSharpe;

    private Map<String, Object> summaryReport;

    public RegimeEvaluationDTO() {}

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getEvaluationType() { return evaluationType; }
    public void setEvaluationType(String evaluationType) { this.evaluationType = evaluationType; }
    public LocalDate getTrainStart() { return trainStart; }
    public void setTrainStart(LocalDate trainStart) { this.trainStart = trainStart; }
    public LocalDate getTrainEnd() { return trainEnd; }
    public void setTrainEnd(LocalDate trainEnd) { this.trainEnd = trainEnd; }
    public LocalDate getTestStart() { return testStart; }
    public void setTestStart(LocalDate testStart) { this.testStart = testStart; }
    public LocalDate getTestEnd() { return testEnd; }
    public void setTestEnd(LocalDate testEnd) { this.testEnd = testEnd; }
    public BigDecimal getBullForwardReturn20d() { return bullForwardReturn20d; }
    public void setBullForwardReturn20d(BigDecimal bullForwardReturn20d) { this.bullForwardReturn20d = bullForwardReturn20d; }
    public BigDecimal getBearForwardReturn20d() { return bearForwardReturn20d; }
    public void setBearForwardReturn20d(BigDecimal bearForwardReturn20d) { this.bearForwardReturn20d = bearForwardReturn20d; }
    public BigDecimal getSidewaysForwardReturn20d() { return sidewaysForwardReturn20d; }
    public void setSidewaysForwardReturn20d(BigDecimal sidewaysForwardReturn20d) { this.sidewaysForwardReturn20d = sidewaysForwardReturn20d; }
    public BigDecimal getBullSharpe() { return bullSharpe; }
    public void setBullSharpe(BigDecimal bullSharpe) { this.bullSharpe = bullSharpe; }
    public BigDecimal getBearSharpe() { return bearSharpe; }
    public void setBearSharpe(BigDecimal bearSharpe) { this.bearSharpe = bearSharpe; }
    public BigDecimal getRegimePersistence() { return regimePersistence; }
    public void setRegimePersistence(BigDecimal regimePersistence) { this.regimePersistence = regimePersistence; }
    public BigDecimal getBaseline1Sma50Sharpe() { return baseline1Sma50Sharpe; }
    public void setBaseline1Sma50Sharpe(BigDecimal baseline1Sma50Sharpe) { this.baseline1Sma50Sharpe = baseline1Sma50Sharpe; }
    public BigDecimal getBaseline2Sma200Sharpe() { return baseline2Sma200Sharpe; }
    public void setBaseline2Sma200Sharpe(BigDecimal baseline2Sma200Sharpe) { this.baseline2Sma200Sharpe = baseline2Sma200Sharpe; }
    public BigDecimal getBaseline3MomentumSharpe() { return baseline3MomentumSharpe; }
    public void setBaseline3MomentumSharpe(BigDecimal baseline3MomentumSharpe) { this.baseline3MomentumSharpe = baseline3MomentumSharpe; }
    public BigDecimal getBaseline4VixSharpe() { return baseline4VixSharpe; }
    public void setBaseline4VixSharpe(BigDecimal baseline4VixSharpe) { this.baseline4VixSharpe = baseline4VixSharpe; }
    public Map<String, Object> getSummaryReport() { return summaryReport; }
    public void setSummaryReport(Map<String, Object> summaryReport) { this.summaryReport = summaryReport; }
}
