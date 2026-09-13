package com.quantlab.watchlist.service;

import com.quantlab.model.Watchlist;
import com.quantlab.repository.WatchlistRepository;
import com.quantlab.watchlist.dto.WatchlistDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;

    public WatchlistService(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    public WatchlistDTO createWatchlist(String name, String userId, String description, List<String> symbols) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Watchlist name cannot be empty");
        }
        String uid = (userId != null && !userId.trim().isEmpty()) ? userId : "default_user";
        List<String> sanitizedSymbols = new ArrayList<>();
        if (symbols != null) {
            for (String s : symbols) {
                if (s != null && !s.trim().isEmpty()) {
                    sanitizedSymbols.add(s.trim().toUpperCase());
                }
            }
        }
        Watchlist wl = new Watchlist(name.trim(), uid, description, sanitizedSymbols);
        Watchlist saved = watchlistRepository.save(wl);
        return WatchlistDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<WatchlistDTO> getWatchlistsByUser(String userId) {
        String uid = (userId != null && !userId.trim().isEmpty()) ? userId : "default_user";
        List<Watchlist> list = watchlistRepository.findByUserId(uid);
        if (list.isEmpty()) {
            // Seed a default Indian equity watchlist if none exists
            Watchlist defaultWl = new Watchlist(
                    "NIFTY 50 Core",
                    uid,
                    "Benchmark Indian equities watchlist",
                    List.of("RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK", "ITC", "BHARTIARTL", "SBIN")
            );
            Watchlist saved = watchlistRepository.save(defaultWl);
            return List.of(WatchlistDTO.fromEntity(saved));
        }
        return list.stream().map(WatchlistDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public Optional<WatchlistDTO> getWatchlistById(Long id) {
        return watchlistRepository.findById(id).map(WatchlistDTO::fromEntity);
    }

    public Optional<WatchlistDTO> addSymbol(Long id, String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be empty");
        }
        String cleanSymbol = symbol.trim().toUpperCase();
        return watchlistRepository.findById(id).map(wl -> {
            List<String> syms = wl.getSymbols();
            if (!syms.contains(cleanSymbol)) {
                syms.add(cleanSymbol);
                wl.setSymbols(syms);
                watchlistRepository.save(wl);
            }
            return WatchlistDTO.fromEntity(wl);
        });
    }

    public Optional<WatchlistDTO> removeSymbol(Long id, String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be empty");
        }
        String cleanSymbol = symbol.trim().toUpperCase();
        return watchlistRepository.findById(id).map(wl -> {
            List<String> syms = wl.getSymbols();
            if (syms.remove(cleanSymbol)) {
                wl.setSymbols(syms);
                watchlistRepository.save(wl);
            }
            return WatchlistDTO.fromEntity(wl);
        });
    }

    public boolean deleteWatchlist(Long id) {
        if (watchlistRepository.existsById(id)) {
            watchlistRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
