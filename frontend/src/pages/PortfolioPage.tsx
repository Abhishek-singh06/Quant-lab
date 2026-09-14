import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RefreshCw, AlertCircle, Link as LinkIcon, AlertTriangle } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { QLBadge } from '@/design-system/QLBadge';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLSection } from '@/design-system/QLSection';
import { QLMetric } from '@/design-system/QLMetric';
import { QLEmptyState } from '@/design-system/QLEmptyState';
import { cn } from '@/lib/utils';

export function PortfolioPage() {
  const navigate = useNavigate();
  const { token } = useAuth();
  const [portfolio, setPortfolio] = useState<any | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPortfolio = async () => {
    setLoading(true);
    setError(null);
    try {
      const headers: Record<string, string> = {};
      if (token) headers['Authorization'] = `Bearer ${token}`;

      const res = await fetch('/api/v1/portfolio', { headers });
      if (res.ok) {
        setPortfolio(await res.json());
      } else if (res.status === 401) {
        setPortfolio(null);
      } else {
        setError('Portfolio data temporarily unavailable.');
      }
    } catch {
      setError('Unable to reach portfolio service.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPortfolio();
  }, [token]);

  const getActionBadgeVariant = (action?: string): 'positive' | 'negative' | 'warning' | 'neutral' => {
    if (!action) return 'neutral';
    if (['BUY', 'ACCUMULATE'].includes(action)) return 'positive';
    if (['SELL', 'AVOID ADDING', 'EXIT CONSIDERATION'].includes(action)) return 'negative';
    if (['HOLD', 'WATCH', 'REDUCE'].includes(action)) return 'warning';
    return 'neutral';
  };

  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>PERSONALIZED PORTFOLIO INTELLIGENCE DESK</span>
            <span>•</span>
            <span>AUTHORIZED BROKER SYNCHRONIZATION</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            YOUR PORTFOLIO
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Real-time portfolio mark-to-market valuations, allocation distribution, concentration risk, and holding intelligence.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <QLButton
            variant="outline"
            size="sm"
            onClick={fetchPortfolio}
            disabled={loading}
          >
            <RefreshCw className={cn('w-3.5 h-3.5', loading && 'animate-spin text-accent')} />
            <span>Refresh</span>
          </QLButton>
          <QLButton
            variant="primary"
            size="sm"
            onClick={() => navigate('/connect-broker')}
          >
            <LinkIcon className="w-3.5 h-3.5 mr-1" />
            <span>Manage Brokers</span>
          </QLButton>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl border border-rose-500/30 bg-rose-500/10 text-rose-300 text-xs font-mono flex items-center gap-2.5">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 animate-pulse">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-28 bg-surface-elevated/50 rounded-xl" />
          ))}
        </div>
      ) : !portfolio || !portfolio.has_portfolio || !portfolio.broker_connected ? (
        /* Empty / Disconnected State */
        <QLSection
          number={1}
          title="Portfolio Integration"
          subtitle="Connect Authorized Broker Account"
        >
          <QLEmptyState
            title="NO PORTFOLIO CONNECTED"
            description="Link your official Groww or Zerodha broker account to import your live holdings, track true P&L, and receive personalized holding intelligence."
            source="BROKER GATEWAY / SEBI AUTHORIZED APIS"
            type="unconnected"
            actionLabel="Connect Broker Account"
            onAction={() => navigate('/connect-broker')}
            nextAction="Authorize your broker API token in Settings to synchronize your real portfolio."
          />
        </QLSection>
      ) : (
        <div className="space-y-10">
          {/* 01 PORTFOLIO METRICS SUMMARY */}
          <QLSection
            number={1}
            title="Portfolio Valuation & P&L"
            subtitle={`Source: ${portfolio.source_provider} • Last Synced: ${new Date(portfolio.updated_at).toLocaleTimeString('en-IN')}`}
          >
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
              <QLMetric
                label="TOTAL INVESTED"
                value={`₹${portfolio.total_invested?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
                subtext="Cost Basis"
                size="md"
              />

              <QLMetric
                label="CURRENT VALUE"
                value={`₹${portfolio.current_value?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
                subtext="Mark-to-Market"
                size="md"
              />

              <QLMetric
                label="TOTAL P&L"
                value={`₹${portfolio.total_pnl?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
                changePercent={portfolio.total_pnl_pct}
                trend={(portfolio.total_pnl ?? 0) >= 0 ? 'up' : 'down'}
                subtext="All-time Return"
                size="md"
              />

              <QLMetric
                label="TODAY'S P&L"
                value={`₹${(portfolio.today_pnl ?? 0).toFixed(2)}`}
                changePercent={portfolio.today_pnl_pct}
                trend={(portfolio.today_pnl ?? 0) >= 0 ? 'up' : 'down'}
                subtext="Daily Session"
                size="md"
              />

              <QLMetric
                label="CASH / MARGIN"
                value={`₹${portfolio.cash_balance?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
                subtext="Available Liquidity"
                size="md"
              />

              <QLMetric
                label="CONCENTRATION"
                value={portfolio.concentration_risk}
                subtext="Risk Level"
                size="md"
              />
            </div>
          </QLSection>

          {/* Attention Items Banner */}
          {portfolio.attention_items?.length > 0 && (
            <div className="p-4 rounded-xl border border-amber-500/30 bg-amber-500/10 space-y-2 text-xs font-mono text-amber-300">
              <div className="flex items-center gap-2 font-bold text-amber-200">
                <AlertTriangle className="w-4 h-4" />
                <span>WHAT NEEDS YOUR ATTENTION:</span>
              </div>
              <div className="space-y-1">
                {portfolio.attention_items.map((item: any, idx: number) => (
                  <p key={idx} className="text-amber-100 font-sans leading-relaxed">
                    &bull; <strong>{item.symbol}:</strong> {item.message}
                  </p>
                ))}
              </div>
            </div>
          )}

          {/* 02 HOLDINGS TABLE */}
          <QLSection
            number={2}
            title="Demat Holdings & Intelligence"
            subtitle={`${portfolio.holdings?.length || 0} Synchronized Positions`}
          >
            <div className="overflow-x-auto rounded-xl border border-border bg-surface font-mono text-xs">
              <table className="w-full text-left">
                <thead className="bg-surface-elevated text-text-muted text-[11px] uppercase border-b border-border">
                  <tr>
                    <th className="px-4 py-3">Instrument</th>
                    <th className="px-4 py-3 text-right">Quantity</th>
                    <th className="px-4 py-3 text-right">Avg Price</th>
                    <th className="px-4 py-3 text-right">Current Price</th>
                    <th className="px-4 py-3 text-right">Current Value</th>
                    <th className="px-4 py-3 text-right">Unrealized P&amp;L</th>
                    <th className="px-4 py-3 text-right">Weight</th>
                    <th className="px-4 py-3">QuantLab Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border/40">
                  {portfolio.holdings?.map((h: any) => (
                    <tr
                      key={h.id}
                      onClick={() => navigate(`/instrument/${h.exchange}/${h.symbol}`)}
                      className="hover:bg-surface-elevated/40 cursor-pointer transition-colors"
                    >
                      <td className="px-4 py-3.5">
                        <div className="flex items-baseline gap-2">
                          <span className="font-bold text-text-primary text-sm">{h.symbol}</span>
                          <span className="text-[10px] text-text-muted">{h.exchange}</span>
                        </div>
                      </td>
                      <td className="px-4 py-3.5 text-right">{h.quantity}</td>
                      <td className="px-4 py-3.5 text-right">₹{h.average_price?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                      <td className="px-4 py-3.5 text-right font-bold text-text-primary">₹{h.current_price?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                      <td className="px-4 py-3.5 text-right font-bold">₹{h.current_value?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                      <td className={cn('px-4 py-3.5 text-right font-bold', h.unrealized_pnl >= 0 ? 'text-emerald-400' : 'text-rose-400')}>
                        {h.unrealized_pnl >= 0 ? '+' : ''}₹{h.unrealized_pnl?.toLocaleString('en-IN', { minimumFractionDigits: 2 })} ({h.unrealized_pnl >= 0 ? '+' : ''}{h.pnl_pct?.toFixed(2)}%)
                      </td>
                      <td className="px-4 py-3.5 text-right font-bold text-accent">{h.weight_pct?.toFixed(1)}%</td>
                      <td className="px-4 py-3.5">
                        <QLBadge variant={getActionBadgeVariant(h.decision_action)} size="xs">
                          {h.decision_action || 'HOLD'}
                        </QLBadge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </QLSection>

          {/* 03 ALLOCATION & CONTRIBUTORS */}
          <QLSection
            number={3}
            title="Portfolio Asset Distribution & Drivers"
            subtitle="Performance Attribution & Sector Exposure"
          >
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <QLPanel variant="surface" padding="md" title="ASSET ALLOCATION">
                <div className="space-y-3 font-mono text-xs pt-1">
                  {Object.entries(portfolio.asset_allocation || {}).map(([asset, pct]: [string, any]) => (
                    <div key={asset} className="space-y-1">
                      <div className="flex justify-between">
                        <span className="text-text-muted">{asset}</span>
                        <span className="font-bold text-text-primary">{pct?.toFixed(1)}%</span>
                      </div>
                      <div className="h-2 rounded-full bg-surface-elevated overflow-hidden">
                        <div className="h-full bg-accent rounded-full" style={{ width: `${pct}%` }} />
                      </div>
                    </div>
                  ))}
                </div>
              </QLPanel>

              <QLPanel variant="surface" padding="md" title="TOP DRIVERS (P&L)">
                <div className="space-y-2 font-mono text-xs pt-1">
                  <span className="text-[11px] text-text-muted uppercase block">Top Contributors:</span>
                  {portfolio.top_contributors?.map((c: any) => (
                    <div key={c.symbol} className="flex justify-between py-1 border-b border-border/40 text-emerald-400 font-bold">
                      <span>{c.symbol}</span>
                      <span>+₹{c.pnl?.toLocaleString('en-IN')} (+{c.pnl_pct?.toFixed(1)}%)</span>
                    </div>
                  ))}
                  {(!portfolio.top_contributors || portfolio.top_contributors.length === 0) && (
                    <p className="text-text-muted italic text-[11px]">None</p>
                  )}

                  <span className="text-[11px] text-text-muted uppercase block pt-2">Top Detractors:</span>
                  {portfolio.top_detractors?.map((d: any) => (
                    <div key={d.symbol} className="flex justify-between py-1 border-b border-border/40 text-rose-400 font-bold">
                      <span>{d.symbol}</span>
                      <span>₹{d.pnl?.toLocaleString('en-IN')} ({d.pnl_pct?.toFixed(1)}%)</span>
                    </div>
                  ))}
                  {(!portfolio.top_detractors || portfolio.top_detractors.length === 0) && (
                    <p className="text-text-muted italic text-[11px]">None</p>
                  )}
                </div>
              </QLPanel>
            </div>
          </QLSection>
        </div>
      )}
    </div>
  );
}
