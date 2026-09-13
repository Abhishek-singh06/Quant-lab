package com.quantlab.institutional.service;

import com.quantlab.institutional.entity.*;
import com.quantlab.institutional.provider.InstitutionalDataProvider;
import com.quantlab.institutional.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InstitutionalIngestionService {

    private static final Logger log = LoggerFactory.getLogger(InstitutionalIngestionService.class);

    private final InstitutionalDataProvider defaultProvider;
    private final AmcMasterRepository amcMasterRepository;
    private final MutualFundSchemeRepository schemeRepository;
    private final FundPortfolioDisclosureRepository disclosureRepository;
    private final FundHoldingRepository holdingRepository;
    private final InstitutionalFlowRepository flowRepository;
    private final InstitutionalOwnershipRepository ownershipRepository;
    private final InstitutionalIngestionRunRepository runRepository;
    private final HoldingChangeDetectionService changeDetectionService;

    public InstitutionalIngestionService(
            @Qualifier("mockInstitutionalDataProvider") InstitutionalDataProvider defaultProvider,
            AmcMasterRepository amcMasterRepository,
            MutualFundSchemeRepository schemeRepository,
            FundPortfolioDisclosureRepository disclosureRepository,
            FundHoldingRepository holdingRepository,
            InstitutionalFlowRepository flowRepository,
            InstitutionalOwnershipRepository ownershipRepository,
            InstitutionalIngestionRunRepository runRepository,
            HoldingChangeDetectionService changeDetectionService) {
        this.defaultProvider = defaultProvider;
        this.amcMasterRepository = amcMasterRepository;
        this.schemeRepository = schemeRepository;
        this.disclosureRepository = disclosureRepository;
        this.holdingRepository = holdingRepository;
        this.flowRepository = flowRepository;
        this.ownershipRepository = ownershipRepository;
        this.runRepository = runRepository;
        this.changeDetectionService = changeDetectionService;
    }

    @Transactional
    public InstitutionalIngestionRun ingestAllSampleData() {
        String runUuid = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        InstitutionalIngestionRun run = new InstitutionalIngestionRun(runUuid, defaultProvider.getProviderName(), startTime, "RUNNING");
        run = runRepository.save(run);

        int totalReceived = 0;
        int totalInserted = 0;
        int duplicates = 0;

        try {
            // 1. AMCs
            List<AmcMaster> amcs = defaultProvider.fetchAmcList();
            totalReceived += amcs.size();
            for (AmcMaster amc : amcs) {
                if (amcMasterRepository.findByAmcCode(amc.getAmcCode()).isEmpty()) {
                    amcMasterRepository.save(amc);
                    totalInserted++;
                } else {
                    duplicates++;
                }
            }

            // 2. Schemes
            for (AmcMaster amc : amcMasterRepository.findAll()) {
                List<MutualFundScheme> schemes = defaultProvider.fetchSchemes(amc.getAmcCode());
                totalReceived += schemes.size();
                for (MutualFundScheme scheme : schemes) {
                    scheme.setAmcId(amc.getId());
                    if (schemeRepository.findBySchemeCode(scheme.getSchemeCode()).isEmpty()) {
                        schemeRepository.save(scheme);
                        totalInserted++;
                    } else {
                        duplicates++;
                    }
                }
            }

            // 3. Disclosures & Holdings for past 2 months
            LocalDate prevMonth = LocalDate.now().minusMonths(2).withDayOfMonth(28);
            LocalDate currMonth = LocalDate.now().minusMonths(1).withDayOfMonth(30);

            for (MutualFundScheme scheme : schemeRepository.findAll()) {
                // Ingest Previous Month
                ingestSchemePortfolio(scheme, prevMonth, null);
                // Ingest Current Month
                ingestSchemePortfolio(scheme, currMonth, prevMonth);
            }

            // 4. Daily Flows
            LocalDate flowStart = LocalDate.now().minusDays(30);
            LocalDate flowEnd = LocalDate.now();
            List<InstitutionalFlow> flows = defaultProvider.fetchDailyFlows(flowStart, flowEnd);
            totalReceived += flows.size();
            for (InstitutionalFlow flow : flows) {
                Optional<InstitutionalFlow> existing = flowRepository.findByTradeDateAndInstitutionTypeAndFlowFrequency(
                    flow.getTradeDate(), flow.getInstitutionType(), flow.getFlowFrequency()
                );
                if (existing.isEmpty()) {
                    flowRepository.save(flow);
                    totalInserted++;
                } else {
                    duplicates++;
                }
            }

            // 5. Shareholding patterns
            List<String> symbols = List.of("HDFCBANK", "RELIANCE", "INFY", "ICICIBANK", "ITC");
            LocalDate quarterEnd = LocalDate.now().minusMonths(3).withDayOfMonth(30);
            for (String sym : symbols) {
                List<InstitutionalOwnership> ownerships = defaultProvider.fetchShareholdingPattern(sym, quarterEnd);
                totalReceived += ownerships.size();
                for (InstitutionalOwnership own : ownerships) {
                    Optional<InstitutionalOwnership> existing = ownershipRepository.findByInstrumentIdAndPeriodEndAndInstitutionType(
                        own.getInstrumentId(), own.getPeriodEnd(), own.getInstitutionType()
                    );
                    if (existing.isEmpty()) {
                        ownershipRepository.save(own);
                        totalInserted++;
                    } else {
                        duplicates++;
                    }
                }
            }

            run.setStatus("SUCCESS");
            run.setRecordsReceived(totalReceived);
            run.setRecordsInserted(totalInserted);
            run.setDuplicatesCount(duplicates);
            run.setEndTime(Instant.now());
            run.setDurationMs(run.getEndTime().toEpochMilli() - startTime.toEpochMilli());
            return runRepository.save(run);

        } catch (Exception e) {
            log.error("Institutional ingestion failed: {}", e.getMessage(), e);
            run.setStatus("FAILED");
            run.setErrorMessage(e.getMessage());
            run.setEndTime(Instant.now());
            run.setDurationMs(run.getEndTime().toEpochMilli() - startTime.toEpochMilli());
            return runRepository.save(run);
        }
    }

    @Transactional
    public void ingestSchemePortfolio(MutualFundScheme scheme, LocalDate dataAsOf, LocalDate previousDataAsOf) {
        Optional<FundPortfolioDisclosure> existing = disclosureRepository.findBySchemeIdAndDataAsOf(scheme.getId(), dataAsOf);
        FundPortfolioDisclosure disclosure;
        if (existing.isPresent()) {
            disclosure = existing.get();
        } else {
            disclosure = defaultProvider.fetchPortfolioDisclosure(scheme.getSchemeCode(), dataAsOf);
            if (disclosure == null) return;
            disclosure.setSchemeId(scheme.getId());
            disclosure = disclosureRepository.save(disclosure);
        }

        // Fetch raw holdings
        List<FundHolding> rawHoldings = defaultProvider.fetchHoldings(disclosure.getId(), scheme.getSchemeCode(), dataAsOf);

        // Fetch previous holdings if available
        List<FundHolding> prevHoldings = null;
        FundPortfolioDisclosure prevDisc = null;
        if (previousDataAsOf != null) {
            Optional<FundPortfolioDisclosure> prevDiscOpt = disclosureRepository.findBySchemeIdAndDataAsOf(scheme.getId(), previousDataAsOf);
            if (prevDiscOpt.isPresent()) {
                prevDisc = prevDiscOpt.get();
                prevHoldings = holdingRepository.findByDisclosureId(prevDisc.getId());
            }
        }

        // Detect changes safely
        List<FundHolding> processedHoldings = changeDetectionService.computeHoldingChanges(
            disclosure, rawHoldings, prevDisc, prevHoldings
        );

        // Save holdings
        for (FundHolding holding : processedHoldings) {
            holding.setDisclosureId(disclosure.getId());
            holding.setSchemeId(scheme.getId());
            holdingRepository.save(holding);
        }
    }
}
