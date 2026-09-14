import { Target, CheckCircle2, ShieldAlert, Layers, BarChart2, AlertTriangle } from 'lucide-react';
import type { Signal } from '@/types/market';
import { cn } from '@/lib/utils';
import { QLPanel } from '@/design-system/QLPanel';
import { QLBadge } from '@/design-system/QLBadge';
import { QLEmptyState } from '@/design-system/QLEmptyState';

interface SignalTerminalCardProps {
  signal?: Signal | null;
  loading?: boolean;
}

export function SignalTerminalCard({ signal, loading }: SignalTerminalCardProps) {
  if (loading) {
    return (
      <QLPanel variant="surface" padding="md" title="PRODUCTION CROSS-CHECK SIGNAL TERMINAL">
        <div className="h-48 bg-surface-elevated/50 rounded-lg animate-pulse" />
      </QLPanel>
    );
  }

  if (!signal) {
    return (
      <QLPanel variant="surface" padding="md" title="PRODUCTION CROSS-CHECK SIGNAL TERMINAL">
        <QLEmptyState
          title="NO ACTIVE CROSS-CHECK SIGNAL"
          description="Independent multi-layer signal evaluation has not been dispatched for this instrument."
          source="INDEPENDENT SIGNAL ENGINE"
          type="unavailable"
          nextAction="Run strategy engine or select a tracked equity symbol."
        />
      </QLPanel>
    );
  }

  const isBuy = signal.signal === 'BUY';
  const isSell = signal.signal === 'SELL';
  const badgeVariant = isBuy ? 'positive' : isSell ? 'negative' : 'warning';
  const scoreColor = signal.signalScore > 0 ? 'text-emerald-400' : signal.signalScore < 0 ? 'text-rose-400' : 'text-text-secondary';

  return (
    <QLPanel
      variant="surface"
      padding="md"
      title="PRODUCTION CROSS-CHECK SIGNAL TERMINAL"
      headerAction={
        <div className="flex items-center gap-2">
          <QLBadge variant="neutral" size="xs">
            {signal.symbol}
          </QLBadge>
          <QLBadge variant={badgeVariant} size="xs" dot>
            {signal.signal}
          </QLBadge>
        </div>
      }
    >
      <div className="space-y-4">
        {/* Core Metrics Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {/* Signal Score */}
          <div className="p-3 bg-surface-elevated/50 rounded-lg border border-border">
            <div className="flex items-center justify-between text-xs text-text-muted mb-1 font-mono">
              <span>Signal Score</span>
              <BarChart2 className="h-3.5 w-3.5 text-accent" />
            </div>
            <div className={cn('text-2xl font-black font-mono', scoreColor)}>
              {signal.signalScore > 0 ? `+${signal.signalScore.toFixed(1)}` : signal.signalScore.toFixed(1)}
            </div>
            <span className="text-[10px] text-text-muted font-mono">Scale -100 to +100</span>
          </div>

          {/* Confidence */}
          <div className="p-3 bg-surface-elevated/50 rounded-lg border border-border">
            <div className="flex items-center justify-between text-xs text-text-muted mb-1 font-mono">
              <span>Confidence</span>
              <CheckCircle2 className="h-3.5 w-3.5 text-sky-400" />
            </div>
            <div className="text-2xl font-black font-mono text-sky-300">
              {(signal.confidence * 100).toFixed(0)}%
            </div>
            <span className="text-[10px] text-text-muted font-mono">Evidence Agreement</span>
          </div>

          {/* Expected Return */}
          <div className="p-3 bg-surface-elevated/50 rounded-lg border border-border">
            <div className="flex items-center justify-between text-xs text-text-muted mb-1 font-mono">
              <span>Expected E[R]</span>
              <Target className="h-3.5 w-3.5 text-emerald-400" />
            </div>
            <div className={cn('text-2xl font-black font-mono', (signal.expectedReturn || 0) >= 0 ? 'text-emerald-400' : 'text-rose-400')}>
              {signal.expectedReturn !== undefined && signal.expectedReturn !== null
                ? `${(signal.expectedReturn * 100).toFixed(2)}%`
                : 'N/A'}
            </div>
            <span className="text-[10px] text-text-muted font-mono">Model 1 (1-Day)</span>
          </div>

          {/* Expected Volatility */}
          <div className="p-3 bg-surface-elevated/50 rounded-lg border border-border">
            <div className="flex items-center justify-between text-xs text-text-muted mb-1 font-mono">
              <span>Volatility</span>
              <AlertTriangle className="h-3.5 w-3.5 text-amber-400" />
            </div>
            <div className="text-2xl font-black font-mono text-amber-300">
              {signal.expectedVolatility !== undefined && signal.expectedVolatility !== null
                ? `${(signal.expectedVolatility * 100).toFixed(2)}%`
                : 'N/A'}
            </div>
            <span className="text-[10px] text-text-muted font-mono">Model 3 (5-Day)</span>
          </div>
        </div>

        {/* Category Component Score Breakdown */}
        {signal.components && Object.keys(signal.components).length > 0 && (
          <div className="p-3.5 bg-surface-elevated/30 rounded-lg border border-border space-y-2">
            <div className="flex items-center justify-between text-xs font-semibold text-text-secondary font-mono">
              <span className="flex items-center gap-1.5">
                <Layers className="h-3.5 w-3.5 text-accent" />
                Cross-Layer Category Scores (-100 to +100)
              </span>
              <QLBadge
                variant={signal.conflictSeverity === 'HIGH' ? 'negative' : signal.conflictSeverity === 'MEDIUM' ? 'warning' : 'positive'}
                size="xs"
              >
                {signal.conflictSeverity} CONFLICT ({signal.conflictScore.toFixed(0)}%)
              </QLBadge>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-1 font-mono text-xs">
              {Object.entries(signal.components).map(([cat, comp]) => {
                const isPos = comp.categoryScore > 0;
                return (
                  <div key={cat} className="p-2 rounded bg-surface border border-border/60 flex flex-col justify-between">
                    <span className="text-[10px] text-text-muted uppercase tracking-wider truncate">{cat.replace('_', ' ')}</span>
                    <span className={cn('text-xs font-bold mt-0.5', isPos ? 'text-emerald-400' : comp.categoryScore < 0 ? 'text-rose-400' : 'text-text-muted')}>
                      {comp.isPresent ? (isPos ? `+${comp.categoryScore.toFixed(0)}` : comp.categoryScore.toFixed(0)) : 'N/A'}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Supporting vs Opposing Evidence */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
          {/* Supporting Evidence */}
          <div className="p-3.5 rounded-lg border border-emerald-500/20 bg-emerald-950/10 space-y-2">
            <div className="flex items-center gap-1.5 font-bold font-mono text-emerald-400">
              <CheckCircle2 className="h-3.5 w-3.5" />
              <span>SUPPORTING EVIDENCE ({signal.supportingEvidence?.length || 0})</span>
            </div>
            <div className="space-y-1.5 max-h-32 overflow-y-auto pr-1">
              {signal.supportingEvidence && signal.supportingEvidence.length > 0 ? (
                signal.supportingEvidence.slice(0, 3).map((item, idx) => (
                  <div key={idx} className="text-text-secondary flex items-start gap-1.5 font-sans leading-relaxed">
                    <span className="text-emerald-400 font-bold shrink-0">&bull;</span>
                    <span>{item.reason}</span>
                  </div>
                ))
              ) : (
                <div className="text-text-muted italic">No strong supporting factors</div>
              )}
            </div>
          </div>

          {/* Opposing Evidence / Risk Factors */}
          <div className="p-3.5 rounded-lg border border-rose-500/20 bg-rose-950/10 space-y-2">
            <div className="flex items-center gap-1.5 font-bold font-mono text-rose-400">
              <ShieldAlert className="h-3.5 w-3.5" />
              <span>OPPOSING FACTORS &amp; RISKS ({signal.opposingEvidence?.length || 0})</span>
            </div>
            <div className="space-y-1.5 max-h-32 overflow-y-auto pr-1">
              {signal.opposingEvidence && signal.opposingEvidence.length > 0 ? (
                signal.opposingEvidence.slice(0, 3).map((item, idx) => (
                  <div key={idx} className="text-text-secondary flex items-start gap-1.5 font-sans leading-relaxed">
                    <span className="text-rose-400 font-bold shrink-0">&bull;</span>
                    <span>{item.reason}</span>
                  </div>
                ))
              ) : (
                <div className="text-text-muted italic">No significant headwinds detected</div>
              )}
            </div>
          </div>
        </div>

        {/* Auditable Reasoning */}
        <div className="p-3 bg-surface-elevated/40 rounded-lg border border-border text-xs text-text-secondary leading-relaxed font-sans">
          <span className="font-bold text-text-primary font-mono mr-1">REASONING:</span>
          {signal.reasoning}
        </div>

        {/* Data Provenance & Freshness Footer */}
        <div className="flex flex-wrap items-center justify-between text-[11px] text-text-muted pt-2 border-t border-border/50 gap-2 font-mono">
          <span>Freshness: <strong>{(signal.freshnessScore * 100).toFixed(0)}%</strong> &bull; Status: <strong>{signal.dataQualityStatus}</strong></span>
          <span>Available: {new Date(signal.informationAvailableAt).toLocaleTimeString('en-IN')} IST &bull; Config: {signal.configurationVersion}</span>
        </div>
      </div>
    </QLPanel>
  );
}
