import { cn } from '@/lib/utils';
import { QLBadge } from './QLBadge';
import { Shield, Target, Cpu } from 'lucide-react';

export interface QLDecisionProps {
  symbol: string;
  companyName: string;
  verdict: 'STRONG BUY' | 'BUY' | 'ACCUMULATE' | 'HOLD' | 'WAIT' | 'REDUCE' | 'AVOID';
  convictionScore: number; // 0 to 100
  modelConfidence?: number; // 0 to 1
  dataConfidence?: number; // 0 to 100
  riskLevel: 'LOW' | 'MODERATE' | 'HIGH' | 'EXTREME';
  action: string;
  entryClassification?: {
    label: string;
    description: string;
    variant?: 'positive' | 'negative' | 'warning' | 'info' | 'neutral';
  };
  t20Forecast?: number | null;
  t5Forecast?: number | null;
  t1Forecast?: number | null;
  riskRewardRatio?: number | null;
  stopLossPrice?: number | null;
  className?: string;
}

export function QLDecision({
  symbol,
  companyName,
  verdict,
  convictionScore,
  modelConfidence = 0.52,
  riskLevel,
  action,
  entryClassification,
  t20Forecast,
  t5Forecast,
  t1Forecast,
  riskRewardRatio,
  className,
}: QLDecisionProps) {
  const getVerdictStyle = (v: string) => {
    switch (v) {
      case 'STRONG BUY':
      case 'BUY':
        return 'text-emerald-400 border-emerald-500/30 bg-emerald-500/10';
      case 'ACCUMULATE':
        return 'text-sky-400 border-sky-500/30 bg-sky-500/10';
      case 'HOLD':
      case 'WAIT':
        return 'text-amber-400 border-amber-500/30 bg-amber-500/10';
      case 'REDUCE':
      case 'AVOID':
        return 'text-rose-400 border-rose-500/30 bg-rose-500/10';
      default:
        return 'text-text-primary border-border bg-surface-elevated';
    }
  };

  const getRiskVariant = (r: string): 'positive' | 'negative' | 'warning' | 'info' => {
    switch (r) {
      case 'LOW':
        return 'positive';
      case 'MODERATE':
        return 'info';
      case 'HIGH':
        return 'warning';
      case 'EXTREME':
        return 'negative';
      default:
        return 'info';
    }
  };

  return (
    <div
      className={cn(
        'rounded-2xl border border-border bg-surface p-6 sm:p-8 space-y-6',
        className
      )}
    >
      {/* Top Ticker Header */}
      <div className="flex flex-col sm:flex-row sm:items-baseline justify-between gap-4 border-b border-border/60 pb-5">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight text-text-primary uppercase font-mono">
              {symbol}
            </h1>
            <QLBadge variant="neutral" size="sm">
              NSE:EQ
            </QLBadge>
            {entryClassification && (
              <QLBadge variant={entryClassification.variant || 'warning'} size="sm">
                {entryClassification.label}
              </QLBadge>
            )}
          </div>
          <p className="text-sm text-text-muted mt-1 font-sans">{companyName}</p>
        </div>

        {/* Big Stance Badge */}
        <div className="flex items-center gap-3">
          <div
            className={cn(
              'px-6 py-2.5 rounded-xl border text-2xl sm:text-3xl font-black tracking-wider uppercase font-mono shadow-sm',
              getVerdictStyle(verdict)
            )}
          >
            {verdict}
          </div>
        </div>
      </div>

      {/* Conviction & Model Confidence Triad */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 font-mono">
        {/* Conviction */}
        <div className="p-4 rounded-xl bg-surface-elevated/70 border border-border/80 space-y-1">
          <div className="flex items-center justify-between text-[11px] text-text-muted uppercase tracking-wider">
            <span>DECISION CONVICTION</span>
            <Target className="w-3.5 h-3.5 text-accent" />
          </div>
          <div className="text-3xl font-black text-accent tracking-tight">
            {convictionScore}
            <span className="text-sm font-normal text-text-muted"> / 100</span>
          </div>
          <p className="text-[10px] text-text-muted font-sans mt-1">
            Multi-factor synthesis agreement score (NOT calibrated as win probability)
          </p>
        </div>

        {/* Model Confidence */}
        <div className="p-4 rounded-xl bg-surface-elevated/70 border border-border/80 space-y-1">
          <div className="flex items-center justify-between text-[11px] text-text-muted uppercase tracking-wider">
            <span>MODEL CONFIDENCE</span>
            <Cpu className="w-3.5 h-3.5 text-sky-400" />
          </div>
          <div className="text-3xl font-black text-sky-400 tracking-tight">
            {(modelConfidence * 100).toFixed(0)}%
          </div>
          <p className="text-[10px] text-text-muted font-sans mt-1">
            OOS statistical stability weight (NOT probability of positive profit)
          </p>
        </div>

        {/* Risk Level & Action */}
        <div className="p-4 rounded-xl bg-surface-elevated/70 border border-border/80 space-y-2">
          <div className="flex items-center justify-between text-[11px] text-text-muted uppercase tracking-wider">
            <span>RISK &amp; SIZING</span>
            <Shield className="w-3.5 h-3.5 text-amber-400" />
          </div>
          <div className="flex items-center gap-2">
            <QLBadge variant={getRiskVariant(riskLevel)} size="md">
              RISK: {riskLevel}
            </QLBadge>
            <span className="text-xs text-text-secondary">{action}</span>
          </div>
          <p className="text-[10px] text-text-muted font-sans">
            Strict max 10% capital cap · Volatility stop envelope
          </p>
        </div>
      </div>

      {/* Forecast Horizons Strip */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 font-mono text-xs">
        <div className="p-3 rounded-lg bg-surface-elevated/40 border border-border/60">
          <span className="text-[10px] text-text-muted uppercase block">T+1 FORECAST</span>
          <span className="font-bold text-text-secondary text-sm">
            {t1Forecast !== null && t1Forecast !== undefined
              ? `${t1Forecast > 0 ? '+' : ''}${(t1Forecast * 100).toFixed(2)}%`
              : 'N/A'}
          </span>
        </div>
        <div className="p-3 rounded-lg bg-surface-elevated/40 border border-border/60">
          <span className="text-[10px] text-text-muted uppercase block">T+5 FORECAST</span>
          <span className="font-bold text-text-secondary text-sm">
            {t5Forecast !== null && t5Forecast !== undefined
              ? `${t5Forecast > 0 ? '+' : ''}${(t5Forecast * 100).toFixed(2)}%`
              : 'N/A'}
          </span>
        </div>
        <div className="p-3 rounded-lg bg-surface-elevated/40 border border-border/60">
          <span className="text-[10px] text-text-muted uppercase block">T+20 FORECAST</span>
          <span
            className={cn(
              'font-bold text-sm',
              t20Forecast !== null && t20Forecast !== undefined
                ? t20Forecast > 0
                  ? 'text-emerald-400'
                  : 'text-rose-400'
                : 'text-text-muted'
            )}
          >
            {t20Forecast !== null && t20Forecast !== undefined
              ? `${t20Forecast > 0 ? '+' : ''}${(t20Forecast * 100).toFixed(2)}%`
              : 'N/A'}
          </span>
        </div>
        <div className="p-3 rounded-lg bg-surface-elevated/40 border border-border/60">
          <span className="text-[10px] text-text-muted uppercase block">RISK / REWARD</span>
          <span className="font-bold text-text-primary text-sm">
            {riskRewardRatio ? `${riskRewardRatio.toFixed(1)} : 1` : 'N/A'}
          </span>
        </div>
      </div>
    </div>
  );
}
