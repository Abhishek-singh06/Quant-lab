package com.quantlab.broker.model;

import java.time.Instant;
import java.util.UUID;

public record ComplianceStatusDTO(
        UUID id,
        String brokerProvider,
        String reviewStatus, // APPROVED_FOR_MANUAL_LIVE_ORDERS, NOT_REVIEWED, PENDING
        String regulatoryFramework,
        boolean isManualConfirmationEnforced,
        boolean isAutomatedTradingDisabled,
        String reviewedBy,
        Instant reviewedAt,
        String reviewNotes
) {}
