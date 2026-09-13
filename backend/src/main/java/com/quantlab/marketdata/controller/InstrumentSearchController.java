package com.quantlab.marketdata.controller;

import com.quantlab.marketdata.model.InstrumentSearchResult;
import com.quantlab.marketdata.service.InstrumentSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instruments")
@CrossOrigin(origins = "*")
public class InstrumentSearchController {

    private final InstrumentSearchService searchService;

    public InstrumentSearchController(InstrumentSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<InstrumentSearchResult>> search(
            @RequestParam(value = "q", required = false, defaultValue = "") String query,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {
        return ResponseEntity.ok(searchService.search(query, limit));
    }
}
