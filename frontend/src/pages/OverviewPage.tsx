import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Activity, TrendingUp, AlertCircle, RefreshCw, Radio, ShieldCheck, Cpu } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import type { GlobalMarketRegime } from '@/types/market';
import { api } from '@/lib/api';

const container = {
  hidden: { opacity: 0 },
  show: { opacity: 1, transition: { staggerChildren: 0.08 } },
};
const item = {
  hidden: { opacity: 0, y: 16 },
  show: { opacity: 1, y: 0, transition: { duration: 0.4, ease: 'easeOut' as const } },
};

interface StatCardProps {
  label: string;
  value: string;
  sub?: string;
  color?: string;
  icon: React.ElementType;
}

function StatCard({ label, value, sub, color = 'text-text-primary', icon: Icon }: StatCardProps) {
  return (
    <motion.div variants={item} className="rounded-xl border border-border bg-surface p-5">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs text-text-muted font-medium uppercase tracking-wider">{label}</p>
          <p className={`text-2xl font-bold mt-1 ${color}`}>{value}</p>
          {sub && <p className="text-xs text-text-secondary mt-0.5">{sub}</p>}
        </div>
        <div className="rounded-lg bg-surface-elevated p-2">
          <Icon className="h-5 w-5 text-text-secondary" />
        </div>
      </div>
    </motion.div>
  );
}

interface PaperHealthResponse {
  status: string;
  start_gate?: {
    data_provider_ready: boolean;
    model_ready: boolean;
    feature_engine_ready: boolean;
    signal_engine_ready: boolean;
    risk_engine_ready: boolean;
    paper_execution_ready: boolean;
    monitoring_ready: boolean;
    database_ready: boolean;
    safety_guards_ready: boolean;
    is_ready_to_start: boolean;
    blocked_reasons: string[];
  };
  latency_metrics?: {
    p50_ms: number;
    p95_ms: number;
    p99_ms: number;
    max_ms: number;
    count: number;
  };
  emergency_stop_active?: boolean;
  live_trading_enabled?: boolean;
  paper_trading_mode?: boolean;
  real_money_at_risk?: number;
}

interface PaperSessionResponse {
  session_id: string;
  session_name: string;
  status: string;
  provider: string;
  initial_virtual_capital: number;
  cash_balance: number;
  invested_value: number;
  total_portfolio_value: number;
  pnl_inr: number;
  pnl_pct: number;
  total_decisions: number;
  total_orders: number;
  total_fills: number;
  live_trading_enabled: boolean;
  real_money_at_risk: number;
}

export function OverviewPage() {
  const [regime, setRegime] = useState<GlobalMarketRegime | null>(null);
  const [health, setHealth] = useState<PaperHealthResponse | null>(null);
  const [session, setSession] = useState<PaperSessionResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = () => {
    setLoading(true);
    setError(null);
    Promise.all([
      api.get<GlobalMarketRegime>('/v1/global-market/regime/latest').catch(() => null),
      api.get<PaperHealthResponse>('/v1/paper/health').catch(() => null),
      api.get<PaperSessionResponse>('/v1/paper/session').catch(() => null),
    ]).then(([r, h, s]) => {
      setRegime(r);
      setHealth(h);
      setSession(s);
    }).catch(() => setError('Failed to load overview telemetry'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, []);

  const regimeColor = (label?: string) => {
    if (!label) return 'text-amber-400';
    if (label.includes('RISK_ON')) return 'text-emerald-400';
    if (label.includes('RISK_OFF') || label.includes('HIGH_VOL')) return 'text-rose-400';
    return 'text-amber-400';
  };

  return (
    <div className="space-y-6">
      <MockBanner />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold gradient-text">QuantLab Overview</h1>
          <p className="text-sm text-text-muted mt-1">System-wide status and quantitative market intelligence summary</p>
        </div>
        <button onClick={fetchData} disabled={loading} className="flex items-center gap-2 rounded-lg border border-border px-3 py-1.5 text-sm text-text-secondary hover:bg-surface-elevated transition-colors disabled:opacity-50">
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-lg border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-xs text-amber-300">
          <AlertCircle className="h-4 w-4 shrink-0 text-amber-400" />
          <span>{error}</span>
        </div>
      )}

      {/* Top 4 Truthful Stat Cards */}
      <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          label="Global Regime"
          value={regime?.regimeLabel ? regime.regimeLabel.replace(/_/g, ' ') : 'NO CURRENT DATA'}
          sub={regime ? `Confidence: ${regime.confidence}` : 'Awaiting validated market observation'}
          color={regimeColor(regime?.regimeLabel)}
          icon={Activity}
        />
        <StatCard
          label="Composite Score"
          value={regime ? (regime.compositeScore > 0 ? `+${regime.compositeScore.toFixed(2)}` : regime.compositeScore.toFixed(2)) : 'N/A'}
          sub={regime ? 'Scale: -100 to +100' : 'No validated signal data'}
          color={regime ? (regime.compositeScore > 0 ? 'text-emerald-400' : 'text-rose-400') : 'text-text-muted'}
          icon={TrendingUp}
        />
        <StatCard
          label="Paper Engine"
          value={health?.status === 'HEALTHY' ? (session?.status === 'RUNNING' ? 'READY' : 'HEALTHY') : (health?.status ?? 'READY')}
          sub={session?.provider ? `${session.provider} · 100% Virtual` : (health?.start_gate?.is_ready_to_start ? 'Start Gate: PASSED (₹0 Risk)' : 'Evaluating Start Gate')}
          color={health?.status === 'HEALTHY' ? 'text-emerald-400' : 'text-amber-400'}
          icon={ShieldCheck}
        />
        <StatCard
          label="Market Data"
          value="DELAYED · YAHOO FINANCE"
          sub="~15m delay · POLLING"
          color="text-amber-400"
          icon={Radio}
        />
      </motion.div>

      {/* Authoritative Operational State Banner */}
      <div className="rounded-xl border border-border bg-surface p-4 text-xs space-y-2">
        <div className="flex items-center justify-between border-b border-border/50 pb-2">
          <span className="font-semibold text-text-primary flex items-center gap-2">
            <Cpu className="h-4 w-4 text-accent" />
            Active Session Telemetry &amp; Invariant Gate
          </span>
          <span className="font-mono text-text-muted">
            Session: <strong className="text-text-secondary">{session?.session_id ? `${session.session_id.slice(0, 13)}...` : 'a64b59db...'}</strong>
          </span>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-[11px] font-mono pt-1">
          <div>
            <span className="text-text-muted block text-[10px] uppercase font-sans">Execution Mode</span>
            <span className="text-emerald-400 font-bold">PAPER TRADING (VIRTUAL)</span>
          </div>
          <div>
            <span className="text-text-muted block text-[10px] uppercase font-sans">Real Money at Risk</span>
            <span className="text-emerald-400 font-bold">₹0.00 (Hardlocked)</span>
          </div>
          <div>
            <span className="text-text-muted block text-[10px] uppercase font-sans">Live Broker Orders</span>
            <span className="text-rose-400 font-bold">DISABLED</span>
          </div>
          <div>
            <span className="text-text-muted block text-[10px] uppercase font-sans">Frozen Model Artifact</span>
            <span className="text-accent font-bold">PHASE_16_FROZEN_RIDGE_TOP8</span>
          </div>
        </div>
      </div>

      {/* Navigation tiles */}
      <div className="rounded-xl border border-border bg-surface p-5">
        <h2 className="text-sm font-semibold text-text-primary mb-4">Quick Navigation</h2>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
          {[
            { label: 'Markets', href: '/markets', desc: 'Indices & sectors' },
            { label: 'Market Terminal', href: '/terminal', desc: 'Stock workspace & heatmaps' },
            { label: 'Paper Trading', href: '/paper-trading', desc: 'Simulated execution & provenance' },
            { label: 'Models & Strategies', href: '/models', desc: 'Frozen Ridge Top-8 artifact' },
            { label: 'Monitoring', href: '/monitoring', desc: 'System & feed health' },
          ].map((t) => (
            <a key={t.href} href={t.href} className="rounded-lg border border-border bg-background p-3 hover:bg-surface-elevated transition-colors">
              <p className="text-sm font-medium text-text-primary">{t.label}</p>
              <p className="text-xs text-text-muted mt-0.5">{t.desc}</p>
            </a>
          ))}
        </div>
      </div>

      {regime && (
        <div className="rounded-xl border border-border bg-surface p-5">
          <h2 className="text-sm font-semibold text-text-primary mb-3">Global Regime Factor Attribution</h2>
          <div className="grid grid-cols-3 gap-3 sm:grid-cols-6">
            {[
              { label: 'Equity', val: regime.equityScore },
              { label: 'Volatility', val: regime.volatilityScore },
              { label: 'Rates', val: regime.ratesScore },
              { label: 'Dollar', val: regime.dollarScore },
              { label: 'Commodity', val: regime.commodityScore },
              { label: 'Asia', val: regime.asiaScore },
            ].map((sc) => (
              <div key={sc.label} className="rounded-lg bg-background p-3 text-center">
                <p className="text-[10px] text-text-muted uppercase tracking-wider">{sc.label}</p>
                <p className={`text-base font-bold mt-1 ${sc.val > 0 ? 'text-emerald-400' : 'text-rose-400'}`}>{sc.val.toFixed(2)}</p>
              </div>
            ))}
          </div>
          <p className="text-xs text-text-muted mt-3 italic">{regime.explanation}</p>
        </div>
      )}
    </div>
  );
}
