import { useState, useEffect, useRef } from 'react';
import { Search, X, ArrowRight } from 'lucide-react';
import { api } from '@/lib/api';
import type { InstrumentSearchResult } from '@/types/terminal';
import { QLBadge } from '@/design-system/QLBadge';

interface InstrumentSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelect: (instrument: InstrumentSearchResult) => void;
}

const DEFAULT_POPULAR_SEARCHES: InstrumentSearchResult[] = [
  { symbol: 'RELIANCE', name: 'Reliance Industries Ltd.', exchange: 'NSE', isin: 'INE002A01018', sector: 'Energy & Petrochemicals', assetClass: 'EQUITY' },
  { symbol: 'TCS', name: 'Tata Consultancy Services Ltd.', exchange: 'NSE', isin: 'INE467B01029', sector: 'Information Technology', assetClass: 'EQUITY' },
  { symbol: 'INFY', name: 'Infosys Ltd.', exchange: 'NSE', isin: 'INE009A01021', sector: 'Information Technology', assetClass: 'EQUITY' },
  { symbol: 'HINDZINC', name: 'Hindustan Zinc Ltd.', exchange: 'NSE', isin: 'INE267A01025', sector: 'Metals & Mining', assetClass: 'EQUITY' },
  { symbol: 'NIFTY', name: 'Nifty 50 Index', exchange: 'NSE', isin: 'INDEX_NIFTY50', sector: 'Benchmark Index', assetClass: 'INDEX' },
];

export function InstrumentSearchModal({ isOpen, onClose, onSelect }: InstrumentSearchModalProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<InstrumentSearchResult[]>([]);
  const [recentSearches, setRecentSearches] = useState<InstrumentSearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    try {
      const saved = localStorage.getItem('quantlab_recent_searches');
      if (saved) {
        setRecentSearches(JSON.parse(saved));
      } else {
        setRecentSearches(DEFAULT_POPULAR_SEARCHES.slice(0, 3));
      }
    } catch {
      setRecentSearches(DEFAULT_POPULAR_SEARCHES.slice(0, 3));
    }
  }, []);

  useEffect(() => {
    if (isOpen) {
      setQuery('');
      setSelectedIndex(0);
      setResults(recentSearches.length > 0 ? recentSearches : DEFAULT_POPULAR_SEARCHES);
      setTimeout(() => inputRef.current?.focus(), 40);
    }
  }, [isOpen, recentSearches]);

  const fetchResults = (searchQuery: string) => {
    if (!searchQuery.trim()) {
      setResults(recentSearches.length > 0 ? recentSearches : DEFAULT_POPULAR_SEARCHES);
      setSelectedIndex(0);
      return;
    }

    setLoading(true);
    api.get<InstrumentSearchResult[]>(`/v1/instruments/search?query=${encodeURIComponent(searchQuery)}&limit=12`)
      .catch(() =>
        api.get<InstrumentSearchResult[]>(`/v1/instruments/search?q=${encodeURIComponent(searchQuery)}&limit=12`)
      )
      .then((data) => {
        setResults(data || []);
        setSelectedIndex(0);
      })
      .catch(() => {
        setResults([]);
      })
      .finally(() => setLoading(false));
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setQuery(val);
    fetchResults(val);
  };

  const handleSelect = (item: InstrumentSearchResult) => {
    const updated = [item, ...recentSearches.filter((r) => r.symbol !== item.symbol)].slice(0, 5);
    setRecentSearches(updated);
    try {
      localStorage.setItem('quantlab_recent_searches', JSON.stringify(updated));
    } catch {}
    onSelect(item);
    onClose();
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev < results.length - 1 ? prev + 1 : 0));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev > 0 ? prev - 1 : results.length - 1));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (results[selectedIndex]) {
        handleSelect(results[selectedIndex]);
      }
    } else if (e.key === 'Escape') {
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center bg-black/80 backdrop-blur-md pt-16 sm:pt-24 p-4 font-sans select-none">
      <div
        className="w-full max-w-2xl rounded-xl border border-border bg-[#0b0c12] shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-100 flex flex-col max-h-[85vh]"
        onKeyDown={handleKeyDown}
      >
        {/* Search Input Bar */}
        <div className="flex items-center border-b border-border/80 px-4 py-3.5 bg-surface-elevated/80 gap-3">
          <Search className="w-4 h-4 text-text-muted shrink-0" />
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={handleInputChange}
            placeholder="Search symbol, ISIN, company name, sector (e.g. RELIANCE, TCS, INFY)..."
            className="w-full bg-transparent text-text-primary placeholder-text-muted text-sm focus:outline-none font-mono"
          />
          {query && (
            <button
              onClick={() => {
                setQuery('');
                fetchResults('');
              }}
              className="p-1 hover:bg-surface rounded text-text-muted hover:text-text-primary cursor-pointer"
            >
              <X className="w-4 h-4" />
            </button>
          )}
          <kbd className="text-[10px] uppercase font-mono px-2 py-0.5 rounded border border-border text-text-muted shrink-0">
            ESC
          </kbd>
        </div>

        {/* Section Header */}
        <div className="px-4 py-2 border-b border-border/60 bg-surface/50 flex items-center justify-between text-[11px] font-mono text-text-muted uppercase">
          <span>{query ? 'Search Results' : 'Recent & Popular Instruments'}</span>
          <span>{results.length} found</span>
        </div>

        {/* Results List */}
        <div className="overflow-y-auto p-2 divide-y divide-border/40 max-h-96 no-scrollbar">
          {loading && (
            <div className="p-8 text-center text-xs text-text-muted font-mono flex items-center justify-center gap-2">
              <span className="w-3.5 h-3.5 border-2 border-accent border-t-transparent rounded-full animate-spin" />
              Searching security universe...
            </div>
          )}

          {!loading && results.length === 0 && (
            <div className="p-10 text-center space-y-2 font-mono text-xs">
              <p className="text-text-muted">No matching instruments found for &ldquo;{query}&rdquo;</p>
              <p className="text-[11px] text-text-subdued">Try searching by NSE symbol, ISIN code, or sector name</p>
            </div>
          )}

          {!loading &&
            results.map((item, index) => {
              const isSelected = index === selectedIndex;
              return (
                <div
                  key={`${item.exchange}-${item.symbol}-${index}`}
                  onClick={() => handleSelect(item)}
                  onMouseEnter={() => setSelectedIndex(index)}
                  className={`flex items-center justify-between p-3 rounded-lg cursor-pointer transition-colors ${
                    isSelected ? 'bg-surface-elevated border border-accent/40' : 'hover:bg-surface-elevated/40'
                  }`}
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="w-8 h-8 rounded-md bg-surface border border-border flex items-center justify-center font-mono font-bold text-xs text-accent shrink-0">
                      {item.symbol.slice(0, 3)}
                    </div>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-sm text-text-primary tracking-wide font-mono">
                          {item.symbol}
                        </span>
                        <QLBadge variant="neutral" size="xs">
                          {item.exchange || 'NSE'}
                        </QLBadge>
                        {item.isin && (
                          <span className="text-[10px] font-mono text-text-muted hidden sm:inline">
                            {item.isin}
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-text-secondary truncate mt-0.5">{item.name}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3 text-right shrink-0">
                    {item.sector && (
                      <span className="text-[11px] text-text-muted font-mono hidden sm:inline">
                        {item.sector}
                      </span>
                    )}
                    <ArrowRight className={`w-4 h-4 ${isSelected ? 'text-accent' : 'text-transparent'}`} />
                  </div>
                </div>
              );
            })}
        </div>

        {/* Footer info */}
        <div className="flex items-center justify-between border-t border-border/80 px-4 py-2.5 bg-surface text-[11px] font-mono text-text-muted">
          <span>Deterministic search · Point-in-time security universe</span>
          <span>Use ↑ ↓ to navigate, Enter to select</span>
        </div>
      </div>
    </div>
  );
}
