import { TrendingDown, Minus, TrendingUp, CheckCircle2, AlertOctagon } from 'lucide-react';
import type { ScenarioAnalysis } from '@/types/recommendation';

interface Props {
  scenarios: ScenarioAnalysis;
  currentPrice?: number;
}

export function ScenarioAnalysisSection({ scenarios }: Props) {
  const { bear_case, base_case, bull_case, notes } = scenarios;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-bold text-text-primary">Scenario Analysis (Bear / Base / Bull)</h3>
          <p className="text-xs text-text-muted">Derived from statistical volatility bands, ATR, and valuation boundaries.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* BEAR CASE */}
        <div className="rounded-xl border border-rose-500/20 bg-rose-950/10 p-5 space-y-4 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="p-1.5 rounded-lg bg-rose-500/20 text-rose-400">
                <TrendingDown className="w-4 h-4" />
              </div>
              <span className="text-xs font-bold uppercase tracking-wider text-rose-400">Bear Case</span>
            </div>
            <span className="text-lg font-mono font-black text-rose-400">
              {bear_case.expected_move_pct > 0 ? `+${bear_case.expected_move_pct}%` : `${bear_case.expected_move_pct}%`}
            </span>
          </div>

          <div>
            <div className="text-xs text-text-muted">Target Price Estimate</div>
            <div className="text-xl font-bold font-mono text-text-primary">
              ₹{bear_case.target_price.toLocaleString('en-IN')}
            </div>
          </div>

          <div className="space-y-2 border-t border-rose-500/15 pt-3">
            <div className="text-[11px] font-semibold text-rose-300 uppercase">Key Drivers:</div>
            <ul className="space-y-1 text-xs text-text-secondary">
              {bear_case.key_drivers.map((d, i) => (
                <li key={i} className="flex items-start gap-1.5">
                  <span className="text-rose-400">•</span>
                  <span>{d}</span>
                </li>
              ))}
            </ul>
          </div>

          <div className="space-y-2 border-t border-rose-500/15 pt-3">
            <div className="text-[11px] font-semibold text-rose-300 uppercase">Trigger Conditions:</div>
            <ul className="space-y-1 text-xs text-text-muted">
              {bear_case.trigger_conditions.map((t, i) => (
                <li key={i} className="flex items-start gap-1.5">
                  <AlertOctagon className="w-3 h-3 text-rose-400 shrink-0 mt-0.5" />
                  <span>{t}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* BASE CASE */}
        <div className="rounded-xl border border-cyan-500/20 bg-cyan-950/10 p-5 space-y-4 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="p-1.5 rounded-lg bg-cyan-500/20 text-cyan-300">
                <Minus className="w-4 h-4" />
              </div>
              <span className="text-xs font-bold uppercase tracking-wider text-cyan-300">Base Case</span>
            </div>
            <span className="text-lg font-mono font-black text-cyan-300">
              +{base_case.expected_move_pct}%
            </span>
          </div>

          <div>
            <div className="text-xs text-text-muted">Target Price Estimate</div>
            <div className="text-xl font-bold font-mono text-text-primary">
              ₹{base_case.target_price.toLocaleString('en-IN')}
            </div>
          </div>

          <div className="space-y-2 border-t border-cyan-500/15 pt-3">
            <div className="text-[11px] font-semibold text-cyan-200 uppercase">Key Drivers:</div>
            <ul className="space-y-1 text-xs text-text-secondary">
              {base_case.key_drivers.map((d, i) => (
                <li key={i} className="flex items-start gap-1.5">
                  <span className="text-cyan-300">•</span>
                  <span>{d}</span>
                </li>
              ))}
            </ul>
          </div>

          <div className="space-y-2 border-t border-cyan-500/15 pt-3">
            <div className="text-[11px] font-semibold text-cyan-200 uppercase">Trigger Conditions:</div>
            <ul className="space-y-1 text-xs text-text-muted">
              {base_case.trigger_conditions.map((t, i) => (
                <li key={i} className="flex items-start gap-1.5">
                  <CheckCircle2 className="w-3 h-3 text-cyan-300 shrink-0 mt-0.5" />
                  <span>{t}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* BULL CASE */}
        <div className="rounded-xl border border-emerald-500/20 bg-emerald-950/10 p-5 space-y-4 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="p-1.5 rounded-lg bg-emerald-500/20 text-emerald-400">
                <TrendingUp className="w-4 h-4" />
              </div>
              <span className="text-xs font-bold uppercase tracking-wider text-emerald-400">Bull Case</span>
            </div>
            <span className="text-lg font-mono font-black text-emerald-400">
              +{bull_case.expected_move_pct}%
            </span>
          </div>

          <div>
            <div className="text-xs text-text-muted">Target Price Estimate</div>
            <div className="text-xl font-bold font-mono text-text-primary">
              ₹{bull_case.target_price.toLocaleString('en-IN')}
            </div>
          </div>

          <div className="space-y-2 border-t border-emerald-500/15 pt-3">
            <div className="text-[11px] font-semibold text-emerald-300 uppercase">Key Drivers:</div>
            <ul className="space-y-1 text-xs text-text-secondary">
              {bull_case.key_drivers.map((d, i) => (
                <li key={i} className="flex items-start gap-1.5">
                  <span className="text-emerald-400">•</span>
                  <span>{d}</span>
                </li>
              ))}
            </ul>
          </div>

          <div className="space-y-2 border-t border-emerald-500/15 pt-3">
            <div className="text-[11px] font-semibold text-emerald-300 uppercase">Trigger Conditions:</div>
            <ul className="space-y-1 text-xs text-text-muted">
              {bull_case.trigger_conditions.map((t, i) => (
                <li key={i} className="flex items-start gap-1.5">
                  <CheckCircle2 className="w-3 h-3 text-emerald-400 shrink-0 mt-0.5" />
                  <span>{t}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>

      <div className="text-xs text-text-muted italic bg-surface-elevated/40 p-3 rounded-lg border border-border/40">
        Note: {notes}
      </div>
    </div>
  );
}
