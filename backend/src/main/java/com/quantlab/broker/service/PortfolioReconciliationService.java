package com.quantlab.broker.service;

import com.quantlab.broker.entity.LivePortfolioReconciliationEntity;
import com.quantlab.broker.entity.LivePositionEntity;
import com.quantlab.broker.model.LivePositionDTO;
import com.quantlab.broker.model.PortfolioReconciliationDTO;
import com.quantlab.broker.provider.BrokerProviderFactory;
import com.quantlab.broker.provider.BrokerTradingProvider;
import com.quantlab.broker.repository.BrokerAccountRepository;
import com.quantlab.broker.repository.LivePortfolioReconciliationRepository;
import com.quantlab.broker.repository.LivePositionRepository;
import com.quantlab.monitoring.service.AlertLifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class PortfolioReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioReconciliationService.class);

    private final BrokerAccountRepository accountRepository;
    private final LivePositionRepository positionRepository;
    private final LivePortfolioReconciliationRepository reconciliationRepository;
    private final BrokerProviderFactory providerFactory;
    private final AlertLifecycleService alertLifecycleService;

    public PortfolioReconciliationService(
            BrokerAccountRepository accountRepository,
            LivePositionRepository positionRepository,
            LivePortfolioReconciliationRepository reconciliationRepository,
            BrokerProviderFactory providerFactory,
            AlertLifecycleService alertLifecycleService) {
        this.accountRepository = accountRepository;
        this.positionRepository = positionRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.providerFactory = providerFactory;
        this.alertLifecycleService = alertLifecycleService;
    }

    @Transactional
    public PortfolioReconciliationDTO reconcileAccount(UUID accountId) {
        return accountRepository.findById(accountId).map(account -> {
            BrokerTradingProvider provider = providerFactory.getProvider(account.getBrokerProvider());
            List<LivePositionDTO> brokerPositions = provider.getPositions(accountId);
            List<LivePositionEntity> localPositions = positionRepository.findByBrokerAccountId(accountId);

            int matched = 0;
            int discrepancies = 0;
            List<String> discrepancyDetails = new ArrayList<>();

            Map<String, LivePositionDTO> brokerMap = new HashMap<>();
            for (LivePositionDTO bp : brokerPositions) {
                brokerMap.put(bp.symbol(), bp);
            }

            for (LivePositionEntity lp : localPositions) {
                LivePositionDTO bp = brokerMap.get(lp.getSymbol());
                if (bp == null) {
                    discrepancies++;
                    discrepancyDetails.add(String.format("Symbol %s present in local ledger (qty %d) but missing at broker", lp.getSymbol(), lp.getQuantity()));
                } else if (bp.quantity() != lp.getQuantity()) {
                    discrepancies++;
                    discrepancyDetails.add(String.format("Quantity mismatch for %s: local=%d, broker=%d", lp.getSymbol(), lp.getQuantity(), bp.quantity()));
                } else {
                    matched++;
                }
            }

            String status = discrepancies == 0 ? "MATCHED" : "MISMATCH";

            LivePortfolioReconciliationEntity entity = new LivePortfolioReconciliationEntity();
            entity.setId(UUID.randomUUID());
            entity.setBrokerAccountId(accountId);
            entity.setReconciliationTimestamp(Instant.now());
            entity.setStatus(status);
            entity.setTotalPositionsMatched(matched);
            entity.setTotalDiscrepanciesCount(discrepancies);
            entity.setDiscrepancyDetails(discrepancyDetails.isEmpty() ? null : String.join("; ", discrepancyDetails));
            entity.setCheckedBy("RECONCILIATION_ENGINE");

            LivePortfolioReconciliationEntity saved = reconciliationRepository.save(entity);

            if (discrepancies > 0) {
                log.warn("PORTFOLIO RECONCILIATION MISMATCH on account {}: {} discrepancies", accountId, discrepancies);
                alertLifecycleService.evaluateAndRaiseAlert(
                        "BROKER_RECONCILIATION_MISMATCH",
                        "BROKER_RECONCILIATION",
                        discrepancies,
                        String.format("Found %d position discrepancies on broker account %s", discrepancies, accountId)
                );
            }

            return new PortfolioReconciliationDTO(
                    saved.getId(), saved.getBrokerAccountId(), saved.getReconciliationTimestamp(),
                    saved.getStatus(), saved.getTotalPositionsMatched(), saved.getTotalDiscrepanciesCount(),
                    saved.getDiscrepancyDetails(), saved.getCheckedBy()
            );
        }).orElse(null);
    }
}
