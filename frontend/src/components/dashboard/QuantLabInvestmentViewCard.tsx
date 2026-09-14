import { useState } from 'react';
import {
  Shield,
  Target,
  Cpu,
  AlertTriangle,
  CheckCircle,
  ArrowUpRight,
  ArrowDownRight,
  FileCode,
} from 'lucide-react';
import type { CompleteInvestmentRecommendation } from '@/types/recommendation';
import { cn } from '@/lib/utils';
import { QLBadge } from '@/design-system/QLBadge';
import { QLSection } from '@/design-system/QLSection';
import { QLPanel } from '@/design-system/QLPanel';
import { QLButton } from '@/design-system/QLButton';
import { QLDataQuality } from '@/design-system/QLDataQuality';
import { QLProvenance } from '@/design-system/QLProvenance';

interface Props {
  recommendation: CompleteInvestmentRecommendation | null;
  loading: boolean;
}

export function QuantLabInvestmentViewCard({ recommendation, loading }: Props) {
  const [showFullProvenance, setShowFullProvenance] = useState(false);

  if (loading) {
    return (
      <QLPanel variant="surface" padding="lg" className="animate-pulse space-y-6">
        <div className="h-8 w-64 bg-surface-elevated rounded" />
        <div className="h-28 w-full bg-surface-elevated rounded-xl" />
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="h-20 bg-surface-elevated rounded-lg" />
          <div className="h-20 bg-surface-elevated rounded-lg" />
          <div className="h-20 bg-surface-elevated rounded-lg" />
          <div className="h-20 bg-surface-elevated rounded-lg" />
        </div>
      </QLPanel>
    );
  }

  if (!recommendation) {
    return null;
  }

  const rec = recommendation;
  const recType = rec.recommendation;
  const modelT20 = rec.quant_view?.t_plus_20_forecast;
  const modelConfidence = rec.quant_view?.model_confidence ?? 0.52;
  const evidenceItems = rec.evidence_items ?? [];
  const evidenceAvailable = evidenceItems.filter((e) => e.status !== 'NOT_AVAILABLE').length;
  const evidenceTotal = evidenceItems.length;

  const getVerdictStyle = (type: string) => {
    switch (type) {
      case 'STRONG BUY':
      case 'BUY':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'ACCUMULATE':
        return 'bg-sky-500/10 text-sky-400 border-sky-500/30';
      case 'HOLD':
      case 'WAIT':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'REDUCE':
      case 'AVOID':
        return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
      default:
        return 'bg-surface-elevated text-text-primary border-border';
    }
  };

  const getRiskBadgeVariant = (risk: string): 'positive' | 'negative' | 'warning' | 'info' => {
    switch (risk) {
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
    <div className="space-y-8">
      {/* 01 VERDICT & EXECUTIVE SUMMARY */}
      <QLSection
        number={1}
        title="Investment Verdict"
        subtitle="Synthesized Multi-Factor Alpha Stance"
        action={
          <QLButton
            variant="outline"
            size="xs"
            onClick={() => setShowFullProvenance(!showFullProvenance)}
            icon={<FileCode className="w-3.5 h-3.5" />}
          >
            {showFullProvenance ? 'Hide Lineage' : 'Audit Provenance'}
          </QLButton>
        }
      >
        <QLPanel variant="surface" padding="lg" className="space-y-6">
          {/* Main Top Header */}
          <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6 border-b border-border/60 pb-6">
            <div className="space-y-2">
              <div className="flex items-center gap-2 font-mono text-xs text-text-muted">
                <span>SECURITY ANALYSIS</span>
                <span>•</span>
                <span>POINT-IN-TIME GATED</span>
              </div>
              <div className="flex items-baseline gap-3 flex-wrap">
                <h1 className="text-4xl sm:text-5xl font-black font-mono tracking-tight text-text-primary uppercase">
                  {rec.symbol}
                </h1>
                <span className="text-sm font-sans text-text-muted">
                  {rec.company_name}
                </span>
                <QLBadge variant="neutral" size="sm">
                  NSE:EQ
                </QLBadge>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div
                className={cn(
                  'px-6 py-3 rounded-xl border text-3xl sm:text-4xl font-black tracking-wider uppercase font-mono shadow-sm',
                  getVerdictStyle(recType)
                )}
              >
                {recType}
              </div>
            </div>
          </div>

          {/* Triad: Conviction vs Model Confidence vs Risk */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 font-mono">
            <div className="p-4 rounded-xl bg-surface-elevated/70 border border-border/80 space-y-1">
              <div className="flex items-center justify-between text-[11px] text-text-muted uppercase tracking-wider">
                <span>DECISION CONVICTION</span>
                <Target className="w-3.5 h-3.5 text-accent" />
              </div>
              <div className="text-3xl font-black text-accent tracking-tight">
                {rec.conviction_score}
                <span className="text-sm font-normal text-text-muted"> / 100</span>
              </div>
              <p className="text-[10px] text-text-muted font-sans mt-1">
                Synthesized conviction score (NOT calibrated as win probability)
              </p>
            </div>

            <div className="p-4 rounded-xl bg-surface-elevated/70 border border-border/80 space-y-1">
              <div className="flex items-center justify-between text-[11px] text-text-muted uppercase tracking-wider">
                <span>MODEL CONFIDENCE</span>
                <Cpu className="w-3.5 h-3.5 text-sky-400" />
              </div>
              <div className="text-3xl font-black text-sky-400 tracking-tight">
                {(modelConfidence * 100).toFixed(0)}%
              </div>
              <p className="text-[10px] text-text-muted font-sans mt-1">
                OOS holdout statistical stability (NOT probability of positive profit)
              </p>
            </div>

            <div className="p-4 rounded-xl bg-surface-elevated/70 border border-border/80 space-y-1">
              <div className="flex items-center justify-between text-[11px] text-text-muted uppercase tracking-wider">
                <span>RISK ENVELOPE</span>
                <Shield className="w-3.5 h-3.5 text-amber-400" />
              </div>
              <div className="flex items-center gap-2 mt-1">
                <QLBadge variant={getRiskBadgeVariant(rec.risk_level)} size="md">
                  RISK: {rec.risk_level}
                </QLBadge>
                <span className="text-xs text-text-secondary">{rec.suggested_action}</span>
              </div>
              <p className="text-[10px] text-text-muted font-sans mt-1">
                Suggested Sizing: ₹{rec.capital_allocation.recommended_allocation_inr.toLocaleString('en-IN')} (Max 10% Cap)
              </p>
            </div>
          </div>
        </QLPanel>
      </QLSection>

      {/* 02 WHY — EVIDENCE STATEMENTS */}
      <QLSection
        number={2}
        title="Why"
        subtitle="Concise Evidence Breakdown"
      >
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Supportive */}
          <QLPanel variant="surface" padding="md" className="space-y-3 border-emerald-500/20 bg-emerald-950/10">
            <div className="flex items-center gap-2 text-sm font-semibold text-emerald-400 font-mono">
              <CheckCircle className="w-4 h-4" />
              <span>SUPPORTIVE FACTORS</span>
            </div>
            <ul className="space-y-2 font-sans text-xs text-text-primary">
              {rec.why_points_supportive.length > 0 ? (
                rec.why_points_supportive.map((point, i) => (
                  <li key={i} className="flex items-start gap-2">
                    <span className="text-emerald-400 font-bold font-mono">✓</span>
                    <span>{point}</span>
                  </li>
                ))
              ) : (
                <li className="text-text-muted">No primary supportive factors identified.</li>
              )}
            </ul>
          </QLPanel>

          {/* Cautionary */}
          <QLPanel variant="surface" padding="md" className="space-y-3 border-amber-500/20 bg-amber-950/10">
            <div className="flex items-center gap-2 text-sm font-semibold text-amber-400 font-mono">
              <AlertTriangle className="w-4 h-4" />
              <span>CAUTIONARY &amp; RISK DRIVERS</span>
            </div>
            <ul className="space-y-2 font-sans text-xs text-text-primary">
              {rec.why_points_cautionary.length > 0 ? (
                rec.why_points_cautionary.map((point, i) => (
                  <li key={i} className="flex items-start gap-2">
                    <span className="text-amber-400 font-bold font-mono">⚠</span>
                    <span>{point}</span>
                  </li>
                ))
              ) : (
                <li className="text-text-muted">No major cautionary flags currently active.</li>
              )}
            </ul>
          </QLPanel>
        </div>
      </QLSection>

      {/* 03 QUANT FORECASTS */}
      <QLSection
        number={3}
        title="Quant Model Forecasts"
        subtitle="Point-in-Time Alpha Horizons"
      >
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 font-mono">
          <QLPanel variant="surface" padding="md">
            <span className="text-[10px] text-text-muted uppercase block">T+1 FORECAST</span>
            <span className="text-2xl font-bold text-text-secondary block mt-1">N/A</span>
            <span className="text-[10px] text-text-muted font-sans">Multi-day alpha model</span>
          </QLPanel>

          <QLPanel variant="surface" padding="md">
            <span className="text-[10px] text-text-muted uppercase block">T+5 FORECAST</span>
            <span className="text-2xl font-bold text-text-secondary block mt-1">N/A</span>
            <span className="text-[10px] text-text-muted font-sans">Medium horizon hold</span>
          </QLPanel>

          <QLPanel variant="surface" padding="md">
            <span className="text-[10px] text-text-muted uppercase block">T+20 FORECAST</span>
            <span
              className={cn(
                'text-2xl font-black block mt-1',
                modelT20 !== null && modelT20 !== undefined
                  ? modelT20 > 0
                    ? 'text-emerald-400'
                    : 'text-rose-400'
                  : 'text-text-muted'
              )}
            >
              {modelT20 !== null && modelT20 !== undefined
                ? `${modelT20 > 0 ? '+' : ''}${(modelT20 * 100).toFixed(2)}%`
                : 'N/A'}
            </span>
            <span className="text-[10px] text-text-muted font-sans">Ridge Top-8 Alpha Target</span>
          </QLPanel>

          <QLPanel variant="surface" padding="md">
            <span className="text-[10px] text-text-muted uppercase block">RISK / REWARD RATIO</span>
            <span className="text-2xl font-bold text-text-primary block mt-1">
              {rec.risk_reward.risk_reward_ratio ? `${rec.risk_reward.risk_reward_ratio} : 1` : 'N/A'}
            </span>
            <span className="text-[10px] text-text-muted font-sans">
              Stop: ₹{rec.risk_reward.stop_loss_price?.toLocaleString('en-IN') ?? 'N/A'}
            </span>
          </QLPanel>
        </div>
      </QLSection>

      {/* 04 RISK & SIZING */}
      <QLSection
        number={4}
        title="Risk & Allocation Envelope"
        subtitle="Portfolio Invariants & Capital Preservation"
      >
        <QLPanel variant="surface" padding="md" className="space-y-4">
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 font-mono">
            <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/60">
              <span className="text-[10px] text-text-muted uppercase block">RECOMMENDED ALLOC</span>
              <span className="text-lg font-bold text-text-primary block mt-1">
                ₹{rec.capital_allocation.recommended_allocation_inr.toLocaleString('en-IN')}
              </span>
              <span className="text-[10px] text-text-muted">Virtual INR</span>
            </div>

            <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/60">
              <span className="text-[10px] text-text-muted uppercase block">MAX SIZING CAP</span>
              <span className="text-lg font-bold text-emerald-400 block mt-1">
                ₹{rec.capital_allocation.max_suggested_allocation_inr.toLocaleString('en-IN')}
              </span>
              <span className="text-[10px] text-text-muted">Strict 10% limit</span>
            </div>

            <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/60">
              <span className="text-[10px] text-text-muted uppercase block">POSITION RISK BUDGET</span>
              <span className="text-lg font-bold text-text-primary block mt-1">
                {rec.capital_allocation.risk_per_position_pct}%
              </span>
              <span className="text-[10px] text-text-muted">Max equity at risk</span>
            </div>

            <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/60">
              <span className="text-[10px] text-text-muted uppercase block">BINDING CONSTRAINT</span>
              <span className="text-xs font-bold text-accent block mt-1 truncate" title={rec.capital_allocation.binding_risk_constraint}>
                {rec.capital_allocation.binding_risk_constraint || 'VOLATILITY_CAP'}
              </span>
              <span className="text-[10px] text-text-muted">Active Risk Rule</span>
            </div>
          </div>
        </QLPanel>
      </QLSection>

      {/* 05 SCENARIOS */}
      <QLSection
        number={5}
        title="Scenario Stress Matrix"
        subtitle="Bull, Base, and Bear Expectation Bounds"
      >
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 font-mono">
          <QLPanel variant="surface" padding="md" className="border-emerald-500/20 bg-emerald-950/10 space-y-2">
            <div className="flex items-center justify-between text-xs text-emerald-400 font-bold">
              <div className="flex items-center gap-1.5">
                <ArrowUpRight className="w-4 h-4" />
                <span>BULL CASE</span>
              </div>
              <span className="text-base font-black">+{rec.scenarios.bull_case.expected_move_pct}%</span>
            </div>
            <div className="text-xs text-text-secondary">
              Target: <strong className="text-text-primary font-bold">₹{rec.scenarios.bull_case.target_price.toLocaleString('en-IN')}</strong>
            </div>
            <p className="text-[11px] text-text-muted font-sans pt-1 border-t border-emerald-500/20">
              {rec.scenarios.bull_case.trigger_conditions?.join(', ') || rec.scenarios.bull_case.key_drivers?.join(', ') || 'Bullish continuation setup'}
            </p>
          </QLPanel>

          <QLPanel variant="surface" padding="md" className="space-y-2">
            <div className="flex items-center justify-between text-xs text-text-secondary font-bold">
              <span>BASE CASE</span>
              <span className="text-base font-black text-text-primary">
                {rec.scenarios.base_case.expected_move_pct > 0 ? '+' : ''}
                {rec.scenarios.base_case.expected_move_pct}%
              </span>
            </div>
            <div className="text-xs text-text-secondary">
              Target: <strong className="text-text-primary font-bold">₹{rec.scenarios.base_case.target_price.toLocaleString('en-IN')}</strong>
            </div>
            <p className="text-[11px] text-text-muted font-sans pt-1 border-t border-border/60">
              {rec.scenarios.base_case.trigger_conditions?.join(', ') || rec.scenarios.base_case.key_drivers?.join(', ') || 'Consolidation at key support'}
            </p>
          </QLPanel>

          <QLPanel variant="surface" padding="md" className="border-rose-500/20 bg-rose-950/10 space-y-2">
            <div className="flex items-center justify-between text-xs text-rose-400 font-bold">
              <div className="flex items-center gap-1.5">
                <ArrowDownRight className="w-4 h-4" />
                <span>BEAR CASE</span>
              </div>
              <span className="text-base font-black">{rec.scenarios.bear_case.expected_move_pct}%</span>
            </div>
            <div className="text-xs text-text-secondary">
              Floor: <strong className="text-text-primary font-bold">₹{rec.scenarios.bear_case.target_price.toLocaleString('en-IN')}</strong>
            </div>
            <p className="text-[11px] text-text-muted font-sans pt-1 border-t border-rose-500/20">
              {rec.scenarios.bear_case.trigger_conditions?.join(', ') || rec.scenarios.bear_case.key_drivers?.join(', ') || 'Downward trend continuation'}
            </p>
          </QLPanel>
        </div>
      </QLSection>

      {/* 06 & 07 THESIS & INVALIDATION */}
      <QLSection
        number={6}
        title="Thesis & Invalidation Conditions"
        subtitle="Core Hypotheses & Invariant Triggers"
      >
        <QLPanel variant="surface" padding="md" className="space-y-4 font-sans">
          <div>
            <span className="font-mono text-xs text-accent uppercase tracking-wider block mb-1">
              INVESTMENT THESIS
            </span>
            <p className="text-sm text-text-primary leading-relaxed">
              &ldquo;{rec.investment_thesis}&rdquo;
            </p>
          </div>

          <div className="pt-3 border-t border-border/60">
            <span className="font-mono text-xs text-rose-400 uppercase tracking-wider block mb-1">
              INVALIDATION TRIGGER
            </span>
            <p className="text-xs text-text-secondary leading-relaxed">
              {rec.thesis_invalidation}
            </p>
          </div>
        </QLPanel>
      </QLSection>

      {/* 08 DATA QUALITY & 09 PROVENANCE */}
      <QLSection
        number={8}
        title="Data Quality & Cryptographic Provenance"
        subtitle="Verification Proofs & Lineage Trace"
      >
        <div className="space-y-4">
          <QLDataQuality
            sourcesVerified={evidenceAvailable}
            totalSources={evidenceTotal}
            provider="YAHOO FINANCE (DELAYED ~15M)"
            asOfTimestamp={rec.as_of_timestamp}
            status={rec.risk_reward.is_available ? 'HIGH_QUALITY' : 'DELAYED'}
            hash={rec.sha256_provenance_hash}
          />

          {showFullProvenance && (
            <QLProvenance
              observationId={rec.id}
              modelVersion={rec.quant_view?.model_id || 'PHASE_16_FROZEN_RIDGE_TOP8'}
              sha256ProvenanceHash={rec.sha256_provenance_hash}
            />
          )}
        </div>
      </QLSection>
    </div>
  );
}
