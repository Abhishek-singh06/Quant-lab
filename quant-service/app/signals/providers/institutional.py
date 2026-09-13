"""
Institutional and Mutual Fund Evidence Providers for QuantLab Part 13.
Consumes Part 6 Mutual Fund & Institutional Intelligence Engine outputs strictly point-in-time.
"""

from datetime import datetime
from typing import Any, Dict, List, Optional
from app.signals.models import Evidence, EvidenceCategory, EvidenceDirection
from app.signals.providers.base import SignalEvidenceProvider


class InstitutionalEvidenceProvider(SignalEvidenceProvider):
    """Calculates normalized institutional evidence from FII/DII net flows and institutional ownership."""
    
    @property
    def category(self) -> EvidenceCategory:
        return EvidenceCategory.INSTITUTIONAL

    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        evidence_list: List[Evidence] = []
        if not context:
            return evidence_list
            
        inst_data = context.get("institutional_data", {})
        if not inst_data:
            return evidence_list

        avail_at = inst_data.get("available_at")
        if avail_at and isinstance(avail_at, datetime) and avail_at > as_of_timestamp:
            return evidence_list

        # 1. FII 20-Day Cumulative Net Flow (Crores)
        fii_flow_20d = inst_data.get("fii_net_flow_20d_cr")
        if fii_flow_20d is not None:
            # +5000 Cr -> +100 score, -5000 Cr -> -100 score
            score = max(-100.0, min(100.0, fii_flow_20d / 50.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="INSTITUTIONAL_INTELLIGENCE_ENGINE",
                category=self.category,
                feature="FII_NET_FLOW_20D",
                raw_value=float(fii_flow_20d),
                raw_value_str=f"₹{fii_flow_20d:+.0f} Cr (20D)",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.4,
                timestamp=as_of_timestamp,
                available_at=avail_at or as_of_timestamp,
                version="1.0.0",
                reason=f"FII 20-day cumulative flow is ₹{fii_flow_20d:+.0f} Cr"
            ))

        # 2. DII 20-Day Cumulative Net Flow (Crores)
        dii_flow_20d = inst_data.get("dii_net_flow_20d_cr")
        if dii_flow_20d is not None:
            score = max(-100.0, min(100.0, dii_flow_20d / 50.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="INSTITUTIONAL_INTELLIGENCE_ENGINE",
                category=self.category,
                feature="DII_NET_FLOW_20D",
                raw_value=float(dii_flow_20d),
                raw_value_str=f"₹{dii_flow_20d:+.0f} Cr (20D)",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=1.0,
                weight=1.2,
                timestamp=as_of_timestamp,
                available_at=avail_at or as_of_timestamp,
                version="1.0.0",
                reason=f"DII 20-day cumulative flow is ₹{dii_flow_20d:+.0f} Cr"
            ))

        # 3. Institutional Ownership QoQ Delta
        inst_holding_delta = inst_data.get("inst_holding_delta_qoq")
        if inst_holding_delta is not None:
            # +2.0% change in ownership -> +100 score
            score = max(-100.0, min(100.0, inst_holding_delta * 50.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="INSTITUTIONAL_INTELLIGENCE_ENGINE",
                category=self.category,
                feature="INST_HOLDING_DELTA_QOQ",
                raw_value=float(inst_holding_delta),
                raw_value_str=f"{inst_holding_delta:+.2f}% QoQ",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=0.8,
                weight=1.0,
                timestamp=as_of_timestamp,
                available_at=avail_at or as_of_timestamp,
                version="1.0.0",
                reason=f"Total institutional holding changed by {inst_holding_delta:+.2f}% QoQ"
            ))

        return evidence_list


class MutualFundEvidenceProvider(SignalEvidenceProvider):
    """Calculates mutual fund scheme holding evidence with disclosure date vs publication date tracking."""
    
    @property
    def category(self) -> EvidenceCategory:
        return EvidenceCategory.MUTUAL_FUNDS

    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        evidence_list: List[Evidence] = []
        if not context:
            return evidence_list
            
        mf_data = context.get("mutual_fund_data", {})
        if not mf_data:
            return evidence_list

        pub_at = mf_data.get("published_at")
        if pub_at and isinstance(pub_at, datetime) and pub_at > as_of_timestamp:
            # Monthly disclosure not yet published at as_of_timestamp
            return evidence_list

        data_as_of = mf_data.get("data_as_of")
        days_age = (as_of_timestamp - data_as_of).days if (data_as_of and isinstance(data_as_of, datetime)) else 30
        freshness = max(0.3, 1.0 - (days_age / 60.0))

        net_buyers = mf_data.get("net_buying_schemes_count", 0)
        net_sellers = mf_data.get("net_selling_schemes_count", 0)
        total_schemes = net_buyers + net_sellers
        
        if total_schemes > 0:
            buyer_ratio = (net_buyers - net_sellers) / total_schemes
            score = buyer_ratio * 100.0
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="MUTUAL_FUND_INTELLIGENCE_ENGINE",
                category=self.category,
                feature="MF_SCHEME_NET_FLOW",
                raw_value=float(buyer_ratio),
                raw_value_str=f"{net_buyers} buyers vs {net_sellers} sellers",
                normalized_score=float(score * freshness),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=0.95,
                freshness=freshness,
                weight=1.0,
                timestamp=pub_at or as_of_timestamp,
                available_at=pub_at or as_of_timestamp,
                version="1.0.0",
                reason=f"Monthly MF disclosure (as of {data_as_of}, age: {days_age}d): {net_buyers} schemes increased weight vs {net_sellers} decreased"
            ))

        return evidence_list
