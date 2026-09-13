package com.quantlab.news.intelligence;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.news.model.EntityMatch;
import com.quantlab.warehouse.entity.Instrument;
import com.quantlab.warehouse.repository.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entity Resolution Engine.
 * Recognizes listed Indian companies, tickers, and company name variants in financial text.
 * Assigns confidence scores and rejects ambiguous/false dictionary matches.
 */
@Service
public class EntityResolutionService {

    private final InstrumentRepository instrumentRepository;

    // Cache of entity dictionaries
    private final Map<String, Instrument> symbolMap = new HashMap<>();
    private final Map<String, Instrument> nameMap = new HashMap<>();

    public EntityResolutionService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
        refreshEntityDictionary();
    }

    public synchronized void refreshEntityDictionary() {
        symbolMap.clear();
        nameMap.clear();

        // Seed core Indian mega-caps
        registerKnownEntity("RELIANCE", "Reliance Industries Limited", "RELIANCE", "INE002A01018");
        registerKnownEntity("TCS", "Tata Consultancy Services Limited", "TCS", "INE467B01029");
        registerKnownEntity("HDFCBANK", "HDFC Bank Limited", "HDFCBANK", "INE040A01034");
        registerKnownEntity("INFY", "Infosys Limited", "INFY", "INE009A01021");
        registerKnownEntity("ICICIBANK", "ICICI Bank Limited", "ICICIBANK", "INE090A01021");
        registerKnownEntity("SBIN", "State Bank of India", "SBIN", "INE062A01020");
        registerKnownEntity("BHARTIARTL", "Bharti Airtel Limited", "BHARTIARTL", "INE397D01024");
        registerKnownEntity("ITC", "ITC Limited", "ITC", "INE154A01025");
        registerKnownEntity("KOTAKBANK", "Kotak Mahindra Bank Limited", "KOTAKBANK", "INE237A01028");
        registerKnownEntity("LT", "Larsen & Toubro Limited", "LT", "INE018A01030");
    }

    private void registerKnownEntity(String symbol, String name, String ticker, String isin) {
        Instrument inst = new Instrument(isin, Exchange.NSE, symbol, name, null, com.quantlab.warehouse.model.InstrumentStatus.ACTIVE, null, null, false);
        inst.setId((long) (symbol.hashCode() & 0x7FFFFFFF));
        symbolMap.put(symbol.toUpperCase(), inst);
        nameMap.put(name.toLowerCase(), inst);
    }

    public List<EntityMatch> resolveEntities(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String lowerText = text.toLowerCase();
        List<EntityMatch> matches = new ArrayList<>();
        Set<String> matchedSymbols = new HashSet<>();

        // 1. Exact Company Name Matching (High confidence: 0.95)
        for (Map.Entry<String, Instrument> entry : nameMap.entrySet()) {
            if (lowerText.contains(entry.getKey()) || lowerText.contains(entry.getKey().replace(" limited", ""))) {
                Instrument inst = entry.getValue();
                if (!matchedSymbols.contains(inst.getCurrentSymbol())) {
                    matches.add(new EntityMatch(
                        inst.getId(),
                        inst.getCurrentSymbol(),
                        inst.getCompanyName(),
                        BigDecimal.valueOf(0.95),
                        "COMPANY_NAME"
                    ));
                    matchedSymbols.add(inst.getCurrentSymbol());
                }
            }
        }

        // 2. Exact Ticker Word Boundary Matching (Confidence: 0.90)
        for (Map.Entry<String, Instrument> entry : symbolMap.entrySet()) {
            String sym = entry.getKey();
            if (matchedSymbols.contains(sym)) continue;

            // Avoid false positives for very short common words like "IT"
            if (sym.length() <= 2 && !sym.equals("LT")) continue;

            Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(sym) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = wordPattern.matcher(text);
            if (matcher.find()) {
                Instrument inst = entry.getValue();
                matches.add(new EntityMatch(
                    inst.getId(),
                    inst.getCurrentSymbol(),
                    inst.getCompanyName(),
                    BigDecimal.valueOf(0.90),
                    "EXACT_TICKER"
                ));
                matchedSymbols.add(sym);
            }
        }

        return matches;
    }
}
