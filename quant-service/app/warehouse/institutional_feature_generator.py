"""Point-in-Time Mutual Fund and Institutional Intelligence Feature Generator.

CRITICAL FINANCIAL INTEGRITY RULE:
Every institutional feature is calculated using ONLY observations where:
available_at <= feature_timestamp

Periodic disclosures (e.g. Month-end MF holdings published ~10-15th of next month,
Quarterly shareholding patterns published ~21 days after quarter end) must NEVER
be used before their verified `available_at` timestamp.
"""

from typing import List, Dict, Any, Optional
import numpy as np
import pandas as pd
from datetime import datetime, timezone


class InstitutionalFeatureGenerator:
    """Generates point-in-time quantitative institutional features (MF holdings,

    FII/DII flows, shareholding changes) with strictly zero look-ahead bias.
    """

    @staticmethod
    def calculate_institutional_features(
        symbol: str,
        holdings: List[Dict[str, Any]],
        flows: List[Dict[str, Any]],
        ownership: List[Dict[str, Any]],
        feature_timestamp: str
    ) -> Dict[str, Any]:
        """Calculates point-in-time institutional features for `symbol` as of `feature_timestamp`.

        STRICT POINT-IN-TIME FILTER:
        Ignores any holding, flow, or ownership record where available_at > feature_timestamp.
        """
        target_time = pd.to_datetime(feature_timestamp, utc=True)

        # 1. Point-in-time filtering
        valid_holdings = [
            h for h in holdings
            if pd.to_datetime(h['available_at'], utc=True) <= target_time
            and (h.get('symbol') == symbol or not symbol)
        ]

        valid_flows = [
            f for f in flows
            if pd.to_datetime(f['available_at'], utc=True) <= target_time
        ]

        valid_ownership = [
            o for o in ownership
            if pd.to_datetime(o['available_at'], utc=True) <= target_time
            and (o.get('symbol') == symbol or not symbol)
        ]

        # 2. Mutual Fund Holding Features (Point-in-Time)
        # Find latest available disclosure date for each fund scheme
        scheme_latest_holding: Dict[Any, Dict[str, Any]] = {}
        for h in valid_holdings:
            scheme_id = h.get('scheme_id')
            data_as_of = pd.to_datetime(h.get('data_as_of'), utc=True)
            if scheme_id not in scheme_latest_holding:
                scheme_latest_holding[scheme_id] = h
            else:
                curr_date = pd.to_datetime(scheme_latest_holding[scheme_id].get('data_as_of'), utc=True)
                if data_as_of > curr_date:
                    scheme_latest_holding[scheme_id] = h

        total_funds_holding = len(scheme_latest_holding)
        funds_increasing = 0
        funds_decreasing = 0
        funds_new_entry = 0
        funds_exited = 0
        sum_portfolio_weight = 0.0
        sum_weight_change_pp = 0.0
        latest_mf_data_as_of = None

        for h in scheme_latest_holding.values():
            change_type = str(h.get('change_type', 'UNCHANGED')).upper()
            weight = float(h.get('portfolio_weight', 0.0))
            weight_change = float(h.get('weight_change_pp', 0.0))
            h_data_as_of = pd.to_datetime(h.get('data_as_of'), utc=True)

            if latest_mf_data_as_of is None or h_data_as_of > latest_mf_data_as_of:
                latest_mf_data_as_of = h_data_as_of

            sum_portfolio_weight += weight
            sum_weight_change_pp += weight_change

            if change_type == 'NEW_POSITION':
                funds_new_entry += 1
            elif change_type == 'EXITED_POSITION':
                funds_exited += 1
            elif change_type == 'INCREASED' or weight_change > 0:
                funds_increasing += 1
            elif change_type == 'DECREASED' or weight_change < 0:
                funds_decreasing += 1

        mf_disclosure_age_days = (
            (target_time - latest_mf_data_as_of).days if latest_mf_data_as_of is not None else None
        )

        # 3. Institutional Flows Features (FII / DII Daily Cash Flows)
        # Sort flows by trade_date ascending
        sorted_flows = sorted(valid_flows, key=lambda x: pd.to_datetime(x.get('trade_date'), utc=True))

        fii_flows_5d = []
        fii_flows_20d = []
        dii_flows_5d = []
        dii_flows_20d = []

        five_days_ago = target_time - pd.Timedelta(days=7)  # approx 5 trading days
        twenty_days_ago = target_time - pd.Timedelta(days=30)  # approx 20 trading days

        for f in sorted_flows:
            t_date = pd.to_datetime(f.get('trade_date'), utc=True)
            itype = str(f.get('institution_type', '')).upper()
            net_val = float(f.get('net_value', 0.0))

            if itype in ('FII', 'FPI'):
                if t_date >= twenty_days_ago:
                    fii_flows_20d.append(net_val)
                if t_date >= five_days_ago:
                    fii_flows_5d.append(net_val)
            elif itype in ('DII', 'MUTUAL_FUND', 'MF'):
                if t_date >= twenty_days_ago:
                    dii_flows_20d.append(net_val)
                if t_date >= five_days_ago:
                    dii_flows_5d.append(net_val)

        fii_net_flow_5d = sum(fii_flows_5d)
        fii_net_flow_20d = sum(fii_flows_20d)
        dii_net_flow_5d = sum(dii_flows_5d)
        dii_net_flow_20d = sum(dii_flows_20d)

        # Flow divergence: FII buying vs DII selling or vice versa
        flow_divergence_20d = fii_net_flow_20d - dii_net_flow_20d

        # 4. Shareholding Pattern Features (Point-in-time quarterly disclosures)
        sorted_ownership = sorted(valid_ownership, key=lambda x: pd.to_datetime(x.get('period_end'), utc=True), reverse=True)
        latest_period = sorted_ownership[0].get('period_end') if sorted_ownership else None

        fii_ownership_pct = 0.0
        dii_ownership_pct = 0.0
        insurance_ownership_pct = 0.0
        total_inst_ownership_pct = 0.0
        ownership_age_days = None

        if latest_period:
            period_date = pd.to_datetime(latest_period, utc=True)
            ownership_age_days = (target_time - period_date).days
            for o in sorted_ownership:
                if o.get('period_end') == latest_period:
                    itype = str(o.get('institution_type', '')).upper()
                    pct = float(o.get('ownership_percentage', 0.0))
                    if itype in ('FII', 'FPI'):
                        fii_ownership_pct += pct
                    elif itype in ('DII', 'MUTUAL_FUND', 'MF'):
                        dii_ownership_pct += pct
                    elif itype == 'INSURANCE':
                        insurance_ownership_pct += pct
                    total_inst_ownership_pct += pct

        return {
            'symbol': symbol,
            'feature_timestamp': feature_timestamp,
            # MF metrics
            'total_funds_holding': total_funds_holding,
            'funds_increasing_stake': funds_increasing,
            'funds_decreasing_stake': funds_decreasing,
            'funds_new_entry': funds_new_entry,
            'funds_exited': funds_exited,
            'avg_fund_portfolio_weight': round(sum_portfolio_weight / max(1, total_funds_holding), 4),
            'sum_fund_weight_change_pp': round(sum_weight_change_pp, 4),
            'mf_disclosure_age_days': mf_disclosure_age_days,
            # Flow metrics
            'fii_net_flow_5d_cr': round(fii_net_flow_5d, 2),
            'fii_net_flow_20d_cr': round(fii_net_flow_20d, 2),
            'dii_net_flow_5d_cr': round(dii_net_flow_5d, 2),
            'dii_net_flow_20d_cr': round(dii_net_flow_20d, 2),
            'flow_divergence_20d_cr': round(flow_divergence_20d, 2),
            # Shareholding metrics
            'fii_ownership_pct': round(fii_ownership_pct, 4),
            'dii_ownership_pct': round(dii_ownership_pct, 4),
            'insurance_ownership_pct': round(insurance_ownership_pct, 4),
            'total_institutional_pct': round(total_inst_ownership_pct, 4),
            'ownership_disclosure_age_days': ownership_age_days
        }
