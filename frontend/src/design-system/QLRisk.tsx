import { cn } from '@/lib/utils';
import { Lock } from 'lucide-react';

export interface QLRiskProps {
  portfolioRisk?: string;
  var95Pct?: number;
  maxDrawdownPct?: number;
  cashAllocationPct?: number;
  grossExposurePct?: number;
  suggestedAllocationInr?: number;
  maxAllocationInr?: number;
  bindingConstraint?: string;
  className?: string;
}

export function QLRisk({
  var95Pct = 1.85,
  maxDrawdownPct = 3.2,
  cashAllocationPct = 84.3,
  grossExposurePct = 15.7,
  suggestedAllocationInr = 45000,
  maxAllocationInr = 50000,
  bindingConstraint = 'VOLATILITY_CAP (10% MAX)',
  className,
}: QLRiskProps) {
  return (
    <div className={cn('space-y-4 font-mono text-xs', className)}>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border space-y-1">
          <span className="text-[10px] text-text-muted uppercase">1-DAY VaR (95%)</span>
          <p className="text-base font-bold text-amber-400 font-mono">
            {var95Pct.toFixed(2)}%
          </p>
          <span className="text-[10px] text-text-muted font-sans">Parametric Gaussian</span>
        </div>

        <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border space-y-1">
          <span className="text-[10px] text-text-muted uppercase">MAX DRAWDOWN</span>
          <p className="text-base font-bold text-rose-400 font-mono">
            {maxDrawdownPct.toFixed(2)}%
          </p>
          <span className="text-[10px] text-text-muted font-sans">Historical peak-to-trough</span>
        </div>

        <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border space-y-1">
          <span className="text-[10px] text-text-muted uppercase">GROSS EXPOSURE</span>
          <p className="text-base font-bold text-text-primary font-mono">
            {grossExposurePct.toFixed(1)}%
          </p>
          <span className="text-[10px] text-text-muted font-sans">Cash: {cashAllocationPct.toFixed(1)}%</span>
        </div>

        <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border space-y-1">
          <span className="text-[10px] text-text-muted uppercase">HARD POSITION CAP</span>
          <p className="text-base font-bold text-emerald-400 font-mono">
            ₹{maxAllocationInr.toLocaleString('en-IN')}
          </p>
          <span className="text-[10px] text-text-muted font-sans">Strict 10% sizing limit</span>
        </div>
      </div>

      {/* Sizing & Binding Envelope Banner */}
      <div className="p-3.5 rounded-lg bg-surface border border-border flex items-center justify-between gap-3 text-xs">
        <div className="flex items-center gap-2">
          <Lock className="w-4 h-4 text-accent shrink-0" />
          <div>
            <span className="font-bold text-text-primary">Binding Risk Constraint: </span>
            <span className="text-text-secondary font-mono">{bindingConstraint}</span>
          </div>
        </div>
        <div className="text-right font-mono">
          <span className="text-text-muted text-[10px] uppercase block">Suggested Sizing</span>
          <span className="text-text-primary font-bold">
            ₹{suggestedAllocationInr.toLocaleString('en-IN')}
          </span>
        </div>
      </div>
    </div>
  );
}
