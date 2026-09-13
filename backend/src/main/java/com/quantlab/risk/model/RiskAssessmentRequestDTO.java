package com.quantlab.risk.model;

import java.time.Instant;
import java.util.UUID;

public class RiskAssessmentRequestDTO {
    private String symbol;
    private String signalType; // BUY, HOLD, SELL
    private double signalScore;
    private double signalConfidence;
    private double entryPrice;
    private Double targetPrice;
    private Double atr;
    private Double securityVolatility;
    private Double expectedVolatility;
    private Double userStopPrice;
    private StopMethod stopMethod = StopMethod.ATR_MULTIPLE;
    private PositionSizingMethod sizingMethod = PositionSizingMethod.FIXED_RISK;
    private UUID portfolioId;
    private UUID riskProfileId;
    private Instant asOfDate;

    public RiskAssessmentRequestDTO() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getSignalType() { return signalType; }
    public void setSignalType(String signalType) { this.signalType = signalType; }

    public double getSignalScore() { return signalScore; }
    public void setSignalScore(double signalScore) { this.signalScore = signalScore; }

    public double getSignalConfidence() { return signalConfidence; }
    public void setSignalConfidence(double signalConfidence) { this.signalConfidence = signalConfidence; }

    public double getEntryPrice() { return entryPrice; }
    public void setEntryPrice(double entryPrice) { this.entryPrice = entryPrice; }

    public Double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(Double targetPrice) { this.targetPrice = targetPrice; }

    public Double getAtr() { return atr; }
    public void setAtr(Double atr) { this.atr = atr; }

    public Double getSecurityVolatility() { return securityVolatility; }
    public void setSecurityVolatility(Double securityVolatility) { this.securityVolatility = securityVolatility; }

    public Double getExpectedVolatility() { return expectedVolatility; }
    public void setExpectedVolatility(Double expectedVolatility) { this.expectedVolatility = expectedVolatility; }

    public Double getUserStopPrice() { return userStopPrice; }
    public void setUserStopPrice(Double userStopPrice) { this.userStopPrice = userStopPrice; }

    public StopMethod getStopMethod() { return stopMethod; }
    public void setStopMethod(StopMethod stopMethod) { this.stopMethod = stopMethod; }

    public PositionSizingMethod getSizingMethod() { return sizingMethod; }
    public void setSizingMethod(PositionSizingMethod sizingMethod) { this.sizingMethod = sizingMethod; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public UUID getRiskProfileId() { return riskProfileId; }
    public void setRiskProfileId(UUID riskProfileId) { this.riskProfileId = riskProfileId; }

    public Instant getAsOfDate() { return asOfDate; }
    public void setAsOfDate(Instant asOfDate) { this.asOfDate = asOfDate; }
}
