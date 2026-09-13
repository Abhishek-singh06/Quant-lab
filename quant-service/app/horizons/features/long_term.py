"""Long-Term Investing Feature Extractor (6 Months to 5+ Years).

Primary focus on business growth, cash flow generation, ROE/ROCE capital efficiency,
balance sheet health, corporate governance, and valuation.
"""

from typing import Dict, List, Any, Optional
import numpy as np
import pandas as pd
from datetime import datetime

from app.horizons.features.base import BaseHorizonFeatureExtractor


class LongTermFeatureExtractor(BaseHorizonFeatureExtractor):
    """Point-in-Time feature extractor for Long-Term investing models."""

    def __init__(self, version: str = "LONG_TERM_FEATURE_SET_V1"):
        super().__init__(feature_set_version=version)

    def get_feature_names(self) -> List[str]:
        return [
            "ttm_revenue_growth_3y_cagr", "ttm_eps_growth_3y_cagr", "ttm_fcf_growth_3y_cagr",
            "ebitda_margin_ttm", "net_profit_margin_ttm",
            "return_on_equity_ttm", "return_on_capital_employed_ttm", "return_on_assets_ttm",
            "debt_to_equity", "net_debt_to_ebitda", "interest_coverage_ratio",
            "pe_ttm_ratio", "pb_ratio", "ev_to_ebitda_ratio", "fcf_yield",
            "institutional_ownership_pct", "mf_holding_change_1y_pp", "fii_holding_change_1y_pp",
            "return_252d", "price_vs_sma200"
        ]

    def extract_features(
        self,
        symbol: str,
        as_of: datetime,
        ohlcv_df: pd.DataFrame,
        regime_data: Optional[Dict[str, Any]] = None,
        fundamental_data: Optional[Dict[str, Any]] = None,
        institutional_data: Optional[Dict[str, Any]] = None,
        news_data: Optional[Dict[str, Any]] = None,
        global_data: Optional[Dict[str, Any]] = None,
    ) -> Dict[str, float]:
        """Extract long-term features ensuring strict Point-in-Time safety."""
        features: Dict[str, float] = {}

        # 1. Price trend & 252d return
        if ohlcv_df is not None and len(ohlcv_df) >= 50:
            df = ohlcv_df.copy()
            if "date" in df.columns:
                df["date"] = pd.to_datetime(df["date"])
                df = df[df["date"] <= pd.to_datetime(as_of)].sort_values("date")
            close = df["close"].values
            curr_p = close[-1]
            features["return_252d"] = (curr_p / close[-252] - 1.0) if len(close) >= 252 else (curr_p / close[0] - 1.0)
            sma200 = np.mean(close[-200:]) if len(close) >= 200 else np.mean(close)
            features["price_vs_sma200"] = (curr_p / sma200 - 1.0) if sma200 > 0 else 0.0
        else:
            features["return_252d"] = 0.12
            features["price_vs_sma200"] = 0.05

        # 2. Fundamentals (TTM, Growth, Margins, Efficiency, Solvency, Valuation)
        if fundamental_data:
            features["ttm_revenue_growth_3y_cagr"] = float(fundamental_data.get("revenue_cagr_3y", 0.14))
            features["ttm_eps_growth_3y_cagr"] = float(fundamental_data.get("eps_cagr_3y", 0.16))
            features["ttm_fcf_growth_3y_cagr"] = float(fundamental_data.get("fcf_cagr_3y", 0.12))

            features["ebitda_margin_ttm"] = float(fundamental_data.get("ebitda_margin", 0.22))
            features["net_profit_margin_ttm"] = float(fundamental_data.get("net_profit_margin", 0.14))

            features["return_on_equity_ttm"] = float(fundamental_data.get("roe", 0.18))
            features["return_on_capital_employed_ttm"] = float(fundamental_data.get("roce", 0.20))
            features["return_on_assets_ttm"] = float(fundamental_data.get("roa", 0.09))

            features["debt_to_equity"] = float(fundamental_data.get("debt_to_equity", 0.35))
            features["net_debt_to_ebitda"] = float(fundamental_data.get("net_debt_to_ebitda", 0.80))
            features["interest_coverage_ratio"] = min(50.0, float(fundamental_data.get("interest_coverage", 8.5)))

            features["pe_ttm_ratio"] = float(fundamental_data.get("pe_ttm", 24.0)) / 50.0
            features["pb_ratio"] = float(fundamental_data.get("pb_ratio", 3.2)) / 10.0
            features["ev_to_ebitda_ratio"] = float(fundamental_data.get("ev_ebitda", 15.0)) / 30.0
            features["fcf_yield"] = float(fundamental_data.get("fcf_yield", 0.035))
        else:
            features["ttm_revenue_growth_3y_cagr"] = 0.12
            features["ttm_eps_growth_3y_cagr"] = 0.14
            features["ttm_fcf_growth_3y_cagr"] = 0.10
            features["ebitda_margin_ttm"] = 0.20
            features["net_profit_margin_ttm"] = 0.12
            features["return_on_equity_ttm"] = 0.16
            features["return_on_capital_employed_ttm"] = 0.18
            features["return_on_assets_ttm"] = 0.08
            features["debt_to_equity"] = 0.40
            features["net_debt_to_ebitda"] = 1.0
            features["interest_coverage_ratio"] = 7.0
            features["pe_ttm_ratio"] = 0.48
            features["pb_ratio"] = 0.32
            features["ev_to_ebitda_ratio"] = 0.50
            features["fcf_yield"] = 0.03

        # 3. Institutional Ownership
        if institutional_data:
            features["institutional_ownership_pct"] = float(institutional_data.get("total_institutional_pct", 38.5)) / 100.0
            features["mf_holding_change_1y_pp"] = float(institutional_data.get("mf_change_1y_pp", 1.2)) / 10.0
            features["fii_holding_change_1y_pp"] = float(institutional_data.get("fii_change_1y_pp", 0.8)) / 10.0
        else:
            features["institutional_ownership_pct"] = 0.35
            features["mf_holding_change_1y_pp"] = 0.05
            features["fii_holding_change_1y_pp"] = 0.02

        return features
