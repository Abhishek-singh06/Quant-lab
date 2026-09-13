"""Point-in-Time Global Market Feature Generator.

CRITICAL FINANCIAL INTEGRITY RULE:
Every global feature is calculated using ONLY observations where:
source_timestamp <= feature_timestamp

Under NO circumstances may an Indian morning feature (e.g. 09:15 IST)
access future US market closes, future European closes, or future Asian closes.
"""

from typing import List, Dict, Any, Optional
import numpy as np
import pandas as pd


class GlobalFeatureGenerator:
    """Generates point-in-time quantitative global market features and regimes

    without introducing forward-looking look-ahead bias across international time zones.
    """

    @staticmethod
    def calculate_global_features(
        snapshots: List[Dict[str, Any]],
        feature_timestamp: str
    ) -> Dict[str, Any]:
        """Calculates point-in-time global market signals as of `feature_timestamp`.

        STRICT POINT-IN-TIME FILTER:
        Filters out all snapshots where source_timestamp > feature_timestamp.
        """
        target_time = pd.to_datetime(feature_timestamp, utc=True)

        # 1. Point-in-time filtering
        valid_snapshots = [
            s for s in snapshots
            if pd.to_datetime(s['source_timestamp'], utc=True) <= target_time
        ]

        # 2. Extract latest valid observation per canonical symbol
        latest_by_symbol: Dict[str, Dict[str, Any]] = {}
        history_by_symbol: Dict[str, List[Dict[str, Any]]] = {}

        for s in valid_snapshots:
            sym = s.get('canonical_symbol')
            t = pd.to_datetime(s['source_timestamp'], utc=True)
            if sym not in history_by_symbol:
                history_by_symbol[sym] = []
            history_by_symbol[sym].append(s)

            if sym not in latest_by_symbol:
                latest_by_symbol[sym] = s
            else:
                curr_t = pd.to_datetime(latest_by_symbol[sym]['source_timestamp'], utc=True)
                if t > curr_t:
                    latest_by_symbol[sym] = s

        # Helper to get latest value and returns
        def get_val(sym: str) -> Optional[float]:
            s = latest_by_symbol.get(sym)
            if s and s.get('close') is not None:
                return float(s['close'])
            return None

        def get_change_pct(sym: str) -> float:
            s = latest_by_symbol.get(sym)
            if s and s.get('change_percent') is not None:
                return float(s['change_percent'])
            return 0.0

        def get_n_day_return(sym: str, n_days: int) -> float:
            hist = history_by_symbol.get(sym, [])
            if not hist:
                return 0.0
            sorted_hist = sorted(hist, key=lambda x: pd.to_datetime(x['source_timestamp'], utc=True))
            if len(sorted_hist) < 2:
                return float(sorted_hist[-1].get('change_percent', 0.0))
            
            cutoff = target_time - pd.Timedelta(days=n_days * 1.5)
            filtered = [x for x in sorted_hist if pd.to_datetime(x['source_timestamp'], utc=True) >= cutoff]
            if len(filtered) >= 2:
                p_start = float(filtered[0]['close'])
                p_end = float(filtered[-1]['close'])
                if p_start > 0:
                    return round(((p_end - p_start) / p_start) * 100.0, 4)
            return float(sorted_hist[-1].get('change_percent', 0.0))

        # 3. Individual Asset Signals
        spx_ret_1d = get_change_pct('SPX')
        nasdaq_ret_1d = get_change_pct('NASDAQ')
        dji_ret_1d = get_change_pct('DJI')
        rut_ret_1d = get_change_pct('RUT')
        vix_level = get_val('VIX') or 18.0
        vix_change_pct = get_change_pct('VIX')
        dxy_level = get_val('DXY') or 103.0
        dxy_change_pct = get_change_pct('DXY')
        us10y_yield = get_val('US10Y') or 4.0
        us10y_change_bps = (float(latest_by_symbol['US10Y'].get('change', 0.0)) * 100.0) if 'US10Y' in latest_by_symbol else 0.0
        usdinr_rate = get_val('USDINR') or 84.0
        usdinr_change_pct = get_change_pct('USDINR')

        n225_ret_1d = get_change_pct('N225')
        hsi_ret_1d = get_change_pct('HSI')
        ssec_ret_1d = get_change_pct('SSEC')
        ftse_ret_1d = get_change_pct('FTSE')
        dax_ret_1d = get_change_pct('DAX')

        crude_ret_1d = get_change_pct('CRUDE_WTI')
        gold_ret_1d = get_change_pct('GOLD')

        # 4. Multi-horizon Rolling Returns
        spx_ret_5d = get_n_day_return('SPX', 5)
        spx_ret_20d = get_n_day_return('SPX', 20)
        nasdaq_ret_5d = get_n_day_return('NASDAQ', 5)
        crude_ret_5d = get_n_day_return('CRUDE_WTI', 5)
        gold_ret_5d = get_n_day_return('GOLD', 5)

        # 5. Composite Global Risk Score (-100 to +100)
        # US Equity Component (25%)
        us_eq_avg = (spx_ret_1d + nasdaq_ret_1d + dji_ret_1d + rut_ret_1d) / 4.0
        equity_score = max(-100.0, min(100.0, us_eq_avg * 50.0))

        # Volatility Component (20%)
        if vix_level < 14.0:
            vol_score = 75.0
        elif vix_level < 18.0:
            vol_score = 35.0
        elif vix_level < 22.0:
            vol_score = -15.0
        elif vix_level < 30.0:
            vol_score = -60.0
        else:
            vol_score = -95.0
        vol_score = max(-100.0, min(100.0, vol_score - (vix_change_pct * 3.0)))

        # Dollar Component (15%)
        dollar_score = max(-100.0, min(100.0, -dxy_change_pct * 60.0))

        # Rates Component (10%)
        rates_score = max(-100.0, min(100.0, -us10y_change_bps * 4.0))

        # Commodities Component (10%)
        commodity_score = max(-100.0, min(100.0, (crude_ret_1d * 0.20 + gold_ret_1d * 0.15) * 100.0))

        # Asia Component (10%)
        asia_avg = (n225_ret_1d + hsi_ret_1d + ssec_ret_1d) / 3.0
        asia_score = max(-100.0, min(100.0, asia_avg * 50.0))

        # Europe Component (10%)
        europe_avg = (ftse_ret_1d + dax_ret_1d) / 2.0
        europe_score = max(-100.0, min(100.0, europe_avg * 50.0))

        composite_score = (
            0.25 * equity_score +
            0.20 * vol_score +
            0.15 * dollar_score +
            0.10 * rates_score +
            0.10 * commodity_score +
            0.10 * asia_score +
            0.10 * europe_score
        )
        composite_score = max(-100.0, min(100.0, composite_score))

        # Regime Label
        if composite_score > 25.0:
            regime_label = 'RISK_ON'
        elif composite_score < -25.0:
            regime_label = 'RISK_OFF'
        elif vix_level > 24.0:
            regime_label = 'HIGH_VOLATILITY'
        elif vix_level < 14.0:
            regime_label = 'LOW_VOLATILITY'
        elif abs(composite_score) < 10.0:
            regime_label = 'NEUTRAL'
        else:
            regime_label = 'TRANSITION'

        # Confidence
        asset_count = len(latest_by_symbol)
        if asset_count >= 12:
            confidence = 'HIGH'
        elif asset_count >= 7:
            confidence = 'MEDIUM'
        else:
            confidence = 'LOW'

        return {
            'feature_timestamp': feature_timestamp,
            'regime_label': regime_label,
            'composite_risk_score': round(composite_score, 4),
            'confidence': confidence,
            'asset_count': asset_count,
            'equity_score': round(equity_score, 4),
            'volatility_score': round(vol_score, 4),
            'dollar_score': round(dollar_score, 4),
            'rates_score': round(rates_score, 4),
            'commodity_score': round(commodity_score, 4),
            'asia_score': round(asia_score, 4),
            'europe_score': round(europe_score, 4),
            # Key returns
            'spx_1d_return': round(spx_ret_1d, 4),
            'spx_5d_return': round(spx_ret_5d, 4),
            'spx_20d_return': round(spx_ret_20d, 4),
            'nasdaq_1d_return': round(nasdaq_ret_1d, 4),
            'nasdaq_5d_return': round(nasdaq_ret_5d, 4),
            'vix_level': round(vix_level, 4),
            'vix_1d_change': round(vix_change_pct, 4),
            'dxy_level': round(dxy_level, 4),
            'dxy_1d_change': round(dxy_change_pct, 4),
            'us10y_yield': round(us10y_yield, 4),
            'us10y_change_bps': round(us10y_change_bps, 4),
            'usdinr_rate': round(usdinr_rate, 4),
            'crude_1d_return': round(crude_ret_1d, 4),
            'gold_1d_return': round(gold_ret_1d, 4)
        }
