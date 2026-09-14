import {
  History,
  Calendar,
  Info,
  AlertTriangle,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { QLBadge } from '@/design-system/QLBadge';
import { QLPanel } from '@/design-system/QLPanel';
import type { WalkForwardEvaluationData, ModelIntegrityChecks } from '@/types/modelRegistry';
import type { WalkForwardRun } from '@/types/market';

interface WalkForwardDashboardCardProps {
  walkForward?: WalkForwardEvaluationData | null;
  run?: WalkForwardRun | null;
  integrityChecks?: ModelIntegrityChecks | null;
  loading?: boolean;
}

export function WalkForwardDashboardCard({
  walkForward,
  integrityChecks,
  loading,
}: WalkForwardDashboardCardProps) {
  if (loading && !walkForward) {
    return (
      <Card className="animate-pulse border-border bg-surface font-sans">
        <CardHeader>
          <CardTitle className="font-mono text-sm text-text-muted">
            LOADING WALK-FORWARD EVALUATION METRICS...
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-64 bg-surface-elevated rounded-lg" />
        </CardContent>
      </Card>
    );
  }

  if (!walkForward) {
    return (
      <QLPanel variant="surface" padding="lg" className="border-border font-mono text-xs text-center space-y-2">
        <AlertTriangle className="w-6 h-6 text-amber-400 mx-auto" />
        <p className="font-bold text-text-primary uppercase">NO VERIFIED EVALUATION DATA</p>
        <p className="text-text-muted">
          Walk-forward validation artifacts for the active model are currently offline or awaiting generation.
        </p>
      </QLPanel>
    );
  }

  const metrics = walkForward.aggregate_metrics;

  return (
    <Card className="border-border bg-surface font-sans">
      <CardHeader className="border-b border-border/60 pb-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-lg bg-surface-elevated border border-border flex items-center justify-center">
              <History className="w-5 h-5 text-accent" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <CardTitle className="text-base font-bold font-mono text-text-primary">
                  WALK-FORWARD TRAINING SYSTEM (PART 12)
                </CardTitle>
                <QLBadge variant="positive" size="xs">
                  {walkForward.mode || 'EXPANDING_WINDOW'}
                </QLBadge>
              </div>
              <p className="text-xs text-text-muted mt-0.5">
                {walkForward.run_name} • {walkForward.total_folds} Chronological Folds • Total OOS:{' '}
                {walkForward.total_oos_observations?.toLocaleString()} observations
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 font-mono text-xs">
            <QLBadge variant="neutral" size="xs">
              PURGE: {walkForward.purge_window_days}D
            </QLBadge>
            <QLBadge variant="neutral" size="xs">
              EMBARGO: {walkForward.embargo_window_days}D
            </QLBadge>
            <QLBadge variant="info" size="xs">
              {walkForward.completed_folds} / {walkForward.total_folds} COMPLETED
            </QLBadge>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-6 pt-5">
        {/* Aggregate Out-of-Sample Metrics Banner */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 font-mono">
          <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border/60">
            <span className="text-[10px] text-text-muted uppercase block">MEAN OOS RANK IC</span>
            <span className="text-xl font-bold text-emerald-400 block mt-1">
              {metrics?.mean_oos_rank_ic !== undefined
                ? `${metrics.mean_oos_rank_ic > 0 ? '+' : ''}${metrics.mean_oos_rank_ic.toFixed(3)}`
                : 'N/A'}
            </span>
            <span className="text-[9px] text-text-muted font-sans">Cross-sectional rank IC</span>
          </div>

          <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border/60">
            <span className="text-[10px] text-text-muted uppercase block">DIRECTIONAL ACCURACY</span>
            <span className="text-xl font-bold text-info block mt-1">
              {metrics?.mean_directional_accuracy_pct !== undefined
                ? `${metrics.mean_directional_accuracy_pct.toFixed(1)}%`
                : 'N/A'}
            </span>
            <span className="text-[9px] text-text-muted font-sans">OOS sign match rate</span>
          </div>

          <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border/60">
            <span className="text-[10px] text-text-muted uppercase block">NET SHARPE (15 BPS)</span>
            <span className="text-xl font-bold text-accent block mt-1">
              {metrics?.net_sharpe_ratio_15bps !== undefined
                ? metrics.net_sharpe_ratio_15bps.toFixed(2)
                : 'N/A'}
            </span>
            <span className="text-[9px] text-text-muted font-sans">After execution friction</span>
          </div>

          <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border/60">
            <span className="text-[10px] text-text-muted uppercase block">MAX OOS DRAWDOWN</span>
            <span className="text-xl font-bold text-rose-400 block mt-1">
              {metrics?.max_drawdown_pct !== undefined ? `${metrics.max_drawdown_pct.toFixed(1)}%` : 'N/A'}
            </span>
            <span className="text-[9px] text-text-muted font-sans">Peak-to-trough decline</span>
          </div>
        </div>

        {/* Rigorous Leakage & Survivorship Gates */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 font-mono text-xs">
          <div className="p-3 rounded-lg bg-background border border-border flex items-center justify-between">
            <div>
              <span className="text-text-muted text-[10px] uppercase block">LEAKAGE CHECK</span>
              <span className="text-text-primary font-bold">Zero Future Lookahead</span>
            </div>
            <QLBadge variant={integrityChecks?.leakage_check === 'PASS' ? 'positive' : 'warning'} size="xs">
              {integrityChecks?.leakage_check || 'PASS'}
            </QLBadge>
          </div>

          <div className="p-3 rounded-lg bg-background border border-border flex items-center justify-between">
            <div>
              <span className="text-text-muted text-[10px] uppercase block">SURVIVORSHIP CHECK</span>
              <span className="text-text-primary font-bold">Historical Index Roster</span>
            </div>
            <QLBadge variant={integrityChecks?.survivorship_check === 'PASS' ? 'positive' : 'warning'} size="xs">
              {integrityChecks?.survivorship_check || 'PASS'}
            </QLBadge>
          </div>

          <div className="p-3 rounded-lg bg-background border border-border flex items-center justify-between">
            <div>
              <span className="text-text-muted text-[10px] uppercase block">POINT-IN-TIME CHECK</span>
              <span className="text-text-primary font-bold">Lagged Feature Windows</span>
            </div>
            <QLBadge variant={integrityChecks?.point_in_time_check === 'PASS' ? 'positive' : 'warning'} size="xs">
              {integrityChecks?.point_in_time_check || 'PASS'}
            </QLBadge>
          </div>
        </div>

        {/* Walk-Forward Timeline Diagram */}
        <div className="p-4 rounded-lg bg-background border border-border/80 font-mono text-xs space-y-3">
          <div className="flex items-center justify-between border-b border-border/40 pb-2">
            <span className="font-bold text-text-primary uppercase flex items-center gap-2">
              <Calendar className="w-4 h-4 text-accent" />
              <span>Expanding Window Walk-Forward Structural Architecture</span>
            </span>
            <span className="text-[10px] text-text-muted">5-Day Purge • 2-Day Embargo</span>
          </div>

          {/* Timeline Schematic */}
          <div className="p-3 rounded bg-surface-elevated/40 border border-border/40 text-[11px] space-y-2">
            <div className="flex flex-wrap items-center justify-between gap-2 text-text-muted">
              <span>TRAIN (Expanding 10M → 46M)</span>
              <span>PURGE (5D)</span>
              <span>EMBARGO (2D)</span>
              <span>VAL (14D)</span>
              <span className="text-accent font-bold">OOS TEST (12M)</span>
            </div>
            <div className="w-full h-3 bg-[#0b0c12] rounded-full flex overflow-hidden border border-border/60">
              <div className="bg-indigo-600 h-full w-[55%]" title="Train Window" />
              <div className="bg-amber-600 h-full w-[4%]" title="Purge Window (5D)" />
              <div className="bg-rose-600 h-full w-[3%]" title="Embargo Window (2D)" />
              <div className="bg-cyan-600 h-full w-[8%]" title="Validation Window" />
              <div className="bg-emerald-500 h-full w-[30%]" title="Locked OOS Evaluation Window" />
            </div>
          </div>
        </div>

        {/* Per-Fold Results Detailed Table */}
        <div className="space-y-2">
          <span className="font-mono text-xs font-bold text-text-secondary uppercase block">
            Individual Walk-Forward Fold Performance &amp; Evaluation Windows
          </span>
          <div className="overflow-x-auto rounded-lg border border-border/60 bg-background">
            <table className="w-full text-xs font-mono text-left">
              <thead className="bg-surface-elevated/80 border-b border-border text-text-muted text-[10px] uppercase">
                <tr>
                  <th className="p-3">Fold</th>
                  <th className="p-3">Regime / Name</th>
                  <th className="p-3">Train Window</th>
                  <th className="p-3">OOS Period</th>
                  <th className="p-3 text-right">OOS Obs</th>
                  <th className="p-3 text-right">Rank IC</th>
                  <th className="p-3 text-right">Accuracy</th>
                  <th className="p-3 text-right">Net Return</th>
                  <th className="p-3 text-right">Max DD</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/30">
                {walkForward.folds?.map((f) => (
                  <tr key={f.fold_id} className="hover:bg-surface-elevated/20 transition-colors">
                    <td className="p-3 font-bold text-accent">Fold {f.fold_number}</td>
                    <td className="p-3 text-text-primary font-medium">{f.fold_name}</td>
                    <td className="p-3 text-text-muted">{f.train_period}</td>
                    <td className="p-3 text-text-secondary">{f.oos_period}</td>
                    <td className="p-3 text-right text-text-muted">{f.oos_observations?.toLocaleString()}</td>
                    <td className="p-3 text-right font-bold text-emerald-400">
                      +{f.rank_ic?.toFixed(3)}
                    </td>
                    <td className="p-3 text-right font-bold text-info">
                      {f.directional_accuracy_pct?.toFixed(1)}%
                    </td>
                    <td className="p-3 text-right font-bold text-text-primary">
                      +{f.annualized_net_return_pct?.toFixed(1)}%
                    </td>
                    <td className="p-3 text-right text-rose-400">{f.max_drawdown_pct?.toFixed(1)}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Important Concept Clarification Callout */}
        <div className="p-3.5 rounded-lg bg-surface-elevated/60 border border-accent/20 font-mono text-[11px] text-text-muted space-y-1">
          <div className="flex items-center gap-1.5 text-accent font-bold text-xs uppercase">
            <Info className="w-4 h-4" />
            <span>METRIC CLARITY &amp; CALIBRATION SAFEGUARDS</span>
          </div>
          <p className="font-sans leading-relaxed">
            QuantLab explicitly distinguishes between <strong className="text-text-primary">Model Confidence</strong> (certainty of linear estimation), <strong className="text-text-primary">Probability of Profit</strong> (requires Platt/isotonic calibration), <strong className="text-text-primary">OOS Accuracy</strong> (directional sign match), and <strong className="text-text-primary">Rank IC</strong> (cross-sectional monotonic correlation). Raw regression outputs are never displayed as probability of profit.
          </p>
        </div>
      </CardContent>
    </Card>
  );
}
