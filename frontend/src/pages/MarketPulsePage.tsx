import { useState, useEffect } from 'react';
import { RefreshCw, AlertCircle, Clock } from 'lucide-react';
import { api } from '@/lib/api';
import type { GlobalMarketRegime, MarketRegime, GlobalMarketSnapshot } from '@/types/market';
import { QLButton } from '@/design-system/QLButton';
import { QLSection } from '@/design-system/QLSection';
import { GlobalMarketsGrid } from '@/components/dashboard/GlobalMarketsGrid';
import { GlobalMarketRegimeCard } from '@/components/dashboard/GlobalMarketRegimeCard';
import { MarketRegimeTerminalCard } from '@/components/dashboard/MarketRegimeTerminalCard';

export function MarketPulsePage() {
  const [globalRegime, setGlobalRegime] = useState<GlobalMarketRegime | null>(null);
  const [marketRegime, setMarketRegime] = useState<MarketRegime | null>(null);
  const [snapshots, setSnapshots] = useState<GlobalMarketSnapshot[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [lastRefresh, setLastRefresh] = useState<Date>(new Date());

  const fetchMarketPulseData = () => {
    setLoading(true);
    setError(null);

    Promise.all([
      api.get<GlobalMarketRegime>('/v1/global-market/regime/latest').catch(() => null),
      api.get<MarketRegime>('/v1/market/regime/latest').catch(() => null),
      api.get<GlobalMarketSnapshot[]>('/v1/global-market/snapshots').catch(() => []),
    ])
      .then(([globalData, indianData, snapData]) => {
        setGlobalRegime(globalData);
        setMarketRegime(indianData);
        setSnapshots(snapData || []);
        setLastRefresh(new Date());
      })
      .catch(() => {
        setError('Market Pulse telemetry temporarily delayed — awaiting next polling cycle.');
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchMarketPulseData();
  }, []);

  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Financial Intelligence Newsroom Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>FINANCIAL INTELLIGENCE DESK</span>
            <span>•</span>
            <span>MULTI-SIGNAL MACRO RADAR</span>
          </div>
          <h1 className="text-3xl sm:text-5xl font-black font-mono tracking-tight text-text-primary uppercase">
            MARKET PULSE
          </h1>
          <p className="text-xs sm:text-sm text-text-secondary mt-2 max-w-2xl leading-relaxed">
            Synthesized macro regimes, Indian market breadth indicators, momentum dispersion, volatility structures, and cross-asset rotations.
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
            onClick={fetchMarketPulseData}
            disabled={loading}
            icon={<RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />}
          >
            Refresh Radar
          </QLButton>
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-lg border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-xs text-amber-300 font-mono">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* 01 GLOBAL MACRO REGIME */}
      <QLSection
        number={1}
        title="Global Macro Regime"
        subtitle="Multi-Asset Scorecard & Sovereign Flow Direction"
      >
        <GlobalMarketRegimeCard regime={globalRegime} loading={loading} />
      </QLSection>

      {/* 02 INDIAN MARKET BREADTH & STRUCTURE */}
      <QLSection
        number={2}
        title="Indian Market Breadth & Momentum"
        subtitle="Advance / Decline, 20D SMA Participation, and Internal Volatility"
      >
        <MarketRegimeTerminalCard regime={marketRegime} loading={loading} />
      </QLSection>

      {/* 03 CROSS-ASSET SNAPSHOTS */}
      <QLSection
        number={3}
        title="Cross-Asset Global Command Center"
        subtitle="Equities, Commodities, Currencies, and Rates"
      >
        <GlobalMarketsGrid snapshots={snapshots} loading={loading} />
      </QLSection>
    </div>
  );
}
