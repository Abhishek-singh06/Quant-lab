import { useEffect, useState } from 'react';
import { RefreshCw, AlertCircle, Clock } from 'lucide-react';
import { api } from '@/lib/api';
import type { MarketIndex, GlobalMarketSnapshot } from '@/types/market';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLSection } from '@/design-system/QLSection';
import { QLMetric } from '@/design-system/QLMetric';
import { MarketOverviewCard } from '@/components/dashboard/MarketOverviewCard';
import { GlobalMarketsGrid } from '@/components/dashboard/GlobalMarketsGrid';
import { ChartCard } from '@/components/dashboard/ChartCard';
import { SystemStatusCard } from '@/components/dashboard/SystemStatusCard';

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
        setError('Market data feed temporarily delayed — awaiting next polling cycle.');
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchMarketData();
  }, []);

  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Editorial Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>INDIAN &amp; GLOBAL MARKETS</span>
            <span>•</span>
            <span>MULTI-ASSET DATA FEED</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            MARKETS &amp; INDICES
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Indian benchmark index performance, sector structures, and cross-asset global market snapshots.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <span className="text-xs text-text-muted flex items-center gap-1 font-mono">
            <Clock className="w-3.5 h-3.5" />
            {lastRefresh.toLocaleTimeString('en-IN')} IST
          </span>
          <QLButton
            variant="outline"
            size="sm"
            onClick={fetchMarketData}
            disabled={loading}
            icon={<RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />}
          >
            Refresh Feed
          </QLButton>
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-lg border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-xs text-amber-300 font-mono">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* 01 INDIAN BENCHMARKS */}
      <QLSection
        number={1}
        title="Indian Benchmark Performance"
        subtitle="NSE / BSE Major Equity Gauges"
      >
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {indices.slice(0, 4).map((idx) => {
            const isPositive = idx.changePercent >= 0;
            return (
              <QLPanel key={idx.symbol} variant="surface" padding="md">
                <QLMetric
                  label={idx.name}
                  value={idx.lastPrice.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  change={idx.change}
                  changePercent={idx.changePercent}
                  trend={isPositive ? 'up' : 'down'}
                  size="md"
                  provenance={`${idx.symbol} • Delayed ~15m`}
                />
              </QLPanel>
            );
          })}
        </div>
      </QLSection>

      {/* 02 SECTORAL PERFORMANCE & HISTORICAL CURVE */}
      <QLSection
        number={2}
        title="Sector Dynamics & Benchmark Curve"
        subtitle="Sectoral Rotation vs Historical Trajectory"
      >
        <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
          <MarketOverviewCard indices={indices} loading={loading} />
          <ChartCard loading={loading} />
        </div>
      </QLSection>

      {/* 03 GLOBAL CROSS-ASSET SNAPSHOTS */}
      <QLSection
        number={3}
        title="Global Cross-Asset Context"
        subtitle="US, Europe, Asia, Commodities, Sovereign Yields, and FX"
      >
        <GlobalMarketsGrid snapshots={globalSnapshots} loading={loading} />
      </QLSection>

      {/* 04 SYSTEM & FEED OBSERVABILITY */}
      <QLSection
        number={4}
        title="Data Feed Observability"
        subtitle="Connection Latency & Coverage Verification"
      >
        <SystemStatusCard />
      </QLSection>
    </div>
  );
}
