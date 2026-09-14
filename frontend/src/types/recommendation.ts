/**
 * Types for QuantLab Multi-Evidence Investment Recommendation Engine.
 */

export type InvestmentRecommendationType =
  | 'STRONG BUY'
  | 'BUY'
  | 'ACCUMULATE'
  | 'HOLD'
  | 'WAIT'
  | 'REDUCE'
  | 'AVOID'
  | 'NO DECISION';

export type ActionStrategy =
  | 'BUY NOW'
  | 'ACCUMULATE GRADUALLY'
  | 'WAIT FOR PULLBACK'
  | 'WAIT FOR CONFIRMATION'
  | 'PARTIAL ENTRY'
  | 'HOLD CURRENT POSITION'
  | 'REDUCE EXPOSURE'
  | 'AVOID / STAY IN CASH'
  | 'NO ACTION / INSUFFICIENT DATA';

export type RiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'EXTREME' | 'UNKNOWN';

export interface DataQualityView {
  market_data: 'AVAILABLE' | 'DELAYED' | 'STALE' | 'UNAVAILABLE';
  technicals: 'AVAILABLE' | 'UNAVAILABLE';
  fundamentals: 'AVAILABLE' | 'UNAVAILABLE';
  news: 'AVAILABLE' | 'UNAVAILABLE';
  institutional: 'AVAILABLE' | 'UNAVAILABLE';
  quant_model: 'AVAILABLE' | 'UNAVAILABLE';
  regime: 'AVAILABLE' | 'UNAVAILABLE';
}

export interface EvidenceSummaryItem {
  category: string;
  feature: string;
  direction: 'BULLISH' | 'BEARISH' | 'NEUTRAL' | 'UNCERTAIN';
  importance: 'HIGH' | 'MEDIUM' | 'LOW';
  claim: string;
  status: 'SUPPORTIVE' | 'CAUTION' | 'NEUTRAL' | 'NOT_AVAILABLE';
  source: string;
  available_at?: string;
}

export interface CapitalAllocationPlan {
  recommended_allocation_inr: number;
  portfolio_weight_pct: number;
  risk_per_position_pct: number;
  max_suggested_allocation_inr: number;
  initial_allocation_inr: number;
  tranche_count: number;
  allocation_reasoning: string;
  conditions_for_further_allocation: string[];
  binding_risk_constraint: string;
  portfolio_capital_inr: number;
  risk_budget_inr: number;
}

export interface RiskRewardProfile {
  current_price: number;
  expected_upside_pct?: number;
  expected_downside_pct?: number;
  target_price?: number;
  stop_loss_price?: number;
  risk_reward_ratio?: number;
  invalidation_level: string;
  expected_holding_period: string;
  is_available: boolean;
  unavailability_reason?: string;
  what_drives_upside: string[];
  what_causes_downside: string[];
}

export interface ScenarioDetail {
  expected_move_pct: number;
  target_price: number;
  key_drivers: string[];
  primary_risks: string[];
  trigger_conditions: string[];
}

export interface ScenarioAnalysis {
  bear_case: ScenarioDetail;
  base_case: ScenarioDetail;
  bull_case: ScenarioDetail;
  notes: string;
}

export interface HorizonOutlook {
  horizon: 'SHORT_TERM' | 'MEDIUM_TERM' | 'LONG_TERM';
  timeframe_label: string;
  outlook: string;
  expected_return_pct?: number | null;
  confidence: number;
  status_text: string;
  is_supported: boolean;
  model: string;
  prediction_timestamp: string;
}

export interface TraderQuestionsAnalysis {
  is_attractive_at_current_price: string;
  is_momentum_favorable: string;
  is_stock_overextended: string;
  is_there_better_entry: string;
  downside_if_thesis_wrong: string;
  upside_if_thesis_works: string;
  strongest_reason_to_buy: string;
  strongest_reason_not_to_buy: string;
  what_could_invalidate_thesis: string;
  what_event_to_watch_next: string;
  deploy_capital_mode: string;
  appropriate_risk_level: string;
  expected_holding_period: string;
}

export interface NewsInterpretation {
  headline_summary: string;
  sentiment_label: string;
  contextual_thesis_impact: string;
  market_pricing_status: string;
  is_point_in_time_verified: boolean;
}

export interface FundamentalInvestorView {
  quality_assessment: string;
  growth_trend: string;
  profitability_and_margins: string;
  balance_sheet_and_leverage: string;
  valuation_assessment: string;
  key_metrics: Record<string, any>;
}

export interface QuantModelView {
  model_id: string;
  model_status: string;
  signal_direction: string;
  t_plus_1_forecast?: number | null;
  t_plus_5_forecast?: number | null;
  t_plus_20_forecast?: number | null;
  model_confidence: number;
  walk_forward_sharpe: number;
  is_model_reliable: boolean;
  is_out_of_distribution?: boolean;
  max_abs_z_score?: number;
  extreme_features?: string[];
}

export interface TechnicalView {
  trend_state: string;
  momentum_state: string;
  support_levels: number[];
  resistance_levels: number[];
  overbought_oversold_status: string;
  synthesized_commentary: string;
}

export interface MarketRegimeView {
  benchmark_symbol: string;
  direction_regime: string;
  volatility_regime: string;
  risk_regime: string;
  regime_impact_on_sizing: string;
}

export interface ConflictAnalysisView {
  has_conflicts: boolean;
  conflict_severity: string;
  conflict_score: number;
  conflicting_pairs: string[];
  resolution_rationale: string;
}

export interface ConfidenceBreakdown {
  data_confidence_pct: number;
  model_confidence_pct: number;
  signal_agreement_pct: number;
  decision_confidence_pct: number;
  explanation: string;
}

export interface WhatChangedComparison {
  previous_recommendation?: string | null;
  current_recommendation: string;
  status_change: string;
  change_reasons: string[];
}

export interface CompleteInvestmentRecommendation {
  id: string;
  symbol: string;
  company_name: string;
  exchange: string;
  sector: string;
  as_of_timestamp: string;
  recommendation: InvestmentRecommendationType;
  conviction_score: number;
  risk_level: RiskLevel;
  suggested_action: ActionStrategy;
  investment_thesis: string;
  thesis_invalidation: string;
  why_points_supportive: string[];
  why_points_cautionary: string[];
  capital_allocation: CapitalAllocationPlan;
  risk_reward: RiskRewardProfile;
  scenarios: ScenarioAnalysis;
  horizons: HorizonOutlook[];
  trader_qa: TraderQuestionsAnalysis;
  news_interpretation: NewsInterpretation;
  fundamentals_view: FundamentalInvestorView;
  quant_view: QuantModelView;
  technical_view: TechnicalView;
  regime_view: MarketRegimeView;
  conflict_analysis: ConflictAnalysisView;
  confidence_breakdown: ConfidenceBreakdown;
  what_changed: WhatChangedComparison;
  evidence_items: EvidenceSummaryItem[];
  live_trading_status: string;
  paper_trading_mode: boolean;
  real_money_at_risk_inr: number;
  sha256_provenance_hash: string;
}
