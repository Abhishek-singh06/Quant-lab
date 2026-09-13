"""Tests for Strategy Lifecycle, Provenance, Configuration Drift, Promotion Gates, and Orchestration."""

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
    reg = StrategyRegistry()
    return reg


@pytest.fixture
def clean_provenance():
    prov = ProvenanceTracker()
    return prov


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
def sample_strategy() -> StrategyDefinition:
    return StrategyDefinition(
        strategy_id="momentum_alpha158",
        version="1.0.0",
        name="Momentum Alpha158 Trend Follower",
        description="GBDT-driven medium-term momentum strategy",
        author="QuantLab Team",
        horizon="MEDIUM",
        target_instruments=["RELIANCE", "TCS", "INFY", "HDFCBANK"],
        feature_set_id="alpha158",
        feature_names=["KMID", "KLOW", "KSFT", "ROC5", "ROC10", "ROC20", "BETA20"],
        model_id="gbdt_model_v1",
        model_type="GBDT",
        entry_rules={"score_threshold": 0.65},
        exit_rules={"score_threshold": 0.35},
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
# 1. Strategy Registry & Immutability Tests
# ---------------------------------------------------------------------------

class TestStrategyRegistry:

    def test_register_and_get_strategy(self, clean_registry, sample_strategy):
        registered = clean_registry.register_strategy(sample_strategy)
        assert registered.strategy_id == "momentum_alpha158"
        assert registered.version == "1.0.0"

        fetched = clean_registry.get_strategy("momentum_alpha158", "1.0.0")
        assert fetched is not None
        assert fetched.name == sample_strategy.name

    def test_immutability_duplicate_version_rejected(self, clean_registry, sample_strategy):
        clean_registry.register_strategy(sample_strategy)
        # Attempting to register same strategy_id and version must raise ValueError
        with pytest.raises(ValueError, match="already exists. Strategy versions are immutable"):
            clean_registry.register_strategy(sample_strategy)

    def test_version_sorting_latest(self, clean_registry, sample_strategy):
        clean_registry.register_strategy(sample_strategy)

        strat_v2 = sample_strategy.model_copy(deep=True)
        strat_v2.version = "1.1.0"
        clean_registry.register_strategy(strat_v2)

        strat_v3 = sample_strategy.model_copy(deep=True)
        strat_v3.version = "2.0.0"
        clean_registry.register_strategy(strat_v3)

        latest = clean_registry.get_strategy("momentum_alpha158")
        assert latest is not None
        assert latest.version == "2.0.0"

        versions = clean_registry.get_versions("momentum_alpha158")
        assert versions == ["2.0.0", "1.1.0", "1.0.0"]

    def test_clone_strategy(self, clean_registry, sample_strategy):
        clean_registry.register_strategy(sample_strategy)
        cloned = clean_registry.clone_strategy(
            strategy_id="momentum_alpha158",
            from_version="1.0.0",
            new_version="1.1.0",
            overrides={"description": "Updated cloned version"}
        )
        assert cloned.version == "1.1.0"
        assert cloned.status == StrategyStatus.DRAFT
        assert cloned.description == "Updated cloned version"


# ---------------------------------------------------------------------------
# 2. Experiment Lineage & PIT Provenance Tests
# ---------------------------------------------------------------------------

class TestProvenanceTracker:

    def test_record_and_retrieve_provenance(self, clean_provenance):
        prov = ExperimentProvenance(
            provenance_id="prov-101",
            strategy_id="strat-alpha",
            strategy_version="1.0.0",
            data_version="data-2023",
            data_hash=compute_hash("canonical-dataset-2023"),
            feature_version="feat-158",
            feature_config_hash=compute_hash(["KMID", "ROC5"]),
            model_version="gbdt-1.0",
            backtest_id="bt-55",
            backtest_metrics={"sharpe_ratio": 1.85, "max_drawdown": 0.12},
            run_id="run-789"
        )
        clean_provenance.record_provenance(prov)
        fetched = clean_provenance.get_provenance("prov-101")
        assert fetched is not None
        assert fetched.data_version == "data-2023"
        assert fetched.backtest_metrics["sharpe_ratio"] == 1.85

    def test_pit_integrity_verification(self, clean_provenance):
        now = datetime.now(timezone.utc)
        earlier = now - timedelta(hours=1)
        later = now + timedelta(hours=1)

        # Valid: Source timestamp is earlier than or equal to information availability
        assert clean_provenance.verify_pit_integrity(earlier, now) is True
        assert clean_provenance.verify_pit_integrity(now, now) is True

        # Invalid: Source timestamp claims info before it was published
        assert clean_provenance.verify_pit_integrity(later, now) is False

    def test_lineage_graph_generation(self, clean_provenance):
        prov = ExperimentProvenance(
            provenance_id="prov-dag-1",
            strategy_id="strat-dag",
            strategy_version="1.0.0",
            data_hash=compute_hash("data"),
            feature_config_hash=compute_hash("features"),
            backtest_id="bt-1",
            run_id="run-1"
        )
        clean_provenance.record_provenance(prov)
        graph = clean_provenance.generate_lineage_graph("strat-dag", "1.0.0")

        assert graph["lineage_complete"] is True
        assert len(graph["nodes"]) == 5
        assert len(graph["edges"]) == 4
        assert graph["audit_status"] == "VERIFIED_PIT_LINEAGE"


# ---------------------------------------------------------------------------
# 3. Configuration Drift Detector Tests
# ---------------------------------------------------------------------------

class TestConfigDriftDetector:

    def test_zero_drift(self, drift_detector, sample_strategy):
        config = sample_strategy.model_dump()
        report = drift_detector.evaluate_drift("strat-1", "1.0.0", config, config)
        assert report.drift_detected is False
        assert report.is_deployment_blocked is False
        assert report.max_severity == DriftSeverity.NONE

    def test_critical_risk_drift_blocks_deployment(self, drift_detector, sample_strategy):
        bt_config = sample_strategy.model_dump()
        paper_config = sample_strategy.model_dump()

        # Loosen risk parameters in paper config
        paper_config["risk_parameters"]["max_position_size"] = 0.25  # relaxed from 0.10
        paper_config["risk_parameters"]["stop_loss_pct"] = 0.10      # relaxed from 0.05
        paper_config["risk_parameters"]["max_leverage"] = 2.0        # increased from 1.0

        report = drift_detector.evaluate_drift("strat-1", "1.0.0", bt_config, paper_config)
        assert report.drift_detected is True
        assert report.is_deployment_blocked is True
        assert report.max_severity == DriftSeverity.CRITICAL
        assert len(report.blocking_reasons) >= 3

    def test_execution_delay_drift_blocks_deployment(self, drift_detector, sample_strategy):
        bt_config = sample_strategy.model_dump()
        paper_config = sample_strategy.model_dump()

        # Paper assumes 0 bar execution delay while backtest used 1 bar delay
        paper_config["execution_config"]["execution_delay_bars"] = 0

        report = drift_detector.evaluate_drift("strat-1", "1.0.0", bt_config, paper_config)
        assert report.drift_detected is True
        assert report.is_deployment_blocked is True
        assert report.max_severity == DriftSeverity.CRITICAL

    def test_universe_expansion_drift_blocks_deployment(self, drift_detector, sample_strategy):
        bt_config = sample_strategy.model_dump()
        paper_config = sample_strategy.model_dump()

        # Paper adds unbacktested instrument
        paper_config["target_instruments"] = ["RELIANCE", "TCS", "INFY", "HDFCBANK", "UNTESTED_CO"]

        report = drift_detector.evaluate_drift("strat-1", "1.0.0", bt_config, paper_config)
        assert report.drift_detected is True
        assert report.is_deployment_blocked is True
        assert any("UNTESTED_CO" in reason for reason in report.blocking_reasons)


# ---------------------------------------------------------------------------
# 4. Promotion Gates & State Machine Tests
# ---------------------------------------------------------------------------

class TestPromotionGates:

    def test_illegal_state_transition_blocked(self, gatekeeper, sample_strategy):
        # Trying to jump directly from DRAFT to LIVE_ELIGIBLE
        eval_res = gatekeeper.evaluate_promotion(sample_strategy, StrategyStatus.LIVE_ELIGIBLE)
        assert eval_res.is_promoted is False
        assert any("Illegal transition" in r for r in eval_res.blocking_reasons)

    def test_draft_to_research_gate(self, gatekeeper, sample_strategy):
        eval_res = gatekeeper.evaluate_promotion(sample_strategy, StrategyStatus.RESEARCH)
        assert eval_res.is_promoted is True

    def test_research_to_validated_gate_thresholds(self, gatekeeper, sample_strategy):
        strat = sample_strategy.model_copy(deep=True)
        strat.status = StrategyStatus.RESEARCH

        # Fails when validation IC and Accuracy are low
        eval_fail = gatekeeper.evaluate_promotion(
            strat, StrategyStatus.VALIDATED,
            context={"validation_metrics": {"ic": 0.005, "accuracy": 0.49}}
        )
        assert eval_fail.is_promoted is False

        # Passes when IC >= 0.02
        eval_pass = gatekeeper.evaluate_promotion(
            strat, StrategyStatus.VALIDATED,
            context={"validation_metrics": {"ic": 0.045, "accuracy": 0.54}}
        )
        assert eval_pass.is_promoted is True

    def test_backtest_gate_thresholds(self, gatekeeper, sample_strategy):
        strat = sample_strategy.model_copy(deep=True)
        strat.status = StrategyStatus.VALIDATED

        # Fails due to high drawdown and low Sharpe
        eval_fail = gatekeeper.evaluate_promotion(
            strat, StrategyStatus.BACKTESTED,
            context={"backtest_metrics": {"sharpe_ratio": 0.6, "max_drawdown": 0.35, "total_trades": 10, "profit_factor": 0.9}}
        )
        assert eval_fail.is_promoted is False

        # Passes when metrics satisfy all thresholds
        eval_pass = gatekeeper.evaluate_promotion(
            strat, StrategyStatus.BACKTESTED,
            context={"backtest_metrics": {"sharpe_ratio": 1.65, "max_drawdown": 0.14, "total_trades": 55, "profit_factor": 1.45}}
        )
        assert eval_pass.is_promoted is True

    def test_live_signoff_gate(self, gatekeeper, sample_strategy):
        strat = sample_strategy.model_copy(deep=True)
        strat.status = StrategyStatus.MANUAL_REVIEW

        # Fails without human reviewer approval
        eval_fail = gatekeeper.evaluate_promotion(strat, StrategyStatus.LIVE_ELIGIBLE, context={})
        assert eval_fail.is_promoted is False

        # Passes with explicit reviewer approval
        eval_pass = gatekeeper.evaluate_promotion(
            strat, StrategyStatus.LIVE_ELIGIBLE,
            context={"reviewer_id": "quant_lead_01", "human_approved": True, "require_user_confirmation": True}
        )
        assert eval_pass.is_promoted is True


# ---------------------------------------------------------------------------
# 5. Workflow Orchestrator Tests
# ---------------------------------------------------------------------------

class TestWorkflowOrchestrator:

    def test_submit_and_idempotency(self, orchestrator):
        job1 = orchestrator.submit_job(
            job_type="FEATURE_PIPELINE",
            params={"universe": ["RELIANCE"]},
            idempotency_key="key-abc-123"
        )
        assert job1.status == JobStatus.PENDING

        job2 = orchestrator.submit_job(
            job_type="FEATURE_PIPELINE",
            params={"universe": ["RELIANCE"]},
            idempotency_key="key-abc-123"
        )
        assert job1.job_id == job2.job_id

    def test_execute_lifecycle_pipeline(self, orchestrator, sample_strategy):
        strat = sample_strategy.model_copy(deep=True)
        strat.strategy_id = "orch_test_strat"
        strat.version = "1.0.0"

        completed_job = orchestrator.execute_lifecycle_pipeline(
            strategy=strat,
            validation_metrics={"ic": 0.045, "accuracy": 0.55},
            backtest_metrics={"sharpe_ratio": 1.75, "max_drawdown": 0.12, "total_trades": 40, "profit_factor": 1.5}
        )

        assert completed_job.status == JobStatus.COMPLETED
        assert completed_job.result["final_status"] == StrategyStatus.PAPER_ELIGIBLE.value
        assert completed_job.result["provenance_recorded"] is True


# ---------------------------------------------------------------------------
# 6. REST API Endpoints Tests
# ---------------------------------------------------------------------------

class TestLifecycleAPI:

    @pytest.fixture
    def client(self):
        return TestClient(app)

    def test_api_register_and_get_strategy(self, client):
        payload = {
            "strategy_id": "api_test_momentum",
            "version": "1.0.0",
            "name": "API Momentum Alpha",
            "horizon": "SHORT",
            "target_instruments": ["TCS", "INFY"],
            "feature_set_id": "alpha158",
            "feature_names": ["KMID", "ROC5"],
            "model_type": "GBDT",
            "risk_parameters": {"max_position_size": 0.05, "stop_loss_pct": 0.03, "take_profit_pct": 0.09, "max_drawdown_limit": 0.15, "max_leverage": 1.0},
            "execution_config": {"slippage_bps": 5.0, "commission_bps": 3.0, "execution_delay_bars": 1}
        }
        res = client.post("/api/v1/lifecycle/strategies", json=payload)
        assert res.status_code == 201
        data = res.json()
        assert data["strategy_id"] == "api_test_momentum"
        assert data["status"] == "DRAFT"

        # Duplicate version rejected
        res_dup = client.post("/api/v1/lifecycle/strategies", json=payload)
        assert res_dup.status_code == 400

        # Get strategy
        res_get = client.get("/api/v1/lifecycle/strategies/api_test_momentum")
        assert res_get.status_code == 200
        assert res_get.json()["version"] == "1.0.0"

    def test_api_drift_check(self, client):
        payload = {
            "strategy_id": "api_test_momentum",
            "strategy_version": "1.0.0",
            "backtest_config": {
                "risk_parameters": {"max_position_size": 0.10, "stop_loss_pct": 0.05, "max_leverage": 1.0},
                "execution_config": {"slippage_bps": 5.0, "execution_delay_bars": 1},
                "target_instruments": ["TCS", "INFY"]
            },
            "paper_config": {
                "risk_parameters": {"max_position_size": 0.30, "stop_loss_pct": 0.05, "max_leverage": 1.0},
                "execution_config": {"slippage_bps": 5.0, "execution_delay_bars": 1},
                "target_instruments": ["TCS", "INFY"]
            }
        }
        res = client.post("/api/v1/lifecycle/drift-check", json=payload)
        assert res.status_code == 200
        data = res.json()
        assert data["drift_detected"] is True
        assert data["is_deployment_blocked"] is True

    def test_api_overview(self, client):
        res = client.get("/api/v1/lifecycle/overview")
        assert res.status_code == 200
        data = res.json()
        assert "total_strategies" in data
        assert "status_distribution" in data
        assert "active_jobs_count" in data
