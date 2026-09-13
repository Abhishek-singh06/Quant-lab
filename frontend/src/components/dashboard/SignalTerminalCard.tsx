import { Target, BarChart2, ShieldAlert, CheckCircle2, AlertTriangle, Layers } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { Signal } from '@/types/market';
import { cn } from '@/lib/utils';

interface SignalTerminalCardProps {
  signal?: Signal | null;
  loading?: boolean;
}

export function SignalTerminalCard({ signal, loading }: SignalTerminalCardProps) {
  if (loading || !signal) {
    return (
      <Card className="animate-pulse border-border">
        <CardHeader>
          <div className="h-6 w-1/3 bg-surface-elevated rounded"></div>
        </CardHeader>
        <CardContent>
          <div className="h-40 bg-surface-elevated rounded"></div>
        </CardContent>
      </Card>
    );
  }

  const isBuy = signal.signal === 'BUY';
  const isSell = signal.signal === 'SELL';

  const badgeVariant = isBuy ? 'success' : isSell ? 'danger' : 'warning';
  const scoreColor = signal.signalScore > 0 ? 'text-success' : signal.signalScore < 0 ? 'text-danger' : 'text-text-secondary';

  return (
    <Card className="border-border">
      <CardHeader>
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <Target className="h-5 w-5 text-accent" />
            <CardTitle>Production Cross-Check Signal Terminal</CardTitle>
            <span className="text-xs font-mono text-text-muted px-2 py-0.5 rounded bg-surface-elevated">
              {signal.symbol}
            </span>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant={badgeVariant} className="text-sm font-bold px-3 py-1">
              {signal.signal}
            </Badge>
            <Badge variant="outline" className="text-xs font-mono">
              {signal.signalVersion}
            </Badge>
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Core Metrics Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {/* Signal Score */}
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <div className="flex items-center justify-between text-xs text-text-secondary mb-1">
              <span>Signal Score</span>
              <BarChart2 className="h-3.5 w-3.5 text-accent" />
            </div>
            <div className={cn("text-xl font-bold font-mono", scoreColor)}>
              {signal.signalScore > 0 ? `+${signal.signalScore.toFixed(1)}` : signal.signalScore.toFixed(1)}
            </div>
            <span className="text-[10px] text-text-muted">Scale -100 to +100</span>
          </div>

          {/* Confidence */}
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <div className="flex items-center justify-between text-xs text-text-secondary mb-1">
              <span>Confidence</span>
              <CheckCircle2 className="h-3.5 w-3.5 text-info" />
            </div>
            <div className="text-xl font-bold font-mono text-info">
              {(signal.confidence * 100).toFixed(0)}%
            </div>
            <span className="text-[10px] text-text-muted">Evidence Agreement</span>
          </div>

          {/* Expected Return */}
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <div className="flex items-center justify-between text-xs text-text-secondary mb-1">
              <span>Expected Return E[R]</span>
              <Target className="h-3.5 w-3.5 text-success" />
            </div>
            <div className={cn("text-xl font-bold font-mono", (signal.expectedReturn || 0) >= 0 ? "text-success" : "text-danger")}>
              {signal.expectedReturn !== undefined && signal.expectedReturn !== null
                ? `${(signal.expectedReturn * 100).toFixed(2)}%`
                : "N/A"}
            </div>
            <span className="text-[10px] text-text-muted">Model 1 (1-Day)</span>
          </div>

          {/* Expected Volatility */}
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <div className="flex items-center justify-between text-xs text-text-secondary mb-1">
              <span>Expected Volatility</span>
              <AlertTriangle className="h-3.5 w-3.5 text-warning" />
            </div>
            <div className="text-xl font-bold font-mono text-warning">
              {signal.expectedVolatility !== undefined && signal.expectedVolatility !== null
                ? `${(signal.expectedVolatility * 100).toFixed(2)}%`
                : "N/A"}
            </div>
            <span className="text-[10px] text-text-muted">Model 3 (5-Day)</span>
          </div>
        </div>

        {/* Category Component Score Breakdown */}
        {signal.components && Object.keys(signal.components).length > 0 && (
          <div className="p-3 bg-surface-elevated/70 rounded-lg border border-border-subtle space-y-2">
            <div className="flex items-center justify-between text-xs font-semibold text-text-secondary">
              <span className="flex items-center gap-1.5">
                <Layers className="h-3.5 w-3.5 text-accent" />
                Cross-Layer Category Scores (-100 to +100)
              </span>
              <Badge variant={signal.conflictSeverity === 'HIGH' ? 'danger' : signal.conflictSeverity === 'MEDIUM' ? 'warning' : 'success'} className="text-[10px] py-0">
                {signal.conflictSeverity} CONFLICT ({signal.conflictScore.toFixed(0)}%)
              </Badge>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-1">
              {Object.entries(signal.components).map(([cat, comp]) => {
                const isPos = comp.categoryScore > 0;
                return (
                  <div key={cat} className="p-2 rounded bg-background/80 border border-border-subtle flex flex-col justify-between">
                    <span className="text-[10px] text-text-muted uppercase tracking-wider truncate">{cat.replace('_', ' ')}</span>
                    <span className={cn("text-xs font-bold font-mono mt-0.5", isPos ? "text-success" : comp.categoryScore < 0 ? "text-danger" : "text-text-secondary")}>
                      {comp.isPresent ? (isPos ? `+${comp.categoryScore.toFixed(0)}` : comp.categoryScore.toFixed(0)) : 'N/A'}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Supporting vs Opposing Evidence */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {/* Supporting Evidence */}
          <div className="p-3 rounded-lg border border-success/20 bg-success/5 space-y-1.5">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-success">
              <CheckCircle2 className="h-3.5 w-3.5" />
              <span>Supporting Evidence ({signal.supportingEvidence?.length || 0})</span>
            </div>
            <div className="space-y-1 max-h-32 overflow-y-auto pr-1">
              {signal.supportingEvidence && signal.supportingEvidence.length > 0 ? (
                signal.supportingEvidence.slice(0, 3).map((item, idx) => (
                  <div key={idx} className="text-xs text-text-primary/90 flex items-start gap-1.5">
                    <span className="text-success font-bold shrink-0">•</span>
                    <span>{item.reason}</span>
                  </div>
                ))
              ) : (
                <div className="text-xs text-text-muted italic">No strong supporting factors</div>
              )}
            </div>
          </div>

          {/* Opposing Evidence / Risk Factors */}
          <div className="p-3 rounded-lg border border-danger/20 bg-danger/5 space-y-1.5">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-danger">
              <ShieldAlert className="h-3.5 w-3.5" />
              <span>Opposing Factors & Risks ({signal.opposingEvidence?.length || 0})</span>
            </div>
            <div className="space-y-1 max-h-32 overflow-y-auto pr-1">
              {signal.opposingEvidence && signal.opposingEvidence.length > 0 ? (
                signal.opposingEvidence.slice(0, 3).map((item, idx) => (
                  <div key={idx} className="text-xs text-text-primary/90 flex items-start gap-1.5">
                    <span className="text-danger font-bold shrink-0">•</span>
                    <span>{item.reason}</span>
                  </div>
                ))
              ) : (
                <div className="text-xs text-text-muted italic">No significant headwinds detected</div>
              )}
            </div>
          </div>
        </div>

        {/* Auditable Reasoning */}
        <div className="p-3 bg-background rounded-lg border border-border text-xs text-text-secondary leading-relaxed">
          <span className="font-semibold text-text-primary mr-1">Deterministic Traceable Reasoning:</span>
          {signal.reasoning}
        </div>

        {/* Data Provenance & Freshness Footer */}
        <div className="flex flex-wrap items-center justify-between text-[11px] text-text-muted pt-1 border-t border-border-subtle gap-2">
          <span>Freshness: <strong>{(signal.freshnessScore * 100).toFixed(0)}%</strong> • Status: <strong>{signal.dataQualityStatus}</strong></span>
          <span>As of: {new Date(signal.informationAvailableAt).toLocaleTimeString('en-IN')} IST • Config: {signal.configurationVersion}</span>
        </div>
      </CardContent>
    </Card>
  );
}
