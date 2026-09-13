"""
Technical Evidence Provider for QuantLab Part 13.
Consumes Part 9 Technical Feature Engine outputs strictly point-in-time.
"""

from datetime import datetime
from typing import Any, Dict, List, Optional
from app.signals.models import Evidence, EvidenceCategory, EvidenceDirection
from app.signals.providers.base import SignalEvidenceProvider


class TechnicalEvidenceProvider(SignalEvidenceProvider):
    """Calculates normalized technical evidence from Part 9 indicators."""
    
    @property
    def category(self) -> EvidenceCategory:
        return EvidenceCategory.TECHNICAL

    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        evidence_list: List[Evidence] = []
        if not context:
            return evidence_list
            
        features = context.get("technical_features", {})
        if not features:
            return evidence_list

        # 1. Trend Evidence (SMA50 vs SMA200 / Price vs SMA200)
        sma50 = features.get("sma_50")
        sma200 = features.get("sma_200")
        close = features.get("close")
        if close is not None and sma200 is not None and sma200 > 0:
            trend_pct = (close - sma200) / sma200
            score = max(-100.0, min(100.0, trend_pct * 400.0))  # +/-25% distance maps to +/-100
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="TECHNICAL_FEATURE_ENGINE",
                category=self.category,
                feature="PRICE_VS_SMA200",
                raw_value=float(trend_pct),
                raw_value_str=f"{trend_pct*100:+.2f}% vs SMA200",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.5,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version="1.0.0",
                reason=f"Price is {abs(trend_pct)*100:.1f}% {'above' if trend_pct >= 0 else 'below'} 200-day moving average"
            ))

        # 2. RSI (14D)
        rsi = features.get("rsi_14")
        if rsi is not None:
            # Neutral at 50, Overbought >70 (+bearish momentum or +bullish trend depending on regime),
            # In quantitative momentum: 50-70 is bullish, <40 is bearish, >75 is overextended
            if rsi >= 50.0:
                score = (rsi - 50.0) * 2.5 if rsi <= 70.0 else 50.0 - (rsi - 70.0) * 2.0
            else:
                score = (rsi - 50.0) * 2.5
            score = max(-100.0, min(100.0, score))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="TECHNICAL_FEATURE_ENGINE",
                category=self.category,
                feature="RSI_14",
                raw_value=float(rsi),
                raw_value_str=f"RSI {rsi:.1f}",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.0,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version="1.0.0",
                reason=f"14-period RSI at {rsi:.1f} indicates {dir_enum.value.lower()} momentum state"
            ))

        # 3. MACD Histogram / Signal Cross
        macd_hist = features.get("macd_hist")
        if macd_hist is not None:
            score = max(-100.0, min(100.0, macd_hist * 50.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="TECHNICAL_FEATURE_ENGINE",
                category=self.category,
                feature="MACD_HISTOGRAM",
                raw_value=float(macd_hist),
                raw_value_str=f"MACD Hist {macd_hist:+.2f}",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.0,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version="1.0.0",
                reason=f"MACD histogram is {macd_hist:+.2f} displaying {dir_enum.value.lower()} impulse"
            ))

        # 4. 52-Week High/Low Position
        pos_52w = features.get("position_52w")  # 0.0 to 1.0
        if pos_52w is not None:
            score = (pos_52w - 0.5) * 200.0  # 1.0 -> +100, 0.0 -> -100
            dir_enum = EvidenceDirection.BULLISH if score > 20 else (EvidenceDirection.BEARISH if score < -20 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="TECHNICAL_FEATURE_ENGINE",
                category=self.category,
                feature="52W_POSITION",
                raw_value=float(pos_52w),
                raw_value_str=f"{pos_52w*100:.1f}% of 52W range",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.2,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version="1.0.0",
                reason=f"Trading at {pos_52w*100:.1f}% percentile of its 52-week price range"
            ))

        # 5. Momentum (20D return vs Benchmark)
        ret_20d = features.get("return_20d")
        if ret_20d is not None:
            score = max(-100.0, min(100.0, ret_20d * 800.0))  # +/-12.5% maps to +/-100
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="TECHNICAL_FEATURE_ENGINE",
                category=self.category,
                feature="MOMENTUM_20D",
                raw_value=float(ret_20d),
                raw_value_str=f"{ret_20d*100:+.2f}% 20D return",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.2,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version="1.0.0",
                reason=f"20-day price momentum is {ret_20d*100:+.2f}%"
            ))

        return evidence_list
