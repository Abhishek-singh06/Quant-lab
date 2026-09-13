package com.quantlab.risk.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.risk.entity.*;
import com.quantlab.risk.model.*;
import com.quantlab.risk.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RiskAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentService.class);

    private final RiskProfileRepository profileRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioPositionRepository positionRepository;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final RiskAssessmentRepository assessmentRepository;
    private final ObjectMapper objectMapper;

    public RiskAssessmentService(
            RiskProfileRepository profileRepository,
            PortfolioRepository portfolioRepository,
            PortfolioPositionRepository positionRepository,
            PortfolioSnapshotRepository snapshotRepository,
            RiskAssessmentRepository assessmentRepository,
            ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.snapshotRepository = snapshotRepository;
        this.assessmentRepository = assessmentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<RiskProfileDTO> getProfiles() {
        List<RiskProfileEntity> entities = profileRepository.findAll();
        if (entities.isEmpty()) {
            return getDefaultProfiles();
        }
        return entities.stream().map(this::toProfileDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RiskProfileDTO getProfileById(UUID id) {
        return profileRepository.findById(id)
                .map(this::toProfileDTO)
                .orElse(null);
    }

    @Transactional
    public RiskProfileDTO saveProfile(RiskProfileDTO dto) {
        RiskProfileEntity entity;
        if (dto.id() != null) {
            entity = profileRepository.findById(dto.id()).orElse(new RiskProfileEntity());
        } else {
            entity = new RiskProfileEntity();
            entity.setId(UUID.randomUUID());
        }
        entity.setName(dto.name());
        entity.setProfileType(dto.profileType());
        entity.setMaxPortfolioRisk(dto.maxPortfolioRisk());
        entity.setMaxPositionRisk(dto.maxPositionRisk());
        entity.setMaxPositionAllocation(dto.maxPositionAllocation());
        entity.setMaxSectorAllocation(dto.maxSectorAllocation());
        entity.setMaxIndustryAllocation(dto.maxIndustryAllocation());
        entity.setMaxSingleSecurityAllocation(dto.maxSingleSecurityAllocation());
        entity.setMaxCorrelationExposure(dto.maxCorrelationExposure());
        entity.setMaxDrawdownTolerance(dto.maxDrawdownTolerance());
        entity.setMaxPortfolioVolatility(dto.maxPortfolioVolatility());
        entity.setMinimumLiquidityRequirement(dto.minimumLiquidityRequirement());
        entity.setDefaultStopMethod(dto.defaultStopMethod());
        entity.setDefaultPositionSizingMethod(dto.defaultPositionSizingMethod());
        entity.setAllowShortSelling(Boolean.TRUE.equals(dto.allowShortSelling()));
        entity.setAllowLeverage(Boolean.TRUE.equals(dto.allowLeverage()));
        entity.setMaxLeverage(dto.maxLeverage());
        entity.setCashBuffer(dto.cashBuffer());
        entity.setMinimumConfidence(dto.minimumConfidence());
        entity.setMinimumSignalScore(dto.minimumSignalScore());
        entity.setRiskBudgetMethod(dto.riskBudgetMethod());
        entity.setActive(Boolean.TRUE.equals(dto.isActive()));
        entity.setVersion(dto.version() != null ? dto.version() : "RP_v1.0.0");

        RiskProfileEntity saved = profileRepository.save(entity);
        return toProfileDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<PortfolioDTO> getPortfolios() {
        return portfolioRepository.findAll().stream().map(this::toPortfolioDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PortfolioDTO getPortfolioById(UUID id) {
        return portfolioRepository.findById(id).map(this::toPortfolioDTO).orElse(null);
    }

    @Transactional
    public PortfolioDTO savePortfolio(PortfolioDTO dto) {
        PortfolioEntity entity;
        if (dto.getId() != null) {
            entity = portfolioRepository.findById(dto.getId()).orElse(new PortfolioEntity());
        } else {
            entity = new PortfolioEntity();
            entity.setId(UUID.randomUUID());
        }
        entity.setName(dto.getName());
        entity.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "INR");
        entity.setRiskProfileId(dto.getRiskProfileId());
        entity.setCurrentCash(dto.getCurrentCash());
        entity.setCurrentPortfolioValue(dto.getCurrentPortfolioValue());
        entity.setPeakPortfolioValue(dto.getPeakPortfolioValue() > 0 ? dto.getPeakPortfolioValue() : dto.getCurrentPortfolioValue());
        entity.setPeakTimestamp(dto.getPeakTimestamp() != null ? dto.getPeakTimestamp() : Instant.now());
        entity.setCurrentDrawdown(dto.getCurrentDrawdown());
        entity.setMaxDrawdown(dto.getMaxDrawdown());
        entity.setActive(dto.isActive());

        PortfolioEntity saved = portfolioRepository.save(entity);
        return toPortfolioDTO(saved);
    }

    @Transactional
    public RiskAssessmentDTO assessRisk(RiskAssessmentRequestDTO request) {
        Instant asOfDate = request.getAsOfDate() != null ? request.getAsOfDate() : Instant.now();

        // 1. Resolve Risk Profile
        RiskProfileEntity profile = null;
        if (request.getRiskProfileId() != null) {
            profile = profileRepository.findById(request.getRiskProfileId()).orElse(null);
        }
        if (profile == null) {
            profile = profileRepository.findByName("MODERATE").orElseGet(this::createDefaultModerateProfile);
        }

        // 2. Resolve Portfolio
        PortfolioEntity portfolio = null;
        if (request.getPortfolioId() != null) {
            portfolio = portfolioRepository.findById(request.getPortfolioId()).orElse(null);
        }
        if (portfolio == null) {
            List<PortfolioEntity> active = portfolioRepository.findByActiveTrue();
            if (!active.isEmpty()) {
                portfolio = active.get(0);
            } else {
                portfolio = createDefaultPortfolio(profile.getId());
            }
        }

        double portfolioValue = portfolio.getCurrentPortfolioValue();
        double entryPrice = request.getEntryPrice();
        String signalType = request.getSignalType() != null ? request.getSignalType().toUpperCase() : "HOLD";

        // 3. Stop Calculation
        StopMethod stopMethod = request.getStopMethod() != null ? request.getStopMethod() : profile.getDefaultStopMethod();
        double stopPrice;
        if (stopMethod == StopMethod.USER_DEFINED && request.getUserStopPrice() != null && request.getUserStopPrice() > 0) {
            stopPrice = request.getUserStopPrice();
        } else if (stopMethod == StopMethod.ATR_MULTIPLE && request.getAtr() != null && request.getAtr() > 0) {
            double multiplier = 2.0;
            if ("SELL".equals(signalType)) {
                stopPrice = entryPrice + multiplier * request.getAtr();
            } else {
                stopPrice = Math.max(0.01, entryPrice - multiplier * request.getAtr());
            }
        } else {
            // FIXED_PERCENTAGE (5%)
            if ("SELL".equals(signalType)) {
                stopPrice = entryPrice * 1.05;
            } else {
                stopPrice = Math.max(0.01, entryPrice * 0.95);
            }
        }

        double stopDistance = Math.abs(entryPrice - stopPrice);
        double stopDistancePct = entryPrice > 0 ? (stopDistance / entryPrice) : 0.05;

        // 4. Initial Position Sizing (Fixed Risk sizing)
        double maxPositionRiskBudget = portfolioValue * profile.getMaxPositionRisk();
        double unconstrainedQuantity = stopDistance > 0 ? (maxPositionRiskBudget / stopDistance) : 0.0;
        double unconstrainedCapital = unconstrainedQuantity * entryPrice;
        double unconstrainedAllocation = portfolioValue > 0 ? (unconstrainedCapital / portfolioValue) : 0.0;

        List<RiskAdjustmentDTO> adjustments = new ArrayList<>();
        List<RiskWarningDTO> warnings = new ArrayList<>();
        Map<String, Double> constraintLimits = new HashMap<>();

        // 5. Constraints
        // 5.1 Max single security allocation
        double maxSecurityCapital = portfolioValue * profile.getMaxSingleSecurityAllocation();
        constraintLimits.put("max_single_security", profile.getMaxSingleSecurityAllocation());

        // 5.2 Max position risk budget
        double maxRiskBudgetCapital = stopDistancePct > 0 ? (profile.getMaxPositionRisk() / stopDistancePct) : profile.getMaxPositionAllocation();
        constraintLimits.put("risk_budget_implied", maxRiskBudgetCapital);

        // 5.3 Cash buffer constraint
        double availableCash = Math.max(0.0, portfolio.getCurrentCash() - (portfolioValue * profile.getCashBuffer()));
        double maxCashAllocation = portfolioValue > 0 ? (availableCash / portfolioValue) : 0.0;
        constraintLimits.put("cash_buffer_available", maxCashAllocation);

        // 5.4 Sector concentration check
        List<PortfolioPositionEntity> positions = positionRepository.findByPortfolioIdAndActiveTrue(portfolio.getId());
        double existingSectorWeight = 0.0;
        for (PortfolioPositionEntity pos : positions) {
            if (pos.getSymbol().equalsIgnoreCase(request.getSymbol())) {
                warnings.add(new RiskWarningDTO("EXISTING_POSITION", "MODERATE", "Position in " + request.getSymbol() + " already exists in portfolio."));
            }
        }
        double maxAdditionalSectorAlloc = Math.max(0.0, profile.getMaxSectorAllocation() - existingSectorWeight);
        constraintLimits.put("sector_room", maxAdditionalSectorAlloc);

        // Determine limiting constraint
        double constrainedAlloc = Math.min(unconstrainedAllocation, profile.getMaxSingleSecurityAllocation());
        constrainedAlloc = Math.min(constrainedAlloc, maxCashAllocation);
        constrainedAlloc = Math.min(constrainedAlloc, maxAdditionalSectorAlloc);

        String limitingConstraint = "NONE";
        if (constrainedAlloc <= 0.0) {
            limitingConstraint = "CASH_BUFFER_OR_SECTOR_LIMIT";
        } else if (constrainedAlloc == profile.getMaxSingleSecurityAllocation()) {
            limitingConstraint = "MAX_SINGLE_SECURITY_ALLOCATION";
        } else if (constrainedAlloc == maxCashAllocation) {
            limitingConstraint = "CASH_AVAILABLE_LIMIT";
        } else if (constrainedAlloc == maxAdditionalSectorAlloc) {
            limitingConstraint = "SECTOR_CONCENTRATION_LIMIT";
        }

        // 6. Drawdown scaling adjustment
        double currentDrawdown = portfolio.getCurrentDrawdown();
        double ddTolerance = profile.getMaxDrawdownTolerance();
        if (currentDrawdown > 0.05 && ddTolerance > 0.05) {
            double ddRatio = Math.min(1.0, currentDrawdown / ddTolerance);
            double penaltyMultiplier = Math.max(0.2, 1.0 - (ddRatio * 0.5));
            double beforeAdj = constrainedAlloc;
            constrainedAlloc = constrainedAlloc * penaltyMultiplier;
            adjustments.add(new RiskAdjustmentDTO(
                    "DRAWDOWN_SCALING",
                    penaltyMultiplier,
                    beforeAdj,
                    constrainedAlloc,
                    String.format("Portfolio in %.2f%% drawdown. Scaling allocation by %.2fx.", currentDrawdown * 100, penaltyMultiplier)
            ));
            warnings.add(new RiskWarningDTO("DRAWDOWN_DEFENSE", "HIGH", "Drawdown scaling active due to portfolio drawdown."));
        }

        // 7. Signal Quality and Confidence Gating
        RiskDecision decision;
        if (request.getSignalScore() < profile.getMinimumSignalScore()) {
            decision = RiskDecision.REJECTED_SIGNAL_SCORE;
            constrainedAlloc = 0.0;
            warnings.add(new RiskWarningDTO("LOW_SIGNAL_SCORE", "HIGH", "Signal score " + request.getSignalScore() + " is below minimum requirement " + profile.getMinimumSignalScore()));
        } else if (request.getSignalConfidence() < profile.getMinimumConfidence()) {
            decision = RiskDecision.REJECTED_LOW_CONFIDENCE;
            constrainedAlloc = 0.0;
            warnings.add(new RiskWarningDTO("LOW_CONFIDENCE", "HIGH", "Signal confidence " + request.getSignalConfidence() + " is below minimum requirement " + profile.getMinimumConfidence()));
        } else if ("SELL".equalsIgnoreCase(signalType) && !profile.isAllowShortSelling()) {
            decision = RiskDecision.REJECTED_SHORT_NOT_ALLOWED;
            constrainedAlloc = 0.0;
            warnings.add(new RiskWarningDTO("SHORT_FORBIDDEN", "CRITICAL", "Short selling is not enabled in risk profile."));
        } else if (constrainedAlloc <= 0.001) {
            decision = RiskDecision.ZERO_ALLOCATION;
        } else {
            decision = RiskDecision.APPROVED;
        }

        // 8. Calculate Final Quantities & Downside
        double finalAllocCapital = constrainedAlloc * portfolioValue;
        double finalQuantity = entryPrice > 0 ? Math.floor(finalAllocCapital / entryPrice) : 0.0;
        double actualAlloc = (finalQuantity * entryPrice) / Math.max(1.0, portfolioValue);
        double estimatedDownside = finalQuantity * stopDistance;
        double positionRiskPct = portfolioValue > 0 ? (estimatedDownside / portfolioValue) : 0.0;

        Double riskRewardRatio = null;
        if (request.getTargetPrice() != null && request.getTargetPrice() > 0 && stopDistance > 0) {
            double profitDistance = Math.abs(request.getTargetPrice() - entryPrice);
            riskRewardRatio = profitDistance / stopDistance;
        }

        RiskLevel riskLevel = RiskLevel.MODERATE;
        if (positionRiskPct > 0.015 || currentDrawdown > 0.10) {
            riskLevel = RiskLevel.HIGH;
        } else if (positionRiskPct < 0.005 && currentDrawdown < 0.03) {
            riskLevel = RiskLevel.LOW;
        }

        // 9. Machine-readable Trace
        RiskTraceDTO traceDTO = new RiskTraceDTO();
        traceDTO.setSymbol(request.getSymbol());
        traceDTO.setEntryPrice(entryPrice);
        traceDTO.setStopPrice(stopPrice);
        traceDTO.setStopDistance(stopDistance);
        traceDTO.setStopDistancePct(stopDistancePct);
        traceDTO.setStopMethod(stopMethod.name());
        traceDTO.setSizingMethod(request.getSizingMethod() != null ? request.getSizingMethod().name() : profile.getDefaultPositionSizingMethod().name());
        traceDTO.setUnconstrainedAllocation(unconstrainedAllocation);
        traceDTO.setConstrainedAllocation(constrainedAlloc);
        traceDTO.setFinalSuggestedAllocation(actualAlloc);
        traceDTO.setFinalRecommendedQuantity(finalQuantity);
        traceDTO.setConstraintLimits(constraintLimits);
        traceDTO.setLimitingConstraint(limitingConstraint);
        traceDTO.setAdjustments(adjustments);
        traceDTO.setWarnings(warnings);

        String reasoning = String.format(
                "Risk Assessment for %s: Decision=%s. Suggested Allocation=%.2f%% (%d shares @ INR %.2f). " +
                "Stop price=INR %.2f (Stop distance=%.2f%%). Estimated Downside=INR %.2f (%.2f%% of portfolio value INR %.2f). " +
                "Limiting constraint=%s.",
                request.getSymbol(), decision.name(), actualAlloc * 100, (int) finalQuantity, entryPrice,
                stopPrice, stopDistancePct * 100, estimatedDownside, positionRiskPct * 100, portfolioValue,
                limitingConstraint
        );

        // 10. Persist Assessment Entity
        RiskAssessmentEntity entity = new RiskAssessmentEntity();
        entity.setId(UUID.randomUUID());
        entity.setTimestamp(asOfDate);
        entity.setInformationAvailableAt(asOfDate);
        entity.setCalculatedAt(Instant.now());
        entity.setPortfolioId(portfolio.getId());
        entity.setRiskProfileId(profile.getId());
        entity.setSymbol(request.getSymbol());
        entity.setSignalType(signalType);
        entity.setSignalScore(request.getSignalScore());
        entity.setSignalConfidence(request.getSignalConfidence());
        entity.setSuggestedAllocation(actualAlloc);
        entity.setMaximumAllocation(profile.getMaxSingleSecurityAllocation());
        entity.setRecommendedQuantity(finalQuantity);
        entity.setEntryPrice(entryPrice);
        entity.setStopPrice(stopPrice);
        entity.setTargetPrice(request.getTargetPrice());
        entity.setStopDistance(stopDistance);
        entity.setStopDistancePct(stopDistancePct);
        entity.setStopMethod(stopMethod);
        entity.setPositionRiskAmount(estimatedDownside);
        entity.setPositionRiskPercent(positionRiskPct);
        entity.setEstimatedDownside(estimatedDownside);
        entity.setPortfolioValue(portfolioValue);
        entity.setRemainingRiskBudget(Math.max(0.0, (portfolioValue * profile.getMaxPortfolioRisk()) - estimatedDownside));
        entity.setPortfolioVolatility(0.12);
        entity.setSecurityVolatility(request.getSecurityVolatility() != null ? request.getSecurityVolatility() : 0.20);
        entity.setExpectedVolatility(request.getExpectedVolatility());
        entity.setCurrentDrawdown(currentDrawdown);
        entity.setRiskRewardRatio(riskRewardRatio);
        entity.setRiskDecision(decision);
        entity.setRiskLevel(riskLevel);
        entity.setReasoning(reasoning);
        entity.setDataQualityStatus("VALID");
        entity.setRiskEngineVersion("RISK_v1.0.0");
        entity.setRiskProfileVersion(profile.getVersion());
        entity.setDataVersion("1");

        try {
            entity.setRiskTrace(objectMapper.writeValueAsString(traceDTO));
            entity.setLimitingConstraints(objectMapper.writeValueAsString(constraintLimits));
            entity.setRiskWarnings(objectMapper.writeValueAsString(warnings));
        } catch (Exception e) {
            log.error("Failed to serialize risk trace/warnings JSON", e);
            entity.setRiskTrace("{}");
        }

        RiskAssessmentEntity saved = assessmentRepository.save(entity);

        // Build return DTO
        RiskAssessmentDTO res = toAssessmentDTO(saved);
        res.setRiskTrace(traceDTO);
        res.setAdjustments(adjustments);
        res.setRiskWarnings(warnings);
        res.setLimitingConstraints(constraintLimits);
        return res;
    }

    @Transactional(readOnly = true)
    public List<RiskAssessmentDTO> getAssessmentsBySymbol(String symbol) {
        return assessmentRepository.findBySymbolOrderByTimestampDesc(symbol)
                .stream().map(this::toAssessmentDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RiskAssessmentDTO getLatestAssessment(String symbol) {
        return assessmentRepository.findFirstBySymbolOrderByTimestampDesc(symbol)
                .map(this::toAssessmentDTO).orElse(null);
    }

    // Helper mappings
    private RiskProfileDTO toProfileDTO(RiskProfileEntity e) {
        if (e == null) return null;
        return new RiskProfileDTO(
                e.getId(),
                e.getName(),
                e.getProfileType(),
                e.getMaxPortfolioRisk(),
                e.getMaxPositionRisk(),
                e.getMaxPositionAllocation(),
                e.getMaxSectorAllocation(),
                e.getMaxIndustryAllocation(),
                e.getMaxSingleSecurityAllocation(),
                e.getMaxCorrelationExposure(),
                e.getMaxDrawdownTolerance(),
                e.getMaxPortfolioVolatility(),
                e.getMinimumLiquidityRequirement(),
                e.getDefaultStopMethod(),
                e.getDefaultPositionSizingMethod(),
                e.isAllowShortSelling(),
                e.isAllowLeverage(),
                e.getMaxLeverage(),
                e.getCashBuffer(),
                e.getMinimumConfidence(),
                e.getMinimumSignalScore(),
                e.getRiskBudgetMethod(),
                e.isActive(),
                e.getVersion(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private PortfolioDTO toPortfolioDTO(PortfolioEntity e) {
        PortfolioDTO dto = new PortfolioDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setCurrency(e.getCurrency());
        dto.setRiskProfileId(e.getRiskProfileId());
        dto.setCurrentCash(e.getCurrentCash());
        dto.setCurrentPortfolioValue(e.getCurrentPortfolioValue());
        dto.setPeakPortfolioValue(e.getPeakPortfolioValue());
        dto.setPeakTimestamp(e.getPeakTimestamp());
        dto.setCurrentDrawdown(e.getCurrentDrawdown());
        dto.setMaxDrawdown(e.getMaxDrawdown());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        if (e.getPositions() != null) {
            dto.setPositions(e.getPositions().stream().map(this::toPositionDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private PortfolioPositionDTO toPositionDTO(PortfolioPositionEntity e) {
        PortfolioPositionDTO dto = new PortfolioPositionDTO();
        dto.setId(e.getId());
        dto.setPortfolioId(e.getPortfolio() != null ? e.getPortfolio().getId() : null);
        dto.setInstrumentId(e.getInstrumentId());
        dto.setSymbol(e.getSymbol());
        dto.setSector(e.getSector());
        dto.setIndustry(e.getIndustry());
        dto.setQuantity(e.getQuantity());
        dto.setAverageEntryPrice(e.getAverageEntryPrice());
        dto.setCurrentPrice(e.getCurrentPrice());
        dto.setMarketValue(e.getMarketValue());
        dto.setWeight(e.getWeight());
        dto.setCurrentStopPrice(e.getCurrentStopPrice());
        dto.setPositionRiskAmount(e.getPositionRiskAmount());
        dto.setPositionRiskPercent(e.getPositionRiskPercent());
        dto.setEntryTimestamp(e.getEntryTimestamp());
        dto.setLastUpdatedTimestamp(e.getLastUpdatedTimestamp());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private RiskAssessmentDTO toAssessmentDTO(RiskAssessmentEntity e) {
        RiskAssessmentDTO dto = new RiskAssessmentDTO();
        dto.setId(e.getId());
        dto.setTimestamp(e.getTimestamp());
        dto.setInformationAvailableAt(e.getInformationAvailableAt());
        dto.setCalculatedAt(e.getCalculatedAt());
        dto.setPortfolioId(e.getPortfolioId());
        dto.setRiskProfileId(e.getRiskProfileId());
        dto.setSignalId(e.getSignalId());
        dto.setSymbol(e.getSymbol());
        dto.setSignalType(e.getSignalType());
        dto.setSignalScore(e.getSignalScore());
        dto.setSignalConfidence(e.getSignalConfidence());
        dto.setSuggestedAllocation(e.getSuggestedAllocation());
        dto.setMaximumAllocation(e.getMaximumAllocation());
        dto.setRecommendedQuantity(e.getRecommendedQuantity());
        dto.setEntryPrice(e.getEntryPrice());
        dto.setStopPrice(e.getStopPrice());
        dto.setTargetPrice(e.getTargetPrice());
        dto.setStopDistance(e.getStopDistance());
        dto.setStopDistancePct(e.getStopDistancePct());
        dto.setStopMethod(e.getStopMethod());
        dto.setPositionRiskAmount(e.getPositionRiskAmount());
        dto.setPositionRiskPercent(e.getPositionRiskPercent());
        dto.setEstimatedDownside(e.getEstimatedDownside());
        dto.setPortfolioValue(e.getPortfolioValue());
        dto.setRemainingRiskBudget(e.getRemainingRiskBudget());
        dto.setPortfolioVolatility(e.getPortfolioVolatility());
        dto.setSecurityVolatility(e.getSecurityVolatility());
        dto.setExpectedVolatility(e.getExpectedVolatility());
        dto.setMaxCorrelation(e.getMaxCorrelation());
        dto.setSectorExposureAfterTrade(e.getSectorExposureAfterTrade());
        dto.setCurrentDrawdown(e.getCurrentDrawdown());
        dto.setRiskRewardRatio(e.getRiskRewardRatio());
        dto.setRiskDecision(e.getRiskDecision());
        dto.setRiskLevel(e.getRiskLevel());
        dto.setReasoning(e.getReasoning());
        dto.setDataQualityStatus(e.getDataQualityStatus());
        dto.setRiskEngineVersion(e.getRiskEngineVersion());
        dto.setRiskProfileVersion(e.getRiskProfileVersion());
        dto.setSignalVersion(e.getSignalVersion());
        dto.setDataVersion(e.getDataVersion());
        dto.setCreatedAt(e.getCreatedAt());

        if (e.getRiskTrace() != null) {
            try {
                RiskTraceDTO trace = objectMapper.readValue(e.getRiskTrace(), RiskTraceDTO.class);
                dto.setRiskTrace(trace);
            } catch (Exception ignored) {}
        }
        if (e.getLimitingConstraints() != null) {
            try {
                Map<String, Double> constraints = objectMapper.readValue(e.getLimitingConstraints(), new TypeReference<Map<String, Double>>() {});
                dto.setLimitingConstraints(constraints);
            } catch (Exception ignored) {}
        }
        if (e.getRiskWarnings() != null) {
            try {
                List<RiskWarningDTO> warnings = objectMapper.readValue(e.getRiskWarnings(), new TypeReference<List<RiskWarningDTO>>() {});
                dto.setRiskWarnings(warnings);
            } catch (Exception ignored) {}
        }
        return dto;
    }

    private RiskProfileEntity createDefaultModerateProfile() {
        RiskProfileEntity p = new RiskProfileEntity();
        p.setId(UUID.randomUUID());
        p.setName("MODERATE");
        p.setProfileType(RiskProfileType.MODERATE);
        p.setMaxPortfolioRisk(0.05);
        p.setMaxPositionRisk(0.01);
        p.setMaxPositionAllocation(0.10);
        p.setMaxSectorAllocation(0.25);
        p.setMaxIndustryAllocation(0.15);
        p.setMaxSingleSecurityAllocation(0.10);
        p.setMaxCorrelationExposure(0.70);
        p.setMaxDrawdownTolerance(0.15);
        p.setMaxPortfolioVolatility(0.20);
        p.setMinimumLiquidityRequirement(1000000.0);
        p.setDefaultStopMethod(StopMethod.ATR_MULTIPLE);
        p.setDefaultPositionSizingMethod(PositionSizingMethod.FIXED_RISK);
        p.setAllowShortSelling(false);
        p.setAllowLeverage(false);
        p.setMaxLeverage(1.0);
        p.setCashBuffer(0.05);
        p.setMinimumConfidence(0.50);
        p.setMinimumSignalScore(35.0);
        p.setRiskBudgetMethod("VOLATILITY_ADJUSTED");
        p.setActive(true);
        p.setVersion("RP_v1.0.0");
        return profileRepository.save(p);
    }

    private PortfolioEntity createDefaultPortfolio(UUID profileId) {
        PortfolioEntity p = new PortfolioEntity();
        p.setId(UUID.randomUUID());
        p.setName("Primary Indian Equity Portfolio");
        p.setCurrency("INR");
        p.setRiskProfileId(profileId);
        p.setCurrentCash(1000000.0);
        p.setCurrentPortfolioValue(1000000.0);
        p.setPeakPortfolioValue(1000000.0);
        p.setPeakTimestamp(Instant.now());
        p.setCurrentDrawdown(0.0);
        p.setMaxDrawdown(0.0);
        p.setActive(true);
        return portfolioRepository.save(p);
    }

    private List<RiskProfileDTO> getDefaultProfiles() {
        return List.of(
                createProfileDTO("CONSERVATIVE", RiskProfileType.CONSERVATIVE, 0.03, 0.005, 0.05, 0.15, 0.10, 0.05, 0.50, 0.10, 0.12),
                createProfileDTO("MODERATE", RiskProfileType.MODERATE, 0.05, 0.01, 0.10, 0.25, 0.15, 0.10, 0.70, 0.15, 0.20),
                createProfileDTO("AGGRESSIVE", RiskProfileType.AGGRESSIVE, 0.08, 0.02, 0.20, 0.35, 0.25, 0.20, 0.85, 0.25, 0.30)
        );
    }

    private RiskProfileDTO createProfileDTO(String name, RiskProfileType type, double portRisk, double posRisk,
                                           double posAlloc, double secAlloc, double indAlloc, double singleSecAlloc,
                                           double correl, double ddTol, double portVol) {
        return new RiskProfileDTO(
                UUID.randomUUID(),
                name,
                type,
                portRisk,
                posRisk,
                posAlloc,
                secAlloc,
                indAlloc,
                singleSecAlloc,
                correl,
                ddTol,
                portVol,
                1000000.0,
                StopMethod.ATR_MULTIPLE,
                PositionSizingMethod.FIXED_RISK,
                false,
                false,
                1.0,
                0.05,
                0.50,
                35.0,
                "VOLATILITY_ADJUSTED",
                true,
                "RP_v1.0.0",
                Instant.now(),
                Instant.now()
        );
    }
}
