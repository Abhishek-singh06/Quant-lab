package com.quantlab.risk.controller;

import com.quantlab.risk.model.*;
import com.quantlab.risk.service.RiskAssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/risk")
public class RiskController {

    private final RiskAssessmentService riskService;

    public RiskController(RiskAssessmentService riskService) {
        this.riskService = riskService;
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<RiskProfileDTO>> getProfiles() {
        return ResponseEntity.ok(riskService.getProfiles());
    }

    @GetMapping("/profiles/{id}")
    public ResponseEntity<RiskProfileDTO> getProfileById(@PathVariable UUID id) {
        RiskProfileDTO profile = riskService.getProfileById(id);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/profiles")
    public ResponseEntity<RiskProfileDTO> saveProfile(@RequestBody RiskProfileDTO profileDTO) {
        return ResponseEntity.ok(riskService.saveProfile(profileDTO));
    }

    @GetMapping("/portfolios")
    public ResponseEntity<List<PortfolioDTO>> getPortfolios() {
        return ResponseEntity.ok(riskService.getPortfolios());
    }

    @GetMapping("/portfolios/{id}")
    public ResponseEntity<PortfolioDTO> getPortfolioById(@PathVariable UUID id) {
        PortfolioDTO portfolio = riskService.getPortfolioById(id);
        if (portfolio == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(portfolio);
    }

    @PostMapping("/portfolios")
    public ResponseEntity<PortfolioDTO> savePortfolio(@RequestBody PortfolioDTO portfolioDTO) {
        return ResponseEntity.ok(riskService.savePortfolio(portfolioDTO));
    }

    @PostMapping("/assess")
    public ResponseEntity<RiskAssessmentDTO> assessRisk(@RequestBody RiskAssessmentRequestDTO request) {
        return ResponseEntity.ok(riskService.assessRisk(request));
    }

    @GetMapping("/assessments/{symbol}")
    public ResponseEntity<List<RiskAssessmentDTO>> getAssessmentsBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(riskService.getAssessmentsBySymbol(symbol));
    }

    @GetMapping("/assessments/{symbol}/latest")
    public ResponseEntity<RiskAssessmentDTO> getLatestAssessment(@PathVariable String symbol) {
        RiskAssessmentDTO assessment = riskService.getLatestAssessment(symbol);
        if (assessment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(assessment);
    }
}
