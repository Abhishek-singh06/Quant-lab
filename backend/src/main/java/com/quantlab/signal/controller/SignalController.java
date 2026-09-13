package com.quantlab.signal.controller;

import com.quantlab.signal.model.*;
import com.quantlab.signal.service.SignalEngineService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/signals")
@CrossOrigin(origins = "*")
public class SignalController {

    private final SignalEngineService signalService;

    public SignalController(SignalEngineService signalService) {
        this.signalService = signalService;
    }

    @GetMapping("/current")
    public ResponseEntity<List<SignalDTO>> getAllCurrentSignals() {
        return ResponseEntity.ok(signalService.getAllCurrentSignals());
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<SignalDTO> getLatestSignal(@PathVariable("symbol") String symbol) {
        return signalService.getLatestSignal(symbol)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{symbol}/history")
    public ResponseEntity<List<SignalDTO>> getSignalHistory(@PathVariable("symbol") String symbol) {
        return ResponseEntity.ok(signalService.getSignalHistory(symbol));
    }

    @GetMapping("/evidence/{signalId}")
    public ResponseEntity<List<SignalEvidenceDTO>> getSignalEvidence(@PathVariable("signalId") UUID signalId) {
        return ResponseEntity.ok(signalService.getSignalEvidence(signalId));
    }

    @GetMapping("/components/{signalId}")
    public ResponseEntity<List<SignalComponentDTO>> getSignalComponents(@PathVariable("signalId") UUID signalId) {
        return ResponseEntity.ok(signalService.getSignalComponents(signalId));
    }

    @GetMapping("/{symbol}/transitions")
    public ResponseEntity<List<SignalTransitionDTO>> getSignalTransitions(@PathVariable("symbol") String symbol) {
        return ResponseEntity.ok(signalService.getSignalTransitions(symbol));
    }

    @GetMapping("/{symbol}/as-of/{timestamp}")
    public ResponseEntity<SignalDTO> getPointInTimeSignal(
        @PathVariable("symbol") String symbol,
        @PathVariable("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant timestamp
    ) {
        return signalService.getPointInTimeSignal(symbol, timestamp)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/configuration")
    public ResponseEntity<SignalConfigurationDTO> getActiveConfiguration() {
        return signalService.getActiveConfiguration()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
