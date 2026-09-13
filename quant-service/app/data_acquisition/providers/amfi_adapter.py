"""AMFI India Open Mutual Fund NAV & Scheme Master Adapter.

Fetches official public Mutual Fund Net Asset Values (NAVs) and scheme metadata
from the Association of Mutual Funds in India (AMFI) regulatory portal.
"""

from typing import List, Dict, Any, Optional, Set
from datetime import date, datetime, timezone
import httpx

from app.data_acquisition.providers.base import (
    BaseMarketDataProvider,
    ProviderCapability,
    ProviderUnavailableError,
    MalformedResponseError,
)
from app.data_acquisition.models import ExchangeEnum, RawOHLCVRecord, CorporateActionRecord


class AMFIOpenDataAdapter(BaseMarketDataProvider):
    """Adapter fetching official Indian Mutual Fund daily NAVs from AMFI."""

    def __init__(self, timeout_seconds: float = 20.0):
        super().__init__(
            provider_name="AMFI_INDIA",
            supported_capabilities={
                ProviderCapability.MUTUAL_FUND_NAV,
            },
        )
        self.timeout_seconds = timeout_seconds
        self.nav_url = "https://www.amfiindia.com/spages/NAVAll.txt"

    async def is_available(self) -> bool:
        """Verifies AMFI portal reachability."""
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                resp = await client.head("https://www.amfiindia.com/spages/NAVAll.txt")
                return resp.status_code == 200
        except Exception:
            return False

    async def fetch_latest_navs(self) -> List[Dict[str, Any]]:
        """Fetches all latest mutual fund NAVs from AMFI daily feed."""
        try:
            async with httpx.AsyncClient(timeout=self.timeout_seconds) as client:
                resp = await client.get(self.nav_url)
                if resp.status_code != 200:
                    raise ProviderUnavailableError(
                        self.provider_name,
                        f"Failed to download AMFI NAV feed: HTTP {resp.status_code}"
                    )
                text = resp.text
        except Exception as exc:
            raise ProviderUnavailableError(
                self.provider_name,
                f"AMFI portal connection failed: {exc}"
            )

        records: List[Dict[str, Any]] = []
        current_amc = "UNKNOWN_AMC"
        current_category = "UNKNOWN_CATEGORY"

        for line in text.splitlines():
            line = line.strip()
            if not line:
                continue

            if ";" not in line:
                # Category or AMC header
                if "Mutual Fund" in line or "AMC" in line:
                    current_amc = line
                else:
                    current_category = line
                continue

            parts = line.split(";")
            if len(parts) >= 6:
                scheme_code = parts[0].strip()
                isin_growth = parts[1].strip() if len(parts) > 1 else ""
                isin_reinv = parts[2].strip() if len(parts) > 2 else ""
                scheme_name = parts[3].strip() if len(parts) > 3 else ""
                nav_str = parts[4].strip() if len(parts) > 4 else ""
                date_str = parts[5].strip() if len(parts) > 5 else ""

                try:
                    nav_val = float(nav_str)
                    # Parse date DD-Mon-YYYY (e.g. 12-Sep-2026)
                    nav_date = datetime.strptime(date_str, "%d-%b-%Y").date()
                except (ValueError, IndexError):
                    continue

                records.append({
                    "scheme_code": scheme_code,
                    "isin_growth": isin_growth or None,
                    "isin_reinvestment": isin_reinv or None,
                    "scheme_name": scheme_name,
                    "amc": current_amc,
                    "category": current_category,
                    "nav": nav_val,
                    "nav_date": nav_date.isoformat(),
                    "source": self.provider_name,
                })

        return records

    async def fetch_historical_ohlcv(
        self,
        symbol: str,
        start_date: date,
        end_date: date,
        exchange: ExchangeEnum = ExchangeEnum.NSE,
        ingestion_run_id: str = "DEFAULT_RUN",
    ) -> List[RawOHLCVRecord]:
        self.assert_capability(ProviderCapability.HISTORICAL_OHLCV)
        return []

    async def fetch_corporate_actions(
        self,
        symbol: str,
        start_date: date,
        end_date: date,
        exchange: ExchangeEnum = ExchangeEnum.NSE,
        ingestion_run_id: str = "DEFAULT_RUN",
    ) -> List[CorporateActionRecord]:
        self.assert_capability(ProviderCapability.CORPORATE_ACTIONS)
        return []
