/**
 * Types for Model Registry, Specifications, Features, Evaluation, and Walk-Forward Diagnostics.
 * Authoritative data served from /api/v1/models endpoints.
 */

export interface RegisteredModelSummary {
  model_id: string;
  model_name: string;
  status: string;
  model_type: string;
  version: string;
  model_hash: string;
  dataset_id: string;
  target_horizon: string;
  validation_method: string;
}

export interface ModelFeatureItem {
  index: number;
  name: string;
  description: string;
  lookback: string;
  source: string;
  pit_status: string;
  weight: number;
  mean: number;
  std: number;
  economic_interpretation: string;
}

export interface ModelFeaturesResponse {
  model_id: string;
  feature_version: string;
  feature_count: number;
  feature_order: string[];
  features: ModelFeatureItem[];
  intercept: number;
}

export interface LockedHoldoutEvaluation {
  period: string;
  observations_count: number;
  rank_ic: number;
  directional_accuracy_pct: number;
  mean_loss: number;
  disclosure: string;
}

export interface ModelIntegrityChecks {
  leakage_check: 'PASS' | 'FAIL' | 'NOT_VERIFIED';
  survivorship_check: 'PASS' | 'FAIL' | 'NOT_VERIFIED';
  point_in_time_check: 'PASS' | 'FAIL' | 'NOT_VERIFIED';
}

export interface ModelEvaluationResponse {
  model_id: string;
  locked_holdout: LockedHoldoutEvaluation;
  integrity_checks: ModelIntegrityChecks;
  calibration_status: string;
  ood_gate: string;
  model_limitations: string;
}

export interface WalkForwardFoldDetail {
  fold_number: number;
  fold_id: string;
  fold_name: string;
  train_period: string;
  validation_period: string;
  oos_period: string;
  oos_observations: number;
  winning_algorithm: string;
  rank_ic: number;
  directional_accuracy_pct: number;
  annualized_net_return_pct: number;
  max_drawdown_pct: number;
}

export interface ModelBenchmarkItem {
  name: string;
  type: string;
  rank_ic: number;
  directional_accuracy_pct: number;
  sharpe_net: number;
  max_dd_pct: number;
  status: string;
}

export interface WalkForwardEvaluationData {
  run_name: string;
  mode: string;
  validation_method: string;
  total_folds: number;
  completed_folds: number;
  train_window_desc: string;
  purge_window_days: number;
  embargo_window_days: number;
  validation_window_desc: string;
  oos_window_desc: string;
  total_oos_observations: number;
  aggregate_metrics: {
    mean_oos_rank_ic: number;
    mean_directional_accuracy_pct: number;
    net_sharpe_ratio_15bps: number;
    max_drawdown_pct: number;
    annual_turnover: number;
  };
  folds: WalkForwardFoldDetail[];
  model_comparison_benchmark: ModelBenchmarkItem[];
}

export interface ModelWalkForwardResponse {
  model_id: string;
  walk_forward: WalkForwardEvaluationData;
}

export interface ModelDetailResponse {
  model_id: string;
  model_name: string;
  status: string;
  model_type: string;
  version: string;
  model_hash: string;
  dataset_id: string;
  dataset_hash: string;
  training_window: string;
  training_observations: number;
  universe_size: number;
  exchange: string;
  hyperparameters: Record<string, any>;
  intercept: number;
  feature_version: string;
  feature_order: string[];
  target_definition: string;
  target_horizon: string;
  validation_method: string;
  calibration_status: string;
  ood_gate: string;
  model_limitations: string;
  integrity_checks: ModelIntegrityChecks;
  locked_holdout_evaluation: LockedHoldoutEvaluation;
  walk_forward_evaluation: WalkForwardEvaluationData;
}

export interface ModelInferenceResponse {
  symbol: string;
  model_id: string;
  observation_timestamp: string;
  provider: string;
  data_freshness: string;
  current_price: number;
  raw_features: Record<string, number>;
  standardized_features: Record<string, number>;
  is_out_of_distribution: boolean;
  max_abs_z_score: number;
  extreme_features: string[];
  predicted_return_t20: number;
  predicted_return_t20_pct: number;
  decision: string;
  calibration_status: string;
  provenance_hash: string;
}
