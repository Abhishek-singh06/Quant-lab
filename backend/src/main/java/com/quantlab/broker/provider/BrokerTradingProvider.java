package com.quantlab.broker.provider;

import com.quantlab.broker.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrokerTradingProvider {

    BrokerProvider getProviderType();

    boolean authenticate(String apiKey, String apiSecret, String requestToken);

    Optional<BrokerAccountDTO> getAccount(UUID accountId);

    List<LivePositionDTO> getPositions(UUID accountId);

    List<LiveOrderDTO> getOrders(UUID accountId);

    Optional<LiveOrderDTO> getOrder(UUID accountId, String brokerOrderId);

    LiveOrderDTO placeOrder(LiveOrderDTO orderIntent);

    LiveOrderDTO modifyOrder(String brokerOrderId, int newQuantity, double newPrice);

    boolean cancelOrder(String brokerOrderId);

    List<LiveTradeDTO> getTrades(UUID accountId);

    boolean isConnected();

    void logout(UUID accountId);
}
