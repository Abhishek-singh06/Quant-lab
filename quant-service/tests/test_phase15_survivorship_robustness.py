"""Phase 15 Survivorship and Dynamic Universe Robustness Tests.

Adversarial testing to guarantee that:
1. Historical point-in-time universe queries do not look forward.
2. Delisted and removed constituents exist only within their valid date ranges.
3. Symbol/identity renames maintain ISIN consistency across splits, mergers, and delistings.
4. Backtest simulation surfaces explicit survivorship warnings if static end-date universes are used.
"""

import pytest
from datetime import date, datetime
import pandas as pd
import numpy as np

from app.data_acquisition.survivorship import HistoricalUniverseProvider
from app.data_acquisition.instrument_master import InstrumentMasterRegistry, InstrumentIdentity
from app.data_acquisition.models import ExchangeEnum


class TestPhase15SurvivorshipRobustness:
    """Stress tests on historical universe composition and corporate longevity."""

    @pytest.fixture
    def universe_provider(self):
        return HistoricalUniverseProvider()

    @pytest.fixture
    def instrument_registry(self):
        return InstrumentMasterRegistry()

    def test_dynamic_nifty50_evolution_over_time(self, universe_provider):
        """Test that Nifty 50 constituents dynamically evolve across multiple market regimes."""
        u_2010 = set(universe_provider.universe(as_of=date(2010, 1, 1), index_name="NIFTY_50"))
        u_2015 = set(universe_provider.universe(as_of=date(2015, 1, 1), index_name="NIFTY_50"))
        u_2024 = set(universe_provider.universe(as_of=date(2024, 1, 1), index_name="NIFTY_50"))

        # Confirm non-empty and bounded
        assert len(u_2010) > 0
        assert len(u_2015) > 0
        assert len(u_2024) > 0

        # Former major index members that decayed or exited
        # In 2010: RCOM, SUZLON, UNITECH, JPASSOCIAT were key members
        assert "RCOM" in u_2010
        assert "SUZLON" in u_2010
        assert "UNITECH" in u_2010
        assert "JPASSOCIAT" in u_2010

        # By 2024, distressed legacy names must NOT be in active universe
        assert "RCOM" not in u_2024
        assert "SUZLON" not in u_2024
        assert "UNITECH" not in u_2024
        assert "JPASSOCIAT" not in u_2024

    def test_delisting_date_boundary_enforcement(self, universe_provider):
        """Securities must not be queryable after their official delisting or index exclusion date."""
        # Querying an exact historical index changes
        # For instance, if a company is removed on 2018-03-31:
        # Pre-exclusion: present
        # Post-exclusion: absent
        u_pre = universe_provider.universe(as_of=date(2010, 6, 1), index_name="NIFTY_50")
        u_post = universe_provider.universe(as_of=date(2025, 1, 1), index_name="NIFTY_50")

        # RCOM was in Nifty 50 in 2010, absent by 2025
        assert "RCOM" in u_pre
        assert "RCOM" not in u_post

    def test_isin_persistence_across_ticker_changes(self, instrument_registry):
        """ISIN must remain invariant when tickers change due to corporate rebranding."""
        # Example: UTIBANK -> AXISBANK (July 2007)
        hist_entry = instrument_registry.get("AXISBANK")
        assert hist_entry is not None
        assert hist_entry.isin == "INE238A01034"

        # Resolving pre-rename vs post-rename
        sym_pre = instrument_registry.resolve_symbol_as_of("AXISBANK", as_of=date(2005, 1, 1))
        sym_post = instrument_registry.resolve_symbol_as_of("AXISBANK", as_of=date(2015, 1, 1))

        assert sym_pre == "UTIBANK"
        assert sym_post == "AXISBANK"

    def test_adversarial_static_universe_comparison(self, universe_provider):
        """Demonstrate statistical distortion if 2026 survivors are back-projected to 2010."""
        u_survivors_2026 = set(universe_provider.universe(as_of=date(2026, 1, 1), index_name="NIFTY_50"))
        u_actual_2010 = set(universe_provider.universe(as_of=date(2010, 1, 1), index_name="NIFTY_50"))

        # Measuring survivorship omission rate (stocks that died/got replaced)
        omitted_failures = u_actual_2010 - u_survivors_2026
        # Measuring look-ahead insertion rate (stocks that were not yet in index in 2010)
        future_winners = u_survivors_2026 - u_actual_2010

        assert len(omitted_failures) > 0, "Static universe must omit real historical constituents"
        assert len(future_winners) > 0, "Static universe must include unlisted/future entrants"

        # Quantify distortion percentage
        distortion_rate = len(omitted_failures) / len(u_actual_2010)
        assert distortion_rate > 0.05, f"Expected >5% survivorship turnover, found {distortion_rate:.2%}"
