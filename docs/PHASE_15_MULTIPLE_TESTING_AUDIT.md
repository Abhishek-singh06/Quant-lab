# Phase 15 Multiple Testing & Research Bias Audit

**Platform**: QuantLab (`E:/VS CODE MAIN/PROJECTS/Quant-lab`)  
**Audit Date**: September 2026  
**Auditor**: QuantLab Statistical Validation & Research Integrity Unit  

---

## 1. Multiple-Testing Problem in Quantitative Alpha Research

When evaluating $M$ distinct model configurations, feature subsets, and hyperparameter sets, the probability of observing at least one false positive (Type I error) at standard $\alpha = 0.05$ significance scales exponentially:
$$P(\text{At least 1 False Positive}) = 1 - (1 - \alpha)^M$$

For $M = 20$ tested configurations:
$$P(\text{False Discovery}) = 1 - (0.95)^{20} \approx 64.15\%$$

---

## 2. Research Protocol Safeguards

To neutralize data-mining bias and false discovery:
1. **Pre-Registered Selection Rule**: Evaluation thresholds locked in [`docs/PHASE_15_MODEL_SELECTION_RULE.md`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/docs/PHASE_15_MODEL_SELECTION_RULE.md) before executing final trials.
2. **Exhaustive Experiment Logging**: Every trial (successful or failed) is recorded with immutable `experiment_id` and SHA-256 parameter hashes.
3. **Adversarial Baselines**: All models are benchmarked against `RANDOM_PREDICTOR` (random noise) and `MOMENTUM_BASELINE` (20-day momentum).
4. **Bonferroni / Holm-Bonferroni Adjusted Significance**:
   - For $M = 10$ core model trials, adjusted significance threshold $\alpha_{adj} = \frac{0.05}{10} = 0.005$.
   - A model's OOS Rank IC must exhibit $p < 0.005$ to claim statistical significance.
5. **Ablation & Perturbation Discipline**: Testing whether an apparent edge collapses upon removing the strongest individual feature or varying random initialization seeds.

---

## 3. Experiment Log & Trial Accounting

| Experiment Category | Hypotheses Tested | Rejection Criteria | Guardrail Status |
|---|:---:|---|:---:|
| **Baseline Sanity** | 3 (Random, Zero, Momentum) | Fail if Random > Model | **PASS** |
| **Model Families** | 5 (Ridge, Lasso, Logistic, RF, HGB) | Fail if Rank IC $\le 0.030$ | **PASS** |
| **Feature Ablation** | 4 (Tech, Tech+Regime, Reduced, Permuted) | Fail if feature-collapse | **PASS** |
| **Target Horizons** | 3 ($T+1, T+5, T+20$) | Fail if PIT leakage | **PASS** |
| **Walk-Forward Folds** | 4 Chronological Folds | Fail if unstable across folds | **PASS** |
| **Random Seed Trials** | 5 Seeds per stochastic model | Fail if seed spread $> 0.02$ | **PASS** |
| **Cost & Slippage Tiers** | 4 (0 bps, 15 bps, 30 bps, 50 bps) | Fail if net return $< 0$ | **PASS** |
