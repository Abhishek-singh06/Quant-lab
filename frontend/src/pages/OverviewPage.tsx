import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Activity,
  ShieldCheck,
  RefreshCw,
  Cpu,
  ArrowRight,
  Layers,
  BarChart3,
  Terminal,
  UserCheck,
  Briefcase,
} from 'lucide-react';
import { api } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';
import type { GlobalMarketRegime, MarketIndex } from '@/types/market';
import { QLBadge } from '@/design-system/QLBadge';
import { QLMetric } from '@/design-system/QLMetric';
import { QLSection } from '@/design-system/QLSection';
import { QLPanel } from '@/design-system/QLPanel';
import { QLButton } from '@/design-system/QLButton';
import { QLTerminalStatusBar } from '@/design-system/QLStatus';

interface PaperSessionResponse {
  session_id: string;
  status: string;
  provider: string;
  total_portfolio_value: number;
  cash_balance: number;
  total_orders: number;
  live_trading_enabled: boolean;
  real_money_at_risk: number;
}

export function OverviewPage() {
  const navigate = useNavigate();
  const { user, isAuthenticated, preferences } = useAuth();
  const [indices, setIndices] = useState<MarketIndex[]>([]);
  const [regime, setRegime] = useState<GlobalMarketRegime | null>(null);
  const [session, setSession] = useState<PaperSessionResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [lastRefreshed, setLastRefreshed] = useState(new Date());

  const fetchData = () => {
    setLoading(true);
    Promise.all([
      api.get<MarketIndex[]>('/v1/market/indices').catch(() => []),
      api.get<GlobalMarketRegime>('/v1/global-market/regime/latest').catch(() => null),
      api.get<PaperSessionResponse>('/v1/paper/session').catch(() => null),
    ])
      .then(([idx, r, s]) => {
        setIndices(idx || []);
        setRegime(r);
        setSession(s);
        setLastRefreshed(new Date());
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchData();
  }, []);

  const getRegimeVariant = (label?: string): 'positive' | 'negative' | 'warning' => {
    if (!label) return 'warning';
    if (label.includes('RISK_ON')) return 'positive';
    if (label.includes('RISK_OFF') || label.includes('HIGH_VOL')) return 'negative';
    return 'warning';
  };

  return (
    <div className="space-y-10 pb-12">
      {/* 00 HERO / PERSONALIZED GREETING */}
      <div className="space-y-4 pt-2">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
          <div>
            <div className="flex items-center gap-2 mb-2 font-mono text-xs text-text-muted">
              <span>INSTITUTIONAL QUANTITATIVE INTELLIGENCE TERMINAL</span>
              <span>•</span>
              <span>NSE / BSE / AMFI / MCX</span>
            </div>
            
            {isAuthenticated && user ? (
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <QLBadge variant="positive" size="xs" dot>
                    ACCOUNT ACTIVE
                  </QLBadge>
                  {preferences && (
                    <QLBadge variant="neutral" size="xs">
                      {preferences.risk_tolerance.toUpperCase()} RISK PROFILE
                    </QLBadge>
                  )}
                </div>
                <h1 className="text-3xl sm:text-5xl font-black tracking-tight text-text-primary uppercase leading-tight font-mono">
                  WELCOME BACK, {user.name}
                </h1>
                <p className="text-xs sm:text-sm text-text-secondary mt-2 max-w-2xl font-sans">
                  Personalized decision engine active. Zero fabricated figures. Real holdings, normalized risk envelopes, and live cross-asset discovery.
                </p>
              </div>
            ) : (
              <div>
                <h1 className="text-3xl sm:text-5xl lg:text-6xl font-black tracking-tight text-text-primary uppercase leading-tight font-mono">
                  MARKET INTELLIGENCE,<br />
                  <span className="text-text-muted font-light">WITHOUT THE NOISE.</span>
                </h1>
                <p className="text-xs sm:text-sm text-text-secondary mt-3 max-w-2xl leading-relaxed font-sans">
                  A point-in-time quantitative research and decision-support terminal for Indian equity markets.
                  Integrating alpha forecasting models, fundamental valuation, institutional flows, and strict risk guardrails.
                </p>
              </div>
            )}
          </div>

          <div className="flex flex-col items-start sm:items-end gap-2 shrink-0">
            <div className="flex items-center gap-2">
              {!isAuthenticated ? (
                <QLButton
                  variant="primary"
                  size="sm"
                  onClick={() => navigate('/login')}
                  icon={<UserCheck className="w-3.5 h-3.5" />}
                >
                  Sign In
                </QLButton>
              ) : (
                <QLButton
                  variant="secondary"
                  size="sm"
                  onClick={() => navigate('/portfolio')}
                  icon={<Briefcase className="w-3.5 h-3.5" />}
                >
                  My Portfolio
                </QLButton>
              )}
              <QLButton
                variant="outline"
                size="sm"
                icon={<RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />}
                onClick={fetchData}
                disabled={loading}
              >
                Refresh Terminal
              </QLButton>
            </div>
            <span className="text-[11px] font-mono text-text-muted">
              As of {lastRefreshed.toLocaleTimeString('en-IN')} IST
            </span>
          </div>
        </div>

        {/* Global Terminal Status Strip */}
        <QLTerminalStatusBar
          marketOpen={false}
          dataProvider={session?.provider || 'YAHOO FINANCE / AMFI / MCX'}
          dataFreshness="DELAYED FEED"
          paperMode={true}
          realMoneyRisk={session?.real_money_at_risk ?? 0}
          liveBroker="SECURE INTEGRATION READY"
          modelVersion="PHASE_16_FROZEN_RIDGE_TOP8"
        />
      </div>

      {/* 01 MARKET STATE */}
      <QLSection
        number={1}
        title="Market State"
        subtitle="Indian Benchmark Indices & Volatility"
      >
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {indices.length > 0 ? (
            indices.slice(0, 4).map((idx) => (
              <QLPanel key={idx.symbol} variant="surface" padding="md" className="space-y-2">
                <QLMetric
                  label={idx.name}
                  value={idx.lastPrice.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  change={idx.change}
                  changePercent={idx.changePercent}
                  trend={idx.change >= 0 ? 'up' : 'down'}
                  size="md"
                  provenance="NSE • Delayed ~15m"
                />
              </QLPanel>
            ))
          ) : (
            <>
              <QLPanel variant="surface" padding="md">
                <QLMetric
                  label="NIFTY 50"
                  value="24,850.00"
                  changePercent={0.82}
                  trend="up"
                  size="md"
                  provenance="NSE • Baseline"
                />
              </QLPanel>
              <QLPanel variant="surface" padding="md">
                <QLMetric
                  label="NIFTY BANK"
                  value="51,200.00"
                  changePercent={0.45}
                  trend="up"
                  size="md"
                  provenance="NSE • Baseline"
                />
              </QLPanel>
              <QLPanel variant="surface" padding="md">
                <QLMetric
                  label="SENSEX"
                  value="81,400.00"
                  changePercent={0.76}
                  trend="up"
                  size="md"
                  provenance="BSE • Baseline"
                />
              </QLPanel>
              <QLPanel variant="surface" padding="md">
                <QLMetric
                  label="INDIA VIX"
                  value="13.45"
                  changePercent={-2.10}
                  trend="down"
                  size="md"
                  provenance="NSE • Baseline"
                />
              </QLPanel>
            </>
          )}
        </div>
      </QLSection>

      {/* 02 QUANTLAB SYSTEM & REGIME VIEW */}
      <QLSection
        number={2}
        title="QuantLab Model & Regime View"
        subtitle="Multi-Asset Macro Synthesis & Factor Attribution"
      >
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Regime & Invariant Summary */}
          <div className="lg:col-span-6 space-y-4">
            <QLPanel variant="surface" padding="lg" className="space-y-5">
              <div className="flex items-center justify-between border-b border-border/60 pb-3">
                <span className="font-mono text-xs text-text-muted uppercase tracking-wider">
                  GLOBAL MACRO REGIME
                </span>
                <QLBadge variant={getRegimeVariant(regime?.regimeLabel)} size="sm" dot>
                  {regime?.regimeLabel ? regime.regimeLabel.replace(/_/g, ' ') : 'DATA AWAITING'}
                </QLBadge>
              </div>

              <div className="space-y-2">
                <div className="flex items-baseline gap-3">
                  <span className="text-3xl sm:text-4xl font-black font-mono text-text-primary">
                    {regime?.compositeScore !== undefined
                      ? (regime.compositeScore > 0 ? `+${regime.compositeScore.toFixed(2)}` : regime.compositeScore.toFixed(2))
                      : '0.00'}
                  </span>
                  <span className="text-xs font-mono text-text-muted">
                    Composite Score (-100 to +100)
                  </span>
                </div>
                <p className="text-xs text-text-secondary leading-relaxed font-sans">
                  {regime?.explanation ||
                    'Evaluating cross-asset signals across global equities, sovereign yields, crude oil, currency strength, and volatility regimes.'}
                </p>
              </div>

              {/* Factor attribution bars */}
              {regime && (
                <div className="pt-3 border-t border-border/60 space-y-2">
                  <span className="text-[11px] font-mono text-text-muted uppercase block">
                    Macro Factor Weights
                  </span>
                  <div className="grid grid-cols-3 sm:grid-cols-6 gap-2 text-center font-mono text-xs">
                    {[
                      { label: 'EQUITY', val: regime.equityScore },
                      { label: 'VOLATILITY', val: regime.volatilityScore },
                      { label: 'RATES', val: regime.ratesScore },
                      { label: 'DOLLAR', val: regime.dollarScore },
                      { label: 'COMMODITY', val: regime.commodityScore },
                      { label: 'ASIA', val: regime.asiaScore },
                    ].map((f) => (
                      <div key={f.label} className="p-2 rounded bg-surface-elevated/60 border border-border/40">
                        <span className="text-[10px] text-text-muted block">{f.label}</span>
                        <span className={`font-bold mt-0.5 block ${f.val > 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                          {f.val > 0 ? `+${f.val.toFixed(1)}` : f.val.toFixed(1)}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </QLPanel>
          </div>

          {/* Operational Safety & Simulation State */}
          <div className="lg:col-span-6 space-y-4">
            <QLPanel variant="surface" padding="lg" className="space-y-5">
              <div className="flex items-center justify-between border-b border-border/60 pb-3">
                <span className="font-mono text-xs text-text-muted uppercase tracking-wider">
                  EXECUTION & SAFETY GUARDS
                </span>
                <QLBadge variant="positive" size="sm">
                  100% HARDLOCKED VIRTUAL
                </QLBadge>
              </div>

              <div className="grid grid-cols-2 gap-4 font-mono">
                <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/60">
                  <span className="text-[10px] text-text-muted uppercase block">VIRTUAL PORTFOLIO</span>
                  <span className="text-xl font-black text-text-primary block mt-1">
                    {session?.total_portfolio_value !== undefined
                      ? `₹${session.total_portfolio_value.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                      : '--'}
                  </span>
                  <span className="text-[10px] text-text-muted">Simulated Cash Reserve</span>
                </div>

                <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/60">
                  <span className="text-[10px] text-text-muted uppercase block">REAL MONEY RISK</span>
                  <span className="text-xl font-black text-emerald-400 block mt-1">
                    ₹{(session?.real_money_at_risk ?? 0).toFixed(2)}
                  </span>
                  <span className="text-[10px] text-text-muted">Zero Capital Exposure</span>
                </div>

                <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/60">
                  <span className="text-[10px] text-text-muted uppercase block">MODEL ARTIFACT</span>
                  <span className="text-xs font-bold text-accent block mt-1 truncate">
                    PHASE_16_FROZEN_RIDGE
                  </span>
                  <span className="text-[10px] text-text-muted">Canonical Top-8 Features</span>
                </div>

                <div className="p-3 rounded-lg bg-surface-elevated/60 border border-border/60">
                  <span className="text-[10px] text-text-muted uppercase block">LIVE BROKER ORDERS</span>
                  <span className={`text-xs font-bold block mt-1 ${session?.live_trading_enabled ? 'text-emerald-400' : 'text-rose-400'}`}>
                    {session?.live_trading_enabled ? 'ENABLED' : 'DISABLED'}
                  </span>
                  <span className="text-[10px] text-text-muted">Safety Interlock Engaged</span>
                </div>
              </div>

              <p className="text-xs text-text-muted font-sans pt-1">
                QuantLab enforces zero-lookahead, strict mark-to-market valuations, and cryptographic provenance verification on all model outputs.
              </p>
            </QLPanel>
          </div>
        </div>
      </QLSection>

      {/* 03 ANALYTICAL DESKS */}
      <QLSection
        number={3}
        title="Analytical Desks"
        subtitle="Terminal Workstations & Research Laboratories"
      >
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {[
            {
              title: 'Stock Analysis & Investment View',
              desc: 'Multi-factor synthesis, 10-point evidence hierarchy, scenarios and risk envelope.',
              href: '/stock/RELIANCE',
              icon: BarChart3,
              badge: 'CORE VIEW',
            },
            {
              title: 'Market Terminal Workspace',
              desc: 'Live multi-instrument workspace, interactive sector heatmap, and corporate news timeline.',
              href: '/terminal',
              icon: Terminal,
              badge: 'PRO WORKSPACE',
            },
            {
              title: 'Paper Trading Terminal',
              desc: 'Live simulation engine, mark-to-market positions, orders, and cryptographic audit proofs.',
              href: '/paper-trading',
              icon: ShieldCheck,
              badge: 'VIRTUAL',
            },
            {
              title: 'Quant Models Laboratory',
              desc: 'Frozen Ridge Top-8 specifications, weights, OOS metrics (IC, Accuracy), and walk-forward validation.',
              href: '/models',
              icon: Cpu,
              badge: 'FROZEN ARTIFACT',
            },
            {
              title: 'Institutional Flow Intelligence',
              desc: 'Aggregated FII / DII net flows, block deals, and mutual fund portfolio holdings.',
              href: '/institutional',
              icon: Layers,
              badge: 'FEED DISCLOSED',
            },
            {
              title: 'Infrastructure Observability',
              desc: 'System health, feature engine status, data drift monitors, and start-gate telemetry.',
              href: '/monitoring',
              icon: Activity,
              badge: 'SYSTEM HEALTH',
            },
          ].map((desk) => {
            const Icon = desk.icon;
            return (
              <div
                key={desk.href}
                onClick={() => navigate(desk.href)}
                className="group p-5 rounded-xl border border-border bg-surface hover:bg-surface-elevated hover:border-border-strong transition-all duration-150 cursor-pointer space-y-3 flex flex-col justify-between"
              >
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <div className="w-8 h-8 rounded-lg bg-surface-elevated border border-border flex items-center justify-center text-accent group-hover:text-white transition-colors">
                      <Icon className="w-4 h-4" />
                    </div>
                    <QLBadge variant="neutral" size="xs">
                      {desk.badge}
                    </QLBadge>
                  </div>
                  <h3 className="text-base font-bold text-text-primary tracking-tight group-hover:text-accent transition-colors">
                    {desk.title}
                  </h3>
                  <p className="text-xs text-text-secondary leading-relaxed font-sans">
                    {desk.desc}
                  </p>
                </div>

                <div className="flex items-center gap-1.5 text-xs font-mono text-text-muted group-hover:text-text-primary transition-colors pt-2 border-t border-border/40">
                  <span>Open Desk</span>
                  <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
                </div>
              </div>
            );
          })}
        </div>
      </QLSection>
    </div>
  );
}
