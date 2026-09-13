package com.quantlab.watchlist.dto;

import java.time.Instant;
import java.util.List;

public record WatchlistDTO(
        Long id,
        String name,
        String userId,
        String description,
        List<String> symbols,
        Instant createdAt,
        Instant updatedAt,
        int itemCount
) {
    public static WatchlistDTO fromEntity(com.quantlab.model.Watchlist entity) {
        if (entity == null) return null;
        List<String> syms = entity.getSymbols() != null ? entity.getSymbols() : List.of();
        return new WatchlistDTO(
                entity.getId(),
                entity.getName(),
                entity.getUserId(),
                entity.getDescription(),
                syms,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                syms.size()
        );
    }
}
