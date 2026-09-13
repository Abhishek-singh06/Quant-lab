"""
Indian Market Context and Global Market Evidence Providers for QuantLab Part 13.
Consumes Part 3/4 Market Data and Part 7 Global Intelligence Engine outputs strictly point-in-time.
"""

from datetime import datetime
from typing import Any, Dict, List, Optional
from app.signals.models import Evidence, EvidenceCategory, EvidenceDirection
from app.signals.providers.base import SignalEvidenceProvider


class IndianMarketEvidenceProvider(SignalEvidenceProvider):
    """Calculates Indian benchmark context (NIFTY trend, India VIX, Market Breadth)."""
    
    @property
    def category(self) -> EvidenceCategory:
        return EvidenceCategory.INDIAN_MARKET

    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        evidence_list: List[Evidence] = []
        if not context:
            return evidence_list
            
        mkt_data = context.get("indian_market_context", {})
        if not mkt_data:
            return evidence_list

        # 1. NIFTY 50 Trend (vs 50-day SMA)
        nifty_pct = mkt_data.get("nifty_vs_sma50_pct")
        if nifty_pct is not None:
            score = max(-100.0, min(100.0, nifty_pct * 1000.0))  # +/-10% -> +/-100
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="INDIAN_MARKET_MONITOR",
                category=self.category,
                feature="NIFTY_TREND_SMA50",
                raw_value=float(nifty_pct),
                raw_value_str=f"NIFTY {nifty_pct*100:+.1f}% vs SMA50",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.3,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version="1.0.0",
                reason=f"Benchmark NIFTY 50 is {abs(nifty_pct)*100:.1f}% {'above' if nifty_pct>=0 else 'below'} 50-day average"
            ))

        # 2. India VIX (Market Volatility & Fear Gauge)
        india_vix = mkt_data.get("india_vix")
        if india_vix is not None:
            # Baseline ~15. VIX > 20 is elevated risk/fear (-score), VIX < 13 is low risk/complacency (+score)
            score = max(-100.0, min(100.0, (16.0 - india_vix) * 12.5))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="INDIAN_MARKET_MONITOR",
                category=self.category,
                feature="INDIA_VIX",
                raw_value=float(india_vix),
                raw_value_str=f"India VIX {india_vix:.1f}",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.1,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version="1.0.0",
                reason=f"India VIX at {india_vix:.1f} indicates {dir_enum.value.lower()} volatility environment"
            ))

        # 3. Market Breadth (Advance / Decline Ratio)
        adv_dec_ratio = mkt_data.get("advance_decline_ratio")
        if adv_dec_ratio is not None and adv_dec_ratio > 0:
            # 1.0 is balanced. 2.0 is +100, 0.5 is -100
            score = max(-100.0, min(100.0, (adv_dec_ratio - 1.0) * 100.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="INDIAN_MARKET_MONITOR",
                category=self.category,
                feature="MARKET_BREADTH",
                raw_value=float(adv_dec_ratio),
                raw_value_str=f"A/D Ratio {adv_dec_ratio:.2f}",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.0,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version="1.0.0",
                reason=f"NSE market breadth advance/decline ratio is {adv_dec_ratio:.2f}"
            ))

        return evidence_list


class GlobalMarketEvidenceProvider(SignalEvidenceProvider):
    """Calculates Global Market evidence (US Indices, DXY, US 10Y Yields, Crude Oil) respecting session cutoffs."""
    
    @property
    def category(self) -> EvidenceCategory:
        return EvidenceCategory.GLOBAL_MARKET

    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        evidence_list: List[Evidence] = []
        if not context:
            return evidence_list
            
        global_data = context.get("global_market_data", {})
        if not global_data:
            return evidence_list

        # 1. US S&P 500 Overnight Return
        sp500_ret = global_data.get("sp500_return_1d")
        sp500_avail = global_data.get("sp500_available_at")
        if sp500_ret is not None and (not sp500_avail or sp500_avail <= as_of_timestamp):
            score = max(-100.0, min(100.0, sp500_ret * 2500.0))  # +/-4% -> +/-100
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="GLOBAL_MARKET_INTELLIGENCE",
                category=self.category,
                feature="SP500_OVERNIGHT",
                raw_value=float(sp500_ret),
                raw_value_str=f"S&P 500 {sp500_ret*100:+.2f}%",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.2,
                timestamp=as_of_timestamp,
                available_at=sp500_avail or as_of_timestamp,
                version="1.0.0",
                reason=f"Overnight US S&P 500 closed {sp500_ret*100:+.2f}%"
            ))

        # 2. US Dollar Index (DXY) Trend
        dxy_chg = global_data.get("dxy_change_pct")
        if dxy_chg is not None:
            # Strong Dollar = EM pressure -> negative for Indian Equities
            score = max(-100.0, min(100.0, -dxy_chg * 4000.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="GLOBAL_MARKET_INTELLIGENCE",
                category=self.category,
                feature="DXY_DOLLAR_INDEX",
                raw_value=float(dxy_chg),
                raw_value_str=f"DXY {dxy_chg*100:+.2f}%",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.0,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version="1.0.0",
                reason=f"US Dollar Index changed by {dxy_chg*100:+.2f}% ({'headwind' if dxy_chg>0 else 'tailwinds'} for Indian equities)"
            ))

        # 3. Brent Crude Oil Change
        crude_chg = global_data.get("crude_oil_change_pct")
        if crude_chg is not None:
            # High crude = inflationary for India (major importer) -> negative score
            score = max(-100.0, min(100.0, -crude_chg * 1500.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="GLOBAL_MARKET_INTELLIGENCE",
                category=self.category,
                feature="BRENT_CRUDE_OIL",
                raw_value=float(crude_chg),
                raw_value_str=f"Crude {crude_chg*100:+.2f}%",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.0,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version="1.0.0",
                reason=f"Brent Crude oil moved {crude_chg*100:+.2f}%"
            ))

        return evidence_list
