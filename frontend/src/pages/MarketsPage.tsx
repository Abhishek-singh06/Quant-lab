import { useEffect, useState } from 'react';
import { RefreshCw, AlertCircle, TrendingUp, TrendingDown, Clock } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import { MarketOverviewCard } from '@/components/dashboard/MarketOverviewCard';
import { GlobalMarketsGrid } from '@/components/dashboard/GlobalMarketsGrid';
import { ChartCard } from '@/components/dashboard/ChartCard';
import { SystemStatusCard } from '@/components/dashboard/SystemStatusCard';
import type { MarketIndex, GlobalMarketSnapshot } from '@/types/market';
import { api } from '@/lib/api';

export function MarketsPage() {
  const [indices, setIndices] = useState<MarketIndex[]>([]);
  const [globalSnapshots, setGlobalSnapshots] = useState<GlobalMarketSnapshot[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastRefresh, setLastRefresh] = useState(new Date());

  const fetchMarketData = () => {
    setLoading(true);
    setError(null);
    Promise.all([
      api.get<MarketIndex[]>('/v1/market/indices'),
      api.get<GlobalMarketSnapshot[]>('/v1/global-market/snapshots').catch(() => []),
    ])
      .then(([idxData, snapData]) => {
        setIndices(idxData || []);
        setGlobalSnapshots(snapData || []);
        setLastRefresh(new Date());
      })
      .catch(() => {
        setError('Market data feed temporarily unavailable — awaiting next polling cycle.');
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchMarketData();
  }, []);

  return (
    <div className="space-y-6">
      <MockBanner />
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-surface p-4 rounded-xl border border-border">
        <div>
          <h1 className="text-2xl font-bold gradient-text">Markets</h1>
          <p className="text-xs text-text-muted mt-1">
            Indian & Global Multi-Asset Market Data &bull; Feed: Yahoo Finance Delayed (~15m) &bull; Polling Mode
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-xs text-text-muted flex items-center gap-1 font-mono">
            <Clock className="h-3.5 w-3.5 text-text-muted" />
            {lastRefresh.toLocaleTimeString('en-IN')} IST
          </span>
          <button
            onClick={fetchMarketData}
            disabled={loading}
            className="flex items-center gap-2 rounded-lg border border-border bg-surface-elevated px-3 py-1.5 text-xs text-text-secondary hover:text-text-primary hover:bg-surface-elevated/80 transition-colors disabled:opacity-50"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-lg border border-red-400/20 bg-red-400/10 px-4 py-3 text-sm text-red-400">
          <AlertCircle className="h-4 w-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Indian benchmark indices summary cards */}
      {indices.length > 0 && (
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
          {indices.slice(0, 4).map((idx) => {
            const isPositive = idx.changePercent >= 0;
            return (
              <div key={idx.symbol} className="rounded-xl border border-border bg-surface p-4">
                <p className="text-xs text-text-muted">{idx.name}</p>
                <p className="text-xl font-bold font-mono text-text-primary mt-1">
                  {idx.lastPrice.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </p>
                <div className={`flex items-center gap-1 text-xs font-medium font-mono mt-1 ${isPositive ? 'text-green-400' : 'text-red-400'}`}>
                  {isPositive ? <TrendingUp className="h-3 w-3" /> : <TrendingDown className="h-3 w-3" />}
                  {isPositive ? '+' : ''}{idx.change.toFixed(2)} ({isPositive ? '+' : ''}{idx.changePercent.toFixed(2)}%)
                </div>
              </div>
            );
          })}
        </div>
      )}

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <MarketOverviewCard indices={indices} loading={loading} />
        <ChartCard loading={loading} />
      </div>

      <GlobalMarketsGrid snapshots={globalSnapshots} loading={loading} />
      <SystemStatusCard />
    </div>
  );
}
