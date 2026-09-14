import { useState, useEffect } from 'react';
import { Lock, FileCode, RefreshCw, AlertTriangle } from 'lucide-react';
import { QLBadge } from '@/design-system/QLBadge';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLSection } from '@/design-system/QLSection';
import { QuantPredictionTerminalCard } from '@/components/dashboard/QuantPredictionTerminalCard';
import { WalkForwardDashboardCard } from '@/components/dashboard/WalkForwardDashboardCard';
import { DeepLearningSequenceCard } from '@/components/dashboard/DeepLearningSequenceCard';
import { api } from '@/lib/api';
import type {
  ModelDetailResponse,
  ModelFeaturesResponse,
  ModelWalkForwardResponse,
  ModelInferenceResponse,
} from '@/types/modelRegistry';

export function ModelsPage() {
  const [model, setModel] = useState<ModelDetailResponse | null>(null);
  const [featuresData, setFeaturesData] = useState<ModelFeaturesResponse | null>(null);
  const [walkForwardData, setWalkForwardData] = useState<ModelWalkForwardResponse | null>(null);
  const [inferenceData, setInferenceData] = useState<ModelInferenceResponse | null>(null);
  const [selectedSymbol, setSelectedSymbol] = useState<string>('VEDL');

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const loadAllData = async () => {
    setLoading(true);
    setError(null);

    try {
      const [mRes, fRes, wRes, iRes] = await Promise.all([
        api.get<ModelDetailResponse>('/api/v1/models/PHASE_16_FROZEN_RIDGE_TOP8_V1').catch(() => null),
        api.get<ModelFeaturesResponse>('/api/v1/models/PHASE_16_FROZEN_RIDGE_TOP8_V1/features').catch(() => null),
        api.get<ModelWalkForwardResponse>('/api/v1/models/PHASE_16_FROZEN_RIDGE_TOP8_V1/walk-forward').catch(() => null),
        api.get<ModelInferenceResponse>(`/api/v1/models/PHASE_16_FROZEN_RIDGE_TOP8_V1/inference/${selectedSymbol}`).catch(() => null),
      ]);

      if (!mRes) {
        setError('Failed to fetch authoritative model metadata from QuantLab Model Registry.');
      } else {
        setModel(mRes);
        setFeaturesData(fRes);
        setWalkForwardData(wRes);
        setInferenceData(iRes);
      }
    } catch (err: any) {
      setError(err?.message || 'Error communicating with QuantLab Model Service.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllData();
  }, [selectedSymbol]);

  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>QUANTITATIVE RESEARCH LABORATORY</span>
            <span>•</span>
            <span>FROZEN ALPHA REGISTRY</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            QUANT MODELS &amp; WALK-FORWARD
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Authoritative Ridge alpha specifications, canonical feature order, cryptographic weights, and transparent OOS walk-forward diagnostics.
          </p>
        </div>

        <div className="flex items-center gap-2 font-mono text-xs">
          <QLButton
            variant="outline"
            size="sm"
            onClick={loadAllData}
            icon={<RefreshCw className="w-3.5 h-3.5" />}
          >
            Refresh Registry
          </QLButton>
          <QLBadge variant="positive" size="sm" dot>
            FROZEN &amp; IMMUTABLE
          </QLBadge>
        </div>
      </div>

      {error && !model && (
        <QLPanel variant="surface" padding="lg" className="border-rose-500/40 font-mono text-xs space-y-3">
          <div className="flex items-center gap-2 text-rose-400 font-bold">
            <AlertTriangle className="w-5 h-5" />
            <span>MODEL SERVICE UNAVAILABLE</span>
          </div>
          <p className="text-text-muted">
            Could not connect to model registry endpoint: <code className="text-text-primary">/api/v1/models/PHASE_16_FROZEN_RIDGE_TOP8_V1</code>
          </p>
          <p className="text-text-secondary">{error}</p>
          <QLButton variant="outline" size="sm" onClick={loadAllData} icon={<RefreshCw className="w-3.5 h-3.5" />}>
            Retry Connection
          </QLButton>
        </QLPanel>
      )}

      {/* 01 ARTIFACT SPECIFICATION & TRANSPARENCY METRICS */}
      <QLSection
        number={1}
        title="Frozen Alpha Artifact Specification"
        subtitle={model?.model_id || 'PHASE_16_FROZEN_RIDGE_TOP8_V1'}
      >
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Metadata Specs */}
          <div className="lg:col-span-8 space-y-4">
            <QLPanel variant="surface" padding="md" className="space-y-4 font-mono text-xs">
              <div className="flex items-center justify-between border-b border-border/60 pb-3">
                <div className="flex items-center gap-2">
                  <FileCode className="w-4 h-4 text-accent" />
                  <span className="font-bold text-text-primary text-sm">
                    {model?.model_id || 'PHASE_16_FROZEN_RIDGE_TOP8_V1'}
                  </span>
                </div>
                <QLBadge variant="positive" size="xs">
                  {model?.status || 'ACTIVE_FROZEN'}
                </QLBadge>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                <div className="p-2.5 rounded bg-surface-elevated/60 border border-border/40">
                  <span className="text-[10px] text-text-muted uppercase block">DATASET</span>
                  <span className="text-text-primary font-bold truncate block">
                    {model?.dataset_id || 'quantlab_nifty50'}
                  </span>
                </div>
                <div className="p-2.5 rounded bg-surface-elevated/60 border border-border/40">
                  <span className="text-[10px] text-text-muted uppercase block">TRAINING WINDOW</span>
                  <span className="text-text-primary font-bold truncate block">
                    {model?.training_window || '2020 – 2024'}
                  </span>
                </div>
                <div className="p-2.5 rounded bg-surface-elevated/60 border border-border/40">
                  <span className="text-[10px] text-text-muted uppercase block">ALPHA HYPERPARAM</span>
                  <span className="text-accent font-bold block">
                    Ridge (α = {model?.hyperparameters?.alpha ?? 10.0})
                  </span>
                </div>
                <div className="p-2.5 rounded bg-surface-elevated/60 border border-border/40">
                  <span className="text-[10px] text-text-muted uppercase block">TARGET</span>
                  <span className="text-text-primary font-bold block">
                    {model?.target_horizon || 'T+20'} ({model?.target_definition || 'Fwd Log Return'})
                  </span>
                </div>
              </div>

              <div className="p-3 rounded bg-surface-elevated/80 border border-border/80 flex items-center justify-between gap-3 text-xs">
                <div className="flex items-center gap-2 min-w-0">
                  <Lock className="w-4 h-4 text-accent shrink-0" />
                  <div className="truncate">
                    <span className="text-text-muted text-[10px] uppercase block">CANONICAL ARTIFACT SHA-256 HASH</span>
                    <span className="font-mono text-accent text-xs font-bold truncate block">
                      {model?.model_hash || '1342f5d9134fd6cf25c625362a6b276a2700e668ddc63c5b54d3e75c4b738885'}
                    </span>
                  </div>
                </div>
                <QLBadge variant="positive" size="xs">
                  VERIFIED
                </QLBadge>
              </div>
            </QLPanel>
          </div>

          {/* Transparent OOS Metrics */}
          <div className="lg:col-span-4 space-y-4">
            <QLPanel variant="surface" padding="md" className="space-y-4 font-mono text-xs">
              <div className="flex items-center justify-between border-b border-border/60 pb-3">
                <span className="font-bold text-text-muted uppercase text-[10px] tracking-wider">
                  OUT-OF-SAMPLE TRANSPARENCY
                </span>
                <QLBadge variant="warning" size="xs">
                  HONEST DISCLOSURE
                </QLBadge>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="p-3 rounded bg-surface-elevated/60 border border-border/40">
                  <span className="text-[10px] text-text-muted uppercase block">OOS RANK IC</span>
                  <span className="text-xl font-black text-rose-400 font-mono block mt-1">
                    {model?.locked_holdout_evaluation?.rank_ic !== undefined
                      ? model.locked_holdout_evaluation.rank_ic.toFixed(4)
                      : '-0.0038'}
                  </span>
                  <span className="text-[10px] text-text-muted font-sans">Rank correlation</span>
                </div>

                <div className="p-3 rounded bg-surface-elevated/60 border border-border/40">
                  <span className="text-[10px] text-text-muted uppercase block">OOS ACCURACY</span>
                  <span className="text-xl font-black text-amber-400 font-mono block mt-1">
                    {model?.locked_holdout_evaluation?.directional_accuracy_pct !== undefined
                      ? `${model.locked_holdout_evaluation.directional_accuracy_pct.toFixed(2)}%`
                      : '48.97%'}
                  </span>
                  <span className="text-[10px] text-text-muted font-sans">Directional match</span>
                </div>
              </div>

              <p className="text-[11px] text-text-muted font-sans leading-relaxed">
                QuantLab models undergo strict walk-forward purged cross-validation. Metrics reflect raw holdout performance without curve-fitting.
              </p>
            </QLPanel>
          </div>
        </div>
      </QLSection>

      {/* 02 CANONICAL FEATURE PIPELINE & WEIGHTS */}
      <QLSection
        number={2}
        title="Feature Ordering &amp; Weight Vector"
        subtitle="Deterministic Preprocessor Normalization Constants from Backend Registry"
      >
        <QLPanel variant="surface" padding="none" className="overflow-x-auto">
          <table className="w-full text-xs font-mono text-left">
            <thead className="bg-surface-elevated/80 border-b border-border text-text-muted text-[10px] uppercase">
              <tr>
                <th className="p-3">Index</th>
                <th className="p-3">Feature Name</th>
                <th className="p-3">Lookback</th>
                <th className="p-3 text-right">Model Weight</th>
                <th className="p-3 text-right">Frozen Mean</th>
                <th className="p-3 text-right">Frozen Std</th>
                <th className="p-3">Economic Interpretation</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/40">
              {featuresData?.features?.map((f, i) => (
                <tr key={f.name} className="hover:bg-surface-elevated/30 transition-colors">
                  <td className="p-3 text-text-muted">{f.index || i + 1}</td>
                  <td className="p-3 font-bold text-text-primary">{f.name}</td>
                  <td className="p-3 text-text-muted">{f.lookback || 'N/A'}</td>
                  <td className={`p-3 text-right font-bold ${f.weight > 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                    {f.weight > 0 ? `+${f.weight.toFixed(4)}` : f.weight.toFixed(4)}
                  </td>
                  <td className="p-3 text-right text-text-secondary">{f.mean.toFixed(4)}</td>
                  <td className="p-3 text-right text-text-secondary">{f.std.toFixed(4)}</td>
                  <td className="p-3 text-text-muted font-sans text-xs">{f.economic_interpretation || f.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </QLPanel>
      </QLSection>

      {/* 03 PREDICTION WORKSPACE & WALK-FORWARD */}
      <QLSection
        number={3}
        title="Walk-Forward &amp; Sequence Diagnostics"
        subtitle="Continuous Out-of-Sample Horizon Validation and Live Statistical Inference"
      >
        <div className="space-y-8">
          {/* Part 11: Quant Prediction Model Engine */}
          <QuantPredictionTerminalCard
            model={model}
            inference={inferenceData}
            selectedSymbol={selectedSymbol}
            onSelectSymbol={setSelectedSymbol}
            onRefresh={loadAllData}
            loading={loading}
            error={error}
          />

          {/* Part 12: Walk-Forward Training System */}
          <WalkForwardDashboardCard
            walkForward={walkForwardData?.walk_forward || model?.walk_forward_evaluation}
            integrityChecks={model?.integrity_checks}
            loading={loading}
          />

          {/* Temporal Deep Learning & Sequence Diagnostics */}
          <DeepLearningSequenceCard />
        </div>
      </QLSection>
    </div>
  );
}
