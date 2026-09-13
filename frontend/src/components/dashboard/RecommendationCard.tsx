import { useState } from 'react';
import { ChevronDown, ChevronUp, TrendingUp, TrendingDown, Minus, AlertCircle, CheckCircle2, XCircle } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { Signal } from '@/types/market';

interface RecommendationCardProps {
  signal: Signal;
  className?: string;
}

function SignalBadge({ signal }: { signal: string }) {
  if (signal === 'BUY') return (
    <span className="flex items-center gap-1 text-xs font-bold text-green-400">
      <TrendingUp className="h-3.5 w-3.5" /> BUY
    </span>
  );
  if (signal === 'SELL') return (
    <span className="flex items-center gap-1 text-xs font-bold text-red-400">
      <TrendingDown className="h-3.5 w-3.5" /> SELL
    </span>
  );
  return (
    <span className="flex items-center gap-1 text-xs font-bold text-text-muted">
      <Minus className="h-3.5 w-3.5" /> {signal}
    </span>
  );
}

function QualityIcon({ score }: { score: number }) {
  if (score >= 0.7) return <CheckCircle2 className="h-3.5 w-3.5 text-green-400" />;
  if (score >= 0.4) return <AlertCircle className="h-3.5 w-3.5 text-yellow-400" />;
  return <XCircle className="h-3.5 w-3.5 text-red-400" />;
}

export function RecommendationCard({ signal, className }: RecommendationCardProps) {
  const [showWhy, setShowWhy] = useState(false);

  const conflictColor =
    signal.conflictSeverity === 'LOW'
      ? 'text-green-400'
      : signal.conflictSeverity === 'MEDIUM'
      ? 'text-yellow-400'
      : 'text-red-400';

  const scoreColor =
    signal.signalScore >= 0.6
      ? 'text-green-400'
      : signal.signalScore >= 0.3
      ? 'text-yellow-400'
      : 'text-red-400';

  return (
    <div className={cn('rounded-xl border border-border bg-surface p-4 space-y-3', className)}>
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-bold text-text-primary">{signal.symbol}</span>
            <SignalBadge signal={signal.signal} />
          </div>
          <p className="text-xs text-text-muted mt-0.5">
            {new Date(signal.signalTimestamp).toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' })} IST
          </p>
        </div>
        <div className="text-right">
          <p className={cn('text-lg font-bold', scoreColor)}>{(signal.signalScore * 100).toFixed(0)}</p>
          <p className="text-[10px] text-text-muted">Score/100</p>
        </div>
      </div>

      {/* Metrics row */}
      <div className="grid grid-cols-3 gap-2 text-center">
        <div className="rounded-lg bg-background p-2">
          <p className="text-xs text-text-muted">Confidence</p>
          <p className="text-sm font-semibold text-text-primary">{(signal.confidence * 100).toFixed(0)}%</p>
        </div>
        <div className="rounded-lg bg-background p-2">
          <p className="text-xs text-text-muted">Conflict</p>
          <p className={cn('text-sm font-semibold', conflictColor)}>{signal.conflictSeverity}</p>
        </div>
        <div className="rounded-lg bg-background p-2">
          <p className="text-xs text-text-muted">Data Quality</p>
          <p className="text-xs font-semibold text-text-primary truncate">{signal.dataQualityStatus.replace(/_/g, ' ')}</p>
        </div>
      </div>

      {/* Summary reasoning */}
      <p className="text-xs text-text-secondary leading-relaxed line-clamp-2">{signal.reasoning}</p>

      {/* WHY toggle */}
      <button
        onClick={() => setShowWhy(!showWhy)}
        className="flex w-full items-center justify-between rounded-lg border border-border bg-background px-3 py-2 text-xs font-medium text-accent hover:bg-surface-elevated transition-colors"
      >
        <span>WHY THIS RECOMMENDATION?</span>
        {showWhy ? <ChevronUp className="h-3.5 w-3.5" /> : <ChevronDown className="h-3.5 w-3.5" />}
      </button>

      {/* Audit drilldown */}
      {showWhy && (
        <div className="space-y-3 border-t border-border pt-3">
          {signal.structuredReasoning && signal.structuredReasoning.length > 0 && (
            <div>
              <p className="text-[10px] font-semibold uppercase tracking-wider text-text-muted mb-1">Structured Reasoning</p>
              <ul className="space-y-1">
                {signal.structuredReasoning.map((item, i) => (
                  <li key={i} className="text-xs text-text-secondary flex items-start gap-1.5">
                    <span className="mt-0.5 text-accent">•</span>
                    <span>{item.statement}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {signal.supportingEvidence && signal.supportingEvidence.length > 0 && (
            <div>
              <p className="text-[10px] font-semibold uppercase tracking-wider text-green-400 mb-1">Supporting Evidence</p>
              <div className="space-y-1">
                {signal.supportingEvidence.map((ev) => (
                  <div key={ev.evidenceId} className="flex items-start gap-2 text-xs">
                    <QualityIcon score={ev.normalizedScore} />
                    <div>
                      <span className="text-text-primary font-medium">{ev.feature}</span>
                      <span className="text-text-muted"> — {ev.reason}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {signal.opposingEvidence && signal.opposingEvidence.length > 0 && (
            <div>
              <p className="text-[10px] font-semibold uppercase tracking-wider text-red-400 mb-1">Opposing Evidence</p>
              <div className="space-y-1">
                {signal.opposingEvidence.map((ev) => (
                  <div key={ev.evidenceId} className="flex items-start gap-2 text-xs">
                    <XCircle className="h-3.5 w-3.5 text-red-400 mt-0.5" />
                    <div>
                      <span className="text-text-primary font-medium">{ev.feature}</span>
                      <span className="text-text-muted"> — {ev.reason}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="flex items-center justify-between text-[10px] text-text-muted pt-1 border-t border-border">
            <span>Signal v{signal.signalVersion}</span>
            {signal.modelVersion && <span>Model: {signal.modelVersion}</span>}
            <span>Config: {signal.configurationVersion}</span>
          </div>
        </div>
      )}
    </div>
  );
}
