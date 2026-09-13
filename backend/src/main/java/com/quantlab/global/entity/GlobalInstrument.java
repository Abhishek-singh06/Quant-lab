package com.quantlab.global.entity;

import com.quantlab.global.model.AssetClass;
import com.quantlab.global.model.GlobalMarket;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "global_instruments",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_gi_canonical", columnList = "canonical_symbol"),
        @Index(name = "idx_gi_asset_class", columnList = "asset_class"),
        @Index(name = "idx_gi_market", columnList = "market")
    }
)
public class GlobalInstrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "canonical_symbol", nullable = false, unique = true, length = 32)
    private String canonicalSymbol;

    @Column(name = "provider_symbol", nullable = false, length = 64)
    private String providerSymbol;

    @Column(name = "instrument_name", nullable = false, length = 255)
    private String instrumentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_class", nullable = false, length = 32)
    private AssetClass assetClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private GlobalMarket market;

    @Column(nullable = false, length = 32)
    private String country;

    @Column(name = "exchange_source", nullable = false, length = 64)
    private String exchangeSource;

    @Column(nullable = false, length = 16)
    private String currency = "USD";

    @Column(nullable = false, length = 64)
    private String timezone;

    @Column(name = "instrument_type", nullable = false, length = 32)
    private String instrumentType;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    public GlobalInstrument() {}

    public GlobalInstrument(String canonicalSymbol, String providerSymbol, String instrumentName,
                            AssetClass assetClass, GlobalMarket market, String country,
                            String exchangeSource, String currency, String timezone,
                            String instrumentType) {
        this.canonicalSymbol = canonicalSymbol;
        this.providerSymbol = providerSymbol;
        this.instrumentName = instrumentName;
        this.assetClass = assetClass;
        this.market = market;
        this.country = country;
        this.exchangeSource = exchangeSource;
        this.currency = currency;
        this.timezone = timezone;
        this.instrumentType = instrumentType;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCanonicalSymbol() { return canonicalSymbol; }
    public void setCanonicalSymbol(String canonicalSymbol) { this.canonicalSymbol = canonicalSymbol; }
    public String getProviderSymbol() { return providerSymbol; }
    public void setProviderSymbol(String providerSymbol) { this.providerSymbol = providerSymbol; }
    public String getInstrumentName() { return instrumentName; }
    public void setInstrumentName(String instrumentName) { this.instrumentName = instrumentName; }
    public AssetClass getAssetClass() { return assetClass; }
    public void setAssetClass(AssetClass assetClass) { this.assetClass = assetClass; }
    public GlobalMarket getMarket() { return market; }
    public void setMarket(GlobalMarket market) { this.market = market; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getExchangeSource() { return exchangeSource; }
    public void setExchangeSource(String exchangeSource) { this.exchangeSource = exchangeSource; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getInstrumentType() { return instrumentType; }
    public void setInstrumentType(String instrumentType) { this.instrumentType = instrumentType; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
