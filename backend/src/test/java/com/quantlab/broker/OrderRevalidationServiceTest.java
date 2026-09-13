package com.quantlab.broker;

import com.quantlab.broker.entity.LiveOrderEntity;
import com.quantlab.broker.service.OrderRevalidationService;
import com.quantlab.broker.service.TradingSafetyGuardService;
import com.quantlab.monitoring.entity.ProviderHealthEntity;
import com.quantlab.monitoring.repository.ProviderHealthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderRevalidationServiceTest {

    private TradingSafetyGuardService safetyGuardService;
    private ProviderHealthRepository providerHealthRepository;
    private OrderRevalidationService revalidationService;

    @BeforeEach
    void setUp() {
        safetyGuardService = mock(TradingSafetyGuardService.class);
        providerHealthRepository = mock(ProviderHealthRepository.class);
        revalidationService = new OrderRevalidationService(safetyGuardService, providerHealthRepository);
    }

    @Test
    void testRevalidationFailsWhenUserNotConfirmed() {
        LiveOrderEntity order = new LiveOrderEntity();
        order.setStatus("AWAITING_CONFIRMATION");

        assertThrows(IllegalArgumentException.class, () ->
                revalidationService.revalidateOrderBeforeLivePlacement(order, false)
        );
    }

    @Test
    void testRevalidationFailsWhenEmergencyStopActive() {
        when(safetyGuardService.isEmergencyStopActive()).thenReturn(true);

        LiveOrderEntity order = new LiveOrderEntity();
        order.setStatus("AWAITING_CONFIRMATION");

        assertThrows(IllegalStateException.class, () ->
                revalidationService.revalidateOrderBeforeLivePlacement(order, true)
        );
    }

    @Test
    void testRevalidationFailsWhenMarketDataFeedIsStale() {
        when(safetyGuardService.isEmergencyStopActive()).thenReturn(false);

        ProviderHealthEntity staleProvider = new ProviderHealthEntity();
        staleProvider.setProvider("NSE");
        staleProvider.setConnectionStatus("CRITICAL");
        staleProvider.setDataAgeSeconds(3600.0); // 1 hour old

        when(providerHealthRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of(staleProvider));

        LiveOrderEntity order = new LiveOrderEntity();
        order.setStatus("AWAITING_CONFIRMATION");

        assertThrows(IllegalStateException.class, () ->
                revalidationService.revalidateOrderBeforeLivePlacement(order, true)
        );
    }

    @Test
    void testRevalidationPassesWhenAllChecksHealthy() {
        when(safetyGuardService.isEmergencyStopActive()).thenReturn(false);
        when(providerHealthRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(Collections.emptyList());

        LiveOrderEntity order = new LiveOrderEntity();
        order.setId(UUID.randomUUID());
        order.setOrderIntentId(UUID.randomUUID());
        order.setStatus("AWAITING_CONFIRMATION");

        assertDoesNotThrow(() ->
                revalidationService.revalidateOrderBeforeLivePlacement(order, true)
        );
    }
}
