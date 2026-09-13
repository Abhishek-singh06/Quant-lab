package com.quantlab.broker.model;

import java.time.Instant;

public record SafetyLockDTO(
        String lockId, // GLOBAL_SAFETY_LOCK, EMERGENCY_STOP
        boolean isActive,
        String lockReason,
        String activatedBy,
        Instant activatedAt
) {}
