package com.quantlab.watchlist.controller;

import com.quantlab.watchlist.dto.WatchlistDTO;
import com.quantlab.watchlist.service.WatchlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/watchlists")
@CrossOrigin(origins = "*")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public ResponseEntity<List<WatchlistDTO>> getWatchlists(
            @RequestParam(value = "userId", defaultValue = "default_user") String userId) {
        return ResponseEntity.ok(watchlistService.getWatchlistsByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WatchlistDTO> getWatchlist(@PathVariable Long id) {
        return watchlistService.getWatchlistById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WatchlistDTO> createWatchlist(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.getOrDefault("name", "New Watchlist");
        String userId = (String) payload.getOrDefault("userId", "default_user");
        String description = (String) payload.get("description");
        List<String> symbols = (List<String>) payload.get("symbols");

        WatchlistDTO created = watchlistService.createWatchlist(name, userId, description, symbols);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{id}/symbols")
    public ResponseEntity<WatchlistDTO> addSymbol(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String symbol = payload.get("symbol");
        if (symbol == null || symbol.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return watchlistService.addSymbol(id, symbol)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/symbols/{symbol}")
    public ResponseEntity<WatchlistDTO> removeSymbol(
            @PathVariable Long id,
            @PathVariable String symbol) {
        return watchlistService.removeSymbol(id, symbol)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWatchlist(@PathVariable Long id) {
        boolean deleted = watchlistService.deleteWatchlist(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
