import { Shield } from 'lucide-react';
import { QLBadge } from '@/design-system/QLBadge';
import { QLPanel } from '@/design-system/QLPanel';
import { QLSection } from '@/design-system/QLSection';
import { BacktestTerminalCard } from '@/components/dashboard/BacktestTerminalCard';
import { WalkForwardDashboardCard } from '@/components/dashboard/WalkForwardDashboardCard';

export function BacktestsPage() {
  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>HISTORICAL SIMULATION STUDIO</span>
            <span>•</span>
            <span>INDIAN MARKET FRICTIONS</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            BACKTESTS
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Historical strategy performance simulations with realistic Indian market STT, brokerage, exchange fees, and slippage models.
          </p>
        </div>

        <div className="flex items-center gap-2 font-mono text-xs">
          <QLBadge variant="positive" size="sm" dot>
            PURGED CV ENFORCED
          </QLBadge>
        </div>
      </div>

      {/* 01 METHODOLOGY & SPLIT INVARIANTS */}
      <QLSection
        number={1}
        title="Walk-Forward Validation Methodology"
        subtitle="Train / Purge / Embargo / Validation Protocol"
      >
        <QLPanel variant="surface" padding="md" className="space-y-4 font-mono text-xs">
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <div className="p-2.5 rounded bg-surface-elevated/60 border border-border/40">
              <span className="text-[10px] text-text-muted uppercase block">UNIVERSE</span>
              <span className="text-text-primary font-bold block mt-0.5">NIFTY 50 TOP-8</span>
            </div>
            <div className="p-2.5 rounded bg-surface-elevated/60 border border-border/40">
              <span className="text-[10px] text-text-muted uppercase block">SIMULATION PERIOD</span>
              <span className="text-text-primary font-bold block mt-0.5">2020 – 2024 (5 Yrs)</span>
            </div>
            <div className="p-2.5 rounded bg-surface-elevated/60 border border-border/40">
              <span className="text-[10px] text-text-muted uppercase block">COST MODEL</span>
              <span className="text-accent font-bold block mt-0.5">STT + 0.05% Slippage</span>
            </div>
            <div className="p-2.5 rounded bg-surface-elevated/60 border border-border/40">
              <span className="text-[10px] text-text-muted uppercase block">PURGE / EMBARGO</span>
              <span className="text-emerald-400 font-bold block mt-0.5">20-Day Buffer</span>
            </div>
          </div>

          <div className="flex items-center gap-2 p-3 rounded bg-surface-elevated/40 border border-border/60 text-[11px] text-text-secondary font-sans">
            <Shield className="w-4 h-4 text-accent shrink-0" />
            <span>
              All backtests strictly enforce a 20-day embargo period between training windows and out-of-sample test folds to eliminate autocorrelation leakage.
            </span>
          </div>
        </QLPanel>
      </QLSection>

      {/* 02 BACKTEST TERMINAL & CURVES */}
      <QLSection
        number={2}
        title="Backtest Execution Terminal"
        subtitle="Equity Curve, Drawdown Distribution & Trade Analytics"
      >
        <BacktestTerminalCard />
      </QLSection>

      {/* 03 WALK FORWARD EVALUATION */}
      <QLSection
        number={3}
        title="Walk-Forward Evaluation Dashboard"
        subtitle="Rolling Window Alpha Stability Across Market Regimes"
      >
        <WalkForwardDashboardCard />
      </QLSection>
    </div>
  );
}
