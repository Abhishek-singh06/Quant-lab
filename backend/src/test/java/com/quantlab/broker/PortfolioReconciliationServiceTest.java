package com.quantlab.broker;

import com.quantlab.broker.entity.BrokerAccountEntity;
import com.quantlab.broker.entity.LivePortfolioReconciliationEntity;
import com.quantlab.broker.entity.LivePositionEntity;
import com.quantlab.broker.model.BrokerProvider;
import com.quantlab.broker.model.LivePositionDTO;
import com.quantlab.broker.model.PortfolioReconciliationDTO;
import com.quantlab.broker.provider.BrokerProviderFactory;
import com.quantlab.broker.provider.BrokerTradingProvider;
import com.quantlab.broker.repository.BrokerAccountRepository;
import com.quantlab.broker.repository.LivePortfolioReconciliationRepository;
import com.quantlab.broker.repository.LivePositionRepository;
import com.quantlab.broker.service.PortfolioReconciliationService;
import com.quantlab.monitoring.service.AlertLifecycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PortfolioReconciliationServiceTest {

    private BrokerAccountRepository accountRepository;
    private LivePositionRepository positionRepository;
    private LivePortfolioReconciliationRepository reconciliationRepository;
    private BrokerProviderFactory providerFactory;
    private AlertLifecycleService alertLifecycleService;
    private PortfolioReconciliationService reconciliationService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(BrokerAccountRepository.class);
        positionRepository = mock(LivePositionRepository.class);
        reconciliationRepository = mock(LivePortfolioReconciliationRepository.class);
        providerFactory = mock(BrokerProviderFactory.class);
        alertLifecycleService = mock(AlertLifecycleService.class);

        reconciliationService = new PortfolioReconciliationService(
                accountRepository, positionRepository, reconciliationRepository,
                providerFactory, alertLifecycleService
        );
    }

    @Test
    void testReconciliationMatchedWhenPositionsMatch() {
        UUID accountId = UUID.randomUUID();
        BrokerAccountEntity account = new BrokerAccountEntity();
        account.setId(accountId);
        account.setBrokerProvider(BrokerProvider.MOCK_SANDBOX);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BrokerTradingProvider mockProvider = mock(BrokerTradingProvider.class);
        when(providerFactory.getProvider(BrokerProvider.MOCK_SANDBOX)).thenReturn(mockProvider);

        LivePositionDTO bp = new LivePositionDTO(
                UUID.randomUUID(), accountId, "RELIANCE", "CASH", 50, 2850.0, 2860.0, 143000.0, 500.0, 0.0, 2750.0, Instant.now()
        );
        when(mockProvider.getPositions(accountId)).thenReturn(List.of(bp));

        LivePositionEntity lp = new LivePositionEntity();
        lp.setBrokerAccountId(accountId);
        lp.setSymbol("RELIANCE");
        lp.setQuantity(50);
        when(positionRepository.findByBrokerAccountId(accountId)).thenReturn(List.of(lp));

        when(reconciliationRepository.save(any(LivePortfolioReconciliationEntity.class))).thenAnswer(i -> i.getArgument(0));

        PortfolioReconciliationDTO result = reconciliationService.reconcileAccount(accountId);

        assertNotNull(result);
        assertEquals("MATCHED", result.status());
        assertEquals(1, result.totalPositionsMatched());
        assertEquals(0, result.totalDiscrepanciesCount());
        verify(alertLifecycleService, never()).evaluateAndRaiseAlert(any(), any(), anyDouble(), any());
    }

    @Test
    void testReconciliationMismatchRaisesAlert() {
        UUID accountId = UUID.randomUUID();
        BrokerAccountEntity account = new BrokerAccountEntity();
        account.setId(accountId);
        account.setBrokerProvider(BrokerProvider.MOCK_SANDBOX);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BrokerTradingProvider mockProvider = mock(BrokerTradingProvider.class);
        when(providerFactory.getProvider(BrokerProvider.MOCK_SANDBOX)).thenReturn(mockProvider);

        // Broker has 40 shares, local ledger has 50 shares
        LivePositionDTO bp = new LivePositionDTO(
                UUID.randomUUID(), accountId, "RELIANCE", "CASH", 40, 2850.0, 2860.0, 114400.0, 400.0, 0.0, 2750.0, Instant.now()
        );
        when(mockProvider.getPositions(accountId)).thenReturn(List.of(bp));

        LivePositionEntity lp = new LivePositionEntity();
        lp.setBrokerAccountId(accountId);
        lp.setSymbol("RELIANCE");
        lp.setQuantity(50);
        when(positionRepository.findByBrokerAccountId(accountId)).thenReturn(List.of(lp));

        when(reconciliationRepository.save(any(LivePortfolioReconciliationEntity.class))).thenAnswer(i -> i.getArgument(0));

        PortfolioReconciliationDTO result = reconciliationService.reconcileAccount(accountId);

        assertNotNull(result);
        assertEquals("MISMATCH", result.status());
        assertEquals(1, result.totalDiscrepanciesCount());
        verify(alertLifecycleService, times(1)).evaluateAndRaiseAlert(
                eq("BROKER_RECONCILIATION_MISMATCH"), eq("BROKER_RECONCILIATION"), eq(1.0), any()
        );
    }
}
