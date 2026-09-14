import { Cpu, Activity, PieChart, Newspaper, Globe, ShieldCheck, Scale } from 'lucide-react';
import type { CompleteInvestmentRecommendation } from '@/types/recommendation';
import { cn } from '@/lib/utils';

interface Props {
  recommendation: CompleteInvestmentRecommendation;
}

export function MultiEvidenceBreakdownSection({ recommendation }: Props) {
  const {
    quant_view,
    technical_view,
    fundamentals_view,
    news_interpretation,
    regime_view,
    conflict_analysis,
    confidence_breakdown,
  } = recommendation;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-bold text-text-primary">Multi-Source Analytical Synthesis</h3>
          <p className="text-xs text-text-muted">Rigorous inspection across all underlying QuantLab quantitative and fundamental modules.</p>
        </div>
      </div>

      {/* Grid of 6 Core Analytical Pillars */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {/* 1. QUANT VIEW */}
        <div className="rounded-xl border border-border bg-surface p-4 space-y-3">
          <div className="flex items-center justify-between border-b border-border pb-2">
            <div className="flex items-center gap-2">
              <Cpu className="w-4 h-4 text-accent" />
              <span className="text-xs font-bold uppercase tracking-wider text-text-primary">Quant Model View</span>
            </div>
            <span className="text-[10px] font-mono bg-accent/10 text-accent px-2 py-0.5 rounded border border-accent/20">
              {quant_view.model_status}
            </span>
          </div>

          <div className="space-y-2 text-xs">
            <div className="flex justify-between">
              <span className="text-text-muted">Model ID:</span>
              <span className="font-mono text-text-primary font-semibold">{quant_view.model_id}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">Alpha Direction:</span>
              <span className={cn('font-bold', quant_view.signal_direction === 'POSITIVE' ? 'text-emerald-400' : 'text-rose-400')}>
                {quant_view.signal_direction}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">T+5 Forecast:</span>
              <span className="font-mono text-text-primary">
                {quant_view.t_plus_5_forecast !== null && quant_view.t_plus_5_forecast !== undefined
                  ? `+${(quant_view.t_plus_5_forecast * 100).toFixed(2)}%`
                  : 'N/A'}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">T+20 Forecast:</span>
              <span className="font-mono text-text-primary">
                {quant_view.t_plus_20_forecast !== null && quant_view.t_plus_20_forecast !== undefined
                  ? `+${(quant_view.t_plus_20_forecast * 100).toFixed(2)}%`
                  : 'N/A'}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">Walk-Forward Sharpe:</span>
              <span className="font-mono text-emerald-400 font-bold">{quant_view.walk_forward_sharpe}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">Model Confidence:</span>
              <span className="font-mono text-accent font-bold">{(quant_view.model_confidence * 100).toFixed(1)}%</span>
            </div>
          </div>
        </div>

        {/* 2. TECHNICAL VIEW */}
        <div className="rounded-xl border border-border bg-surface p-4 space-y-3">
          <div className="flex items-center justify-between border-b border-border pb-2">
            <div className="flex items-center gap-2">
              <Activity className="w-4 h-4 text-cyan-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-text-primary">Technical View</span>
            </div>
            <span className="text-[10px] font-mono bg-cyan-500/10 text-cyan-300 px-2 py-0.5 rounded border border-cyan-500/20">
              {technical_view.trend_state}
            </span>
          </div>

          <div className="space-y-2 text-xs">
            <div className="flex justify-between">
              <span className="text-text-muted">Momentum State:</span>
              <span className="text-text-primary font-semibold">{technical_view.momentum_state}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">Overbought Status:</span>
              <span className="font-semibold text-accent">{technical_view.overbought_oversold_status}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">Support Levels:</span>
              <span className="font-mono text-text-primary">
                {technical_view.support_levels.length > 0 ? technical_view.support_levels.map((l) => `₹${l}`).join(', ') : 'N/A'}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">Resistance Levels:</span>
              <span className="font-mono text-text-primary">
                {technical_view.resistance_levels.length > 0 ? technical_view.resistance_levels.map((l) => `₹${l}`).join(', ') : 'N/A'}
              </span>
            </div>
            <p className="text-[11px] text-text-secondary pt-1 border-t border-border/50">
              {technical_view.synthesized_commentary}
            </p>
          </div>
        </div>

        {/* 3. FUNDAMENTAL VIEW */}
        <div className="rounded-xl border border-border bg-surface p-4 space-y-3">
          <div className="flex items-center justify-between border-b border-border pb-2">
            <div className="flex items-center gap-2">
              <PieChart className="w-4 h-4 text-emerald-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-text-primary">Fundamental View</span>
            </div>
            <span className="text-[10px] font-mono bg-emerald-500/10 text-emerald-400 px-2 py-0.5 rounded border border-emerald-500/20">
              {fundamentals_view.quality_assessment} QUALITY
            </span>
          </div>

          <div className="space-y-2 text-xs">
            <div className="flex justify-between">
              <span className="text-text-muted">Growth Trend:</span>
              <span className="text-text-primary font-semibold">{fundamentals_view.growth_trend}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">Margins & ROE:</span>
              <span className="text-text-primary">{fundamentals_view.profitability_and_margins}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">Balance Sheet:</span>
              <span className="text-text-primary">{fundamentals_view.balance_sheet_and_leverage}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">Valuation Multiples:</span>
              <span className="text-accent font-mono font-semibold">{fundamentals_view.valuation_assessment}</span>
            </div>
          </div>
        </div>

        {/* 4. NEWS & DISCLOSURES */}
        <div className="rounded-xl border border-border bg-surface p-4 space-y-3">
          <div className="flex items-center justify-between border-b border-border pb-2">
            <div className="flex items-center gap-2">
              <Newspaper className="w-4 h-4 text-purple-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-text-primary">News Interpretation</span>
            </div>
            <span className={cn('text-[10px] font-mono px-2 py-0.5 rounded border', news_interpretation.sentiment_label === 'POSITIVE' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : 'bg-purple-500/10 text-purple-300 border-purple-500/20')}>
              {news_interpretation.sentiment_label}
            </span>
          </div>

          <div className="space-y-2 text-xs">
            <p className="text-text-primary font-medium">{news_interpretation.headline_summary}</p>
            <div className="p-2.5 rounded-lg bg-surface-elevated/70 border border-border/50 space-y-1">
              <div className="text-[11px] font-semibold text-text-muted">Contextual Impact:</div>
              <p className="text-text-secondary text-[11px]">{news_interpretation.contextual_thesis_impact}</p>
            </div>
            <div className="flex justify-between pt-1">
              <span className="text-text-muted">Market Pricing:</span>
              <span className="text-text-primary font-medium">{news_interpretation.market_pricing_status}</span>
            </div>
          </div>
        </div>

        {/* 5. MARKET REGIME */}
        <div className="rounded-xl border border-border bg-surface p-4 space-y-3">
          <div className="flex items-center justify-between border-b border-border pb-2">
            <div className="flex items-center gap-2">
              <Globe className="w-4 h-4 text-amber-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-text-primary">Market Regime</span>
            </div>
            <span className="text-[10px] font-mono bg-amber-500/10 text-amber-300 px-2 py-0.5 rounded border border-amber-500/20">
              {regime_view.benchmark_symbol}
            </span>
          </div>

          <div className="space-y-2 text-xs">
            <div className="flex justify-between">
              <span className="text-text-muted">Direction:</span>
              <span className="font-bold text-emerald-400">{regime_view.direction_regime}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">Volatility State:</span>
              <span className="font-semibold text-text-primary">{regime_view.volatility_regime}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-muted">Risk Mode:</span>
              <span className="font-semibold text-cyan-300">{regime_view.risk_regime}</span>
            </div>
            <p className="text-[11px] text-text-secondary pt-1 border-t border-border/50">
              {regime_view.regime_impact_on_sizing}
            </p>
          </div>
        </div>

        {/* 6. CONFLICT DETECTION */}
        <div className="rounded-xl border border-border bg-surface p-4 space-y-3">
          <div className="flex items-center justify-between border-b border-border pb-2">
            <div className="flex items-center gap-2">
              <Scale className="w-4 h-4 text-rose-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-text-primary">Conflict Analysis</span>
            </div>
            <span className={cn('text-[10px] font-mono px-2 py-0.5 rounded border', conflict_analysis.conflict_severity === 'LOW' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : 'bg-rose-500/10 text-rose-400 border-rose-500/20')}>
              {conflict_analysis.conflict_severity} SEVERITY
            </span>
          </div>

          <div className="space-y-2 text-xs">
            <div className="flex justify-between">
              <span className="text-text-muted">Divergence Score:</span>
              <span className="font-mono text-text-primary font-bold">{conflict_analysis.conflict_score}/100</span>
            </div>
            <div className="space-y-1">
              <span className="text-text-muted text-[11px]">Active Conflicts:</span>
              {conflict_analysis.conflicting_pairs.length > 0 ? (
                <ul className="space-y-1 text-[11px] text-amber-300">
                  {conflict_analysis.conflicting_pairs.map((c, i) => (
                    <li key={i} className="flex items-start gap-1">
                      <span>•</span>
                      <span>{c}</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <div className="text-[11px] text-emerald-400">No active signal contradictions.</div>
              )}
            </div>
            <p className="text-[11px] text-text-secondary pt-1 border-t border-border/50">
              {conflict_analysis.resolution_rationale}
            </p>
          </div>
        </div>
      </div>

      {/* Confidence Breakdown Banner */}
      <div className="rounded-xl border border-border bg-surface p-4">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <ShieldCheck className="w-4 h-4 text-accent" />
            <span className="text-xs font-bold uppercase tracking-wider text-text-primary">Decision Confidence Decomposition</span>
          </div>
          <span className="text-xs font-mono text-accent font-bold">
            Total Decision Confidence: {confidence_breakdown.decision_confidence_pct}%
          </span>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          <div className="p-2.5 rounded-lg bg-surface-elevated border border-border/60">
            <div className="text-[11px] text-text-muted">Data Confidence</div>
            <div className="text-base font-bold text-text-primary font-mono">{confidence_breakdown.data_confidence_pct}%</div>
          </div>
          <div className="p-2.5 rounded-lg bg-surface-elevated border border-border/60">
            <div className="text-[11px] text-text-muted">Model Confidence</div>
            <div className="text-base font-bold text-accent font-mono">{confidence_breakdown.model_confidence_pct}%</div>
          </div>
          <div className="p-2.5 rounded-lg bg-surface-elevated border border-border/60">
            <div className="text-[11px] text-text-muted">Signal Agreement</div>
            <div className="text-base font-bold text-emerald-400 font-mono">{confidence_breakdown.signal_agreement_pct}%</div>
          </div>
          <div className="p-2.5 rounded-lg bg-surface-elevated border border-border/60">
            <div className="text-[11px] text-text-muted">Decision Confidence</div>
            <div className="text-base font-bold text-cyan-300 font-mono">{confidence_breakdown.decision_confidence_pct}%</div>
          </div>
        </div>
        <div className="text-[11px] text-text-muted mt-2">
          {confidence_breakdown.explanation} (Confidence reflects analytical consensus & data verification, NOT a guaranteed probability of profit.)
        </div>
      </div>
    </div>
  );
}
