package com.quantlab.controller;

import com.quantlab.service.QuantServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/quant")
public class QuantProxyController {

    private final QuantServiceClient quantServiceClient;

    public QuantProxyController(QuantServiceClient quantServiceClient) {
        this.quantServiceClient = quantServiceClient;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> quantHealth() {
        Map<String, Object> health = quantServiceClient.checkHealth();
        return ResponseEntity.ok(health);
    }
}
