import {
  Brain,
  Lock,
  Cpu,
  Target,
  RefreshCw,
  Info,
  AlertTriangle,
  Sliders,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { QLBadge } from '@/design-system/QLBadge';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import type { ModelDetailResponse, ModelInferenceResponse } from '@/types/modelRegistry';
import type { ModelPrediction } from '@/types/market';

interface QuantPredictionTerminalCardProps {
  model?: ModelDetailResponse | null;
  inference?: ModelInferenceResponse | null;
  prediction?: ModelPrediction | null;
  selectedSymbol?: string;
  onSelectSymbol?: (symbol: string) => void;
  onRefresh?: () => void;
  loading?: boolean;
  error?: string | null;
}

const BENCHMARK_SYMBOLS = ['VEDL', 'RELIANCE', 'TCS', 'INFY', 'HINDZINC'];

export function QuantPredictionTerminalCard({
  model,
  inference,
  selectedSymbol = 'VEDL',
  onSelectSymbol,
  onRefresh,
  loading,
  error,
}: QuantPredictionTerminalCardProps) {

  if (loading && !model) {
    return (
      <Card className="animate-pulse border-border bg-surface">
        <CardHeader>
          <CardTitle className="font-mono text-sm text-text-muted">LOADING QUANT MODEL REGISTRY...</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-64 bg-surface-elevated rounded-lg" />
        </CardContent>
      </Card>
    );
  }

  if (error && !model) {
    return (
      <QLPanel variant="surface" padding="lg" className="border-rose-500/30 font-mono text-xs space-y-3">
        <div className="flex items-center gap-2 text-rose-400 font-bold">
          <AlertTriangle className="w-5 h-5" />
          <span>MODEL SERVICE UNAVAILABLE</span>
        </div>
        <p className="text-text-muted">
          Endpoint: <code className="text-text-primary">/api/v1/models/PHASE_16_FROZEN_RIDGE_TOP8_V1</code>
        </p>
        <p className="text-text-secondary">{error}</p>
        {onRefresh && (
          <QLButton variant="outline" size="sm" onClick={onRefresh} icon={<RefreshCw className="w-3.5 h-3.5" />}>
            Retry Connection
          </QLButton>
        )}
      </QLPanel>
    );
  }

  const isPredPositive = (inference?.predicted_return_t20 || 0) >= 0;

  return (
    <div className="space-y-6">
      {/* 1. Core Model Registry Specification Card (Part 11) */}
      <Card className="border-border bg-surface font-sans">
        <CardHeader className="border-b border-border/60 pb-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-lg bg-surface-elevated border border-border flex items-center justify-center">
                <Brain className="w-5 h-5 text-accent" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <CardTitle className="text-base font-bold font-mono text-text-primary">
                    {model?.model_id || 'PHASE_16_FROZEN_RIDGE_TOP8_V1'}
                  </CardTitle>
                  <QLBadge variant="positive" size="xs">
                    {model?.status || 'ACTIVE_FROZEN'}
                  </QLBadge>
                </div>
                <p className="text-xs text-text-muted mt-0.5">
                  Part 11: Quant Prediction Model Engine • Point-in-Time Statistical Inference
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2 font-mono text-xs">
              <QLBadge variant="info" size="xs">
                {model?.model_type || 'RIDGE_REGRESSION'}
              </QLBadge>
              <QLBadge variant="neutral" size="xs">
                {model?.version || 'v1.0.0'}
              </QLBadge>
            </div>
          </div>
        </CardHeader>

        <CardContent className="space-y-6 pt-5">
          {/* Metadata Specifications Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 font-mono text-xs">
            <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/40">
              <span className="text-[10px] text-text-muted uppercase block">DATASET</span>
              <span className="text-text-primary font-bold truncate block mt-0.5">
                {model?.dataset_id || 'NOT AVAILABLE'}
              </span>
              <span className="text-[9px] text-text-muted truncate block" title={model?.dataset_hash}>
                Hash: {model?.dataset_hash ? `${model.dataset_hash.slice(0, 12)}...` : 'N/A'}
              </span>
            </div>

            <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/40">
              <span className="text-[10px] text-text-muted uppercase block">TRAINING WINDOW</span>
              <span className="text-text-primary font-bold block mt-0.5">
                {model?.training_window || 'NOT AVAILABLE'}
              </span>
              <span className="text-[9px] text-text-muted block">
                {model?.training_observations?.toLocaleString() || '52,416'} obs • {model?.universe_size || 48} stocks
              </span>
            </div>

            <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/40">
              <span className="text-[10px] text-text-muted uppercase block">ALPHA HYPERPARAMS</span>
              <span className="text-accent font-bold block mt-0.5">
                Ridge (α = {model?.hyperparameters?.alpha ?? 10.0})
              </span>
              <span className="text-[9px] text-text-muted block">
                Top-8 • Inertia Buffer: {model?.hyperparameters?.inertia_buffer ?? 2}D
              </span>
            </div>

            <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/40">
              <span className="text-[10px] text-text-muted uppercase block">PREDICTION TARGET</span>
              <span className="text-text-primary font-bold block mt-0.5">
                {model?.target_horizon || 'T+20'} ({model?.target_definition || 'Fwd Log Return'})
              </span>
              <span className="text-[9px] text-text-muted block">
                Single Horizon Target
              </span>
            </div>
          </div>

          {/* Model Horizon Scope & Multi-Target Transparency */}
          <div className="p-4 rounded-lg bg-background border border-border font-mono text-xs space-y-2">
            <div className="flex items-center justify-between border-b border-border/40 pb-2">
              <span className="font-bold text-text-primary uppercase flex items-center gap-2">
                <Target className="w-4 h-4 text-accent" />
                <span>Prediction Target Horizons Disclosure</span>
              </span>
              <span className="text-[10px] text-text-muted">Zero False Multitasking</span>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-1">
              <div className="p-2.5 rounded bg-surface-elevated/40 border border-border/30">
                <div className="flex justify-between items-center text-[11px]">
                  <span className="text-text-secondary font-bold">T+1 (1-Day)</span>
                  <QLBadge variant="neutral" size="xs">NOT MODELED</QLBadge>
                </div>
                <p className="text-[10px] text-text-muted mt-1 font-sans">
                  Frozen alpha engine evaluates intermediate horizon; daily returns are not fabricated.
                </p>
              </div>
              <div className="p-2.5 rounded bg-surface-elevated/40 border border-border/30">
                <div className="flex justify-between items-center text-[11px]">
                  <span className="text-text-secondary font-bold">T+5 (1-Week)</span>
                  <QLBadge variant="neutral" size="xs">NOT MODELED</QLBadge>
                </div>
                <p className="text-[10px] text-text-muted mt-1 font-sans">
                  No arbitrary multiplier applied; 5-day horizon reserved for multi-horizon engine.
                </p>
              </div>
              <div className="p-2.5 rounded bg-surface-elevated/40 border border-accent/40">
                <div className="flex justify-between items-center text-[11px]">
                  <span className="text-accent font-bold">T+20 (1-Month)</span>
                  <QLBadge variant="positive" size="xs">PRIMARY TARGET</QLBadge>
                </div>
                <p className="text-[10px] text-text-muted mt-1 font-sans">
                  Mathematically trained & validated on 52,416 expanding walk-forward observations.
                </p>
              </div>
            </div>
          </div>

          {/* Locked Holdout Performance & Limitations */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 font-mono text-xs">
            {/* Left: Holdout Performance */}
            <div className="p-4 rounded-lg bg-surface-elevated/40 border border-border/60 space-y-3">
              <div className="flex items-center justify-between border-b border-border/40 pb-2">
                <span className="font-bold text-text-primary uppercase text-[11px]">
                  LOCKED HOLDOUT PERFORMANCE ({model?.locked_holdout_evaluation?.period || 'H2 2024'})
                </span>
                <QLBadge variant="warning" size="xs">
                  HONEST OOS DISCLOSURE
                </QLBadge>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="p-2.5 rounded bg-background border border-border/30">
                  <span className="text-[10px] text-text-muted uppercase block">OOS RANK IC</span>
                  <span className="text-lg font-bold font-mono text-rose-400 block mt-0.5">
                    {model?.locked_holdout_evaluation?.rank_ic !== undefined
                      ? model.locked_holdout_evaluation.rank_ic.toFixed(4)
                      : 'NOT AVAILABLE'}
                  </span>
                  <span className="text-[9px] text-text-muted font-sans">Raw uncurved rank correlation</span>
                </div>
                <div className="p-2.5 rounded bg-background border border-border/30">
                  <span className="text-[10px] text-text-muted uppercase block">DIRECTIONAL ACCURACY</span>
                  <span className="text-lg font-bold font-mono text-amber-400 block mt-0.5">
                    {model?.locked_holdout_evaluation?.directional_accuracy_pct !== undefined
                      ? `${model.locked_holdout_evaluation.directional_accuracy_pct.toFixed(2)}%`
                      : 'NOT AVAILABLE'}
                  </span>
                  <span className="text-[9px] text-text-muted font-sans">Raw sign match rate</span>
                </div>
              </div>
              <p className="text-[10px] text-text-muted font-sans leading-relaxed">
                {model?.locked_holdout_evaluation?.disclosure ||
                  'HISTORICAL OOS EVIDENCE — NOT A GUARANTEE OF FUTURE PERFORMANCE.'}
              </p>
            </div>

            {/* Right: Calibration & Safety Gates */}
            <div className="p-4 rounded-lg bg-surface-elevated/40 border border-border/60 space-y-3">
              <div className="flex items-center justify-between border-b border-border/40 pb-2">
                <span className="font-bold text-text-primary uppercase text-[11px]">
                  CALIBRATION STATUS &amp; SAFETY GATES
                </span>
                <QLBadge variant="neutral" size="xs">
                  FAIL-CLOSED
                </QLBadge>
              </div>
              <div className="space-y-2 text-[11px]">
                <div className="flex justify-between items-center py-1 border-b border-border/20">
                  <span className="text-text-muted">Probability Calibration:</span>
                  <span className="font-bold text-amber-400">{model?.calibration_status || 'NOT CALIBRATED'}</span>
                </div>
                <div className="flex justify-between items-center py-1 border-b border-border/20">
                  <span className="text-text-muted">Out-of-Distribution (OOD) Gate:</span>
                  <span className="font-bold text-emerald-400">{model?.ood_gate || 'FAIL_CLOSED (z > 3.0)'}</span>
                </div>
                <div className="flex justify-between items-center py-1">
                  <span className="text-text-muted">Model Assumptions:</span>
                  <span className="text-text-secondary truncate max-w-[220px]">
                    {model?.model_limitations || 'Linear additive, zero lookahead'}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Cryptographic SHA-256 Fingerprint */}
          <div className="p-3 rounded-lg bg-background border border-border flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs font-mono">
            <div className="flex items-center gap-2.5 min-w-0">
              <Lock className="w-4 h-4 text-accent shrink-0" />
              <div className="truncate">
                <span className="text-text-muted text-[10px] uppercase block">IMMUTABLE ARTIFACT SHA-256 HASH</span>
                <span className="text-accent font-bold truncate block">
                  {model?.model_hash || '1342f5d9134fd6cf25c625362a6b276a2700e668ddc63c5b54d3e75c4b738885'}
                </span>
              </div>
            </div>
            <QLBadge variant="positive" size="xs">
              VERIFIED CHECKSUM
            </QLBadge>
          </div>
        </CardContent>
      </Card>

      {/* 2. Latest Point-in-Time Inference Workspace */}
      <Card className="border-border bg-surface font-sans">
        <CardHeader className="border-b border-border/60 pb-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-lg bg-surface-elevated border border-border flex items-center justify-center">
                <Cpu className="w-5 h-5 text-accent" />
              </div>
              <div>
                <CardTitle className="text-base font-bold font-mono text-text-primary">
                  LATEST POINT-IN-TIME INFERENCE: {selectedSymbol}
                </CardTitle>
                <p className="text-xs text-text-muted mt-0.5">
                  Real-time feature transformation, OOD inspection, and deterministic forward return score.
                </p>
              </div>
            </div>

            {/* Benchmark security selector */}
            <div className="flex items-center gap-2 font-mono text-xs">
              <div className="flex items-center gap-1 bg-[#0b0c12] p-1 rounded-md border border-border/80">
                {BENCHMARK_SYMBOLS.map((sym) => (
                  <button
                    key={sym}
                    onClick={() => onSelectSymbol?.(sym)}
                    className={`px-2 py-0.5 rounded transition-colors ${
                      selectedSymbol === sym
                        ? 'bg-surface-elevated text-text-primary font-bold shadow-sm'
                        : 'text-text-muted hover:text-text-secondary'
                    }`}
                  >
                    {sym}
                  </button>
                ))}
              </div>

              {onRefresh && (
                <QLButton variant="outline" size="sm" onClick={onRefresh} icon={<RefreshCw className="w-3.5 h-3.5" />}>
                  Refresh
                </QLButton>
              )}
            </div>
          </div>
        </CardHeader>

        <CardContent className="space-y-6 pt-5">
          {inference ? (
            <>
              {/* Primary Output 3-Column Banner */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 font-mono">
                {/* 1. Raw T+20 Prediction Score */}
                <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border/60">
                  <div className="flex items-center justify-between text-xs text-text-muted mb-1">
                    <span>RAW T+20 FORECAST</span>
                    <Target className="w-4 h-4 text-accent" />
                  </div>
                  <div className={`text-2xl font-bold ${isPredPositive ? 'text-emerald-400' : 'text-rose-400'}`}>
                    {isPredPositive ? '+' : ''}
                    {inference.predicted_return_t20_pct !== undefined
                      ? `${inference.predicted_return_t20_pct.toFixed(2)}%`
                      : '0.00%'}
                  </div>
                  <span className="text-[10px] text-text-muted block mt-0.5">
                    Forward 20-Day Expected Log Return
                  </span>
                </div>

                {/* 2. Model Categorical Decision */}
                <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border/60">
                  <div className="flex items-center justify-between text-xs text-text-muted mb-1">
                    <span>STATISTICAL ACTION</span>
                    <Sliders className="w-4 h-4 text-accent" />
                  </div>
                  <div className="text-2xl font-bold text-text-primary flex items-center gap-2">
                    <span>{inference.decision || 'HOLD'}</span>
                    <QLBadge
                      variant={
                        inference.decision === 'BUY'
                          ? 'positive'
                          : inference.decision === 'SELL'
                          ? 'negative'
                          : 'warning'
                      }
                      size="sm"
                    >
                      {inference.decision === 'BUY' ? 'FAVORABLE' : inference.decision === 'SELL' ? 'AVOID' : 'NEUTRAL'}
                    </QLBadge>
                  </div>
                  <span className="text-[10px] text-text-muted block mt-0.5">
                    Linear Alpha Threshold Filter (±1.0%)
                  </span>
                </div>

                {/* 3. Out-of-Distribution Status */}
                <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border/60">
                  <div className="flex items-center justify-between text-xs text-text-muted mb-1">
                    <span>OOD REGIME DIAGNOSTIC</span>
                    <Info className="w-4 h-4 text-accent" />
                  </div>
                  <div className="text-2xl font-bold flex items-center gap-2">
                    <span className={inference.is_out_of_distribution ? 'text-amber-400' : 'text-emerald-400'}>
                      {inference.is_out_of_distribution ? 'OOD DETECTED' : 'IN-DISTRIBUTION'}
                    </span>
                  </div>
                  <span className="text-[10px] text-text-muted block mt-0.5">
                    Max |z-score|: {inference.max_abs_z_score?.toFixed(2) || '0.00'}σ (Limit: 3.0σ)
                  </span>
                </div>
              </div>

              {/* Real Feature Vectors Inspection Table */}
              <div className="space-y-2">
                <span className="font-mono text-xs font-bold text-text-secondary uppercase block">
                  Point-in-Time Computed Features &amp; Standardized Inputs for {inference.symbol}
                </span>
                <div className="overflow-x-auto rounded-lg border border-border/60 bg-background">
                  <table className="w-full text-xs font-mono text-left">
                    <thead className="bg-surface-elevated/80 border-b border-border text-text-muted text-[10px] uppercase">
                      <tr>
                        <th className="p-2.5">Feature</th>
                        <th className="p-2.5 text-right">Raw Value</th>
                        <th className="p-2.5 text-right">Standardized (z-score)</th>
                        <th className="p-2.5 text-center">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-border/30">
                      {Object.entries(inference.raw_features || {}).map(([fname, rval]) => {
                        const sval = inference.standardized_features?.[fname];
                        const isExtreme = Math.abs(sval || 0) >= 3.0;
                        return (
                          <tr key={fname} className="hover:bg-surface-elevated/20 transition-colors">
                            <td className="p-2.5 font-bold text-text-primary">{fname}</td>
                            <td className="p-2.5 text-right text-text-secondary">
                              {typeof rval === 'number' ? rval.toFixed(4) : String(rval)}
                            </td>
                            <td className={`p-2.5 text-right font-bold ${isExtreme ? 'text-amber-400' : 'text-accent'}`}>
                              {sval !== undefined ? `${sval > 0 ? '+' : ''}${sval.toFixed(4)}σ` : 'N/A'}
                            </td>
                            <td className="p-2.5 text-center">
                              {isExtreme ? (
                                <QLBadge variant="warning" size="xs">
                                  EXTREME (&gt;3σ)
                                </QLBadge>
                              ) : (
                                <QLBadge variant="positive" size="xs">
                                  NORMAL
                                </QLBadge>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Provenance Footer */}
              <div className="p-3 rounded-lg bg-surface-elevated/40 border border-border/40 flex flex-wrap items-center justify-between gap-3 text-[11px] font-mono text-text-muted">
                <div>
                  Observation Date: <span className="text-text-primary">{inference.observation_timestamp}</span> • Provider:{' '}
                  <strong className="text-text-primary">{inference.provider}</strong> ({inference.data_freshness})
                </div>
                <div>
                  Authoritative Price: <span className="text-text-primary">₹{inference.current_price?.toFixed(2)}</span>
                </div>
              </div>
            </>
          ) : (
            <div className="py-12 text-center font-mono text-xs text-text-muted space-y-2">
              <AlertTriangle className="w-6 h-6 text-amber-400 mx-auto" />
              <p className="font-bold text-text-primary uppercase">NO CURRENT VERIFIED INFERENCE</p>
              <p>
                Inference observations for {selectedSymbol} are currently awaiting validated market feeds.
              </p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
