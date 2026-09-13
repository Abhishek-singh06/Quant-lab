package com.quantlab.scheduler.model;

/**
 * Execution status lifecycle for QuantLab background scheduled jobs.
 */
public enum JobExecutionStatus {
    SCHEDULED,
    RUNNING,
    SUCCESS,
    FAILED,
    SKIPPED,
    DISABLED
}
