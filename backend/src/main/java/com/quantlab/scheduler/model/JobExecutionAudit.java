package com.quantlab.scheduler.model;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable audit record for scheduled background jobs.
 */
public record JobExecutionAudit(
    String jobName,
    String runId,
    JobExecutionStatus status,
    Instant startTime,
    Instant endTime,
    Long durationMs,
    String message,
    boolean isMarketHoursRestricted,
    int itemsProcessed,
    Map<String, Object> metadata
) {}
