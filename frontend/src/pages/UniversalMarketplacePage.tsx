import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search,
  ArrowRight
} from 'lucide-react';
import { QLPanel } from '@/design-system/QLPanel';
import { QLBadge } from '@/design-system/QLBadge';
import { QLEmptyState } from '@/design-system/QLEmptyState';
import { QLSection } from '@/design-system/QLSection';
import { cn } from '@/lib/utils';

export function UniversalMarketplacePage() {
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [segment, setSegment] = useState<'ALL' | 'NSE' | 'BSE' | 'MUTUAL_FUNDS' | 'COMMODITIES'>('ALL');
  const [instruments, setInstruments] = useState<any[]>([]);
  const [mutualFunds, setMutualFunds] = useState<any[]>([]);
  const [commodities, setCommodities] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchResults = async () => {
    setLoading(true);
    try {
      if (segment === 'MUTUAL_FUNDS') {
        const res = await fetch(`/api/v1/mutual-funds/search?query=${encodeURIComponent(query || 'HDFC')}`);
        if (res.ok) setMutualFunds(await res.json());
      } else if (segment === 'COMMODITIES') {
        const res = await fetch(`/api/v1/commodities${query ? `?symbol=${encodeURIComponent(query)}` : ''}`);
        if (res.ok) setCommodities(await res.json());
      } else {
        const searchQ = query.trim() || 'A';
        const exchangeFilter = segment === 'NSE' ? '&exchange=NSE' : segment === 'BSE' ? '&exchange=BSE' : '';
        const res = await fetch(`/api/v1/instruments/search?query=${encodeURIComponent(searchQ)}${exchangeFilter}`);
        if (res.ok) setInstruments(await res.json());
      }
    } catch {
      // Safe fallback
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchResults();
  }, [query, segment]);

  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>UNIVERSAL MULTI-ASSET MARKETPLACE</span>
            <span>•</span>
            <span>REAL EXCHANGE REGISTRY</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            MARKETPLACE
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Search and evaluate supported NSE &amp; BSE Equities, AMFI Mutual Funds, and MCX Commodities.
          </p>
        </div>

        <div className="flex items-center gap-2 font-mono text-xs">
          <QLBadge variant="positive" size="sm" dot>
            REAL REGISTRY ACTIVE
          </QLBadge>
        </div>
      </div>

      {/* Universal Search & Segment Filter Bar */}
      <QLPanel variant="surface" padding="md" className="border-border">
        <div className="space-y-4">
          <div className="relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-accent" />
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search stocks, ETFs, mutual funds, commodities (e.g. RELIANCE, HDFC, GOLD, INF179K01BE2)..."
              className="w-full bg-surface-elevated border border-border rounded-xl pl-12 pr-4 py-3.5 text-sm sm:text-base text-text-primary placeholder:text-text-muted font-sans focus:outline-none focus:border-accent transition-colors"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2 pt-1 font-mono text-xs">
            {[
              { id: 'ALL', label: 'ALL MARKETS' },
              { id: 'NSE', label: 'NSE EQUITIES' },
              { id: 'BSE', label: 'BSE EQUITIES' },
              { id: 'MUTUAL_FUNDS', label: 'MUTUAL FUNDS (AMFI)' },
              { id: 'COMMODITIES', label: 'COMMODITIES (MCX)' },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setSegment(tab.id as any)}
                className={cn(
                  'px-3 py-1.5 rounded-lg transition-all font-bold',
                  segment === tab.id
                    ? 'bg-accent text-white shadow-sm'
                    : 'bg-surface-elevated text-text-muted hover:text-text-primary border border-border/50'
                )}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>
      </QLPanel>

      {/* Results Section */}
      <QLSection
        number={1}
        title={
          segment === 'MUTUAL_FUNDS'
            ? 'AMFI Mutual Fund Schemes'
            : segment === 'COMMODITIES'
            ? 'MCX Commodity Derivatives'
            : 'Exchange Listed Instruments'
        }
        subtitle="Ranked Real-Time Ingestion Universe"
      >
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 animate-pulse">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="h-32 bg-surface-elevated/50 rounded-xl" />
            ))}
          </div>
        ) : segment === 'MUTUAL_FUNDS' ? (
          mutualFunds.length === 0 ? (
            <QLEmptyState
              title="NO MUTUAL FUNDS MATCHED"
              description="No schemes found matching the query in the AMFI disclosure database."
              source="AMFI MONTHLY DISCLOSURES"
              type="empty"
            />
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {mutualFunds.map((mf) => (
                <div
                  key={mf.scheme_code}
                  className="p-4 rounded-xl border border-border bg-surface-elevated/40 hover:border-border-strong transition-all space-y-3 flex flex-col justify-between"
                >
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-mono font-bold text-accent">{mf.category}</span>
                      <QLBadge variant="neutral" size="xs">{mf.plan}</QLBadge>
                    </div>
                    <h3 className="text-sm font-bold text-text-primary font-sans leading-snug">
                      {mf.scheme_name}
                    </h3>
                    <p className="text-[11px] font-mono text-text-muted">
                      AMC: {mf.amc} &bull; Code: {mf.scheme_code}
                    </p>
                  </div>

                  <div className="pt-3 border-t border-border/50 flex items-center justify-between font-mono text-xs">
                    <div>
                      <span className="text-[10px] text-text-muted block">NAV ({mf.nav_date})</span>
                      <span className="text-base font-black text-text-primary block mt-0.5">₹{mf.nav?.toFixed(2)}</span>
                    </div>
                    <div className="text-right">
                      <span className="text-[10px] text-text-muted block">1Y RETURN</span>
                      <span className="text-sm font-bold text-emerald-400 block mt-0.5">+{mf.returns_1y}%</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )
        ) : segment === 'COMMODITIES' ? (
          commodities.length === 0 ? (
            <QLEmptyState
              title="NO COMMODITY CONTRACTS MATCHED"
              description="No active commodity contracts found for the query."
              source="MCX DERIVATIVES"
              type="empty"
            />
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {commodities.map((comm) => (
                <div
                  key={comm.contract_id}
                  className="p-4 rounded-xl border border-border bg-surface-elevated/40 hover:border-border-strong transition-all space-y-3 flex flex-col justify-between"
                >
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between">
                      <span className="text-base font-black font-mono text-text-primary">{comm.symbol}</span>
                      <QLBadge variant="warning" size="xs">MCX FUTURES</QLBadge>
                    </div>
                    <h3 className="text-xs text-text-secondary font-sans leading-snug">
                      {comm.name}
                    </h3>
                    <p className="text-[11px] font-mono text-text-muted">
                      Expiry: {comm.expiry} &bull; Lot: {comm.unit}
                    </p>
                  </div>

                  <div className="pt-3 border-t border-border/50 flex items-center justify-between font-mono text-xs">
                    <div>
                      <span className="text-[10px] text-text-muted block">LAST PRICE</span>
                      <span className="text-base font-black text-text-primary block mt-0.5">₹{comm.last_price?.toLocaleString('en-IN')}</span>
                    </div>
                    <div className="text-right">
                      <span className="text-[10px] text-text-muted block">CHANGE</span>
                      <span className={cn('text-sm font-bold block mt-0.5', comm.change >= 0 ? 'text-emerald-400' : 'text-rose-400')}>
                        {comm.change >= 0 ? '+' : ''}{comm.change?.toFixed(2)} ({comm.changePercent?.toFixed(2)}%)
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )
        ) : instruments.length === 0 ? (
          <QLEmptyState
            title="NO INSTRUMENTS MATCHED"
            description="No securities found matching the query in the security master database."
            source="NSE / BSE REGISTRY"
            type="empty"
          />
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {instruments.map((inst) => (
              <div
                key={inst.instrument_id}
                onClick={() => navigate(`/instrument/${inst.exchange}/${inst.symbol}`)}
                className="group p-4 rounded-xl border border-border bg-surface-elevated/40 hover:bg-surface-elevated hover:border-border-strong cursor-pointer transition-all space-y-3 flex flex-col justify-between"
              >
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between">
                    <span className="text-base font-black font-mono text-text-primary group-hover:text-accent transition-colors">
                      {inst.symbol}
                    </span>
                    <QLBadge variant="neutral" size="xs">
                      {inst.exchange}
                    </QLBadge>
                  </div>
                  <h3 className="text-xs font-semibold text-text-secondary font-sans leading-snug truncate">
                    {inst.company_name}
                  </h3>
                  <div className="flex items-center gap-2 text-[10px] font-mono text-text-muted">
                    <span className="truncate">{inst.sector || 'Equities'}</span>
                    <span>&bull;</span>
                    <span>{inst.isin || 'INE...'}</span>
                  </div>
                </div>

                <div className="pt-2 border-t border-border/50 flex items-center justify-between text-xs font-mono text-text-muted group-hover:text-text-primary transition-colors">
                  <span>Open Intelligence View</span>
                  <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
                </div>
              </div>
            ))}
          </div>
        )}
      </QLSection>
    </div>
  );
}
