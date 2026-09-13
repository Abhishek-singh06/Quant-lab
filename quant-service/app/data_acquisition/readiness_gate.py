"""Dataset Readiness Gate for Quantitative Research and Backtesting Validation."""

from typing import Dict, Any, List, Optional, Tuple
from dataclasses import dataclass, field
from datetime import date, datetime
from enum import Enum
import pandas as pd

from app.data_acquisition.models import DatasetSnapshot, ValidationStatusEnum
from app.data_acquisition.validator import DataQualityEngine
from app.data_acquisition.trading_calendar import IndianTradingCalendar


class GateStatus(str, Enum):
    PASS = "PASS"
    WARN = "WARN"
    FAIL = "FAIL"


class DatasetTier(str, Enum):
    TIER_1 = "TIER_1_RESEARCH_GRADE"
    TIER_2 = "TIER_2_USABLE_LIMITED"
    TIER_3 = "TIER_3_DIAGNOSTIC_ONLY"
    TIER_4 = "TIER_4_MOCK"


@dataclass
class ReadinessEvaluationReport:
    """Detailed evaluation output of Dataset Readiness Gate."""
    dataset_version: str
    tier: DatasetTier
    is_eligible_for_backtesting: bool
    is_eligible_for_model_training: bool
    checks: Dict[str, GateStatus]
    metrics: Dict[str, Any]
    reasons: List[str] = field(default_factory=list)


class DatasetReadinessGate:
    """Evaluates datasets against the Research Data Contract standards."""

    def __init__(self, min_completeness_pct: float = 90.0):
        self.min_completeness_pct = min_completeness_pct
        self.quality_engine = DataQualityEngine()
        self.calendar = IndianTradingCalendar()

    def evaluate(
        self,
        df: pd.DataFrame,
        snapshot: Optional[DatasetSnapshot] = None,
        is_real_data: bool = True,
        is_survivorship_safe: bool = True,
        is_pit_safe: bool = True,
    ) -> ReadinessEvaluationReport:
        """Runs comprehensive gate checks on dataset."""
        checks: Dict[str, GateStatus] = {}
        reasons: List[str] = []
        metrics: Dict[str, Any] = {}

        version_str = snapshot.dataset_version if snapshot else "UNVERSIONED"

        if df.empty:
            return ReadinessEvaluationReport(
                dataset_version=version_str,
                tier=DatasetTier.TIER_3,
                is_eligible_for_backtesting=False,
                is_eligible_for_model_training=False,
                checks={"DATA_EXISTS": GateStatus.FAIL},
                metrics={"row_count": 0},
                reasons=["Dataset is completely empty."],
            )

        n_rows = len(df)
        symbols = df["symbol"].unique().tolist() if "symbol" in df.columns else []
        metrics["row_count"] = n_rows
        metrics["symbols_count"] = len(symbols)

        # 1. REAL_DATA check
        if is_real_data:
            checks["REAL_DATA"] = GateStatus.PASS
        else:
            checks["REAL_DATA"] = GateStatus.FAIL
            reasons.append("Dataset contains mock/synthetic data (violates REAL_DATA mandate).")

        # 2. PIT_SAFE check
        if is_pit_safe:
            checks["PIT_SAFE"] = GateStatus.PASS
        else:
            checks["PIT_SAFE"] = GateStatus.FAIL
            reasons.append("Point-In-Time violations or future look-ahead detected.")

        # 3. SURVIVORSHIP_SAFE check
        if is_survivorship_safe:
            checks["SURVIVORSHIP_SAFE"] = GateStatus.PASS
        else:
            checks["SURVIVORSHIP_SAFE"] = GateStatus.FAIL
            reasons.append("Universe lacks historical constituent transitions (survivorship bias risk).")

        # 4. CORPORATE_ACTION_SAFE check (requires raw vs adj separation)
        has_raw = "raw_close" in df.columns or "close" in df.columns
        has_adj = "adj_close" in df.columns
        if has_raw and has_adj:
            checks["CORPORATE_ACTION_SAFE"] = GateStatus.PASS
        elif has_raw and not has_adj:
            checks["CORPORATE_ACTION_SAFE"] = GateStatus.WARN
            reasons.append("Dataset has raw prices but missing explicit corporate action adjusted series.")
        else:
            checks["CORPORATE_ACTION_SAFE"] = GateStatus.FAIL
            reasons.append("Missing required price columns (raw_close or adj_close).")

        # 5. PROVENANCE_COMPLETE check
        has_source = "source" in df.columns and not df["source"].isnull().any()
        has_run_id = "ingestion_run_id" in df.columns and not df["ingestion_run_id"].isnull().any()
        if has_source and has_run_id and snapshot is not None:
            checks["PROVENANCE_COMPLETE"] = GateStatus.PASS
        elif has_source or has_run_id:
            checks["PROVENANCE_COMPLETE"] = GateStatus.WARN
            reasons.append("Partial provenance recorded; snapshot metadata missing.")
        else:
            checks["PROVENANCE_COMPLETE"] = GateStatus.FAIL
            reasons.append("Zero provenance metadata attached to records.")

        # 6. QUALITY_VALID check
        # Inspect OHLC bounds
        invalid_ohlc = 0
        if "raw_open" in df.columns and "raw_high" in df.columns and "raw_low" in df.columns and "raw_close" in df.columns:
            # High must be >= Open and >= Close
            h_bad = (df["raw_high"] < df["raw_open"] - 1e-4) | (df["raw_high"] < df["raw_close"] - 1e-4)
            l_bad = (df["raw_low"] > df["raw_open"] + 1e-4) | (df["raw_low"] > df["raw_close"] + 1e-4)
            invalid_ohlc = int((h_bad | l_bad).sum())

        metrics["invalid_ohlc_count"] = invalid_ohlc
        if invalid_ohlc == 0:
            checks["QUALITY_VALID"] = GateStatus.PASS
        else:
            checks["QUALITY_VALID"] = GateStatus.FAIL
            reasons.append(f"{invalid_ohlc} records contain mathematically impossible OHLC bounds.")

        # 7. CALENDAR_VALID & Completeness check
        if "trading_date" in df.columns and len(symbols) > 0:
            dates = pd.to_datetime(df["trading_date"]).dt.date
            min_d, max_d = dates.min(), dates.max()
            metrics["min_date"] = min_d.isoformat()
            metrics["max_date"] = max_d.isoformat()

            expected_days = self.calendar.count_expected_sessions(min_d, max_d)
            metrics["expected_sessions_per_symbol"] = expected_days

            # Calculate average completeness across symbols
            comp_list = []
            for s in symbols:
                s_dates = dates[df["symbol"] == s].tolist()
                s_actual = len(set(s_dates))
                s_pct = (s_actual / (expected_days + 1e-12)) * 100.0
                comp_list.append(s_pct)

            avg_comp = float(pd.Series(comp_list).mean()) if comp_list else 0.0
            metrics["average_completeness_pct"] = round(avg_comp, 2)

            if avg_comp >= 95.0:
                checks["CALENDAR_VALID"] = GateStatus.PASS
            elif avg_comp >= self.min_completeness_pct:
                checks["CALENDAR_VALID"] = GateStatus.WARN
                reasons.append(f"Calendar completeness is {avg_comp:.1f}% (between 90% and 95%).")
            else:
                checks["CALENDAR_VALID"] = GateStatus.FAIL
                reasons.append(f"Calendar completeness is {avg_comp:.1f}% (below minimum {self.min_completeness_pct}%).")
        else:
            checks["CALENDAR_VALID"] = GateStatus.WARN

        # 8. VERSIONED check
        if snapshot is not None and snapshot.dataset_version and snapshot.checksum_sha256:
            checks["VERSIONED"] = GateStatus.PASS
        else:
            checks["VERSIONED"] = GateStatus.WARN
            reasons.append("Dataset snapshot or SHA-256 checksum not registered.")

        # Determine Tier
        has_critical_fails = any(
            v == GateStatus.FAIL
            for k, v in checks.items()
            if k in ("REAL_DATA", "PIT_SAFE", "SURVIVORSHIP_SAFE", "QUALITY_VALID")
        )

        if not is_real_data:
            tier = DatasetTier.TIER_4
        elif has_critical_fails or checks.get("CALENDAR_VALID") == GateStatus.FAIL:
            tier = DatasetTier.TIER_3
        elif any(v == GateStatus.WARN for v in checks.values()):
            tier = DatasetTier.TIER_2
        else:
            tier = DatasetTier.TIER_1

        eligible = tier in (DatasetTier.TIER_1, DatasetTier.TIER_2) and not has_critical_fails

        return ReadinessEvaluationReport(
            dataset_version=version_str,
            tier=tier,
            is_eligible_for_backtesting=eligible,
            is_eligible_for_model_training=eligible,
            checks=checks,
            metrics=metrics,
            reasons=reasons,
        )
