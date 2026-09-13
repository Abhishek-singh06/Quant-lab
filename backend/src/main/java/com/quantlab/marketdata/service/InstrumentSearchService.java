package com.quantlab.marketdata.service;

import com.quantlab.global.entity.GlobalInstrument;
import com.quantlab.global.repository.GlobalInstrumentRepository;
import com.quantlab.marketdata.model.InstrumentSearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class InstrumentSearchService {

    private final GlobalInstrumentRepository globalInstrumentRepository;

    private static final List<InstrumentSearchResult> CORE_INDIAN_UNIVERSE = List.of(
            new InstrumentSearchResult("NIFTY 50", "Nifty 50 Benchmark Index", "NSE", "Benchmark", "INDEX", "IN0000000010"),
            new InstrumentSearchResult("BANKNIFTY", "Nifty Bank Sectoral Index", "NSE", "Financial Services", "INDEX", "IN0000000011"),
            new InstrumentSearchResult("SENSEX", "BSE SENSEX Benchmark Index", "BSE", "Benchmark", "INDEX", "IN0000000012"),
            new InstrumentSearchResult("INDIA VIX", "India Volatility Index", "NSE", "Volatility", "INDEX", "IN0000000013"),
            new InstrumentSearchResult("RELIANCE", "Reliance Industries Ltd", "NSE", "Energy & Oil", "EQUITY", "INE002A01018"),
            new InstrumentSearchResult("TCS", "Tata Consultancy Services Ltd", "NSE", "Information Technology", "EQUITY", "INE467B01029"),
            new InstrumentSearchResult("HDFCBANK", "HDFC Bank Ltd", "NSE", "Banking & Finance", "EQUITY", "INE040A01034"),
            new InstrumentSearchResult("INFY", "Infosys Ltd", "NSE", "Information Technology", "EQUITY", "INE009A01021"),
            new InstrumentSearchResult("ICICIBANK", "ICICI Bank Ltd", "NSE", "Banking & Finance", "EQUITY", "INE090A01021"),
            new InstrumentSearchResult("ITC", "ITC Ltd", "NSE", "FMCG", "EQUITY", "INE154A01025"),
            new InstrumentSearchResult("BHARTIARTL", "Bharti Airtel Ltd", "NSE", "Telecommunications", "EQUITY", "INE397D01024"),
            new InstrumentSearchResult("SBIN", "State Bank of India", "NSE", "Public Banking", "EQUITY", "INE062A01020"),
            new InstrumentSearchResult("LT", "Larsen & Toubro Ltd", "NSE", "Infrastructure", "EQUITY", "INE018A01030"),
            new InstrumentSearchResult("KOTAKBANK", "Kotak Mahindra Bank Ltd", "NSE", "Banking & Finance", "EQUITY", "INE237A01028"),
            new InstrumentSearchResult("HINDUNILVR", "Hindustan Unilever Ltd", "NSE", "FMCG", "EQUITY", "INE030A01027"),
            new InstrumentSearchResult("AXISBANK", "Axis Bank Ltd", "NSE", "Banking & Finance", "EQUITY", "INE238A01034"),
            new InstrumentSearchResult("ASIANPAINT", "Asian Paints Ltd", "NSE", "Consumer Durables", "EQUITY", "INE021A01026"),
            new InstrumentSearchResult("MARUTI", "Maruti Suzuki India Ltd", "NSE", "Automobile", "EQUITY", "INE585B01010"),
            new InstrumentSearchResult("TATAMOTORS", "Tata Motors Ltd", "NSE", "Automobile", "EQUITY", "INE155A01022"),
            new InstrumentSearchResult("SUNPHARMA", "Sun Pharmaceutical Industries Ltd", "NSE", "Healthcare", "EQUITY", "INE044A01036"),
            new InstrumentSearchResult("TITAN", "Titan Company Ltd", "NSE", "Consumer Discretionary", "EQUITY", "INE280A01028"),
            new InstrumentSearchResult("BAJFINANCE", "Bajaj Finance Ltd", "NSE", "Non-Banking Financial", "EQUITY", "INE296A01024"),
            new InstrumentSearchResult("WIPRO", "Wipro Ltd", "NSE", "Information Technology", "EQUITY", "INE075A01022"),
            new InstrumentSearchResult("HCLTECH", "HCL Technologies Ltd", "NSE", "Information Technology", "EQUITY", "INE860A01027"),
            new InstrumentSearchResult("NTPC", "NTPC Ltd", "NSE", "Power & Utilities", "EQUITY", "INE733E01010"),
            new InstrumentSearchResult("POWERGRID", "Power Grid Corporation of India", "NSE", "Power & Utilities", "EQUITY", "INE752E01010"),
            new InstrumentSearchResult("TATASTEEL", "Tata Steel Ltd", "NSE", "Metals & Mining", "EQUITY", "INE081A01020"),
            new InstrumentSearchResult("ADANIENT", "Adani Enterprises Ltd", "NSE", "Conglomerate", "EQUITY", "INE423A01024"),
            new InstrumentSearchResult("ADANIPORTS", "Adani Ports and Special Economic Zone", "NSE", "Infrastructure", "EQUITY", "INE742F01042"),
            new InstrumentSearchResult("COALINDIA", "Coal India Ltd", "NSE", "Mining & Energy", "EQUITY", "INE522F01014")
    );

    public InstrumentSearchService(GlobalInstrumentRepository globalInstrumentRepository) {
        this.globalInstrumentRepository = globalInstrumentRepository;
    }

    public List<InstrumentSearchResult> search(String query, int limit) {
        int maxLimit = Math.min(Math.max(limit, 1), 50);
        List<InstrumentSearchResult> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            // Return top benchmark instruments when query is empty
            for (int i = 0; i < Math.min(CORE_INDIAN_UNIVERSE.size(), maxLimit); i++) {
                results.add(CORE_INDIAN_UNIVERSE.get(i));
            }
            return results;
        }

        String cleanQuery = query.trim().toUpperCase(Locale.ROOT);

        // 1. Exact or prefix matches on symbol first
        for (InstrumentSearchResult item : CORE_INDIAN_UNIVERSE) {
            if (item.symbol().equalsIgnoreCase(cleanQuery) || item.symbol().startsWith(cleanQuery)) {
                results.add(item);
                if (results.size() >= maxLimit) return results;
            }
        }

        // 2. Name / Sector contains matches
        for (InstrumentSearchResult item : CORE_INDIAN_UNIVERSE) {
            if (!results.contains(item)) {
                if (item.name().toUpperCase(Locale.ROOT).contains(cleanQuery) ||
                    item.sector().toUpperCase(Locale.ROOT).contains(cleanQuery) ||
                    (item.isin() != null && item.isin().equalsIgnoreCase(cleanQuery))) {
                    results.add(item);
                    if (results.size() >= maxLimit) return results;
                }
            }
        }

        // 3. Search in Global instruments repository
        try {
            List<GlobalInstrument> globals = globalInstrumentRepository.findByActiveTrue();
            for (GlobalInstrument gi : globals) {
                if (gi.getCanonicalSymbol().toUpperCase(Locale.ROOT).contains(cleanQuery) ||
                    gi.getInstrumentName().toUpperCase(Locale.ROOT).contains(cleanQuery)) {
                    InstrumentSearchResult res = new InstrumentSearchResult(
                            gi.getCanonicalSymbol(),
                            gi.getInstrumentName(),
                            gi.getExchangeSource(),
                            gi.getAssetClass().name(),
                            gi.getAssetClass().name(),
                            null
                    );
                    if (!results.contains(res)) {
                        results.add(res);
                        if (results.size() >= maxLimit) return results;
                    }
                }
            }
        } catch (Exception ignored) {
            // Safe fallback if DB global instruments not initialized
        }

        return results;
    }
}
