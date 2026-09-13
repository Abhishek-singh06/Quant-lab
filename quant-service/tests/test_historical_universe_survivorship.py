"""Tests for Historical Universe Engine and Survivorship Bias Elimination."""

import pytest
from datetime import date

from app.data_acquisition.survivorship import HistoricalUniverseProvider


class TestHistoricalUniverseSurvivorship:
    """Verifies that point-in-time universe queries accurately reflect historical membership without survivorship bias."""

    def test_2009_historical_universe_contains_delisted_and_exited_stocks(self):
        provider = HistoricalUniverseProvider()
        as_of = date(2009, 6, 1)
        univ = provider.universe(as_of=as_of, index_name="NIFTY_50")

        # In 2009, these stocks were active NIFTY 50 constituents
        assert "RCOM" in univ
        assert "SUZLON" in univ
        assert "UNITECH" in univ
        assert "JPASSOCIAT" in univ
        assert "RELIANCE" in univ
        assert "INFY" in univ

        # Recent entries must NOT be present in 2009
        assert "TRENT" not in univ
        assert "BEL" not in univ
        assert "YESBANK" not in univ  # Entered in 2015

    def test_2017_historical_universe_reflects_rebalances(self):
        provider = HistoricalUniverseProvider()
        as_of = date(2017, 6, 1)
        univ = provider.universe(as_of=as_of, index_name="NIFTY_50")

        # Active in 2017
        assert "YESBANK" in univ
        assert "ZEEL" in univ
        assert "INFRATEL" in univ
        assert "RELIANCE" in univ

        # Exited prior to 2017
        assert "RCOM" not in univ
        assert "SUZLON" not in univ
        assert "UNITECH" not in univ
        assert "JPASSOCIAT" not in univ

    def test_2025_modern_universe(self):
        provider = HistoricalUniverseProvider()
        as_of = date(2025, 1, 1)
        univ = provider.universe(as_of=as_of, index_name="NIFTY_50")

        # Modern additions
        assert "TRENT" in univ
        assert "BEL" in univ
        assert "SHRIRAMFIN" in univ

        # Historical removals must NOT be present in 2025
        assert "YESBANK" not in univ
        assert "ZEEL" not in univ
        assert "UPL" not in univ
        assert "RCOM" not in univ

    def test_get_delisted_or_removed_members(self):
        provider = HistoricalUniverseProvider()
        # On 2010-01-01, check which members would eventually be removed or delisted
        exits = provider.get_delisted_or_removed_members(as_of=date(2010, 1, 1), index_name="NIFTY_50")
        exit_symbols = [e["symbol"] for e in exits]

        assert "RCOM" in exit_symbols
        assert "SUZLON" in exit_symbols
        assert "JPASSOCIAT" in exit_symbols
        assert "GAIL" in exit_symbols
