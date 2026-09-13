package com.quantlab.institutional.service;

import com.quantlab.institutional.entity.*;
import com.quantlab.institutional.model.*;
import com.quantlab.institutional.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class InstitutionalAnalyticsService {

    private final MutualFundSchemeRepository schemeRepository;
    private final FundPortfolioDisclosureRepository disclosureRepository;
    private final FundHoldingRepository holdingRepository;
    private final InstitutionalFlowRepository flowRepository;
    private final InstitutionalOwnershipRepository ownershipRepository;

    public InstitutionalAnalyticsService(
            MutualFundSchemeRepository schemeRepository,
            FundPortfolioDisclosureRepository disclosureRepository,
            FundHoldingRepository holdingRepository,
            InstitutionalFlowRepository flowRepository,
            InstitutionalOwnershipRepository ownershipRepository) {
        this.schemeRepository = schemeRepository;
        this.disclosureRepository = disclosureRepository;
        this.holdingRepository = holdingRepository;
        this.flowRepository = flowRepository;
        this.ownershipRepository = ownershipRepository;
    }

    public StockInstitutionalOwnershipDTO getStockInstitutionalOwnership(String symbol, Instant asOfTime) {
        if (asOfTime == null) {
            asOfTime = Instant.now();
        }

        List<InstitutionalOwnership> ownershipRecords = ownershipRepository.findOwnershipBySymbolAvailableAt(symbol, asOfTime);
        LocalDate dataAsOf = LocalDate.now();
        LocalDate publishedAt = LocalDate.now();
        Instant availableAt = asOfTime;
        String source = "MOCK";
        long ageDays = 0;
        BigDecimal totalWeight = BigDecimal.ZERO;

        if (!ownershipRecords.isEmpty()) {
            InstitutionalOwnership latest = ownershipRecords.get(0);
            dataAsOf = latest.getDataAsOf();
            publishedAt = latest.getPublishedAt();
            availableAt = latest.getAvailableAt();
            source = latest.getSource();
            ageDays = ChronoUnit.DAYS.between(latest.getDataAsOf(), LocalDate.now());

            for (InstitutionalOwnership rec : ownershipRecords) {
                if (rec.getPeriodEnd().equals(latest.getPeriodEnd()) && rec.getOwnershipPercentage() != null) {
                    totalWeight = totalWeight.add(rec.getOwnershipPercentage());
                }
            }
        }

        // Fetch Top Mutual Fund Schemes holding this stock available at asOfTime
        List<FundHolding> holdings = holdingRepository.findHoldingsBySymbolAvailableAt(symbol, asOfTime);
        List<StockInstitutionalOwnershipDTO.DisclosedFundHolding> topFunds = new ArrayList<>();
        for (FundHolding h : holdings) {
            Optional<MutualFundScheme> schemeOpt = schemeRepository.findById(h.getSchemeId());
            String schemeName = schemeOpt.map(MutualFundScheme::getSchemeName).orElse("Scheme #" + h.getSchemeId());
            String amcName = schemeOpt.map(MutualFundScheme::getAmcName).orElse("AMC");
            topFunds.add(new StockInstitutionalOwnershipDTO.DisclosedFundHolding(
                h.getSchemeId(),
                schemeName,
                amcName,
                h.getPortfolioWeight(),
                h.getWeightChangePp(),
                h.getRelativeWeightChangePercent(),
                h.getChangeType(),
                h.getQuantity(),
                h.getMarketValue()
            ));
        }

        return new StockInstitutionalOwnershipDTO(
                symbol,
                dataAsOf,
                publishedAt,
                availableAt,
                source,
                ageDays,
                totalWeight,
                holdings.size(),
                0,
                0,
                BigDecimal.ZERO,
                topFunds
        );
    }

    public FundPortfolioDTO getFundPortfolio(String schemeCode, Instant asOfTime) {
        if (asOfTime == null) {
            asOfTime = Instant.now();
        }

        Optional<MutualFundScheme> schemeOpt = schemeRepository.findBySchemeCode(schemeCode);
        if (schemeOpt.isEmpty()) {
            return null;
        }

        MutualFundScheme scheme = schemeOpt.get();
        Optional<FundPortfolioDisclosure> disclosureOpt = disclosureRepository.findLatestDisclosureAsOf(scheme.getId(), asOfTime);
        if (disclosureOpt.isEmpty()) {
            return null;
        }

        FundPortfolioDisclosure disclosure = disclosureOpt.get();
        List<FundHolding> holdings = holdingRepository.findByDisclosureIdOrderByPortfolioWeightDesc(disclosure.getId());

        List<FundPortfolioDTO.DisclosedStockPosition> topHoldings = new ArrayList<>();
        Map<String, BigDecimal> sectorAllocations = new HashMap<>();

        for (FundHolding h : holdings) {
            topHoldings.add(new FundPortfolioDTO.DisclosedStockPosition(
                    h.getInstrumentId(),
                    h.getSymbol(),
                    h.getCompanyName(),
                    h.getSector(),
                    h.getPortfolioWeight(),
                    h.getWeightChangePp(),
                    h.getRelativeWeightChangePercent(),
                    h.getChangeType(),
                    h.getQuantity(),
                    h.getMarketValue()
            ));
            if (h.getSector() != null && h.getPortfolioWeight() != null) {
                sectorAllocations.merge(h.getSector(), h.getPortfolioWeight(), BigDecimal::add);
            }
        }

        return new FundPortfolioDTO(
                scheme.getId(),
                scheme.getSchemeName(),
                scheme.getSchemeCode(),
                scheme.getAmcName(),
                scheme.getCategory(),
                disclosure.getDataAsOf(),
                disclosure.getPublishedAt(),
                disclosure.getAvailableAt(),
                disclosure.getSource(),
                ChronoUnit.DAYS.between(disclosure.getDataAsOf(), LocalDate.now()),
                disclosure.getTotalAum(),
                disclosure.getPortfolioScope(),
                topHoldings,
                sectorAllocations,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    public List<InstitutionalFlowDTO> getInstitutionalFlows(LocalDate fromDate, LocalDate toDate, Instant asOfTime) {
        if (asOfTime == null) {
            asOfTime = Instant.now();
        }

        List<InstitutionalFlow> flows = flowRepository.findFlowsInRangeAvailableAt(fromDate, toDate, asOfTime);
        List<InstitutionalFlowDTO> dtos = new ArrayList<>();

        for (InstitutionalFlow flow : flows) {
            dtos.add(new InstitutionalFlowDTO(
                    flow.getTradeDate(),
                    flow.getMarket(),
                    flow.getInstitutionType(),
                    flow.getBuyValue(),
                    flow.getSellValue(),
                    flow.getNetValue(),
                    flow.getFlowFrequency(),
                    flow.getDataAsOf(),
                    flow.getPublishedAt(),
                    flow.getAvailableAt(),
                    flow.getSource()
            ));
        }

        return dtos;
    }
}
