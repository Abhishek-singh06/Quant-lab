import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search,
  RefreshCw,
  ArrowRight,
  ChevronDown,
  Database
} from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { QLBadge } from '@/design-system/QLBadge';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLSection } from '@/design-system/QLSection';
import { QLEmptyState } from '@/design-system/QLEmptyState';
import { cn } from '@/lib/utils';

interface MutualFundScheme {
  scheme_code: string;
  scheme_name: string;
  isin?: string;
  amc: string;
  category: string;
  plan: string;
  option: string;
  nav?: number;
  nav_date?: string;
  benchmark?: string;
  risk_o_meter?: string;
  source?: string;
  source_timestamp?: string;
}

interface AMFIStatus {
  provider: string;
  status: string;
  total_schemes: number;
  total_nav_records: number;
  last_synchronized_at?: string;
  data_nature: string;
  is_realtime: boolean;
}

export function MutualFundsPage() {
  const navigate = useNavigate();
  const { token } = useAuth();
  
  const [status, setStatus] = useState<AMFIStatus | null>(null);
  const [schemes, setSchemes] = useState<MutualFundScheme[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [dropdownQuery, setDropdownQuery] = useState('');
  const [dropdownResults, setDropdownResults] = useState<MutualFundScheme[]>([]);
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [userMfHoldings, setUserMfHoldings] = useState<any[]>([]);

  // Fetch AMFI status
  const fetchStatus = async () => {
    try {
      const res = await fetch('/api/v1/mutual-funds/status');
      if (res.ok) {
        setStatus(await res.json());
      }
    } catch {
      // Safe fallback
    }
  };

  // Search schemes dynamically from SQLite database
  const fetchSchemes = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (searchQuery.trim()) params.append('query', searchQuery.trim());
      if (selectedCategory !== 'ALL') params.append('category', selectedCategory);
      params.append('limit', '40');

      const res = await fetch(`/api/v1/mutual-funds/search?${params.toString()}`);
      if (res.ok) {
        setSchemes(await res.json());
      }
    } catch {
      // Safe fallback
    } finally {
      setLoading(false);
    }
  };

  // Search for dropdown modal
  const fetchDropdownResults = async (q: string) => {
    if (!q.trim()) {
      setDropdownResults([]);
      return;
    }
    try {
      const res = await fetch(`/api/v1/mutual-funds/search?query=${encodeURIComponent(q.trim())}&limit=8`);
      if (res.ok) {
        setDropdownResults(await res.json());
      }
    } catch {
      // Safe fallback
    }
  };

  // Trigger on-demand sync from AMFI
  const handleSyncAMFI = async () => {
    setSyncing(true);
    try {
      const res = await fetch('/api/v1/mutual-funds/sync', { method: 'POST' });
      if (res.ok) {
        await fetchStatus();
        await fetchSchemes();
      }
    } catch {
      // Safe fallback
    } finally {
      setSyncing(false);
    }
  };

  useEffect(() => {
    fetchStatus();
    fetchSchemes();
  }, [selectedCategory]);

  // Debounced search query
  useEffect(() => {
    const handler = setTimeout(() => {
      fetchSchemes();
    }, 250);
    return () => clearTimeout(handler);
  }, [searchQuery]);

  // Dropdown query debounce
  useEffect(() => {
    const handler = setTimeout(() => {
      fetchDropdownResults(dropdownQuery);
    }, 200);
    return () => clearTimeout(handler);
  }, [dropdownQuery]);

  // Load user mutual fund holdings if authenticated
  useEffect(() => {
    if (token) {
      fetch('/api/v1/portfolio/holdings', {
        headers: { Authorization: `Bearer ${token}` }
      })
        .then((r) => r.ok ? r.json() : [])
        .then((data: any[]) => {
          const mfOnly = data.filter((h) => h.asset_class === 'MUTUAL_FUND' || h.symbol?.startsWith('INF'));
          setUserMfHoldings(mfOnly);
        })
        .catch(() => setUserMfHoldings([]));
    }
  }, [token]);

  const categories = [
    { label: 'All Schemes', val: 'ALL' },
    { label: 'Equity / Growth', val: 'Equity' },
    { label: 'Flexi / Multi Cap', val: 'Flexi Cap' },
    { label: 'Large Cap', val: 'Large Cap' },
    { label: 'Mid Cap', val: 'Mid Cap' },
    { label: 'Small Cap', val: 'Small Cap' },
    { label: 'Debt & Liquid', val: 'Debt' },
    { label: 'Index & ETF', val: 'Index' },
  ];

  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>DOMESTIC ASSET MANAGEMENT INTELLIGENCE</span>
            <span>•</span>
            <span>OFFICIAL AMFI LIVE FEED</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            MUTUAL FUNDS
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Real AMFI scheme universe, daily Net Asset Values (NAV), calculated historical performance, and portfolio disclosures.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <QLBadge
            variant={status?.status === 'CONNECTED' ? 'positive' : 'warning'}
            size="sm"
            dot
          >
            {status?.status === 'CONNECTED'
              ? `AMFI CONNECTED: ${status.total_schemes?.toLocaleString('en-IN')} SCHEMES`
              : 'AMFI AWAITING SYNC'}
          </QLBadge>
          
          <QLButton
            variant="outline"
            size="sm"
            onClick={handleSyncAMFI}
            disabled={syncing}
            icon={<RefreshCw className={`w-3.5 h-3.5 ${syncing ? 'animate-spin' : ''}`} />}
          >
            {syncing ? 'Syncing Feed...' : 'Sync AMFI Feed'}
          </QLButton>
        </div>
      </div>

      {/* 01 SEARCH & COMMAND DROPDOWN */}
      <QLSection
        number={1}
        title="Universal Fund Discovery"
        subtitle="Search by Scheme Name, AMC, Scheme Code, or ISIN"
      >
        <div className="space-y-4">
          <div className="flex flex-col md:flex-row items-center gap-3">
            {/* Real Search Input */}
            <div className="relative flex-1 w-full">
              <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search mutual funds (e.g. Parag Parikh, HDFC, SBI, Small Cap, INF879O01027)..."
                className="w-full bg-surface border border-border rounded-xl pl-10 pr-4 py-2.5 text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent font-sans"
              />
            </div>

            {/* Searchable Command Dropdown Button */}
            <div className="relative w-full md:w-72">
              <button
                type="button"
                onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                className="w-full flex items-center justify-between px-4 py-2.5 rounded-xl border border-border bg-surface text-xs font-mono text-text-primary hover:border-border-strong transition-colors"
              >
                <div className="flex items-center gap-2 truncate">
                  <Database className="w-3.5 h-3.5 text-accent shrink-0" />
                  <span className="truncate">SELECT MUTUAL FUND</span>
                </div>
                <ChevronDown className="w-3.5 h-3.5 text-text-muted" />
              </button>

              {/* Dropdown Modal/Popout */}
              {isDropdownOpen && (
                <div className="absolute right-0 top-full mt-2 w-full sm:w-96 rounded-xl border border-border bg-surface-elevated shadow-2xl p-3 z-50 space-y-3 font-sans">
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-text-muted" />
                    <input
                      type="text"
                      autoFocus
                      value={dropdownQuery}
                      onChange={(e) => setDropdownQuery(e.target.value)}
                      placeholder="Type scheme name or AMC..."
                      className="w-full bg-surface border border-border/80 rounded-lg pl-8 pr-3 py-1.5 text-xs text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent"
                    />
                  </div>

                  <div className="max-h-60 overflow-y-auto space-y-1.5 text-xs">
                    {dropdownResults.length > 0 ? (
                      dropdownResults.map((r) => (
                        <div
                          key={r.scheme_code}
                          onClick={() => {
                            setIsDropdownOpen(false);
                            navigate(`/mutual-funds/${r.scheme_code}`);
                          }}
                          className="p-2 rounded-lg hover:bg-surface border border-transparent hover:border-border cursor-pointer transition-colors space-y-0.5"
                        >
                          <div className="flex items-baseline justify-between gap-2">
                            <span className="font-bold text-text-primary truncate">{r.scheme_name}</span>
                            {r.nav !== undefined && (
                              <span className="font-mono font-bold text-accent shrink-0">₹{r.nav.toFixed(2)}</span>
                            )}
                          </div>
                          <div className="flex items-center gap-2 text-[10px] text-text-muted font-mono">
                            <span>{r.amc}</span>
                            <span>•</span>
                            <span>{r.plan} {r.option}</span>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="text-center py-4 text-xs text-text-muted font-mono">
                        {dropdownQuery ? 'No matching mutual fund schemes found.' : 'Search above to filter schemes...'}
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Category Filter Pills */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
            {categories.map((c) => (
              <button
                key={c.val}
                onClick={() => setSelectedCategory(c.val)}
                className={cn(
                  'px-3 py-1 rounded-lg text-xs font-mono whitespace-nowrap transition-colors border',
                  selectedCategory === c.val
                    ? 'bg-accent/15 border-accent text-accent font-bold'
                    : 'bg-surface border-border text-text-muted hover:text-text-primary'
                )}
              >
                {c.label}
              </button>
            ))}
          </div>
        </div>
      </QLSection>

      {/* 02 USER'S MUTUAL FUND HOLDINGS (IF CONNECTED) */}
      {userMfHoldings.length > 0 && (
        <QLSection
          number={2}
          title="Your Mutual Fund Holdings"
          subtitle="Synchronized From Connected Demat Portfolio"
        >
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {userMfHoldings.map((h, idx) => (
              <QLPanel key={idx} variant="surface" padding="md" className="space-y-3">
                <div className="flex items-baseline justify-between gap-2">
                  <span className="font-bold text-text-primary text-sm truncate">{h.symbol}</span>
                  <QLBadge variant="positive" size="xs">ACTIVE HOLDING</QLBadge>
                </div>
                <div className="grid grid-cols-2 gap-2 text-xs font-mono">
                  <div>
                    <span className="text-[10px] text-text-muted block">UNITS</span>
                    <span className="font-bold text-text-primary">{h.quantity}</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-text-muted block">CURRENT VALUE</span>
                    <span className="font-bold text-text-primary">₹{h.current_value?.toLocaleString('en-IN')}</span>
                  </div>
                </div>
              </QLPanel>
            ))}
          </div>
        </QLSection>
      )}

      {/* 03 AMFI SCHEME CATALOG */}
      <QLSection
        number={userMfHoldings.length > 0 ? 3 : 2}
        title="Mutual Fund Scheme Master"
        subtitle={`Displaying ${schemes.length} Verified AMFI Scheme Records`}
      >
        {loading ? (
          <div className="text-center py-12 text-sm font-mono text-text-muted">
            <RefreshCw className="w-5 h-5 animate-spin mx-auto mb-2 text-accent" />
            Querying AMFI database...
          </div>
        ) : schemes.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {schemes.map((s) => (
              <div
                key={s.scheme_code}
                onClick={() => navigate(`/mutual-funds/${s.scheme_code}`)}
                className="group p-4 rounded-xl border border-border bg-surface hover:bg-surface-elevated hover:border-border-strong transition-all cursor-pointer flex flex-col justify-between space-y-4"
              >
                <div className="space-y-2">
                  <div className="flex items-start justify-between gap-2">
                    <div className="space-y-0.5">
                      <span className="text-[10px] font-mono text-text-muted uppercase block">{s.amc}</span>
                      <h3 className="text-sm font-bold text-text-primary group-hover:text-accent transition-colors line-clamp-2">
                        {s.scheme_name}
                      </h3>
                    </div>
                    {s.risk_o_meter && (
                      <QLBadge variant="neutral" size="xs" className="shrink-0 font-mono text-[9px]">
                        {s.risk_o_meter}
                      </QLBadge>
                    )}
                  </div>

                  <div className="flex flex-wrap items-center gap-1.5 text-[10px] font-mono text-text-muted">
                    <span className="px-1.5 py-0.5 rounded bg-surface-elevated border border-border/60">
                      {s.plan || 'Direct'}
                    </span>
                    <span className="px-1.5 py-0.5 rounded bg-surface-elevated border border-border/60">
                      {s.option || 'Growth'}
                    </span>
                    <span className="truncate">{s.category}</span>
                  </div>
                </div>

                <div className="pt-3 border-t border-border/60 flex items-baseline justify-between font-mono">
                  <div>
                    <span className="text-[10px] text-text-muted block uppercase">Net Asset Value (NAV)</span>
                    <div className="flex items-baseline gap-1.5 mt-0.5">
                      <span className="text-lg font-black text-text-primary">
                        {s.nav !== undefined && s.nav !== null ? `₹${s.nav.toFixed(2)}` : 'NAV UNAVAILABLE'}
                      </span>
                    </div>
                    {s.nav_date && (
                      <span className="text-[10px] text-text-muted block">NAV as of {s.nav_date}</span>
                    )}
                  </div>

                  <div className="flex items-center gap-1 text-xs text-accent font-semibold group-hover:translate-x-0.5 transition-transform">
                    <span>Analyze</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <QLEmptyState
            title="NO MATCHING SCHEMES FOUND"
            description={`No mutual fund records matched '${searchQuery}'. Search by fund house (e.g. Parag Parikh, HDFC), scheme name, or ISIN code.`}
            source="AMFI MUTUAL FUNDS MASTER"
            type="empty"
          />
        )}
      </QLSection>
    </div>
  );
}
