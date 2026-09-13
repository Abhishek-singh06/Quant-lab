"""Instrument Master & Time-Aware Security Identity Registry."""

from typing import List, Dict, Any, Optional
from datetime import date
import pandas as pd

from app.data_acquisition.models import InstrumentIdentity, ExchangeEnum


class InstrumentMasterRegistry:
    """Manages security master identity, ISIN mapping, and historical symbol changes."""

    def __init__(self):
        self._instruments: Dict[str, InstrumentIdentity] = {}  # key: "EXCHANGE:SYMBOL"
        self._isin_map: Dict[str, str] = {}  # isin -> key
        self._seed_initial_nifty_master()

    def register(self, instrument: InstrumentIdentity) -> None:
        """Registers or updates an instrument identity."""
        key = f"{instrument.exchange.value}:{instrument.symbol.upper()}"
        self._instruments[key] = instrument
        if instrument.isin:
            self._isin_map[instrument.isin] = key

    def get(self, symbol: str, exchange: ExchangeEnum = ExchangeEnum.NSE) -> Optional[InstrumentIdentity]:
        """Retrieves instrument identity by current symbol and exchange."""
        key = f"{exchange.value}:{symbol.upper()}"
        return self._instruments.get(key)

    def resolve_symbol_as_of(self, symbol: str, as_of: date, exchange: ExchangeEnum = ExchangeEnum.NSE) -> str:
        """Resolves historical symbol ticker valid on the specified date `as_of`."""
        inst = self.get(symbol, exchange)
        if not inst or not inst.symbol_history:
            return symbol.upper()

        for h in inst.symbol_history:
            v_from = h.get("valid_from")
            v_to = h.get("valid_to")
            if (v_from is None or as_of >= v_from) and (v_to is None or as_of <= v_to):
                return h.get("symbol", symbol).upper()

        return symbol.upper()

    def list_all(self, exchange: Optional[ExchangeEnum] = None, active_only: bool = False) -> List[InstrumentIdentity]:
        """Lists all registered instruments matching criteria."""
        res = list(self._instruments.values())
        if exchange:
            res = [i for i in res if i.exchange == exchange]
        if active_only:
            res = [i for i in res if i.status == "ACTIVE"]
        return res

    def _seed_initial_nifty_master(self):
        """Pre-seeds flagship Indian equity master records with valid ISINs and sector metadata."""
        nifty_stocks = [
            ("RELIANCE", "INE002A01018", "Reliance Industries Ltd.", "Oil Gas & Consumable Fuels", date(1977, 11, 29)),
            ("TCS", "INE467B01029", "Tata Consultancy Services Ltd.", "Information Technology", date(2004, 8, 25)),
            ("HDFCBANK", "INE040A01034", "HDFC Bank Ltd.", "Financial Services", date(1995, 5, 19)),
            ("INFY", "INE009A01021", "Infosys Ltd.", "Information Technology", date(1993, 6, 14)),
            ("ICICIBANK", "INE090A01021", "ICICI Bank Ltd.", "Financial Services", date(1997, 9, 22)),
            ("HINDUNILVR", "INE030A01027", "Hindustan Unilever Ltd.", "Fast Moving Consumer Goods", date(1956, 1, 1)),
            ("ITC", "INE154A01025", "ITC Ltd.", "Fast Moving Consumer Goods", date(1954, 1, 1)),
            ("SBIN", "INE062A01020", "State Bank of India", "Financial Services", date(1995, 3, 1)),
            ("BHARTIARTL", "INE397D01024", "Bharti Airtel Ltd.", "Telecommunication", date(2002, 2, 18)),
            ("KOTAKBANK", "INE237A01028", "Kotak Mahindra Bank Ltd.", "Financial Services", date(2003, 4, 8)),
            ("LT", "INE018A01030", "Larsen & Toubro Ltd.", "Construction", date(1950, 1, 1)),
            ("AXISBANK", "INE238A01034", "Axis Bank Ltd.", "Financial Services", date(1998, 11, 16)),
            ("BAJFINANCE", "INE296A01024", "Bajaj Finance Ltd.", "Financial Services", date(1987, 3, 25)),
            ("MARUTI", "INE585B01010", "Maruti Suzuki India Ltd.", "Automobile and Auto Components", date(2003, 7, 9)),
            ("TATAMOTORS", "INE155A01022", "Tata Motors Ltd.", "Automobile and Auto Components", date(1955, 1, 1)),
            ("SUNPHARMA", "INE044A01036", "Sun Pharmaceutical Industries Ltd.", "Healthcare", date(1994, 12, 20)),
            ("TITAN", "INE280A01028", "Titan Company Ltd.", "Consumer Durables", date(1987, 3, 1)),
            ("ASIANPAINT", "INE021A01026", "Asian Paints Ltd.", "Consumer Durables", date(1982, 1, 1)),
            ("NTPC", "INE733E01010", "NTPC Ltd.", "Power", date(2004, 11, 5)),
            ("TATACONSUM", "INE192A01025", "Tata Consumer Products Ltd.", "Fast Moving Consumer Goods", date(1977, 1, 1)),
            ("M&M", "INE101A01026", "Mahindra & Mahindra Ltd.", "Automobile and Auto Components", date(1956, 1, 1)),
            ("WIPRO", "INE075A01022", "Wipro Ltd.", "Information Technology", date(1995, 1, 1)),
            ("ULTRACEMCO", "INE481G01011", "UltraTech Cement Ltd.", "Construction Materials", date(2004, 8, 24)),
            ("POWERGRID", "INE752E01010", "Power Grid Corporation of India Ltd.", "Power", date(2007, 10, 5)),
            ("BAJAJFINSV", "INE918I01026", "Bajaj Finserv Ltd.", "Financial Services", date(2008, 5, 26)),
        ]

        # Historic AXISBANK rename history: UTIBANK -> AXISBANK in July 2007
        axis_history = [
            {"symbol": "UTIBANK", "valid_from": date(1998, 11, 16), "valid_to": date(2007, 7, 29), "reason": "UTI Bank Limited"},
            {"symbol": "AXISBANK", "valid_from": date(2007, 7, 30), "valid_to": None, "reason": "Renamed to Axis Bank Ltd"},
        ]

        for sym, isin, name, sector, list_d in nifty_stocks:
            hist = axis_history if sym == "AXISBANK" else []
            self.register(
                InstrumentIdentity(
                    symbol=sym,
                    exchange=ExchangeEnum.NSE,
                    company_name=name,
                    isin=isin,
                    listing_date=list_d,
                    status="ACTIVE",
                    sector=sector,
                    symbol_history=hist,
                )
            )
