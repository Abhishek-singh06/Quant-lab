package com.quantlab.monitoring.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MonitoringMetricsService {

    private final MeterRegistry meterRegistry;
    private final Counter apiRequestsCounter;
    private final Counter apiErrorsCounter;
    private final Counter ingestionSuccessCounter;
    private final Counter ingestionFailureCounter;
    private final Counter providerFailuresCounter;
    private final Counter staleDataCounter;
    private final Counter alertsCounter;
    private final Counter paperTradingRejectionsCounter;
    private final Counter modelEvaluationsCounter;

    @Autowired
    public MonitoringMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.apiRequestsCounter = Counter.builder("quantlab.api.requests.total")
                .description("Total number of API requests served")
                .register(meterRegistry);

        this.apiErrorsCounter = Counter.builder("quantlab.api.errors.total")
                .description("Total number of API request errors")
                .register(meterRegistry);

        this.ingestionSuccessCounter = Counter.builder("quantlab.ingestion.success.total")
                .description("Total number of successful ingestion runs")
                .register(meterRegistry);

        this.ingestionFailureCounter = Counter.builder("quantlab.ingestion.failure.total")
                .description("Total number of failed ingestion runs")
                .register(meterRegistry);

        this.providerFailuresCounter = Counter.builder("quantlab.provider.failures.total")
                .description("Total number of market data provider connection failures")
                .register(meterRegistry);

        this.staleDataCounter = Counter.builder("quantlab.stale_data.events.total")
                .description("Total number of stale data events detected")
                .register(meterRegistry);

        this.alertsCounter = Counter.builder("quantlab.monitoring.alerts.total")
                .description("Total number of monitoring alerts triggered")
                .register(meterRegistry);

        this.paperTradingRejectionsCounter = Counter.builder("quantlab.papertrading.rejections.total")
                .description("Total number of paper trading orders rejected due to safety or data health checks")
                .register(meterRegistry);

        this.modelEvaluationsCounter = Counter.builder("quantlab.model.evaluations.total")
                .description("Total number of quantitative model drift evaluations performed")
                .register(meterRegistry);
    }

    public void recordApiRequest(String endpoint, long latencyMs, boolean success) {
        apiRequestsCounter.increment();
        if (!success) {
            apiErrorsCounter.increment();
        }
        Timer.builder("quantlab.api.latency")
                .tag("endpoint", endpoint)
                .register(meterRegistry)
                .record(latencyMs, TimeUnit.MILLISECONDS);
    }

    public void recordIngestionRun(boolean success) {
        if (success) {
            ingestionSuccessCounter.increment();
        } else {
            ingestionFailureCounter.increment();
        }
    }

    public void recordProviderFailure(String provider) {
        providerFailuresCounter.increment();
        Counter.builder("quantlab.provider.failures")
                .tag("provider", provider)
                .register(meterRegistry)
                .increment();
    }

    public void recordStaleDataEvent() {
        staleDataCounter.increment();
    }

    public void recordAlert(String severity, String component) {
        alertsCounter.increment();
        Counter.builder("quantlab.monitoring.alerts")
                .tag("severity", severity)
                .tag("component", component)
                .register(meterRegistry)
                .increment();
    }

    public void recordPaperTradingRejection(String reason) {
        paperTradingRejectionsCounter.increment();
        Counter.builder("quantlab.papertrading.rejections")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void recordModelEvaluation() {
        modelEvaluationsCounter.increment();
    }
}
