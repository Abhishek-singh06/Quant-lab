"""Yahoo Finance Indian Market Data Adapter (NSE / BSE).

Fetches legitimate historical OHLCV, stock splits, and cash dividends for Indian equities
(symbols formatted with .NS for NSE or .BO for BSE), keeping raw unadjusted prices
strictly separated from corporate action events.
"""

from typing import List, Dict, Any, Optional, Set
from datetime import date, datetime, timezone, time
import asyncio
import httpx
import hashlib
import json

from app.data_acquisition.models import (
    RawOHLCVRecord,
    CorporateActionRecord,
    CorporateActionTypeEnum,
    ExchangeEnum,
)
from app.data_acquisition.providers.base import (
    BaseMarketDataProvider,
    ProviderCapability,
    ProviderUnavailableError,
    MalformedResponseError,
    RateLimitError,
)


class YahooFinanceIndianMarketAdapter(BaseMarketDataProvider):
    """Adapter fetching real Indian equity data via Yahoo Finance chart endpoints."""

    def __init__(
        self,
        timeout_seconds: float = 15.0,
        max_retries: int = 3,
        rate_limit_delay: float = 0.2,
    ):
        super().__init__(
            provider_name="YAHOO_FINANCE",
            supported_capabilities={
                ProviderCapability.HISTORICAL_OHLCV,
                ProviderCapability.CORPORATE_ACTIONS,
            },
        )
        self.timeout_seconds = timeout_seconds
        self.max_retries = max_retries
        self.rate_limit_delay = rate_limit_delay
        self._headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        # Safe Provider Telemetry (Phase 18.2)
        self.request_count: int = 0
        self.last_request_time: Optional[datetime] = None
        self.last_response_status: Optional[str] = None
        self.last_provider_timestamp: Optional[datetime] = None
        self.last_received_timestamp: Optional[datetime] = None

    def get_telemetry(self) -> Dict[str, Any]:
        """Returns safe provider telemetry without logging secrets or credentials."""
        return {
            "provider_name": self.provider_name,
            "provider_request_count": self.request_count,
            "last_provider_request": self.last_request_time.isoformat() if self.last_request_time else None,
            "last_provider_response": self.last_response_status,
            "last_provider_timestamp": self.last_provider_timestamp.isoformat() if self.last_provider_timestamp else None,
            "last_received_timestamp": self.last_received_timestamp.isoformat() if self.last_received_timestamp else None,
            "actual_data_mechanism": "POLLING",
            "provider_mode": "HISTORICAL_AND_DELAYED_CHART_POLLING",
            "is_streaming": False,
            "is_delayed": True,
            "is_real_external_data": True
        }

    def _format_ticker(self, symbol: str, exchange: ExchangeEnum) -> str:
        """Formats ticker symbol for Yahoo Finance (e.g. RELIANCE.NS, TCS.NS, INFY.BO)."""
        clean_sym = symbol.strip().upper()
        if clean_sym.endswith(".NS") or clean_sym.endswith(".BO"):
            return clean_sym
        suffix = ".BO" if exchange == ExchangeEnum.BSE else ".NS"
        return f"{clean_sym}{suffix}"

    async def is_available(self) -> bool:
        """Verifies endpoint reachability."""
        try:
            async with httpx.AsyncClient(timeout=5.0, headers=self._headers) as client:
                self.request_count += 1
                self.last_request_time = datetime.now(timezone.utc)
                resp = await client.get(
                    "https://query1.finance.yahoo.com/v8/finance/chart/%5ENSEI?interval=1d&range=1d"
                )
                self.last_response_status = f"HTTP {resp.status_code}"
                self.last_received_timestamp = datetime.now(timezone.utc)
                return resp.status_code == 200
        except Exception as exc:
            self.last_response_status = f"ERROR: {type(exc).__name__}"
            return False

    async def _fetch_chart_json(
        self,
        symbol: str,
        start_date: date,
        end_date: date,
        exchange: ExchangeEnum,
    ) -> Dict[str, Any]:
        """Performs HTTP GET to Yahoo chart API with retries and rate limiting."""
        ticker = self._format_ticker(symbol, exchange)
        p1 = int(datetime.combine(start_date, time.min, tzinfo=timezone.utc).timestamp())
        p2 = int(datetime.combine(end_date, time.max, tzinfo=timezone.utc).timestamp())

        url = f"https://query1.finance.yahoo.com/v8/finance/chart/{ticker}"
        params = {
            "period1": p1,
            "period2": p2,
            "interval": "1d",
            "events": "div|split",
            "includePrePost": "false",
        }

        await asyncio.sleep(self.rate_limit_delay)

        for attempt in range(1, self.max_retries + 1):
            try:
                self.request_count += 1
                self.last_request_time = datetime.now(timezone.utc)
                async with httpx.AsyncClient(timeout=self.timeout_seconds, headers=self._headers) as client:
                    resp = await client.get(url, params=params)
                    self.last_received_timestamp = datetime.now(timezone.utc)

                    if resp.status_code == 429:
                        self.last_response_status = "HTTP 429 RATE_LIMIT"
                        if attempt == self.max_retries:
                            raise RateLimitError(self.provider_name, "Rate limit exceeded (HTTP 429).")
                        await asyncio.sleep(attempt * 1.5)
                        continue

                    if resp.status_code != 200:
                        self.last_response_status = f"HTTP {resp.status_code}"
                        raise ProviderUnavailableError(
                            self.provider_name,
                            f"HTTP {resp.status_code} for ticker {ticker}: {resp.text[:200]}"
                        )

                    self.last_response_status = f"HTTP {resp.status_code} OK"
                    data = resp.json()
                    chart = data.get("chart", {})
                    if chart.get("error"):
                        raise MalformedResponseError(
                            self.provider_name,
                            f"Chart API error for {ticker}: {chart['error']}"
                        )

                    result = chart.get("result")
                    if not result or len(result) == 0:
                        raise MalformedResponseError(
                            self.provider_name,
                            f"Empty result returned for ticker {ticker} in range {start_date} to {end_date}."
                        )

                    timestamps = result[0].get("timestamp", [])
                    if timestamps:
                        self.last_provider_timestamp = datetime.fromtimestamp(timestamps[-1], tz=timezone.utc)

                    return result[0]

            except (httpx.RequestError, httpx.TimeoutException) as exc:
                self.last_response_status = f"ERROR: {type(exc).__name__}"
                if attempt == self.max_retries:
                    raise ProviderUnavailableError(
                        self.provider_name,
                        f"Network connection failed after {self.max_retries} attempts: {exc}"
                    )
                await asyncio.sleep(attempt * 1.0)

        raise ProviderUnavailableError(self.provider_name, f"Failed fetching data for {ticker}.")

    async def fetch_historical_ohlcv(
        self,
        symbol: str,
        start_date: date,
        end_date: date,
        exchange: ExchangeEnum = ExchangeEnum.NSE,
        ingestion_run_id: str = "DEFAULT_RUN",
    ) -> List[RawOHLCVRecord]:
        """Fetches raw unadjusted OHLCV price series."""
        chart_data = await self._fetch_chart_json(symbol, start_date, end_date, exchange)

        timestamps = chart_data.get("timestamp", [])
        indicators = chart_data.get("indicators", {})
        quote_list = indicators.get("quote", [{}])
        if not quote_list:
            return []

        quotes = quote_list[0]
        opens = quotes.get("open", [])
        highs = quotes.get("high", [])
        lows = quotes.get("low", [])
        closes = quotes.get("close", [])
        volumes = quotes.get("volume", [])

        records: List[RawOHLCVRecord] = []
        clean_symbol = symbol.replace(".NS", "").replace(".BO", "").strip().upper()

        for idx, ts in enumerate(timestamps):
            o = opens[idx] if idx < len(opens) else None
            h = highs[idx] if idx < len(highs) else None
            l = lows[idx] if idx < len(lows) else None
            c = closes[idx] if idx < len(closes) else None
            v = volumes[idx] if idx < len(volumes) else None

            # Skip null sessions / missing quotes
            if o is None or h is None or l is None or c is None or v is None:
                continue

            dt_utc = datetime.fromtimestamp(ts, tz=timezone.utc)
            t_date = dt_utc.date()

            # Filter range
            if t_date < start_date or t_date > end_date:
                continue

            # Compute raw checksum
            raw_str = f"{clean_symbol}_{t_date}_{o}_{h}_{l}_{c}_{v}"
            checksum = hashlib.sha256(raw_str.encode("utf-8")).hexdigest()

            records.append(
                RawOHLCVRecord(
                    symbol=clean_symbol,
                    exchange=exchange,
                    trading_date=t_date,
                    timestamp=dt_utc,
                    open_price=float(o),
                    high_price=float(h),
                    low_price=float(l),
                    close_price=float(c),
                    volume=int(v),
                    source=self.provider_name,
                    ingestion_run_id=ingestion_run_id,
                    source_timestamp=dt_utc,
                    total_traded_value=float(c) * float(v),
                    raw_payload_checksum=checksum,
                )
            )

        return sorted(records, key=lambda r: r.trading_date)

    async def fetch_corporate_actions(
        self,
        symbol: str,
        start_date: date,
        end_date: date,
        exchange: ExchangeEnum = ExchangeEnum.NSE,
        ingestion_run_id: str = "DEFAULT_RUN",
    ) -> List[CorporateActionRecord]:
        """Extracts official stock splits and cash dividends."""
        chart_data = await self._fetch_chart_json(symbol, start_date, end_date, exchange)
        clean_symbol = symbol.replace(".NS", "").replace(".BO", "").strip().upper()

        events = chart_data.get("events", {})
        actions: List[CorporateActionRecord] = []

        # 1. Parse Splits
        splits_data = events.get("splits", {})
        for _, s_info in splits_data.items():
            ts = s_info.get("date")
            if not ts:
                continue
            dt_utc = datetime.fromtimestamp(ts, tz=timezone.utc)
            ex_date = dt_utc.date()
            if ex_date < start_date or ex_date > end_date:
                continue

            num = float(s_info.get("numerator", 1.0))
            denom = float(s_info.get("denominator", 1.0))
            # Split factor: e.g. 5:1 split -> factor = 5.0 / 1.0 = 5.0
            split_ratio = num / (denom + 1e-12)

            actions.append(
                CorporateActionRecord(
                    symbol=clean_symbol,
                    exchange=exchange,
                    action_type=CorporateActionTypeEnum.SPLIT,
                    ex_date=ex_date,
                    information_available_at=dt_utc,
                    source=self.provider_name,
                    ingestion_run_id=ingestion_run_id,
                    ratio_numerator=num,
                    ratio_denominator=denom,
                    adjustment_factor=split_ratio,
                    description=f"Stock Split {num}:{denom}",
                )
            )

        # 2. Parse Dividends
        divs_data = events.get("dividends", {})
        for _, d_info in divs_data.items():
            ts = d_info.get("date")
            amount = d_info.get("amount")
            if not ts or amount is None:
                continue
            dt_utc = datetime.fromtimestamp(ts, tz=timezone.utc)
            ex_date = dt_utc.date()
            if ex_date < start_date or ex_date > end_date:
                continue

            actions.append(
                CorporateActionRecord(
                    symbol=clean_symbol,
                    exchange=exchange,
                    action_type=CorporateActionTypeEnum.DIVIDEND,
                    ex_date=ex_date,
                    information_available_at=dt_utc,
                    source=self.provider_name,
                    ingestion_run_id=ingestion_run_id,
                    dividend_amount=float(amount),
                    description=f"Cash Dividend Rs. {amount}",
                )
            )

        return sorted(actions, key=lambda a: a.ex_date)
