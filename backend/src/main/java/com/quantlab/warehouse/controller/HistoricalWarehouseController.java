package com.quantlab.warehouse.controller;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.warehouse.entity.HistoricalDataQuality;
import com.quantlab.warehouse.entity.HistoricalIngestionRun;
import com.quantlab.warehouse.entity.HistoricalPriceAdjusted;
import com.quantlab.warehouse.entity.HistoricalPriceRaw;
import com.quantlab.warehouse.model.AdjustmentMethodology;
import com.quantlab.warehouse.model.PointInTimeUniverse;
import com.quantlab.warehouse.service.HistoricalBackfillService;
import com.quantlab.warehouse.service.HistoricalDataWarehouseService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/warehouse")
public class HistoricalWarehouseController {

    private final HistoricalDataWarehouseService warehouseService;
    private final HistoricalBackfillService backfillService;

    public HistoricalWarehouseController(
            HistoricalDataWarehouseService warehouseService,
            HistoricalBackfillService backfillService) {
        this.warehouseService = warehouseService;
        this.backfillService = backfillService;
    }

    @GetMapping("/prices/{symbol}/raw")
    public ResponseEntity<List<HistoricalPriceRaw>> getRawPrices(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "NSE") Exchange exchange,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(warehouseService.getRawPrices(symbol, exchange, from, to));
    }

    @GetMapping("/prices/{symbol}/adjusted")
    public ResponseEntity<List<HistoricalPriceAdjusted>> getAdjustedPrices(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "NSE") Exchange exchange,
            @RequestParam(defaultValue = "SPLIT_ADJUSTED") AdjustmentMethodology methodology,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(warehouseService.getAdjustedPrices(symbol, exchange, methodology, from, to));
    }

    @GetMapping("/universe/{indexSymbol}")
    public ResponseEntity<PointInTimeUniverse> getPointInTimeUniverse(
            @PathVariable String indexSymbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        return ResponseEntity.ok(warehouseService.getUniverseAsOf(indexSymbol, asOfDate));
    }

    @GetMapping("/quality-reports")
    public ResponseEntity<Page<HistoricalDataQuality>> getQualityReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(warehouseService.getDataQualityReports(page, size));
    }

    @PostMapping("/backfill")
    public ResponseEntity<HistoricalIngestionRun> triggerBackfill(
            @RequestBody Map<String, Object> payload) {
        List<String> symbols = (List<String>) payload.getOrDefault("symbols", List.of("NIFTY 50", "RELIANCE", "TCS"));
        Exchange exchange = Exchange.valueOf(payload.getOrDefault("exchange", "NSE").toString().toUpperCase());
        LocalDate fromDate = LocalDate.parse(payload.getOrDefault("fromDate", "2024-01-01").toString());
        LocalDate toDate = LocalDate.parse(payload.getOrDefault("toDate", LocalDate.now().toString()).toString());

        HistoricalIngestionRun run = backfillService.executeBackfill(symbols, exchange, fromDate, toDate);
        return ResponseEntity.ok(run);
    }
}
