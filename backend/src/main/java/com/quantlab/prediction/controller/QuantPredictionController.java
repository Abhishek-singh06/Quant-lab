package com.quantlab.prediction.controller;

import com.quantlab.prediction.model.ModelPredictionDTO;
import com.quantlab.prediction.model.ModelStatus;
import com.quantlab.prediction.model.ModelType;
import com.quantlab.prediction.model.QuantModelDTO;
import com.quantlab.prediction.service.ModelRegistryService;
import com.quantlab.prediction.service.PredictionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/models")
public class QuantPredictionController {

    private final ModelRegistryService modelRegistryService;
    private final PredictionService predictionService;

    public QuantPredictionController(
            ModelRegistryService modelRegistryService,
            PredictionService predictionService) {
        this.modelRegistryService = modelRegistryService;
        this.predictionService = predictionService;
    }

    @GetMapping
    public ResponseEntity<List<QuantModelDTO>> listModels(
            @RequestParam(name = "type", required = false) ModelType type,
            @RequestParam(name = "status", required = false) ModelStatus status) {
        List<QuantModelDTO> models = modelRegistryService.listModels(type, status);
        return ResponseEntity.ok(models);
    }

    @GetMapping("/{modelId}")
    public ResponseEntity<QuantModelDTO> getModelById(@PathVariable("modelId") String modelId) {
        return modelRegistryService.getModelById(modelId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<QuantModelDTO> registerModel(@RequestBody QuantModelDTO request) {
        QuantModelDTO registered = modelRegistryService.registerModel(request);
        return ResponseEntity.ok(registered);
    }

    @GetMapping("/predictions/latest")
    public ResponseEntity<ModelPredictionDTO> getLatestPrediction(
            @RequestParam(name = "symbol", defaultValue = "NIFTY 50") String symbol,
            @RequestParam(name = "asOf", required = false) Instant asOf) {
        return predictionService.getLatestPrediction(symbol, asOf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/predictions/{symbol}")
    public ResponseEntity<List<ModelPredictionDTO>> getPredictionsBySymbol(@PathVariable("symbol") String symbol) {
        List<ModelPredictionDTO> list = predictionService.getPredictionsBySymbol(symbol);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/predictions/cross-section")
    public ResponseEntity<List<ModelPredictionDTO>> getCrossSection(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "version", required = false) String version) {
        List<ModelPredictionDTO> list = predictionService.getCrossSectionByDate(date, version);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/predictions/record")
    public ResponseEntity<ModelPredictionDTO> recordPrediction(@RequestBody ModelPredictionDTO request) {
        ModelPredictionDTO saved = predictionService.savePrediction(request);
        return ResponseEntity.ok(saved);
    }
}
