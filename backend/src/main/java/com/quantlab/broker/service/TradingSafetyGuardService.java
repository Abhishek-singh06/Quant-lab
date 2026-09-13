package com.quantlab.broker.service;

import com.quantlab.broker.entity.TradingSafetyLockEntity;
import com.quantlab.broker.model.SafetyLockDTO;
import com.quantlab.broker.repository.TradingSafetyLockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class TradingSafetyGuardService {

    private static final Logger log = LoggerFactory.getLogger(TradingSafetyGuardService.class);

    private final TradingSafetyLockRepository safetyLockRepository;

    public TradingSafetyGuardService(TradingSafetyLockRepository safetyLockRepository) {
        this.safetyLockRepository = safetyLockRepository;
    }

    public boolean isEmergencyStopActive() {
        return safetyLockRepository.findById("EMERGENCY_STOP")
                .map(TradingSafetyLockEntity::isActive)
                .orElse(false);
    }

    public SafetyLockDTO getSafetyLockStatus() {
        return safetyLockRepository.findById("EMERGENCY_STOP")
                .map(lock -> new SafetyLockDTO(lock.getId(), lock.isActive(), lock.getLockReason(), lock.getActivatedBy(), lock.getActivatedAt()))
                .orElseGet(() -> new SafetyLockDTO("EMERGENCY_STOP", false, "Normal operations", null, null));
    }

    @Transactional
    public SafetyLockDTO setEmergencyStop(boolean active, String reason, String user) {
        TradingSafetyLockEntity lock = safetyLockRepository.findById("EMERGENCY_STOP")
                .orElseGet(() -> {
                    TradingSafetyLockEntity l = new TradingSafetyLockEntity();
                    l.setId("EMERGENCY_STOP");
                    return l;
                });
        lock.setActive(active);
        lock.setLockReason(reason);
        lock.setActivatedBy(user);
        lock.setActivatedAt(Instant.now());
        lock.setUpdatedAt(Instant.now());
        safetyLockRepository.save(lock);

        log.warn("TRADING SAFETY LOCK UPDATED: Emergency Stop = {} by {} (Reason: {})", active, user, reason);
        return new SafetyLockDTO(lock.getId(), lock.isActive(), lock.getLockReason(), lock.getActivatedBy(), lock.getActivatedAt());
    }

    public void validateSafetyGuards(double orderValue, double maxDailyLoss, double currentDayLoss) {
        if (isEmergencyStopActive()) {
            throw new IllegalStateException("CRITICAL SAFETY BLOCK: Emergency stop is active. All live orders are blocked.");
        }
        if (maxDailyLoss > 0 && (currentDayLoss + orderValue * 0.05) > maxDailyLoss) {
            throw new IllegalStateException(String.format("DAILY LOSS GUARD BREACH: Projected loss exceeds max daily loss limit of %.2f", maxDailyLoss));
        }
    }
}
