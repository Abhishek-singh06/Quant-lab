package com.quantlab.broker.service;

import com.quantlab.broker.model.LiveOrderDTO;
import com.quantlab.broker.provider.BrokerTradingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SafeOrderExecutionServiceTest {

    private SafeOrderExecutionService safeExecutionService;
    private BrokerTradingProvider mockProvider;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        safeExecutionService = new SafeOrderExecutionService();
        mockProvider = Mockito.mock(BrokerTradingProvider.class);
        accountId = UUID.randomUUID();
    }

    @Test
    void testPrimaryOrderPlacementSuccess() {
        LiveOrderDTO intent = createSampleOrder("cl-001", null, "PENDING");
        when(mockProvider.placeOrder(any())).thenReturn(intent);

        LiveOrderDTO result = safeExecutionService.placeOrderWithReconciliation(mockProvider, accountId, intent);

        assertNotNull(result);
        assertEquals("cl-001", result.idempotencyKey());
        verify(mockProvider, times(1)).placeOrder(any());
        verify(mockProvider, never()).getOrders(any());
    }

    @Test
    void testTimeoutReconciliationFindsExistingOrderPreventsDuplicate() {
        LiveOrderDTO intent = createSampleOrder("cl-dup-002", null, "PENDING");
        LiveOrderDTO confirmedOrder = createSampleOrder("cl-dup-002", "NSE-ORD-999", "OPEN");

        // First attempt throws timeout exception
        when(mockProvider.placeOrder(intent))
                .thenThrow(new RuntimeException("Connection timeout"))
                .thenReturn(intent);

        // Broker query reveals order actually exists!
        when(mockProvider.getOrders(accountId)).thenReturn(List.of(confirmedOrder));

        LiveOrderDTO result = safeExecutionService.placeOrderWithReconciliation(mockProvider, accountId, intent);

        assertNotNull(result);
        assertEquals("NSE-ORD-999", result.brokerOrderId());
        assertEquals("OPEN", result.status());
        // placeOrder should only be called ONCE (the failed initial attempt). It must NOT be retried!
        verify(mockProvider, times(1)).placeOrder(any());
    }

    @Test
    void testTimeoutReconciliationConfirmsOrderAbsentAndRetriesSafely() {
        LiveOrderDTO intent = createSampleOrder("cl-retry-003", null, "PENDING");

        when(mockProvider.placeOrder(intent))
                .thenThrow(new RuntimeException("Connection timeout"))
                .thenReturn(intent);

        // Broker confirms order does not exist
        when(mockProvider.getOrders(accountId)).thenReturn(Collections.emptyList());

        LiveOrderDTO result = safeExecutionService.placeOrderWithReconciliation(mockProvider, accountId, intent);

        assertNotNull(result);
        // Initial attempt + single safe retry = 2 invocations
        verify(mockProvider, times(2)).placeOrder(any());
    }

    @Test
    void testAmbiguousStateThrowsAndHaltsTrading() {
        LiveOrderDTO intent = createSampleOrder("cl-ambiguous-004", null, "PENDING");

        when(mockProvider.placeOrder(intent)).thenThrow(new RuntimeException("Read timeout"));
        // Reconciliation query ALSO fails
        when(mockProvider.getOrders(accountId)).thenThrow(new RuntimeException("Broker API unreachable"));

        assertThrows(IllegalStateException.class, () ->
                safeExecutionService.placeOrderWithReconciliation(mockProvider, accountId, intent)
        );

        // Must never retry placement if broker state is unverified
        verify(mockProvider, times(1)).placeOrder(any());
    }

    private LiveOrderDTO createSampleOrder(String idempotencyKey, String brokerOrderId, String status) {
        return new LiveOrderDTO(
                UUID.randomUUID(),
                accountId,
                brokerOrderId,
                UUID.randomUUID(),
                "RELIANCE",
                "NSE",
                "BUY",
                "LIMIT",
                "CASH",
                10,
                2500.0,
                null,
                null,
                null,
                status,
                0,
                null,
                null,
                UUID.randomUUID(),
                0.85,
                0.90,
                0.05,
                UUID.randomUUID(),
                "LOW",
                0.10,
                true,
                "admin",
                Instant.now(),
                Instant.now(),
                null,
                idempotencyKey,
                Instant.now()
        );
    }
}
