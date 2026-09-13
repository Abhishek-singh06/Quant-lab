"""Adversarial Tests for Historical Universe and Survivorship Bias Elimination."""

import pytest
from datetime import date

from app.data_acquisition.survivorship import HistoricalUniverseProvider
from app.data_acquisition.instrument_master import InstrumentMasterRegistry, InstrumentIdentity
from app.data_acquisition.models import ExchangeEnum


class TestSurvivorshipAdversarial:
    """Rigorous verification that historical simulations do not use future surviving universes."""

    def test_current_universe_vs_historical_universe_discrepancy(self):
        provider = HistoricalUniverseProvider()

        # Query universe in 2010 vs 2026
        u_2010 = set(provider.universe(as_of=date(2010, 1, 1), index_name="NIFTY_50"))
        u_2026 = set(provider.universe(as_of=date(2026, 1, 1), index_name="NIFTY_50"))

        # In 2010, RCOM, SUZLON, JPASSOCIAT, UNITECH were active members
        assert "RCOM" in u_2010
        assert "SUZLON" in u_2010
        assert "JPASSOCIAT" in u_2010

        # In 2026, none of those should be present!
        assert "RCOM" not in u_2026
        assert "SUZLON" not in u_2026
        assert "JPASSOCIAT" not in u_2026

        # In 2026, TRENT and BEL are active members, but they did NOT exist in 2010 NIFTY 50
        assert "TRENT" in u_2026
        assert "BEL" in u_2026
        assert "TRENT" not in u_2010
        assert "BEL" not in u_2010

        # The intersection must be strictly smaller than either set
        overlap = u_2010.intersection(u_2026)
        assert len(overlap) < len(u_2010)
        assert len(overlap) < len(u_2026)

    def test_symbol_rename_identity_preservation(self):
        """Verify that AXISBANK resolves to UTIBANK prior to July 2007."""
        registry = InstrumentMasterRegistry()

        # Prior to July 30, 2007: ticker was UTIBANK
        sym_1999 = registry.resolve_symbol_as_of("AXISBANK", as_of=date(1999, 1, 1))
        assert sym_1999 == "UTIBANK"

        sym_2007_pre = registry.resolve_symbol_as_of("AXISBANK", as_of=date(2007, 7, 20))
        assert sym_2007_pre == "UTIBANK"

        # After July 30, 2007: ticker is AXISBANK
        sym_2007_post = registry.resolve_symbol_as_of("AXISBANK", as_of=date(2007, 8, 1))
        assert sym_2007_post == "AXISBANK"

        sym_2025 = registry.resolve_symbol_as_of("AXISBANK", as_of=date(2025, 1, 1))
        assert sym_2025 == "AXISBANK"

        # ISIN must remain identical across renames
        inst = registry.get("AXISBANK")
        assert inst is not None
        assert inst.isin == "INE238A01034"
