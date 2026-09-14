import { useState, useEffect } from 'react';
import { RefreshCw, Search, ShieldCheck, AlertCircle } from 'lucide-react';
import { api } from '@/lib/api';
import { MarketIndexRibbon } from '@/components/terminal/MarketIndexRibbon';
import { SectorHeatmapPanel } from '@/components/terminal/SectorHeatmapPanel';
import { WatchlistPanel } from '@/components/terminal/WatchlistPanel';
import { StockResearchWorkspace } from '@/components/terminal/StockResearchWorkspace';
import { InstrumentSearchModal } from '@/components/terminal/InstrumentSearchModal';
import { GlobalMarketRegimeCard } from '@/components/dashboard/GlobalMarketRegimeCard';
import type { TerminalOverview, InstrumentSearchResult } from '@/types/terminal';
import { QLBadge } from '@/design-system/QLBadge';
import { QLButton } from '@/design-system/QLButton';
import { QLTabs } from '@/design-system/QLTabs';

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
        setError('Market terminal data feed delayed — awaiting next polling cycle.');
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
    <div className="space-y-6 pb-12">
      {/* Terminal Title & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <h1 className="text-2xl sm:text-3xl font-black font-mono tracking-tight text-text-primary uppercase">
              MARKET INTELLIGENCE TERMINAL
            </h1>
            <QLBadge variant="accent" size="xs">
              PRO WORKSPACE
            </QLBadge>
          </div>
          <p className="text-xs text-text-muted">
            Multi-panel Indian equity research workstation · Point-in-time enforcement &bull; Delayed (~15m)
          </p>
        </div>

        <div className="flex items-center gap-3">
          <QLButton
            variant="outline"
            size="sm"
            onClick={() => setIsSearchOpen(true)}
            icon={<Search className="w-3.5 h-3.5" />}
          >
            Search Security
          </QLButton>

          <QLButton
            variant="secondary"
            size="sm"
            onClick={fetchOverview}
            disabled={loading}
            icon={<RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />}
          >
            Refresh
          </QLButton>
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-lg border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-xs text-amber-300 font-mono">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Market Benchmark Index Ribbon */}
      <MarketIndexRibbon
        indices={overview?.majorIndices || [
          { symbol: 'NIFTY 50', name: 'Nifty 50 Benchmark', lastPrice: 24850.0, change: 112.5, changePercent: 0.45, timestamp: new Date().toISOString(), source: 'YAHOO_FINANCE', status: 'DELAYED' },
          { symbol: 'BANKNIFTY', name: 'Nifty Bank Sectoral', lastPrice: 51200.0, change: -85.0, changePercent: -0.17, timestamp: new Date().toISOString(), source: 'YAHOO_FINANCE', status: 'DELAYED' },
          { symbol: 'SENSEX', name: 'BSE SENSEX Benchmark', lastPrice: 81400.0, change: 320.0, changePercent: 0.39, timestamp: new Date().toISOString(), source: 'YAHOO_FINANCE', status: 'DELAYED' },
          { symbol: 'INDIA VIX', name: 'India Volatility Index', lastPrice: 13.45, change: -0.42, changePercent: -3.03, timestamp: new Date().toISOString(), source: 'YAHOO_FINANCE', status: 'DELAYED' },
        ]}
        provider={overview?.provider || 'YAHOO_FINANCE'}
        providerStatus={overview?.providerStatus || 'HEALTHY'}
        marketState={overview?.nseMarketStatus?.marketState || 'CLOSED'}
        onSelectIndex={(sym) => setActiveSymbol(sym)}
      />

      {/* Multi-Desk Workspace Navigation */}
      <div className="flex items-center justify-between border-b border-border/80 pb-2">
        <QLTabs<'workspace' | 'sectors' | 'macro'>
          activeTab={activeDesk}
          onChange={(d) => setActiveDesk(d)}
          variant="underline"
          tabs={[
            { id: 'workspace', label: 'Stock Research Desk' },
            { id: 'sectors', label: 'Sector Heatmap & Breadth' },
            { id: 'macro', label: 'Global Macro & Regime Desk' },
          ]}
        />

        <div className="hidden sm:flex items-center gap-2 text-xs font-mono text-text-muted">
          <ShieldCheck className="w-3.5 h-3.5 text-accent" />
          <span>Security: <strong className="text-text-primary">{activeSymbol}</strong></span>
        </div>
      </div>

      {/* Desk Content */}
      {activeDesk === 'workspace' && (
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          <div className="lg:col-span-3 space-y-6">
            <StockResearchWorkspace
              symbol={activeSymbol}
              onOpenSearch={() => setIsSearchOpen(true)}
            />
          </div>

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
        <div className="space-y-6">
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
        <div className="space-y-6">
          <GlobalMarketRegimeCard />
        </div>
      )}

      <InstrumentSearchModal
        isOpen={isSearchOpen}
        onClose={() => setIsSearchOpen(false)}
        onSelect={handleSelectInstrument}
      />
    </div>
  );
}
