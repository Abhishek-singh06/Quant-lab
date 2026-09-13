package com.quantlab.horizon.controller;

import com.quantlab.horizon.model.*;
import com.quantlab.horizon.service.HorizonEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/horizons")
public class HorizonController {

    private final HorizonEngineService horizonService;

    public HorizonController(HorizonEngineService horizonService) {
        this.horizonService = horizonService;
    }

    @GetMapping("/{symbol}/cross-horizon")
    public ResponseEntity<CrossHorizonViewDTO> getCrossHorizonView(@PathVariable String symbol) {
        return ResponseEntity.ok(horizonService.getCrossHorizonView(symbol));
    }

    @GetMapping("/{symbol}/predictions")
    public ResponseEntity<List<HorizonPredictionDTO>> getPredictions(
            @PathVariable String symbol,
            @RequestParam(required = false) TradingHorizon horizon) {
        return ResponseEntity.ok(horizonService.getPredictions(symbol, horizon));
    }

    @GetMapping("/{symbol}/predictions/{horizon}/latest")
    public ResponseEntity<HorizonPredictionDTO> getLatestPrediction(
            @PathVariable String symbol,
            @PathVariable TradingHorizon horizon) {
        HorizonPredictionDTO pred = horizonService.getLatestPrediction(symbol, horizon);
        if (pred == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pred);
    }
}
