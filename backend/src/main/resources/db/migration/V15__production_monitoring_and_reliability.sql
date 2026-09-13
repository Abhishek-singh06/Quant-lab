-- V15__production_monitoring_and_reliability.sql
-- Schema for QuantLab Part 19: Production Monitoring, Data Quality, Drift & Reliability

CREATE TABLE IF NOT EXISTS monitoring_health_checks (
    id UUID PRIMARY KEY,
    component VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL, -- HEALTHY, DEGRADED, WARNING, CRITICAL, UNKNOWN, UNAVAILABLE
    latency_ms DOUBLE PRECISION,
    message TEXT,
    details JSONB,
    checked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS provider_health (
    id UUID PRIMARY KEY,
    provider VARCHAR(64) NOT NULL,
    connection_status VARCHAR(32) NOT NULL,
    data_freshness_status VARCHAR(32) NOT NULL,
    last_successful_update TIMESTAMP WITH TIME ZONE,
    last_market_timestamp TIMESTAMP WITH TIME ZONE,
    latency_ms DOUBLE PRECISION,
    data_age_seconds DOUBLE PRECISION,
    consecutive_failures INT NOT NULL DEFAULT 0,
    health_score DOUBLE PRECISION NOT NULL DEFAULT 100.0,
    universe_coverage_pct DOUBLE PRECISION NOT NULL DEFAULT 100.0,
    is_failing_over BOOLEAN NOT NULL DEFAULT false,
    fallback_provider VARCHAR(64),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS data_quality_events (
    id UUID PRIMARY KEY,
    provider VARCHAR(64) NOT NULL,
    symbol VARCHAR(32),
    event_type VARCHAR(64) NOT NULL, -- PRICE_ANOMALY, FUTURE_DATA_DETECTED, MISSING_OHLC, NULL_SPIKE, STALE_FEED
    severity VARCHAR(32) NOT NULL, -- INFO, WARNING, ERROR, CRITICAL
    description TEXT NOT NULL,
    affected_records_count INT NOT NULL DEFAULT 1,
    source_timestamp TIMESTAMP WITH TIME ZONE,
    available_timestamp TIMESTAMP WITH TIME ZONE,
    detected_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS feature_drift_events (
    id UUID PRIMARY KEY,
    feature_name VARCHAR(64) NOT NULL,
    metric_type VARCHAR(32) NOT NULL, -- PSI, KS_TEST, WASSERSTEIN, MEAN_DIFF
    observed_value DOUBLE PRECISION NOT NULL,
    threshold DOUBLE PRECISION NOT NULL,
    drift_status VARCHAR(32) NOT NULL, -- NORMAL, WATCH, WARNING, CRITICAL
    sample_size INT NOT NULL,
    reference_window VARCHAR(64),
    evaluation_window VARCHAR(64),
    detected_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS signal_anomaly_events (
    id UUID PRIMARY KEY,
    signal_type VARCHAR(32) NOT NULL,
    anomaly_category VARCHAR(64) NOT NULL, -- BUY_SPIKE, SELL_SPIKE, CONFIDENCE_COLLAPSE, CONCENTRATION_SPIKE
    observed_rate DOUBLE PRECISION NOT NULL,
    expected_rate DOUBLE PRECISION NOT NULL,
    affected_sector VARCHAR(64),
    severity VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    detected_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS monitoring_alerts (
    id UUID PRIMARY KEY,
    rule_id VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    alert_type VARCHAR(64) NOT NULL, -- DATA_FEED_STOPPED, FUTURE_DATA_DETECTED, MODEL_DEGRADED, API_LATENCY_HIGH, etc.
    component VARCHAR(64) NOT NULL,
    severity VARCHAR(32) NOT NULL, -- INFO, WARNING, ERROR, CRITICAL
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN', -- OPEN, ACKNOWLEDGED, RESOLVED, SUPPRESSED
    observed_value VARCHAR(128),
    threshold_value VARCHAR(128),
    message TEXT NOT NULL,
    runbook_ref VARCHAR(128),
    acknowledged_by VARCHAR(64),
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS monitoring_incidents (
    id UUID PRIMARY KEY,
    incident_number VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(256) NOT NULL,
    severity VARCHAR(32) NOT NULL, -- CRITICAL, HIGH, MEDIUM, LOW
    status VARCHAR(32) NOT NULL DEFAULT 'DETECTED', -- DETECTED, INVESTIGATING, MITIGATED, RESOLVED, POSTMORTEM
    affected_components TEXT NOT NULL,
    root_cause TEXT,
    timeline JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS monitoring_rules (
    id VARCHAR(64) PRIMARY KEY,
    rule_version VARCHAR(32) NOT NULL DEFAULT '1.0.0',
    name VARCHAR(128) NOT NULL,
    target_component VARCHAR(64) NOT NULL,
    metric_name VARCHAR(64) NOT NULL,
    condition_operator VARCHAR(16) NOT NULL, -- GT, GTE, LT, LTE, EQ, NEQ
    threshold_value DOUBLE PRECISION NOT NULL,
    window_seconds INT NOT NULL DEFAULT 300,
    cooldown_seconds INT NOT NULL DEFAULT 600,
    severity VARCHAR(32) NOT NULL DEFAULT 'WARNING',
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    runbook_ref VARCHAR(128),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_mon_health_comp ON monitoring_health_checks(component, checked_at DESC);
CREATE INDEX IF NOT EXISTS idx_mon_alerts_status ON monitoring_alerts(status, severity, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_mon_dq_detected ON data_quality_events(detected_at DESC);
