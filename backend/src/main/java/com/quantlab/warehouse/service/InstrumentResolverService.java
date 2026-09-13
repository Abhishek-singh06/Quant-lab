package com.quantlab.warehouse.service;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.warehouse.entity.Instrument;
import com.quantlab.warehouse.entity.InstrumentHistory;
import com.quantlab.warehouse.repository.InstrumentHistoryRepository;
import com.quantlab.warehouse.repository.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Time-aware Instrument and Symbol Resolver.
 * Maps historical tickers to the correct persistent Instrument ID at any historical point in time.
 */
@Service
public class InstrumentResolverService {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentHistoryRepository historyRepository;

    public InstrumentResolverService(
            InstrumentRepository instrumentRepository,
            InstrumentHistoryRepository historyRepository) {
        this.instrumentRepository = instrumentRepository;
        this.historyRepository = historyRepository;
    }

    public Optional<Long> resolveInstrumentId(String symbol, Exchange exchange, LocalDate asOfDate) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return Optional.empty();
        }
        String cleanSymbol = symbol.trim().toUpperCase();

        // 1. Check historical timeline mappings first
        if (asOfDate != null) {
            Optional<InstrumentHistory> hist = historyRepository.findSymbolAtDate(cleanSymbol, asOfDate);
            if (hist.isPresent()) {
                return Optional.of(hist.get().getInstrumentId());
            }
        }

        // 2. Fallback to current symbol lookup in master
        return instrumentRepository.findByCurrentSymbolAndExchange(cleanSymbol, exchange != null ? exchange : Exchange.NSE)
            .map(Instrument::getId);
    }

    public Optional<String> resolveSymbolAtDate(Long instrumentId, LocalDate asOfDate) {
        if (instrumentId == null) {
            return Optional.empty();
        }

        if (asOfDate != null) {
            Optional<InstrumentHistory> hist = historyRepository.findInstrumentAtDate(instrumentId, asOfDate);
            if (hist.isPresent()) {
                return Optional.of(hist.get().getSymbol());
            }
        }

        return instrumentRepository.findById(instrumentId).map(Instrument::getCurrentSymbol);
    }
}
