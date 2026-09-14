import { cn } from '@/lib/utils';
import { Cpu } from 'lucide-react';
import { QLBadge } from './QLBadge';

export interface QLSignalProps {
  symbol: string;
  direction: 'BULLISH' | 'BEARISH' | 'NEUTRAL';
  score: number;
  confidence: number;
  modelVersion?: string;
  timestamp?: string;
  reasons?: string[];
  className?: string;
}

export function QLSignal({
  symbol,
  direction,
  score,
  confidence,
  modelVersion = 'PHASE_16_FROZEN_RIDGE_TOP8',
  reasons = [],
  className,
}: QLSignalProps) {
  const isBullish = direction === 'BULLISH';
  const isBearish = direction === 'BEARISH';

  return (
    <div
      className={cn(
        'p-4 rounded-xl border border-border bg-surface-elevated/50 space-y-3 font-mono text-xs',
        className
      )}
    >
      <div className="flex items-center justify-between border-b border-border/60 pb-2">
        <div className="flex items-center gap-2">
          <Cpu className="w-4 h-4 text-cyan-400" />
          <span className="font-bold text-text-primary text-sm">{symbol} QUANT ALPHA SIGNAL</span>
        </div>
        <QLBadge
          variant={isBullish ? 'positive' : isBearish ? 'negative' : 'neutral'}
          size="xs"
        >
          {direction}
        </QLBadge>
      </div>

      <div className="grid grid-cols-3 gap-2 text-center">
        <div className="p-2 rounded bg-surface border border-border/60">
          <span className="text-[10px] text-text-muted uppercase block">SCORE</span>
          <span className="text-base font-bold text-text-primary">{score.toFixed(1)}</span>
        </div>
        <div className="p-2 rounded bg-surface border border-border/60">
          <span className="text-[10px] text-text-muted uppercase block">CONFIDENCE</span>
          <span className="text-base font-bold text-sky-400">{(confidence * 100).toFixed(0)}%</span>
        </div>
        <div className="p-2 rounded bg-surface border border-border/60">
          <span className="text-[10px] text-text-muted uppercase block">ARTIFACT</span>
          <span className="text-[10px] text-text-muted truncate block" title={modelVersion}>
            {modelVersion.slice(0, 10)}...
          </span>
        </div>
      </div>

      {reasons.length > 0 && (
        <div className="space-y-1 pt-1 font-sans text-xs text-text-secondary">
          {reasons.map((r, i) => (
            <div key={i} className="flex items-start gap-1.5">
              <span className="text-text-muted">•</span>
              <span>{r}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
