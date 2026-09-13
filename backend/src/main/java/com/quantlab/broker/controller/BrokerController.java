package com.quantlab.broker.controller;

import com.quantlab.broker.model.*;
import com.quantlab.broker.service.BrokerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/broker")
public class BrokerController {

    private final BrokerService brokerService;

    public BrokerController(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<BrokerAccountDTO>> getAccounts() {
        return ResponseEntity.ok(brokerService.getAccounts());
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<BrokerAccountDTO> getAccount(@PathVariable UUID id) {
        return brokerService.getAccount(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/accounts/{id}/positions")
    public ResponseEntity<List<LivePositionDTO>> getPositions(@PathVariable UUID id) {
        return ResponseEntity.ok(brokerService.getPositions(id));
    }

    @GetMapping("/accounts/{id}/orders")
    public ResponseEntity<List<LiveOrderDTO>> getOrders(@PathVariable UUID id) {
        return ResponseEntity.ok(brokerService.getOrders(id));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<LiveOrderDTO>> getAllOrders() {
        return ResponseEntity.ok(brokerService.getAllOrders());
    }

    @GetMapping("/safety-lock")
    public ResponseEntity<SafetyLockDTO> getSafetyLock() {
        return ResponseEntity.ok(brokerService.getSafetyLockStatus());
    }

    @PostMapping("/safety-lock/emergency-stop")
    public ResponseEntity<SafetyLockDTO> triggerEmergencyStop(@RequestBody(required = false) Map<String, String> body) {
        String reason = body != null && body.containsKey("reason") ? body.get("reason") : "MANUAL_EMERGENCY_STOP";
        String user = body != null && body.containsKey("user") ? body.get("user") : "OPERATOR";
        return ResponseEntity.ok(brokerService.setEmergencyStop(true, reason, user));
    }

    @PostMapping("/safety-lock/release")
    public ResponseEntity<SafetyLockDTO> releaseEmergencyStop(@RequestBody(required = false) Map<String, String> body) {
        String user = body != null && body.containsKey("user") ? body.get("user") : "OPERATOR";
        return ResponseEntity.ok(brokerService.setEmergencyStop(false, "Emergency stop released after verification.", user));
    }

    @GetMapping("/compliance")
    public ResponseEntity<ComplianceStatusDTO> getComplianceStatus() {
        return ResponseEntity.ok(brokerService.getComplianceStatus());
    }

    @GetMapping("/accounts/{id}/reconciliation")
    public ResponseEntity<PortfolioReconciliationDTO> getReconciliation(@PathVariable UUID id) {
        return brokerService.getReconciliation(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/orders/preview")
    public ResponseEntity<LiveOrderDTO> previewOrder(@RequestBody Map<String, Object> req) {
        UUID accountId = req.containsKey("brokerAccountId") ? UUID.fromString((String) req.get("brokerAccountId")) : UUID.randomUUID();
        String symbol = (String) req.get("symbol");
        String side = (String) req.getOrDefault("side", "BUY");
        String orderType = (String) req.getOrDefault("orderType", "LIMIT");
        int quantity = Integer.parseInt(req.get("quantity").toString());
        double price = Double.parseDouble(req.get("price").toString());
        Double stopLoss = req.containsKey("stopLossPrice") ? Double.parseDouble(req.get("stopLossPrice").toString()) : null;
        Double target = req.containsKey("targetPrice") ? Double.parseDouble(req.get("targetPrice").toString()) : null;
        UUID signalId = req.containsKey("signalId") ? UUID.fromString((String) req.get("signalId")) : null;
        Double signalScore = req.containsKey("signalScore") ? Double.parseDouble(req.get("signalScore").toString()) : null;
        Double signalConfidence = req.containsKey("signalConfidence") ? Double.parseDouble(req.get("signalConfidence").toString()) : null;
        Double expectedReturn = req.containsKey("expectedReturn") ? Double.parseDouble(req.get("expectedReturn").toString()) : null;
        UUID riskId = req.containsKey("riskAssessmentId") ? UUID.fromString((String) req.get("riskAssessmentId")) : null;
        String riskLevel = (String) req.getOrDefault("riskLevel", "MODERATE");
        Double suggestedAlloc = req.containsKey("suggestedAllocation") ? Double.parseDouble(req.get("suggestedAllocation").toString()) : null;

        return ResponseEntity.ok(brokerService.previewOrder(
                accountId, symbol, side, orderType, quantity, price, stopLoss, target,
                signalId, signalScore, signalConfidence, expectedReturn, riskId, riskLevel, suggestedAlloc
        ));
    }

    @PostMapping("/orders/{intentId}/confirm")
    public ResponseEntity<LiveOrderDTO> confirmOrder(
            @PathVariable UUID intentId,
            @RequestBody Map<String, Object> req) {
        String user = req.containsKey("user") ? (String) req.get("user") : "OPERATOR";
        boolean confirmed = req.containsKey("confirmed") && Boolean.parseBoolean(req.get("confirmed").toString());

        return brokerService.confirmAndPlaceLiveOrder(intentId, user, confirmed)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}
