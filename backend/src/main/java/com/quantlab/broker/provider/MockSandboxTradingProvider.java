package com.quantlab.broker.provider;

import com.quantlab.broker.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockSandboxTradingProvider implements BrokerTradingProvider {

    private static final Logger log = LoggerFactory.getLogger(MockSandboxTradingProvider.class);

    private final Map<UUID, BrokerAccountDTO> accounts = new ConcurrentHashMap<>();
    private final Map<String, LiveOrderDTO> orders = new ConcurrentHashMap<>();
    private final Map<UUID, List<LivePositionDTO>> positions = new ConcurrentHashMap<>();
    private final Map<UUID, List<LiveTradeDTO>> trades = new ConcurrentHashMap<>();
    private boolean connected = true;

    public MockSandboxTradingProvider() {
        UUID defaultAccountId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        accounts.put(defaultAccountId, new BrokerAccountDTO(
                defaultAccountId,
                "SYSTEM_DEFAULT_USER",
                BrokerProvider.MOCK_SANDBOX,
                "MOCK-CLIENT-001",
                "QuantLab Sandbox Account",
                "CONNECTED",
                "SANDBOX",
                false,
                500000.0,
                0.0,
                Instant.now(),
                Instant.now().plusSeconds(86400),
                Instant.now()
        ));
    }

    @Override
    public BrokerProvider getProviderType() {
        return BrokerProvider.MOCK_SANDBOX;
    }

    @Override
    public boolean authenticate(String apiKey, String apiSecret, String requestToken) {
        this.connected = true;
        log.info("MockSandboxTradingProvider authenticated successfully.");
        return true;
    }

    @Override
    public Optional<BrokerAccountDTO> getAccount(UUID accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    @Override
    public List<LivePositionDTO> getPositions(UUID accountId) {
        return positions.getOrDefault(accountId, Collections.emptyList());
    }

    @Override
    public List<LiveOrderDTO> getOrders(UUID accountId) {
        return orders.values().stream().filter(o -> accountId.equals(o.brokerAccountId())).toList();
    }

    @Override
    public Optional<LiveOrderDTO> getOrder(UUID accountId, String brokerOrderId) {
        return Optional.ofNullable(orders.get(brokerOrderId));
    }

    @Override
    public LiveOrderDTO placeOrder(LiveOrderDTO intent) {
        String brokerOrderId = "MOCK-ORD-" + System.currentTimeMillis();
        Instant now = Instant.now();

        LiveOrderDTO placed = new LiveOrderDTO(
                intent.id(),
                intent.brokerAccountId(),
                brokerOrderId,
                intent.orderIntentId(),
                intent.symbol(),
                intent.exchange(),
                intent.side(),
                intent.orderType(),
                intent.productType(),
                intent.quantity(),
                intent.price(),
                intent.triggerPrice(),
                intent.stopLossPrice(),
                intent.targetPrice(),
                "FILLED",
                intent.quantity(),
                intent.price(),
                null,
                intent.signalId(),
                intent.signalScore(),
                intent.signalConfidence(),
                intent.expectedReturn(),
                intent.riskAssessmentId(),
                intent.riskLevel(),
                intent.suggestedAllocation(),
                true,
                intent.confirmedByUser(),
                intent.confirmedAt(),
                now,
                now,
                intent.idempotencyKey(),
                intent.createdAt()
        );

        orders.put(brokerOrderId, placed);

        // Record synthetic Trade
        LiveTradeDTO trade = new LiveTradeDTO(
                UUID.randomUUID(),
                placed.id(),
                "MOCK-TRD-" + System.currentTimeMillis(),
                placed.symbol(),
                placed.side(),
                placed.quantity(),
                placed.price(),
                20.0,
                placed.price() * placed.quantity() * 0.001,
                placed.price() * placed.quantity() * 0.00003,
                18.0,
                placed.price() * placed.quantity() * 0.00015,
                40.0,
                now
        );
        trades.computeIfAbsent(placed.brokerAccountId(), k -> new ArrayList<>()).add(trade);

        return placed;
    }

    @Override
    public LiveOrderDTO modifyOrder(String brokerOrderId, int newQuantity, double newPrice) {
        LiveOrderDTO existing = orders.get(brokerOrderId);
        if (existing == null) return null;
        LiveOrderDTO modified = new LiveOrderDTO(
                existing.id(), existing.brokerAccountId(), existing.brokerOrderId(), existing.orderIntentId(),
                existing.symbol(), existing.exchange(), existing.side(), existing.orderType(), existing.productType(),
                newQuantity, newPrice, existing.triggerPrice(), existing.stopLossPrice(), existing.targetPrice(),
                "OPEN", existing.filledQuantity(), existing.averageFillPrice(), existing.rejectionReason(),
                existing.signalId(), existing.signalScore(), existing.signalConfidence(), existing.expectedReturn(),
                existing.riskAssessmentId(), existing.riskLevel(), existing.suggestedAllocation(),
                existing.isManuallyConfirmed(), existing.confirmedByUser(), existing.confirmedAt(),
                existing.orderSubmittedAt(), existing.orderExecutedAt(), existing.idempotencyKey(), existing.createdAt()
        );
        orders.put(brokerOrderId, modified);
        return modified;
    }

    @Override
    public boolean cancelOrder(String brokerOrderId) {
        LiveOrderDTO existing = orders.get(brokerOrderId);
        if (existing == null) return false;
        LiveOrderDTO cancelled = new LiveOrderDTO(
                existing.id(), existing.brokerAccountId(), existing.brokerOrderId(), existing.orderIntentId(),
                existing.symbol(), existing.exchange(), existing.side(), existing.orderType(), existing.productType(),
                existing.quantity(), existing.price(), existing.triggerPrice(), existing.stopLossPrice(), existing.targetPrice(),
                "CANCELLED", existing.filledQuantity(), existing.averageFillPrice(), existing.rejectionReason(),
                existing.signalId(), existing.signalScore(), existing.signalConfidence(), existing.expectedReturn(),
                existing.riskAssessmentId(), existing.riskLevel(), existing.suggestedAllocation(),
                existing.isManuallyConfirmed(), existing.confirmedByUser(), existing.confirmedAt(),
                existing.orderSubmittedAt(), existing.orderExecutedAt(), existing.idempotencyKey(), existing.createdAt()
        );
        orders.put(brokerOrderId, cancelled);
        return true;
    }

    @Override
    public List<LiveTradeDTO> getTrades(UUID accountId) {
        return trades.getOrDefault(accountId, Collections.emptyList());
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void logout(UUID accountId) {
        this.connected = false;
    }
}
