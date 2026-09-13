package com.quantlab.global.model;

import java.time.Instant;
import java.time.ZonedDateTime;

public class GlobalMarketStatusDTO {

    private GlobalMarket market;
    private String country;
    private String timezone;
    private SessionStatus sessionStatus;
    private Instant evaluatedAt;
    private String localTimeFormatted;
    private String marketSessionHours;
    private boolean isTradingDay;

    public GlobalMarketStatusDTO() {}

    public GlobalMarketStatusDTO(GlobalMarket market, String country, String timezone,
                                 SessionStatus sessionStatus, Instant evaluatedAt,
                                 String localTimeFormatted, String marketSessionHours,
                                 boolean isTradingDay) {
        this.market = market;
        this.country = country;
        this.timezone = timezone;
        this.sessionStatus = sessionStatus;
        this.evaluatedAt = evaluatedAt;
        this.localTimeFormatted = localTimeFormatted;
        this.marketSessionHours = marketSessionHours;
        this.isTradingDay = isTradingDay;
    }

    public GlobalMarket getMarket() { return market; }
    public void setMarket(GlobalMarket market) { this.market = market; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public SessionStatus getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(SessionStatus sessionStatus) { this.sessionStatus = sessionStatus; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    public String getLocalTimeFormatted() { return localTimeFormatted; }
    public void setLocalTimeFormatted(String localTimeFormatted) { this.localTimeFormatted = localTimeFormatted; }
    public String getMarketSessionHours() { return marketSessionHours; }
    public void setMarketSessionHours(String marketSessionHours) { this.marketSessionHours = marketSessionHours; }
    public boolean isTradingDay() { return isTradingDay; }
    public void setTradingDay(boolean tradingDay) { isTradingDay = tradingDay; }
}
