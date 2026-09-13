import { useState, useEffect } from 'react';
import { RefreshCw, Search, LayoutGrid, Layers, Building, ShieldCheck, AlertCircle } from 'lucide-react';
import { api } from '@/lib/api';
import { MockBanner } from '@/components/dashboard/MockBanner';
import { MarketIndexRibbon } from '@/components/terminal/MarketIndexRibbon';
import { SectorHeatmapPanel } from '@/components/terminal/SectorHeatmapPanel';
import { WatchlistPanel } from '@/components/terminal/WatchlistPanel';
import { StockResearchWorkspace } from '@/components/terminal/StockResearchWorkspace';
import { InstrumentSearchModal } from '@/components/terminal/InstrumentSearchModal';
import { GlobalMarketRegimeCard } from '@/components/dashboard/GlobalMarketRegimeCard';
import type { TerminalOverview, InstrumentSearchResult } from '@/types/terminal';

export function MarketIntelligenceTerminalPage() {
  const [activeSymbol, setActiveSymbol] = useState('RELIANCE');
  const [overview, setOverview] = useState<TerminalOverview | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [activeDesk, setActiveDesk] = useState<'workspace' | 'macro' | 'sectors'>('workspace');

  const fetchOverview = () => {
    setLoading(true);
    setError(null);
    api.get<TerminalOverview>('/v1/terminal/overview')
      .then((data) => {
        setOverview(data);
      })
      .catch(() => {
        setError('Failed to fetch real-time terminal overview');
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchOverview();
  }, []);

  const handleSelectInstrument = (inst: InstrumentSearchResult) => {
    setActiveSymbol(inst.symbol);
  };

  return (
    <div className="space-y-5">
      <MockBanner />

      {/* Terminal Title & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-surface p-4 rounded-xl border border-border">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-extrabold gradient-text">Market Intelligence Terminal</h1>
            <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-accent/10 text-accent border border-accent/20 font-bold uppercase">
              Phase 9
            </span>
          </div>
          <p className="text-xs text-text-muted mt-1">
            Multi-panel Indian equity research workspace with point-in-time enforcement
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setIsSearchOpen(true)}
            className="flex items-center gap-2 rounded-lg border border-accent/40 bg-accent-muted/30 px-3.5 py-2 text-xs font-semibold text-accent hover:bg-accent-muted/50 transition-all"
          >
            <Search className="h-4 w-4" />
            <span>Search Security</span>
            <kbd className="hidden sm:inline-block px-1.5 py-0.5 rounded bg-surface border border-border text-[9px] font-mono text-text-muted">
              /
            </kbd>
          </button>

          <button
            onClick={fetchOverview}
            disabled={loading}
            className="flex items-center gap-2 rounded-lg border border-border bg-surface-elevated px-3.5 py-2 text-xs font-medium text-text-secondary hover:text-text-primary hover:bg-surface-elevated/80 transition-all disabled:opacity-50"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-lg border border-red-400/20 bg-red-400/10 px-4 py-3 text-sm text-red-400">
          <AlertCircle className="h-4 w-4" />
          <span>{error} — Backend services may be running in offline/local mock mode.</span>
        </div>
      )}

      {/* Market Benchmark Index Bar */}
      <MarketIndexRibbon
        indices={overview?.majorIndices || [
          { symbol: 'NIFTY 50', name: 'Nifty 50 Benchmark', lastPrice: 24850.0, change: 112.5, changePercent: 0.45, timestamp: new Date().toISOString(), source: 'NSE_DIRECT', status: 'REALTIME' },
          { symbol: 'BANKNIFTY', name: 'Nifty Bank Sectoral', lastPrice: 51200.0, change: -85.0, changePercent: -0.17, timestamp: new Date().toISOString(), source: 'NSE_DIRECT', status: 'REALTIME' },
          { symbol: 'SENSEX', name: 'BSE SENSEX Benchmark', lastPrice: 81400.0, change: 320.0, changePercent: 0.39, timestamp: new Date().toISOString(), source: 'BSE_DIRECT', status: 'REALTIME' },
          { symbol: 'INDIA VIX', name: 'India Volatility Index', lastPrice: 13.45, change: -0.42, changePercent: -3.03, timestamp: new Date().toISOString(), source: 'NSE_DIRECT', status: 'REALTIME' },
        ]}
        provider={overview?.provider || 'NSE_DIRECT'}
        providerStatus={overview?.providerStatus || 'HEALTHY'}
        marketState={overview?.nseMarketStatus?.marketState || 'OPEN'}
        onSelectIndex={(sym) => setActiveSymbol(sym)}
      />

      {/* Multi-Desk Workspace Navigation */}
      <div className="flex items-center justify-between border-b border-border pb-1">
        <div className="flex gap-2">
          {[
            { id: 'workspace', label: 'Stock Research Desk', icon: Building },
            { id: 'sectors', label: 'Sector Heatmap & Breadth', icon: LayoutGrid },
            { id: 'macro', label: 'Global Macro & Regime Desk', icon: Layers },
          ].map((d) => {
            const Icon = d.icon;
            const isSelected = activeDesk === d.id;
            return (
              <button
                key={d.id}
                onClick={() => setActiveDesk(d.id as any)}
                className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-xs font-bold transition-all ${
                  isSelected
                    ? 'bg-accent text-white shadow-sm'
                    : 'text-text-secondary hover:text-text-primary hover:bg-surface-elevated'
                }`}
              >
                <Icon className="h-4 w-4" />
                <span>{d.label}</span>
              </button>
            );
          })}
        </div>

        <div className="hidden sm:flex items-center gap-2 text-xs text-text-muted">
          <ShieldCheck className="h-4 w-4 text-accent" />
          <span>Active Instrument: <strong className="text-text-primary font-bold">{activeSymbol}</strong></span>
        </div>
      </div>

      {/* Desk Content */}
      {activeDesk === 'workspace' && (
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-5">
          {/* Left 3 columns: Stock Research Workspace */}
          <div className="lg:col-span-3 space-y-5">
            <StockResearchWorkspace
              symbol={activeSymbol}
              onOpenSearch={() => setIsSearchOpen(true)}
            />
          </div>

          {/* Right 1 column: Research Watchlist Manager */}
          <div className="lg:col-span-1">
            <WatchlistPanel
              activeSymbol={activeSymbol}
              onSelectSymbol={(sym) => setActiveSymbol(sym)}
              onOpenSearch={() => setIsSearchOpen(true)}
            />
          </div>
        </div>
      )}

      {activeDesk === 'sectors' && (
        <div className="space-y-5">
          <SectorHeatmapPanel
            sectors={overview?.sectors || [
              { sectorName: 'NIFTY IT', performance1D: 1.45, advancingCount: 8, decliningCount: 2, topGainer: 'TCS (+2.1%)', topLoser: 'WIPRO (-0.4%)' },
              { sectorName: 'NIFTY AUTO', performance1D: 0.88, advancingCount: 11, decliningCount: 4, topGainer: 'TATAMOTORS (+1.8%)', topLoser: 'BAJAJ-AUTO (-0.2%)' },
              { sectorName: 'NIFTY PHARMA', performance1D: 0.62, advancingCount: 14, decliningCount: 6, topGainer: 'SUNPHARMA (+1.5%)', topLoser: 'CIPLA (-0.1%)' },
              { sectorName: 'NIFTY BANK', performance1D: -0.17, advancingCount: 5, decliningCount: 7, topGainer: 'ICICIBANK (+0.4%)', topLoser: 'HDFCBANK (-0.8%)' },
              { sectorName: 'NIFTY METAL', performance1D: -0.95, advancingCount: 3, decliningCount: 12, topGainer: 'TATASTEEL (+0.1%)', topLoser: 'HINDALCO (-1.9%)' },
              { sectorName: 'NIFTY FMCG', performance1D: 0.35, advancingCount: 9, decliningCount: 6, topGainer: 'ITC (+1.1%)', topLoser: 'NESTLEIND (-0.5%)' },
            ]}
            onSelectSector={(sec) => console.log('Selected sector:', sec)}
          />
        </div>
      )}

      {activeDesk === 'macro' && (
        <div className="space-y-5">
          <GlobalMarketRegimeCard />
        </div>
      )}

      {/* Security Search Modal */}
      <InstrumentSearchModal
        isOpen={isSearchOpen}
        onClose={() => setIsSearchOpen(false)}
        onSelect={handleSelectInstrument}
      />
    </div>
  );
}
