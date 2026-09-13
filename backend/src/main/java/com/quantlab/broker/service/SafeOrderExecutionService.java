package com.quantlab.broker.service;

import com.quantlab.broker.model.LiveOrderDTO;
import com.quantlab.broker.provider.BrokerTradingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Institutional safe order execution service.
 * Enforces CCXT/institutional execution rule: Never blindly retry PLACE_ORDER
 * or CANCEL_ORDER on ambiguous network timeouts without prior broker reconciliation.
 */
@Service
public class SafeOrderExecutionService {

    private static final Logger log = LoggerFactory.getLogger(SafeOrderExecutionService.class);

    /**
     * Executes order placement with strict timeout reconciliation and duplicate prevention.
     *
     * @param provider the active trading provider
     * @param accountId the broker account UUID
     * @param orderIntent the declared order intent containing idempotencyKey
     * @return the confirmed LiveOrderDTO
     * @throws IllegalStateException if order state remains ambiguous
     */
    public LiveOrderDTO placeOrderWithReconciliation(
            BrokerTradingProvider provider,
            UUID accountId,
            LiveOrderDTO orderIntent) {

        String key = orderIntent.idempotencyKey();
        if (key == null || key.isBlank()) {
            key = "idemp-" + UUID.randomUUID().toString().substring(0, 8);
        }

        try {
            // Attempt primary placement
            return provider.placeOrder(orderIntent);
        } catch (Exception primaryEx) {
            log.warn("Primary order placement failed or timed out for idempotencyKey: {}. Initiating state reconciliation before any retry.", key, primaryEx);

            // 1. Reconcile: Query broker orders by idempotencyKey / brokerOrderId to see if order actually reached the broker
            final String finalKey = key;
            try {
                Optional<LiveOrderDTO> existingOrder = provider.getOrders(accountId).stream()
                        .filter(o -> finalKey.equals(o.idempotencyKey()) || (orderIntent.brokerOrderId() != null && orderIntent.brokerOrderId().equals(o.brokerOrderId())))
                        .findFirst();

                if (existingOrder.isPresent()) {
                    LiveOrderDTO confirmed = existingOrder.get();
                    log.info("Order already received and recorded by broker for key: {}. Reconciling to status: {}",
                            finalKey, confirmed.status());
                    return confirmed;
                }

                log.warn("Broker confirmed order {} was NOT created. Safe to evaluate single controlled retry.", finalKey);
                // Safe single retry
                return provider.placeOrder(orderIntent);

            } catch (Exception reconEx) {
                log.error("CRITICAL: Broker state reconciliation failed for key: {}. Halting order to prevent duplicate execution.",
                        finalKey, reconEx);
                throw new IllegalStateException("Ambiguous broker state during order placement. Trading halted for key: " + finalKey, reconEx);
            }
        }
    }
}
