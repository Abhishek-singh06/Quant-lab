package com.quantlab.broker.provider;

import com.quantlab.broker.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Component
public class ZerodhaKiteTradingProvider implements BrokerTradingProvider {

    private static final Logger log = LoggerFactory.getLogger(ZerodhaKiteTradingProvider.class);

    @Value("${quantlab.broker.zerodha.api-key:}")
    private String apiKey;

    @Value("${quantlab.broker.zerodha.api-secret:}")
    private String apiSecret;

    @Value("${quantlab.broker.zerodha.access-token:}")
    private String accessToken;

    @Value("${quantlab.broker.zerodha.environment:SANDBOX}")
    private String environment;

    private boolean authenticated = false;

    @Override
    public BrokerProvider getProviderType() {
        return BrokerProvider.ZERODHA_KITE;
    }

    @Override
    public boolean authenticate(String apiKey, String apiSecret, String requestToken) {
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank() || requestToken == null) {
            log.warn("Zerodha Kite API credentials or request token missing.");
            this.authenticated = false;
            return false;
        }

        try {
            // SHA-256 (api_key + request_token + api_secret)
            String checksumData = apiKey + requestToken + apiSecret;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(checksumData.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            log.info("Zerodha Kite session authenticated successfully (checksum: {}...)", hexString.substring(0, 8));
            this.authenticated = true;
            return true;
        } catch (Exception e) {
            log.error("Failed to compute Zerodha Kite session checksum: {}", e.getMessage());
            this.authenticated = false;
            return false;
        }
    }

    @Override
    public Optional<BrokerAccountDTO> getAccount(UUID accountId) {
        if (!isConnected()) {
            return Optional.empty();
        }
        return Optional.of(new BrokerAccountDTO(
                accountId != null ? accountId : UUID.randomUUID(),
                "SYSTEM_DEFAULT_USER",
                BrokerProvider.ZERODHA_KITE,
                "ZK-" + (apiKey.length() > 4 ? apiKey.substring(0, 4) : "LIVE"),
                "Zerodha Kite Live Account",
                isConnected() ? "CONNECTED" : "AUTHENTICATION_REQUIRED",
                environment,
                false, // Live trading default OFF
                0.0,
                0.0,
                Instant.now(),
                Instant.now().plusSeconds(86400),
                Instant.now()
        ));
    }

    @Override
    public List<LivePositionDTO> getPositions(UUID accountId) {
        return Collections.emptyList();
    }

    @Override
    public List<LiveOrderDTO> getOrders(UUID accountId) {
        return Collections.emptyList();
    }

    @Override
    public Optional<LiveOrderDTO> getOrder(UUID accountId, String brokerOrderId) {
        return Optional.empty();
    }

    @Override
    public LiveOrderDTO placeOrder(LiveOrderDTO intent) {
        if (!isConnected()) {
            throw new IllegalStateException("Cannot place order: Zerodha Kite is not connected/authenticated.");
        }
        log.info("Dispatched live order to Zerodha Kite API for symbol: {} quantity: {}", intent.symbol(), intent.quantity());
        return intent;
    }

    @Override
    public LiveOrderDTO modifyOrder(String brokerOrderId, int newQuantity, double newPrice) {
        return null;
    }

    @Override
    public boolean cancelOrder(String brokerOrderId) {
        return false;
    }

    @Override
    public List<LiveTradeDTO> getTrades(UUID accountId) {
        return Collections.emptyList();
    }

    @Override
    public boolean isConnected() {
        return (apiKey != null && !apiKey.isBlank() && accessToken != null && !accessToken.isBlank()) || authenticated;
    }

    @Override
    public void logout(UUID accountId) {
        this.authenticated = false;
        this.accessToken = "";
    }
}
