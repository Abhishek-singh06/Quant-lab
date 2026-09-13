"""Adversarial Tests for Dataset Readiness Gate and Contract Violations."""

import pytest
from datetime import date, datetime, timezone
import pandas as pd

from app.data_acquisition.readiness_gate import (
    DatasetReadinessGate,
    GateStatus,
    DatasetTier,
)
from app.data_acquisition.models import DatasetSnapshot


@pytest.fixture
def valid_dataset_df():
    # 5 trading days for RELIANCE
    dates = pd.date_range("2024-01-01", periods=5, freq="B").date
    rows = []
    for d in dates:
        rows.append({
            "trading_date": d,
            "timestamp": datetime(d.year, d.month, d.day, 10, 0, tzinfo=timezone.utc),
            "symbol": "RELIANCE",
            "exchange": "NSE",
            "raw_open": 2800.0,
            "raw_high": 2850.0,
            "raw_low": 2790.0,
            "raw_close": 2840.0,
            "volume": 5000000,
            "adj_open": 2800.0,
            "adj_high": 2850.0,
            "adj_low": 2790.0,
            "adj_close": 2840.0,
            "source": "YAHOO_FINANCE",
            "ingestion_run_id": "RUN_VALID_01",
        })
    return pd.DataFrame(rows)


@pytest.fixture
def valid_snapshot():
    return DatasetSnapshot(
        dataset_id="RUN_VALID_01",
        dataset_version="quantlab_dataset_20240101_20240105_v1",
        as_of_timestamp=datetime(2024, 1, 6, tzinfo=timezone.utc),
        start_date=date(2024, 1, 1),
        end_date=date(2024, 1, 5),
        symbols=["RELIANCE"],
        total_records=5,
        checksum_sha256="VALID_SHA256_HASH_1234567890",
        provider="YAHOO_FINANCE",
    )


class TestDataReadinessAdversarial:

    def test_valid_dataset_passes_tier1(self, valid_dataset_df, valid_snapshot):
        gate = DatasetReadinessGate()
        report = gate.evaluate(
            df=valid_dataset_df,
            snapshot=valid_snapshot,
            is_real_data=True,
            is_survivorship_safe=True,
            is_pit_safe=True,
        )

        assert report.tier == DatasetTier.TIER_1
        assert report.is_eligible_for_backtesting is True
        assert report.is_eligible_for_model_training is True
        assert report.checks["REAL_DATA"] == GateStatus.PASS
        assert report.checks["PIT_SAFE"] == GateStatus.PASS
        assert report.checks["SURVIVORSHIP_SAFE"] == GateStatus.PASS
        assert report.checks["QUALITY_VALID"] == GateStatus.PASS
        assert report.checks["CALENDAR_VALID"] == GateStatus.PASS

    def test_mock_data_strictly_blocked_as_tier4(self, valid_dataset_df, valid_snapshot):
        gate = DatasetReadinessGate()
        report = gate.evaluate(
            df=valid_dataset_df,
            snapshot=valid_snapshot,
            is_real_data=False,  # Mock data!
            is_survivorship_safe=True,
            is_pit_safe=True,
        )

        assert report.tier == DatasetTier.TIER_4
        assert report.is_eligible_for_backtesting is False
        assert report.checks["REAL_DATA"] == GateStatus.FAIL
        assert "violates REAL_DATA mandate" in report.reasons[0]

    def test_impossible_ohlc_fails_quality_gate(self, valid_dataset_df, valid_snapshot):
        gate = DatasetReadinessGate()
        corrupt_df = valid_dataset_df.copy()
        # Corrupt High to be lower than Open
        corrupt_df.loc[0, "raw_high"] = 2000.0  # Open is 2800!

        report = gate.evaluate(
            df=corrupt_df,
            snapshot=valid_snapshot,
            is_real_data=True,
            is_survivorship_safe=True,
            is_pit_safe=True,
        )

        assert report.checks["QUALITY_VALID"] == GateStatus.FAIL
        assert report.is_eligible_for_backtesting is False
        assert report.tier == DatasetTier.TIER_3
        assert any("impossible OHLC" in r for r in report.reasons)

    def test_missing_adjusted_prices_warns_and_downgrades(self, valid_dataset_df, valid_snapshot):
        gate = DatasetReadinessGate()
        unadjusted_df = valid_dataset_df.drop(columns=["adj_close", "adj_open", "adj_high", "adj_low"])

        report = gate.evaluate(
            df=unadjusted_df,
            snapshot=valid_snapshot,
            is_real_data=True,
            is_survivorship_safe=True,
            is_pit_safe=True,
        )

        assert report.checks["CORPORATE_ACTION_SAFE"] == GateStatus.WARN
        assert report.tier == DatasetTier.TIER_2

    def test_empty_dataset_rejected(self):
        gate = DatasetReadinessGate()
        report = gate.evaluate(df=pd.DataFrame())
        assert report.is_eligible_for_backtesting is False
        assert report.tier == DatasetTier.TIER_3
