package com.quantlab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class QuantServiceClient {

    private static final Logger log = LoggerFactory.getLogger(QuantServiceClient.class);

    private final RestClient restClient;

    public QuantServiceClient(
            @Value("${quantlab.quant-service.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> checkHealth() {
        try {
            return restClient.get()
                .uri("/health")
                .retrieve()
                .body(Map.class);
        } catch (Exception e) {
            log.warn("Quant service health check failed: {}", e.getMessage());
            return Map.of(
                "status", "DOWN",
                "service", "quant-service",
                "error", e.getMessage()
            );
        }
    }
}
