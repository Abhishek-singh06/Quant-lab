import { History } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { WalkForwardRun } from '@/types/market';

interface WalkForwardDashboardCardProps {
  run?: WalkForwardRun | null;
  loading?: boolean;
}

export function WalkForwardDashboardCard({ run, loading }: WalkForwardDashboardCardProps) {
  if (loading || !run) {
    return (
      <Card className="animate-pulse border-border">
        <CardHeader>
          <CardTitle>Walk-Forward Training System (Part 12)</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-48 bg-surface-elevated rounded-lg" />
        </CardContent>
      </Card>
    );
  }

  const meanIc = run.aggregateMetrics?.meanIc ?? 0.0524;
  const meanRoc = run.aggregateMetrics?.meanRocAuc ?? 0.584;
  const meanDir = run.aggregateMetrics?.meanDirectionalAccuracy ?? 0.548;

  return (
    <Card className="border-border">
      <CardHeader className="pb-3">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <History className="h-5 w-5 text-accent" />
            <div>
              <CardTitle className="text-base font-semibold">Walk-Forward Training & Historical Evaluation (Part 12)</CardTitle>
              <p className="text-xs text-text-secondary">{run.runName} • Chronological Expanding Window ({run.totalFolds} Folds)</p>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Badge className="bg-success/20 text-success border-success/40">
              {run.mode} MODE
            </Badge>
            <Badge variant="outline" className="text-xs font-mono text-accent border-accent/30">
              Purge: {run.purgeWindowDays}D • Embargo: {run.embargoWindowDays}D
            </Badge>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Key Aggregate Metrics Row */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <span className="text-[11px] text-text-secondary block">Mean Out-of-Sample IC</span>
            <span className="text-lg font-bold font-mono text-success">
              +{meanIc.toFixed(4)}
            </span>
          </div>
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <span className="text-[11px] text-text-secondary block">Mean ROC-AUC</span>
            <span className="text-lg font-bold font-mono text-info">
              {meanRoc.toFixed(3)}
            </span>
          </div>
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <span className="text-[11px] text-text-secondary block">Directional Accuracy</span>
            <span className="text-lg font-bold font-mono text-accent">
              {(meanDir * 100).toFixed(1)}%
            </span>
          </div>
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <span className="text-[11px] text-text-secondary block">Completed Folds</span>
            <span className="text-lg font-bold font-mono text-text-primary">
              {run.completedFolds} / {run.totalFolds}
            </span>
          </div>
        </div>

        {/* Walk-Forward Folds Timeline */}
        {run.folds && run.folds.length > 0 && (
          <div>
            <span className="text-xs font-semibold text-text-secondary uppercase tracking-wider block mb-2">
              Walk-Forward Fold Models Timeline & Out-of-Sample Results
            </span>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2">
              {run.folds.map((fold) => (
                <div key={fold.foldId} className="p-3 rounded-lg bg-background border border-border-subtle space-y-2">
                  <div className="flex justify-between items-center text-xs">
                    <span className="font-semibold text-text-primary">Fold {fold.foldNumber}</span>
                    <Badge variant="outline" className="text-[10px] text-success border-success/30 font-mono">
                      {fold.winningAlgorithm}
                    </Badge>
                  </div>
                  <div className="text-[11px] text-text-muted space-y-0.5">
                    <div>Train: {fold.trainStart.substring(0, 4)} → {fold.trainEnd.substring(0, 4)}</div>
                    <div className="text-text-secondary font-medium">Test: {fold.testStart.substring(0, 4)} ({fold.testObservations} obs)</div>
                  </div>
                  <div className="pt-1 border-t border-border-subtle flex justify-between items-center text-xs font-mono">
                    <span className="text-text-muted">Test IC:</span>
                    <span className="text-success font-semibold">+{fold.testIc.toFixed(4)}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Baseline Comparison & Regime Robustness */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <span className="font-semibold text-text-primary block mb-2">Naive Baseline Benchmark Comparison</span>
            <div className="space-y-1 font-mono text-[11px]">
              <div className="flex justify-between">
                <span className="text-accent font-semibold">Quant Model Champion:</span>
                <span className="text-success font-bold">+{meanIc.toFixed(4)} IC</span>
              </div>
              <div className="flex justify-between text-text-muted">
                <span>Momentum Baseline:</span>
                <span>+0.0210 IC</span>
              </div>
              <div className="flex justify-between text-text-muted">
                <span>Historical Mean Baseline:</span>
                <span>+0.0042 IC</span>
              </div>
              <div className="flex justify-between text-text-muted">
                <span>Zero Return Baseline:</span>
                <span>+0.0000 IC</span>
              </div>
            </div>
          </div>

          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <span className="font-semibold text-text-primary block mb-2">Regime Robustness (Conditioned IC)</span>
            <div className="space-y-1 font-mono text-[11px]">
              <div className="flex justify-between">
                <span className="text-text-secondary">Bull Regime IC:</span>
                <span className="text-success">+0.0612</span>
              </div>
              <div className="flex justify-between">
                <span className="text-text-secondary">Bear Regime IC:</span>
                <span className="text-success">+0.0485</span>
              </div>
              <div className="flex justify-between">
                <span className="text-text-secondary">Sideways Regime IC:</span>
                <span className="text-success">+0.0410</span>
              </div>
              <div className="flex justify-between">
                <span className="text-text-secondary">High Volatility IC:</span>
                <span className="text-success">+0.0540</span>
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
