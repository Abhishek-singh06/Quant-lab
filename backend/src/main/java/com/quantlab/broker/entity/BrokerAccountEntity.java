package com.quantlab.broker.entity;

import com.quantlab.broker.model.BrokerProvider;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "broker_accounts")
public class BrokerAccountEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId = "SYSTEM_DEFAULT_USER";

    @Enumerated(EnumType.STRING)
    @Column(name = "broker_provider", nullable = false, length = 64)
    private BrokerProvider brokerProvider = BrokerProvider.ZERODHA_KITE;

    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;

    @Column(name = "account_name", nullable = false, length = 128)
    private String accountName;

    @Column(name = "connection_status", nullable = false, length = 32)
    private String connectionStatus = "DISCONNECTED";

    @Column(nullable = false, length = 32)
    private String environment = "SANDBOX";

    @Column(name = "is_live_trading_enabled", nullable = false)
    private boolean isLiveTradingEnabled = false;

    @Column(name = "available_cash", nullable = false)
    private double availableCash = 0.0;

    @Column(name = "used_margin", nullable = false)
    private double usedMargin = 0.0;

    @Column(name = "last_authenticated_at")
    private Instant lastAuthenticatedAt;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "last_health_check_at")
    private Instant lastHealthCheckAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BrokerProvider getBrokerProvider() { return brokerProvider; }
    public void setBrokerProvider(BrokerProvider brokerProvider) { this.brokerProvider = brokerProvider; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public boolean isLiveTradingEnabled() { return isLiveTradingEnabled; }
    public void setLiveTradingEnabled(boolean liveTradingEnabled) { isLiveTradingEnabled = liveTradingEnabled; }

    public double getAvailableCash() { return availableCash; }
    public void setAvailableCash(double availableCash) { this.availableCash = availableCash; }

    public double getUsedMargin() { return usedMargin; }
    public void setUsedMargin(double usedMargin) { this.usedMargin = usedMargin; }

    public Instant getLastAuthenticatedAt() { return lastAuthenticatedAt; }
    public void setLastAuthenticatedAt(Instant lastAuthenticatedAt) { this.lastAuthenticatedAt = lastAuthenticatedAt; }

    public Instant getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(Instant tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }

    public Instant getLastHealthCheckAt() { return lastHealthCheckAt; }
    public void setLastHealthCheckAt(Instant lastHealthCheckAt) { this.lastHealthCheckAt = lastHealthCheckAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
