package com.quantlab.broker.service;

import com.quantlab.broker.entity.LiveOrderEntity;
import com.quantlab.monitoring.entity.ProviderHealthEntity;
import com.quantlab.monitoring.repository.ProviderHealthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderRevalidationService {

    private static final Logger log = LoggerFactory.getLogger(OrderRevalidationService.class);

    private final TradingSafetyGuardService safetyGuardService;
    private final ProviderHealthRepository providerHealthRepository;

    public OrderRevalidationService(
            TradingSafetyGuardService safetyGuardService,
            ProviderHealthRepository providerHealthRepository) {
        this.safetyGuardService = safetyGuardService;
        this.providerHealthRepository = providerHealthRepository;
    }

    public void revalidateOrderBeforeLivePlacement(LiveOrderEntity order, boolean userConfirmed) {
        // 1. User Confirmation Check
        if (!userConfirmed) {
            throw new IllegalArgumentException("REVALIDATION_FAILED: Live order cannot be placed without explicit user confirmation.");
        }

        // 2. Global Safety Lock / Emergency Stop Check
        if (safetyGuardService.isEmergencyStopActive()) {
            throw new IllegalStateException("REVALIDATION_FAILED: Global Emergency Stop is active. Live trading is blocked.");
        }

        // 3. Provider Data Health Gate (Part 19 Integration)
        List<ProviderHealthEntity> providers = providerHealthRepository.findAllByOrderByUpdatedAtDesc();
        for (ProviderHealthEntity provider : providers) {
            if ("CRITICAL".equalsIgnoreCase(provider.getConnectionStatus()) || "UNAVAILABLE".equalsIgnoreCase(provider.getConnectionStatus())) {
                log.warn("REVALIDATION_WARNING: Market data provider [{}] is in {} state.", provider.getProvider(), provider.getConnectionStatus());
                if (provider.getDataAgeSeconds() != null && provider.getDataAgeSeconds() > 1800) {
                    throw new IllegalStateException("REVALIDATION_FAILED: Market data feed is stale (" + provider.getDataAgeSeconds().intValue() + "s old). Stale feed blocks live orders.");
                }
            }
        }

        // 4. Order Status Gate
        if (!"AWAITING_CONFIRMATION".equalsIgnoreCase(order.getStatus()) && !"CREATED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("REVALIDATION_FAILED: Order is in invalid state: " + order.getStatus() + ". Expected AWAITING_CONFIRMATION.");
        }

        log.info("Order [{}] passed authoritative revalidation successfully.", order.getOrderIntentId());
    }
}
