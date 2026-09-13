package com.quantlab.broker.service;

import com.quantlab.broker.entity.*;
import com.quantlab.broker.model.*;
import com.quantlab.broker.provider.BrokerProviderFactory;
import com.quantlab.broker.provider.BrokerTradingProvider;
import com.quantlab.broker.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class BrokerService {

    private static final Logger log = LoggerFactory.getLogger(BrokerService.class);

    private final BrokerAccountRepository accountRepository;
    private final LiveOrderRepository orderRepository;
    private final LivePositionRepository positionRepository;
    private final LiveTradeRepository tradeRepository;
    private final LivePortfolioReconciliationRepository reconciliationRepository;
    private final TradingSafetyGuardService safetyGuardService;
    private final OrderValidationService orderValidationService;
    private final OrderRevalidationService orderRevalidationService;
    private final PortfolioReconciliationService reconciliationService;
    private final BrokerProviderFactory providerFactory;

    public BrokerService(
            BrokerAccountRepository accountRepository,
            LiveOrderRepository orderRepository,
            LivePositionRepository positionRepository,
            LiveTradeRepository tradeRepository,
            LivePortfolioReconciliationRepository reconciliationRepository,
            TradingSafetyGuardService safetyGuardService,
            OrderValidationService orderValidationService,
            OrderRevalidationService orderRevalidationService,
            PortfolioReconciliationService reconciliationService,
            BrokerProviderFactory providerFactory) {
        this.accountRepository = accountRepository;
        this.orderRepository = orderRepository;
        this.positionRepository = positionRepository;
        this.tradeRepository = tradeRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.safetyGuardService = safetyGuardService;
        this.orderValidationService = orderValidationService;
        this.orderRevalidationService = orderRevalidationService;
        this.reconciliationService = reconciliationService;
        this.providerFactory = providerFactory;
    }

    public List<BrokerAccountDTO> getAccounts() {
        return accountRepository.findAll().stream().map(this::mapAccount).toList();
    }

    public Optional<BrokerAccountDTO> getAccount(UUID id) {
        return accountRepository.findById(id).map(this::mapAccount);
    }

    public List<LivePositionDTO> getPositions(UUID accountId) {
        return positionRepository.findByBrokerAccountId(accountId).stream().map(this::mapPosition).toList();
    }

    public List<LiveOrderDTO> getOrders(UUID accountId) {
        return orderRepository.findByBrokerAccountIdOrderByCreatedAtDesc(accountId).stream().map(this::mapOrder).toList();
    }

    public List<LiveOrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::mapOrder).toList();
    }

    public List<LiveTradeDTO> getTrades(UUID accountId) {
        return tradeRepository.findAll().stream()
                .map(this::mapTrade)
                .toList();
    }

    public SafetyLockDTO getSafetyLockStatus() {
        return safetyGuardService.getSafetyLockStatus();
    }

    @Transactional
    public SafetyLockDTO setEmergencyStop(boolean active, String reason, String user) {
        return safetyGuardService.setEmergencyStop(active, reason, user);
    }

    public ComplianceStatusDTO getComplianceStatus() {
        return new ComplianceStatusDTO(
                UUID.randomUUID(),
                "ZERODHA_KITE / ANGEL_ONE_SMARTAPI",
                "APPROVED_FOR_MANUAL_LIVE_ORDERS",
                "SEBI Retail Algo & API Mandate 2024/2025",
                true,
                true,
                "COMPLIANCE_OFFICER",
                Instant.now(),
                "Quant-lab operates strictly under Manual User Confirmation mode. Automated live order routing is disabled by default."
        );
    }

    public Optional<PortfolioReconciliationDTO> getReconciliation(UUID accountId) {
        return Optional.ofNullable(reconciliationService.reconcileAccount(accountId));
    }

    @Transactional
    public LiveOrderDTO previewOrder(
            UUID brokerAccountId, String symbol, String side, String orderType,
            int quantity, double price, Double stopLoss, Double target,
            UUID signalId, Double signalScore, Double signalConfidence, Double expectedReturn,
            UUID riskAssessmentId, String riskLevel, Double suggestedAllocation) {

        // 1. Validate parameters
        double availableCash = 0.0;
        if (brokerAccountId != null) {
            availableCash = accountRepository.findById(brokerAccountId)
                    .map(BrokerAccountEntity::getAvailableCash)
                    .orElse(0.0);
        }

        orderValidationService.validateOrderParameters(
                symbol, side, orderType, quantity, price, stopLoss, target, availableCash
        );

        // 2. Safety Guards
        safetyGuardService.validateSafetyGuards(quantity * price, 50000.0, 0.0);

        UUID intentId = UUID.randomUUID();
        LiveOrderEntity order = new LiveOrderEntity();
        order.setId(UUID.randomUUID());
        order.setBrokerAccountId(brokerAccountId != null ? brokerAccountId : UUID.randomUUID());
        order.setOrderIntentId(intentId);
        order.setSymbol(symbol.toUpperCase());
        order.setSide(side != null ? side.toUpperCase() : "BUY");
        order.setOrderType(orderType != null ? orderType.toUpperCase() : "LIMIT");
        order.setQuantity(quantity);
        order.setPrice(price);
        order.setStopLossPrice(stopLoss);
        order.setTargetPrice(target);
        order.setStatus("AWAITING_CONFIRMATION");
        order.setSignalId(signalId);
        order.setSignalScore(signalScore != null ? signalScore : 0.0);
        order.setSignalConfidence(signalConfidence != null ? signalConfidence : 0.0);
        order.setExpectedReturn(expectedReturn);
        order.setRiskAssessmentId(riskAssessmentId);
        order.setRiskLevel(riskLevel != null ? riskLevel : "MODERATE");
        order.setSuggestedAllocation(suggestedAllocation != null ? suggestedAllocation : 0.05);
        order.setManuallyConfirmed(false);
        order.setIdempotencyKey("ORD-INTENT-" + intentId);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        return mapOrder(orderRepository.save(order));
    }

    @Transactional
    public Optional<LiveOrderDTO> confirmAndPlaceLiveOrder(UUID orderIntentId, String user, boolean confirmed) {
        LiveOrderEntity order = orderRepository.findAll().stream()
                .filter(o -> orderIntentId.equals(o.getOrderIntentId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Order intent not found: " + orderIntentId));

        // Authoritative Revalidation
        orderRevalidationService.revalidateOrderBeforeLivePlacement(order, confirmed);

        // Dispatch to Provider
        BrokerAccountEntity account = accountRepository.findById(order.getBrokerAccountId()).orElse(null);
        BrokerProvider providerType = account != null ? account.getBrokerProvider() : BrokerProvider.MOCK_SANDBOX;
        BrokerTradingProvider provider = providerFactory.getProvider(providerType);

        order.setStatus("SUBMITTED");
        order.setManuallyConfirmed(true);
        order.setConfirmedByUser(user != null ? user : "OPERATOR");
        order.setConfirmedAt(Instant.now());
        order.setOrderSubmittedAt(Instant.now());
        order.setBrokerOrderId("BROKER-LIVE-" + System.currentTimeMillis());
        order.setUpdatedAt(Instant.now());

        LiveOrderEntity saved = orderRepository.save(order);
        LiveOrderDTO placedDTO = provider.placeOrder(mapOrder(saved));

        saved.setStatus(placedDTO.status());
        saved.setFilledQuantity(placedDTO.filledQuantity());
        saved.setAverageFillPrice(placedDTO.averageFillPrice());
        saved.setOrderExecutedAt(placedDTO.orderExecutedAt());

        return Optional.of(mapOrder(orderRepository.save(saved)));
    }

    private BrokerAccountDTO mapAccount(BrokerAccountEntity e) {
        return new BrokerAccountDTO(
                e.getId(), e.getUserId(), e.getBrokerProvider(), e.getClientId(), e.getAccountName(),
                e.getConnectionStatus(), e.getEnvironment(), e.isLiveTradingEnabled(),
                e.getAvailableCash(), e.getUsedMargin(), e.getLastAuthenticatedAt(),
                e.getTokenExpiresAt(), e.getLastHealthCheckAt()
        );
    }

    private LivePositionDTO mapPosition(LivePositionEntity e) {
        return new LivePositionDTO(
                e.getId(), e.getBrokerAccountId(), e.getSymbol(), e.getProductType(),
                e.getQuantity(), e.getAveragePrice(), e.getCurrentMarketPrice(),
                e.getMarketValue(), e.getUnrealizedPnl(), e.getRealizedPnl(),
                e.getStopPrice(), e.getLastSyncedAt()
        );
    }

    private LiveOrderDTO mapOrder(LiveOrderEntity e) {
        return new LiveOrderDTO(
                e.getId(), e.getBrokerAccountId(), e.getBrokerOrderId(), e.getOrderIntentId(),
                e.getSymbol(), e.getExchange(), e.getSide(), e.getOrderType(), e.getProductType(),
                e.getQuantity(), e.getPrice(), e.getTriggerPrice(), e.getStopLossPrice(), e.getTargetPrice(),
                e.getStatus(), e.getFilledQuantity(), e.getAverageFillPrice(), e.getRejectionReason(),
                e.getSignalId(), e.getSignalScore(), e.getSignalConfidence(), e.getExpectedReturn(),
                e.getRiskAssessmentId(), e.getRiskLevel(), e.getSuggestedAllocation(),
                e.isManuallyConfirmed(), e.getConfirmedByUser(), e.getConfirmedAt(),
                e.getOrderSubmittedAt(), e.getOrderExecutedAt(), e.getIdempotencyKey(),
                e.getCreatedAt()
        );
    }

    private LiveTradeDTO mapTrade(LiveTradeEntity e) {
        return new LiveTradeDTO(
                e.getId(), e.getLiveOrderId(), e.getBrokerTradeId(), e.getSymbol(),
                e.getSide(), e.getQuantity(), e.getExecutionPrice(), e.getBrokerage(),
                e.getStt(), e.getExchangeCharges(), e.getGst(), e.getStampDuty(),
                e.getTotalFees(), e.getExecutionTimestamp()
        );
    }
}
