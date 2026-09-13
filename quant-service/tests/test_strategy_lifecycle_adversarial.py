"""Adversarial Verification Suite for Phase 6 Strategy Lifecycle and QuantDinger Integration.

Tests:
1. Immutability violation attacks (attempting in-place mutations).
2. Topological state machine circumvention attacks (skipping states, illegal regressions).
3. Risk drift and execution parameter manipulation attacks.
4. Lineage and hash collision / determinism attacks.
5. Point-in-Time future data tampering attacks.
6. Workflow idempotency and duplicate execution prevention attacks.
7. Live trading boundary attacks (attempting unauthorized live activation).
"""

import pytest
from datetime import datetime, timezone, timedelta
from fastapi.testclient import TestClient

from app.main import app
from app.strategy_lifecycle import (
    StrategyDefinition,
    StrategyStatus,
    GateStatus,
    DriftSeverity,
    JobStatus,
    ExperimentProvenance,
    StrategyRegistry,
    ProvenanceTracker,
    ConfigDriftDetector,
    StrategyPromotionGatekeeper,
    WorkflowOrchestrator,
    compute_hash,
)


@pytest.fixture
def clean_registry():
    return StrategyRegistry()


@pytest.fixture
def clean_provenance():
    return ProvenanceTracker()


@pytest.fixture
def drift_detector():
    return ConfigDriftDetector()


@pytest.fixture
def gatekeeper():
    return StrategyPromotionGatekeeper()


@pytest.fixture
def orchestrator():
    return WorkflowOrchestrator()


@pytest.fixture
def canonical_strategy() -> StrategyDefinition:
    return StrategyDefinition(
        strategy_id="alpha_trend_v1",
        version="1.0.0",
        name="Alpha Trend Follower",
        description="Canonical GBDT trend following model",
        author="Quant Research",
        horizon="MEDIUM",
        target_instruments=["RELIANCE", "TCS", "INFY", "HDFCBANK"],
        feature_set_id="alpha158_canonical",
        feature_names=["KMID", "ROC5", "ROC10", "ROC20"],
        model_id="gbdt_checkpoint_001",
        model_type="GBDT",
        risk_parameters={
            "max_position_size": 0.10,
            "stop_loss_pct": 0.05,
            "take_profit_pct": 0.15,
            "max_drawdown_limit": 0.20,
            "max_leverage": 1.0,
        },
        execution_config={
            "slippage_bps": 5.0,
            "commission_bps": 3.0,
            "execution_delay_bars": 1,
        },
        status=StrategyStatus.DRAFT,
    )


# ---------------------------------------------------------------------------
# 1. Strategy Registry Immutability Adversarial Tests
# ---------------------------------------------------------------------------

class TestRegistryAdversarial:

    def test_mutation_attack_rejected(self, clean_registry, canonical_strategy):
        # 1. Register 1.0.0
        clean_registry.register_strategy(canonical_strategy)

        # 2. Attempt to register modified 1.0.0 with relaxed risk without version bump
        tampered = canonical_strategy.model_copy(deep=True)
        tampered.risk_parameters["max_position_size"] = 0.50

        with pytest.raises(ValueError, match="already exists. Strategy versions are immutable"):
            clean_registry.register_strategy(tampered)

        # 3. Verify original stored strategy in registry was not mutated
        stored = clean_registry.get_strategy("alpha_trend_v1", "1.0.0")
        assert stored.risk_parameters["max_position_size"] == 0.10

    def test_version_bump_allowed(self, clean_registry, canonical_strategy):
        clean_registry.register_strategy(canonical_strategy)

        # Increment to 1.1.0 with modifications
        v1_1 = canonical_strategy.model_copy(deep=True)
        v1_1.version = "1.1.0"
        v1_1.risk_parameters["max_position_size"] = 0.12

        registered_1_1 = clean_registry.register_strategy(v1_1)
        assert registered_1_1.version == "1.1.0"
        assert registered_1_1.risk_parameters["max_position_size"] == 0.12

        # 1.0.0 remains intact
        v1_0 = clean_registry.get_strategy("alpha_trend_v1", "1.0.0")
        assert v1_0.risk_parameters["max_position_size"] == 0.10


# ---------------------------------------------------------------------------
# 2. Promotion State Machine Adversarial Tests (9-Stage Lifecycle)
# ---------------------------------------------------------------------------

class TestStateMachineAdversarial:

    def test_skip_states_attacks(self, gatekeeper, canonical_strategy):
        # DRAFT -> VALIDATED (skipping RESEARCH) -> MUST BE BLOCKED
        eval_res = gatekeeper.evaluate_promotion(canonical_strategy, StrategyStatus.VALIDATED)
        assert eval_res.is_promoted is False
        assert any("Illegal transition" in r for r in eval_res.blocking_reasons)

        # DRAFT -> BACKTESTED (skipping RESEARCH, VALIDATED) -> MUST BE BLOCKED
        eval_res2 = gatekeeper.evaluate_promotion(canonical_strategy, StrategyStatus.BACKTESTED)
        assert eval_res2.is_promoted is False

        # DRAFT -> PAPER_RUNNING (skipping all research/backtest) -> MUST BE BLOCKED
        eval_res3 = gatekeeper.evaluate_promotion(canonical_strategy, StrategyStatus.PAPER_RUNNING)
        assert eval_res3.is_promoted is False

        # DRAFT -> LIVE_ELIGIBLE -> MUST BE BLOCKED
        eval_res4 = gatekeeper.evaluate_promotion(canonical_strategy, StrategyStatus.LIVE_ELIGIBLE)
        assert eval_res4.is_promoted is False

    def test_paper_running_without_paper_eligible(self, gatekeeper, canonical_strategy):
        strat = canonical_strategy.model_copy(deep=True)
        strat.status = StrategyStatus.BACKTESTED
        eval_res = gatekeeper.evaluate_promotion(strat, StrategyStatus.PAPER_RUNNING)
        assert eval_res.is_promoted is False

    def test_paper_validated_without_paper_running(self, gatekeeper, canonical_strategy):
        strat = canonical_strategy.model_copy(deep=True)
        strat.status = StrategyStatus.PAPER_ELIGIBLE
        eval_res = gatekeeper.evaluate_promotion(strat, StrategyStatus.PAPER_VALIDATED)
        assert eval_res.is_promoted is False

    def test_live_eligible_without_manual_review(self, gatekeeper, canonical_strategy):
        strat = canonical_strategy.model_copy(deep=True)
        strat.status = StrategyStatus.PAPER_VALIDATED
        eval_res = gatekeeper.evaluate_promotion(
            strat, StrategyStatus.LIVE_ELIGIBLE,
            context={"reviewer_id": "auditor", "human_approved": True, "require_user_confirmation": True}
        )
        assert eval_res.is_promoted is False
        assert any("Illegal transition" in r for r in eval_res.blocking_reasons)

    def test_live_eligible_without_user_confirmation(self, gatekeeper, canonical_strategy):
        strat = canonical_strategy.model_copy(deep=True)
        strat.status = StrategyStatus.MANUAL_REVIEW
        # Human approved but require_user_confirmation is False
        eval_res = gatekeeper.evaluate_promotion(
            strat, StrategyStatus.LIVE_ELIGIBLE,
            context={"reviewer_id": "auditor", "human_approved": True, "require_user_confirmation": False}
        )
        assert eval_res.is_promoted is False
        assert any("confirmation" in r for r in eval_res.blocking_reasons)

    def test_promotion_evidence_omission_attacks(self, gatekeeper, canonical_strategy):
        # 1. RESEARCH -> VALIDATED without validation metrics
        strat_res = canonical_strategy.model_copy(deep=True)
        strat_res.status = StrategyStatus.RESEARCH
        eval_no_metrics = gatekeeper.evaluate_promotion(strat_res, StrategyStatus.VALIDATED, context={})
        assert eval_no_metrics.is_promoted is False

        # 2. VALIDATED -> BACKTESTED without backtest metrics
        strat_val = canonical_strategy.model_copy(deep=True)
        strat_val.status = StrategyStatus.VALIDATED
        eval_no_bt = gatekeeper.evaluate_promotion(strat_val, StrategyStatus.BACKTESTED, context={})
        assert eval_no_bt.is_promoted is False

        # 3. BACKTESTED -> PAPER_ELIGIBLE without provenance
        strat_bt = canonical_strategy.model_copy(deep=True)
        strat_bt.status = StrategyStatus.BACKTESTED
        eval_no_prov = gatekeeper.evaluate_promotion(strat_bt, StrategyStatus.PAPER_ELIGIBLE, context={})
        assert eval_no_prov.is_promoted is False


# ---------------------------------------------------------------------------
# 3. Backtest-to-Paper Configuration Drift Adversarial Tests
# ---------------------------------------------------------------------------

class TestConfigDriftAdversarial:

    def test_all_dangerous_drift_attacks_blocked(self, drift_detector, canonical_strategy):
        bt = canonical_strategy.model_dump()

        # Attack 1: Increase max position size
        paper1 = canonical_strategy.model_dump()
        paper1["risk_parameters"]["max_position_size"] = 0.20
        r1 = drift_detector.evaluate_drift("s1", "1.0", bt, paper1)
        assert r1.is_deployment_blocked is True
        assert r1.max_severity == DriftSeverity.CRITICAL

        # Attack 2: Loosen stop loss
        paper2 = canonical_strategy.model_dump()
        paper2["risk_parameters"]["stop_loss_pct"] = 0.10
        r2 = drift_detector.evaluate_drift("s1", "1.0", bt, paper2)
        assert r2.is_deployment_blocked is True
        assert r2.max_severity == DriftSeverity.CRITICAL

        # Attack 3: Increase leverage
        paper3 = canonical_strategy.model_dump()
        paper3["risk_parameters"]["max_leverage"] = 3.0
        r3 = drift_detector.evaluate_drift("s1", "1.0", bt, paper3)
        assert r3.is_deployment_blocked is True
        assert r3.max_severity == DriftSeverity.CRITICAL

        # Attack 4: Reduce execution delay (lookahead hazard)
        paper4 = canonical_strategy.model_dump()
        paper4["execution_config"]["execution_delay_bars"] = 0
        r4 = drift_detector.evaluate_drift("s1", "1.0", bt, paper4)
        assert r4.is_deployment_blocked is True
        assert r4.max_severity == DriftSeverity.CRITICAL

        # Attack 5: Reduce slippage assumption
        paper5 = canonical_strategy.model_dump()
        paper5["execution_config"]["slippage_bps"] = 1.0  # Backtest had 5.0
        r5 = drift_detector.evaluate_drift("s1", "1.0", bt, paper5)
        assert r5.is_deployment_blocked is True

        # Attack 6: Introduce unbacktested ticker
        paper6 = canonical_strategy.model_dump()
        paper6["target_instruments"] = ["RELIANCE", "TCS", "INFY", "HDFCBANK", "PENNY_STOCK"]
        r6 = drift_detector.evaluate_drift("s1", "1.0", bt, paper6)
        assert r6.is_deployment_blocked is True

        # Attack 7: Feature set ID mismatch
        paper7 = canonical_strategy.model_dump()
        paper7["feature_set_id"] = "different_feature_set"
        r7 = drift_detector.evaluate_drift("s1", "1.0", bt, paper7)
        assert r7.is_deployment_blocked is True

        # Attack 8: Model checkpoint ID mismatch
        paper8 = canonical_strategy.model_dump()
        paper8["model_id"] = "unverified_model_checkpoint"
        r8 = drift_detector.evaluate_drift("s1", "1.0", bt, paper8)
        assert r8.is_deployment_blocked is True

    def test_safe_equivalent_or_stricter_configuration_allowed(self, drift_detector, canonical_strategy):
        bt = canonical_strategy.model_dump()
        paper_safe = canonical_strategy.model_dump()

        # Tightening risk is safe: reducing max position size to 5% and stop loss to 3%
        paper_safe["risk_parameters"]["max_position_size"] = 0.05
        paper_safe["risk_parameters"]["stop_loss_pct"] = 0.03
        # Conservative slippage assumption (higher in paper than backtest)
        paper_safe["execution_config"]["slippage_bps"] = 8.0

        report = drift_detector.evaluate_drift("s1", "1.0", bt, paper_safe)
        assert report.is_deployment_blocked is False


# ---------------------------------------------------------------------------
# 4. Provenance Deterministic Hashing & PIT Adversarial Tests
# ---------------------------------------------------------------------------

class TestProvenanceAndPITAdversarial:

    def test_deterministic_sha256_hashing(self):
        input1 = {"instruments": ["RELIANCE", "TCS"], "split": "2023"}
        input2 = {"split": "2023", "instruments": ["RELIANCE", "TCS"]}  # Key ordering differs
        input3 = {"instruments": ["RELIANCE", "INFY"], "split": "2023"}  # Content differs

        h1 = compute_hash(input1)
        h2 = compute_hash(input2)
        h3 = compute_hash(input3)

        assert h1 == h2, "Identical content must generate identical SHA-256 regardless of dict ordering"
        assert h1 != h3, "Different content must produce different SHA-256 hashes"
        assert len(h1) == 64, "SHA-256 must be 64 hexadecimal characters"

    def test_future_information_tampering_blocked(self, clean_provenance):
        now = datetime.now(timezone.utc)
        future_published = now + timedelta(days=5)

        # Signal generated as of 'now' cannot use data that is only available at 'future_published'
        assert clean_provenance.verify_pit_integrity(future_published, now) is False
        assert clean_provenance.verify_pit_integrity(now, future_published) is True


# ---------------------------------------------------------------------------
# 5. Workflow Orchestration Idempotency & Safety Tests
# ---------------------------------------------------------------------------

class TestWorkflowOrchestratorAdversarial:

    def test_idempotency_duplicate_submission_single_logical_job(self, orchestrator):
        key = "idem-key-batch-001"
        job1 = orchestrator.submit_job("DATA_INGESTION", {"dataset": "nse_eod"}, idempotency_key=key)
        job2 = orchestrator.submit_job("DATA_INGESTION", {"dataset": "nse_eod"}, idempotency_key=key)

        assert job1.job_id == job2.job_id
        all_jobs = orchestrator.list_jobs()
        matching = [j for j in all_jobs if j.idempotency_key == key]
        assert len(matching) == 1, "Duplicate submission with same idempotency key must not create duplicate jobs"

    def test_job_failure_isolation(self, orchestrator, canonical_strategy):
        # Invalid strategy missing fields to force failure
        broken_strat = StrategyDefinition(
            strategy_id="broken",
            version="1.0.0",
            name="Broken",
            target_instruments=[],  # Empty universe fails Gate 1
            risk_parameters={},
            execution_config={}
        )

        job = orchestrator.execute_lifecycle_pipeline(broken_strat)
        assert job.status == JobStatus.COMPLETED
        # The result must reflect failure to promote beyond DRAFT
        assert job.result["final_status"] == StrategyStatus.DRAFT.value
