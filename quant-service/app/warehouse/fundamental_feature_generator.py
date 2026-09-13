"""Point-in-Time Fundamental Feature Generator for Indian Listed Equities.

CRITICAL FINANCIAL INTEGRITY RULE:
Every fundamental feature is calculated using ONLY corporate disclosures and financial statements where:
available_at <= feature_timestamp

Under NO circumstances should period_end <= feature_timestamp be used as the inclusion criterion,
as corporate earnings for a quarter ending e.g. March 31 are published ~45 days later in May.
"""

from typing import List, Dict, Any, Optional
import numpy as np
import pandas as pd
from datetime import datetime, timezone


class FundamentalFeatureGenerator:
    """Generates point-in-time financial statement ratios, YoY growth rates, TTM aggregates,
    and valuation metrics with strictly zero look-ahead bias.
    """

    @staticmethod
    def calculate_fundamental_features(
        symbol: str,
        statements: List[Dict[str, Any]],
        feature_timestamp: str,
        current_market_price: Optional[float] = None,
        total_shares_outstanding: Optional[int] = None,
        current_market_cap_cr: Optional[float] = None,
        reporting_basis: str = "CONSOLIDATED"
    ) -> Dict[str, Any]:
        """Calculates point-in-time fundamental features for `symbol` as of `feature_timestamp`.

        STRICT POINT-IN-TIME FILTER:
        Discards any filing/statement where available_at > feature_timestamp.
        """
        target_time = pd.to_datetime(feature_timestamp, utc=True)

        # 1. Point-in-time filter
        valid_statements = [
            s for s in statements
            if pd.to_datetime(s.get('available_at'), utc=True) <= target_time
            and (s.get('symbol') == symbol or not s.get('symbol'))
            and (s.get('reporting_basis', reporting_basis).upper() == reporting_basis.upper())
        ]

        if not valid_statements:
            return {
                'symbol': symbol,
                'feature_timestamp': feature_timestamp,
                'has_fundamental_data': False,
                'reporting_basis': reporting_basis,
                'latest_period_end': None,
                'filing_lag_days': None,
                'pe_ratio': None,
                'pb_ratio': None,
                'roe': None,
                'roce': None,
                'ebitda_margin': None,
                'net_profit_margin': None,
                'debt_to_equity': None,
                'revenue_growth_yoy': None,
                'pat_growth_yoy': None,
                'ttm_revenue_cr': None,
                'ttm_net_profit_cr': None,
                'ttm_pe_ratio': None
            }

        # Sort statements by period_end descending, and then by available_at descending (for restatements)
        sorted_statements = sorted(
            valid_statements,
            key=lambda x: (
                pd.to_datetime(x.get('period_end'), utc=True),
                pd.to_datetime(x.get('available_at'), utc=True),
                int(x.get('version', 1))
            ),
            reverse=True
        )

        # Separate quarterly and annual statements
        quarterly_stmts = [s for s in sorted_statements if str(s.get('period_type', '')).upper() == 'QUARTERLY']
        annual_stmts = [s for s in sorted_statements if str(s.get('period_type', '')).upper() == 'ANNUAL']

        latest_stmt = sorted_statements[0]
        latest_period_end = pd.to_datetime(latest_stmt.get('period_end'), utc=True)
        latest_available_at = pd.to_datetime(latest_stmt.get('available_at'), utc=True)
        filing_lag_days = (latest_available_at - latest_period_end).days
        is_restated = bool(latest_stmt.get('restatement', False) or int(latest_stmt.get('version', 1)) > 1)

        # Basic financial items from latest available statement
        rev = float(latest_stmt.get('revenue', 0.0) or 0.0)
        ebitda = float(latest_stmt.get('ebitda', 0.0) or 0.0)
        ebit = float(latest_stmt.get('ebit', 0.0) or 0.0)
        pat = float(latest_stmt.get('net_profit', 0.0) or 0.0)
        assets = float(latest_stmt.get('total_assets', 0.0) or 0.0)
        equity = float(latest_stmt.get('total_equity', 0.0) or 0.0)
        total_debt = float(latest_stmt.get('total_debt', 0.0) or 0.0)
        cash = float(latest_stmt.get('cash_and_equivalents', 0.0) or 0.0)
        curr_assets = float(latest_stmt.get('current_assets', 0.0) or 0.0)
        curr_liab = float(latest_stmt.get('current_liabilities', 0.0) or 0.0)
        interest = float(latest_stmt.get('interest_expense', 0.0) or 0.0)

        # Margins & Returns
        ebitda_margin = (ebitda / rev * 100.0) if rev > 0 else None
        net_profit_margin = (pat / rev * 100.0) if rev > 0 else None
        roe = (pat / equity * 100.0) if equity > 0 else None
        capital_employed = (assets - curr_liab) if (assets > 0 and curr_liab > 0) else (equity + total_debt)
        roce = (ebit / capital_employed * 100.0) if (ebit and capital_employed > 0) else None
        roa = (pat / assets * 100.0) if assets > 0 else None

        # Solvency & Coverage
        debt_to_equity = (total_debt / equity) if equity > 0 else None
        net_debt = total_debt - cash
        net_debt_to_ebitda = (net_debt / ebitda) if ebitda > 0 else None
        interest_coverage = (ebit / interest) if (ebit and interest > 0) else None
        current_ratio = (curr_assets / curr_liab) if curr_liab > 0 else None

        # YoY Growth
        prior_yoy_stmt = None
        target_prior_period = latest_period_end - pd.DateOffset(years=1)
        for s in sorted_statements:
            s_period = pd.to_datetime(s.get('period_end'), utc=True)
            if s.get('period_type') == latest_stmt.get('period_type') and abs((s_period - target_prior_period).days) <= 45:
                prior_yoy_stmt = s
                break

        rev_growth_yoy = None
        pat_growth_yoy = None
        ebitda_growth_yoy = None
        if prior_yoy_stmt:
            prior_rev = float(prior_yoy_stmt.get('revenue', 0.0) or 0.0)
            prior_pat = float(prior_yoy_stmt.get('net_profit', 0.0) or 0.0)
            prior_ebitda = float(prior_yoy_stmt.get('ebitda', 0.0) or 0.0)
            if prior_rev > 0:
                rev_growth_yoy = round((rev - prior_rev) / prior_rev * 100.0, 4)
            if prior_pat > 0:
                pat_growth_yoy = round((pat - prior_pat) / prior_pat * 100.0, 4)
            if prior_ebitda > 0:
                ebitda_growth_yoy = round((ebitda - prior_ebitda) / prior_ebitda * 100.0, 4)

        # TTM Metrics (Sum of latest 4 distinct quarters available at feature_timestamp)
        ttm_revenue = None
        ttm_pat = None
        ttm_ebitda = None

        # Deduplicate quarterly statements by period_end (keep latest available/version)
        seen_periods = set()
        dedup_quarterly = []
        for q in quarterly_stmts:
            p_end = q.get('period_end')
            if p_end not in seen_periods:
                seen_periods.add(p_end)
                dedup_quarterly.append(q)

        if len(dedup_quarterly) >= 4:
            last_4_q = dedup_quarterly[:4]
            ttm_revenue = sum(float(q.get('revenue', 0.0) or 0.0) for q in last_4_q)
            ttm_pat = sum(float(q.get('net_profit', 0.0) or 0.0) for q in last_4_q)
            ttm_ebitda = sum(float(q.get('ebitda', 0.0) or 0.0) for q in last_4_q)

        # Market Cap & Valuation Ratios
        mcap = current_market_cap_cr
        if mcap is None and current_market_price is not None and total_shares_outstanding is not None:
            # Price in INR * Shares / 1e7 = Crores
            mcap = (current_market_price * total_shares_outstanding) / 1e7

        pe_ratio = None
        pb_ratio = None
        ev_ebitda = None
        ev_sales = None
        ttm_pe_ratio = None

        if mcap is not None and mcap > 0:
            ev = mcap + net_debt
            if pat > 0:
                annualized_pat = pat * (4.0 if latest_stmt.get('period_type') == 'QUARTERLY' else 1.0)
                pe_ratio = round(mcap / annualized_pat, 2)
            if equity > 0:
                pb_ratio = round(mcap / equity, 2)
            if ebitda > 0:
                annualized_ebitda = ebitda * (4.0 if latest_stmt.get('period_type') == 'QUARTERLY' else 1.0)
                ev_ebitda = round(ev / annualized_ebitda, 2)
            if rev > 0:
                annualized_rev = rev * (4.0 if latest_stmt.get('period_type') == 'QUARTERLY' else 1.0)
                ev_sales = round(ev / annualized_rev, 2)
            if ttm_pat is not None and ttm_pat > 0:
                ttm_pe_ratio = round(mcap / ttm_pat, 2)

        return {
            'symbol': symbol,
            'feature_timestamp': feature_timestamp,
            'has_fundamental_data': True,
            'reporting_basis': reporting_basis,
            'latest_period_end': str(latest_period_end.date()),
            'latest_available_at': str(latest_available_at),
            'filing_lag_days': filing_lag_days,
            'is_restated': is_restated,
            # Ratios
            'ebitda_margin': round(ebitda_margin, 4) if ebitda_margin is not None else None,
            'net_profit_margin': round(net_profit_margin, 4) if net_profit_margin is not None else None,
            'roe': round(roe, 4) if roe is not None else None,
            'roce': round(roce, 4) if roce is not None else None,
            'roa': round(roa, 4) if roa is not None else None,
            'debt_to_equity': round(debt_to_equity, 4) if debt_to_equity is not None else None,
            'net_debt_to_ebitda': round(net_debt_to_ebitda, 4) if net_debt_to_ebitda is not None else None,
            'interest_coverage': round(interest_coverage, 4) if interest_coverage is not None else None,
            'current_ratio': round(current_ratio, 4) if current_ratio is not None else None,
            # Growth
            'revenue_growth_yoy': rev_growth_yoy,
            'pat_growth_yoy': pat_growth_yoy,
            'ebitda_growth_yoy': ebitda_growth_yoy,
            # TTM
            'ttm_revenue_cr': round(ttm_revenue, 2) if ttm_revenue is not None else None,
            'ttm_net_profit_cr': round(ttm_pat, 2) if ttm_pat is not None else None,
            'ttm_ebitda_cr': round(ttm_ebitda, 2) if ttm_ebitda is not None else None,
            # Valuation
            'market_cap_cr': round(mcap, 2) if mcap is not None else None,
            'pe_ratio': pe_ratio,
            'pb_ratio': pb_ratio,
            'ev_ebitda': ev_ebitda,
            'ev_sales': ev_sales,
            'ttm_pe_ratio': ttm_pe_ratio
        }
