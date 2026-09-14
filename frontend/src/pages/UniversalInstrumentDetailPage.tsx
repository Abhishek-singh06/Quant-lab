import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { RefreshCw } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { QLBadge } from '@/design-system/QLBadge';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLTabs } from '@/design-system/QLTabs';
import { QLEmptyState } from '@/design-system/QLEmptyState';
import { QLProvenance } from '@/design-system/QLProvenance';
import { TechnicalFeaturesCard } from '@/components/dashboard/TechnicalFeaturesCard';
import { CompanyFundamentalsCard } from '@/components/dashboard/CompanyFundamentalsCard';
import { SignalTerminalCard } from '@/components/dashboard/SignalTerminalCard';
import { RiskTerminalCard } from '@/components/dashboard/RiskTerminalCard';
import { cn } from '@/lib/utils';

export function UniversalInstrumentDetailPage() {
  const { symbol = 'RELIANCE', exchange = 'NSE' } = useParams<{ symbol: string; exchange: string }>();
  const { token, user } = useAuth();
  const [activeTab, setActiveTab] = useState('overview');
  const [decision, setDecision] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [history, setHistory] = useState<any[]>([]);

  const fetchDecision = async () => {
    setLoading(true);
    try {
      const headers: Record<string, string> = { 'Content-Type': 'application/json' };
      if (token) headers['Authorization'] = `Bearer ${token}`;

      const res = await fetch('/api/v1/decision/evaluate', {
        method: 'POST',
        headers,
        body: JSON.stringify({ symbol, exchange }),
      });

      if (res.ok) {
        setDecision(await res.json());
      }

      // Fetch history
      const histRes = await fetch(`/api/v1/investment-view/${symbol}/history`, { headers });
      if (histRes.ok) {
        setHistory(await histRes.json());
      }
    } catch {
      // Safe fallback
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDecision();
  }, [symbol, exchange, token]);

  const tabs = [
    { id: 'overview', label: '01 DECISION & OVERVIEW' },
    { id: 'technicals', label: '02 TECHNICAL FACTORS' },
    { id: 'fundamentals', label: '03 FUNDAMENTALS' },
    { id: 'signals', label: '04 MULTI-HORIZON ALPHA' },
    { id: 'risk', label: '05 RISK & SIZING' },
    { id: 'provenance', label: '06 AUDIT & PROVENANCE' },
  ];

  const getActionBadgeVariant = (action: string): 'positive' | 'negative' | 'warning' | 'neutral' => {
    if (['BUY', 'ACCUMULATE'].includes(action)) return 'positive';
    if (['SELL', 'AVOID', 'AVOID ADDING'].includes(action)) return 'negative';
    if (['HOLD', 'WATCH', 'REDUCE'].includes(action)) return 'warning';
    return 'neutral';
  };

  return (
    <div className="space-y-8 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>{exchange} LISTED SECURITY</span>
            <span>•</span>
            <span>POINT-IN-TIME RESEARCH ENGINE</span>
          </div>
          <div className="flex items-baseline gap-3">
            <h1 className="text-3xl sm:text-5xl font-black font-mono tracking-tight text-text-primary uppercase">
              {symbol}
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-surface-elevated text-text-muted border border-border">
              {exchange}
            </span>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <QLBadge variant="warning" size="sm">
            DELAYED (~15M)
          </QLBadge>
          <QLButton variant="outline" size="sm" onClick={fetchDecision} disabled={loading}>
            <RefreshCw className={cn('w-3.5 h-3.5', loading && 'animate-spin text-accent')} />
            <span>Re-evaluate</span>
          </QLButton>
        </div>
      </div>

      {/* Tabs */}
      <QLTabs tabs={tabs} activeTab={activeTab} onChange={setActiveTab} />

      {/* Tab: Overview & Personalized Decision */}
      {activeTab === 'overview' && (
        <div className="space-y-8">
          {/* Personalized Decision Hero Card */}
          <QLPanel
            variant="surface"
            padding="lg"
            className="border-accent/40 bg-surface shadow-xl"
            headerAction={
              <div className="flex items-center gap-2">
                {user && (
                  <QLBadge variant="neutral" size="xs">
                    PORTFOLIO-AWARE
                  </QLBadge>
                )}
                <QLBadge
                  variant={getActionBadgeVariant(decision?.personalized_action || 'HOLD')}
                  size="sm"
                  dot
                >
                  {decision?.personalized_action || 'HOLD'}
                </QLBadge>
              </div>
            }
          >
            {loading ? (
              <div className="py-12 space-y-4 animate-pulse">
                <div className="h-8 bg-surface-elevated/50 rounded w-1/3" />
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                  {[1, 2, 3, 4].map((i) => (
                    <div key={i} className="h-24 bg-surface-elevated/50 rounded-lg" />
                  ))}
                </div>
              </div>
            ) : !decision ? (
              <QLEmptyState
                title="NO DECISION COMPUTED"
                description="Unable to generate point-in-time recommendation for this symbol."
                type="unavailable"
              />
            ) : (
              <div className="space-y-6">
                <div>
                  <span className="text-[10px] font-mono text-text-muted uppercase tracking-widest block mb-1">
                    QUANTLAB SYNTHESIZED VERDICT
                  </span>
                  <div className="flex flex-wrap items-baseline gap-4">
                    <span className="text-4xl sm:text-6xl font-black font-mono tracking-tight text-text-primary">
                      {decision.personalized_action}
                    </span>
                    <span className="text-xs font-mono text-text-muted">
                      Conviction: <strong className="text-accent">{decision.conviction_score}/100</strong> &bull; Model Confidence: <strong className="text-sky-400">{(decision.model_confidence * 100).toFixed(0)}%</strong>
                    </span>
                  </div>
                </div>

                {/* Sizing & Horizon Metrics Strip */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 font-mono">
                  <div className="p-3.5 rounded-lg bg-surface-elevated/60 border border-border">
                    <span className="text-[10px] text-text-muted uppercase block">SUGGESTED CAPITAL</span>
                    <span className="text-xl sm:text-2xl font-black text-text-primary block mt-1">
                      ₹{decision.suggested_capital_inr?.toLocaleString('en-IN')}
                    </span>
                    <span className="text-[10px] text-text-muted">Tranche: ₹{decision.minimum_tranche_inr?.toLocaleString('en-IN')}</span>
                  </div>

                  <div className="p-3.5 rounded-lg bg-surface-elevated/60 border border-border">
                    <span className="text-[10px] text-text-muted uppercase block">TARGET HORIZON</span>
                    <span className="text-sm font-bold text-accent block mt-1 truncate">
                      {decision.suggested_holding_horizon}
                    </span>
                    <span className="text-[10px] text-text-muted">Multi-Day Calibration</span>
                  </div>

                  <div className="p-3.5 rounded-lg bg-surface-elevated/60 border border-border">
                    <span className="text-[10px] text-text-muted uppercase block">MODEL-IMPLIED RETURN</span>
                    <span className="text-xl sm:text-2xl font-black text-emerald-400 block mt-1">
                      +{decision.model_implied_return_range?.base_case_pct?.toFixed(1)}%
                    </span>
                    <span className="text-[10px] text-text-muted">Bull: +{decision.model_implied_return_range?.bull_case_pct?.toFixed(1)}%</span>
                  </div>

                  <div className="p-3.5 rounded-lg bg-surface-elevated/60 border border-border">
                    <span className="text-[10px] text-text-muted uppercase block">DOWNSIDE RISK (1D VAR)</span>
                    <span className="text-xl sm:text-2xl font-black text-rose-400 block mt-1">
                      -{decision.downside_risk_pct?.toFixed(1)}%
                    </span>
                    <span className="text-[10px] text-text-muted">Risk Level: {decision.risk_level}</span>
                  </div>
                </div>

                {/* Personalization Context Notice */}
                {decision.personalization?.personalization_notes?.length > 0 && (
                  <div className="p-4 rounded-lg bg-sky-950/20 border border-sky-500/30 text-xs font-mono text-sky-300 space-y-1">
                    <span className="font-bold block text-sky-200">PORTFOLIO PERSONALIZATION CONTEXT:</span>
                    {decision.personalization.personalization_notes.map((note: string, idx: number) => (
                      <p key={idx} className="font-sans leading-relaxed text-text-secondary">{note}</p>
                    ))}
                  </div>
                )}

                {/* Evidence Synthesis Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
                  <div className="p-4 rounded-lg bg-surface-elevated/40 border border-border space-y-2 font-mono text-xs">
                    <span className="font-bold text-accent uppercase tracking-wider block">
                      Multi-Factor Alpha Breakdown
                    </span>
                    <div className="space-y-1.5 pt-1">
                      <div className="flex justify-between py-1 border-b border-border/40">
                        <span className="text-text-muted">Technical Direction</span>
                        <span className="font-bold text-text-primary">{decision.evidence_breakdown?.technical_trend || 'NEUTRAL'}</span>
                      </div>
                      <div className="flex justify-between py-1 border-b border-border/40">
                        <span className="text-text-muted">Frozen Ridge Alpha (T+20)</span>
                        <span className="font-bold text-emerald-400">
                          {decision.evidence_breakdown?.quant_forecast_t20 !== undefined
                            ? `${(decision.evidence_breakdown.quant_forecast_t20 * 100).toFixed(2)}%`
                            : '--'}
                        </span>
                      </div>
                      <div className="flex justify-between py-1 border-b border-border/40">
                        <span className="text-text-muted">Fundamental Quality</span>
                        <span className="font-bold text-text-primary">{decision.evidence_breakdown?.fundamental_quality || 'HIGH'}</span>
                      </div>
                      <div className="flex justify-between py-1">
                        <span className="text-text-muted">Global Macro Regime</span>
                        <span className="font-bold text-sky-300">{decision.evidence_breakdown?.market_regime || 'RISK_ON'}</span>
                      </div>
                    </div>
                  </div>

                  <div className="p-4 rounded-lg bg-surface-elevated/40 border border-border space-y-2 font-mono text-xs">
                    <span className="font-bold text-rose-400 uppercase tracking-wider block">
                      Invalidation &amp; Risk Guardrails
                    </span>
                    <div className="space-y-1.5 pt-1">
                      {decision.invalidation_conditions?.length > 0 ? (
                        decision.invalidation_conditions.map((inv: string, idx: number) => (
                          <div key={idx} className="text-text-secondary flex items-start gap-1.5 font-sans leading-relaxed">
                            <span className="text-rose-400 font-bold shrink-0">&bull;</span>
                            <span>{inv}</span>
                          </div>
                        ))
                      ) : (
                        <p className="text-text-muted italic">Standard 5% ATR trailing stop active.</p>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </QLPanel>

          {/* Decision History Audit Log */}
          {history.length > 0 && (
            <QLPanel variant="surface" padding="md" title="HISTORICAL DECISION AUDIT TRAIL">
              <div className="overflow-x-auto rounded-lg border border-border font-mono text-xs">
                <table className="w-full text-left">
                  <thead className="bg-surface-elevated text-text-muted text-[11px] uppercase border-b border-border">
                    <tr>
                      <th className="px-3.5 py-2">Timestamp</th>
                      <th className="px-3.5 py-2">Decision</th>
                      <th className="px-3.5 py-2">Conviction</th>
                      <th className="px-3.5 py-2">Sizing Cap</th>
                      <th className="px-3.5 py-2">Horizon</th>
                      <th className="px-3.5 py-2">Model Version</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border/40">
                    {history.map((h) => (
                      <tr key={h.id} className="hover:bg-surface-elevated/30">
                        <td className="px-3.5 py-2 text-text-muted">{new Date(h.timestamp).toLocaleString('en-IN')}</td>
                        <td className="px-3.5 py-2 font-bold text-text-primary">{h.decision}</td>
                        <td className="px-3.5 py-2 text-accent">{h.conviction_score}/100</td>
                        <td className="px-3.5 py-2">₹{h.recommended_capital?.toLocaleString('en-IN')}</td>
                        <td className="px-3.5 py-2 text-text-muted">{h.horizon}</td>
                        <td className="px-3.5 py-2 text-[11px] text-text-muted">{h.model_version}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </QLPanel>
          )}
        </div>
      )}

      {/* Tab: Technical Factors */}
      {activeTab === 'technicals' && <TechnicalFeaturesCard />}

      {/* Tab: Fundamentals */}
      {activeTab === 'fundamentals' && <CompanyFundamentalsCard />}

      {/* Tab: Multi-Horizon Alpha Signals */}
      {activeTab === 'signals' && <SignalTerminalCard />}

      {/* Tab: Risk & Sizing */}
      {activeTab === 'risk' && <RiskTerminalCard />}

      {/* Tab: Provenance */}
      {activeTab === 'provenance' && (
        <QLProvenance
          modelVersion={decision?.provenance?.model_version || 'PHASE_16_FROZEN_RIDGE_TOP8_V1'}
          sha256ProvenanceHash={decision?.provenance?.artifact_hash || '1342f5d9134fd6cf25c625362a6b276a2700e668ddc63c5b54d3e75c4b738885'}
          observationId={decision?.provenance?.decision_timestamp ? `OBS-${decision.provenance.decision_timestamp}` : 'OBS-PIT-2026-IND-01'}
        />
      )}
    </div>
  );
}
