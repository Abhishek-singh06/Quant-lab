import { useState, useEffect, useRef } from 'react';
import { Search, X, Building, ArrowRight, Tag } from 'lucide-react';
import { api } from '@/lib/api';
import type { InstrumentSearchResult } from '@/types/terminal';

interface InstrumentSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelect: (instrument: InstrumentSearchResult) => void;
}

export function InstrumentSearchModal({ isOpen, onClose, onSelect }: InstrumentSearchModalProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<InstrumentSearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (isOpen) {
      setQuery('');
      setSelectedIndex(0);
      fetchResults('');
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  }, [isOpen]);

  const fetchResults = (searchQuery: string) => {
    setLoading(true);
    api.get<InstrumentSearchResult[]>(`/v1/instruments/search?q=${encodeURIComponent(searchQuery)}&limit=15`)
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
        onSelect(results[selectedIndex]);
        onClose();
      }
    } else if (e.key === 'Escape') {
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center bg-black/70 backdrop-blur-sm pt-20 p-4">
      <div
        className="w-full max-w-2xl rounded-xl border border-border bg-surface shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150"
        onKeyDown={handleKeyDown}
      >
        {/* Search Input Bar */}
        <div className="flex items-center border-b border-border px-4 py-3 bg-surface-elevated">
          <Search className="h-5 w-5 text-text-muted mr-3" />
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={handleInputChange}
            placeholder="Search symbol, company name, sector (e.g. RELIANCE, TCS, Banking)..."
            className="w-full bg-transparent text-text-primary placeholder-text-muted text-sm focus:outline-none"
          />
          {query && (
            <button
              onClick={() => {
                setQuery('');
                fetchResults('');
              }}
              className="p-1 hover:bg-surface rounded text-text-muted hover:text-text-primary mr-2"
            >
              <X className="h-4 w-4" />
            </button>
          )}
          <span className="text-[10px] uppercase font-mono px-2 py-0.5 rounded border border-border text-text-muted">
            ESC to close
          </span>
        </div>

        {/* Results List */}
        <div className="max-h-96 overflow-y-auto p-2 divide-y divide-border/40">
          {loading && (
            <div className="p-6 text-center text-xs text-text-muted animate-pulse">
              Searching instruments...
            </div>
          )}

          {!loading && results.length === 0 && (
            <div className="p-8 text-center text-xs text-text-muted">
              No matching instruments found for &quot;{query}&quot;
            </div>
          )}

          {!loading &&
            results.map((item, index) => {
              const isSelected = index === selectedIndex;
              return (
                <div
                  key={`${item.exchange}-${item.symbol}`}
                  onClick={() => {
                    onSelect(item);
                    onClose();
                  }}
                  onMouseEnter={() => setSelectedIndex(index)}
                  className={`flex items-center justify-between p-3 rounded-lg cursor-pointer transition-colors ${
                    isSelected ? 'bg-accent-muted/40 border border-accent/30' : 'hover:bg-surface-elevated'
                  }`}
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="rounded-md bg-surface p-2 border border-border flex items-center justify-center">
                      <Building className="h-4 w-4 text-text-secondary" />
                    </div>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-sm text-text-primary tracking-wide">{item.symbol}</span>
                        <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-surface border border-border text-text-secondary">
                          {item.exchange}
                        </span>
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">
                          {item.assetClass}
                        </span>
                      </div>
                      <p className="text-xs text-text-secondary truncate mt-0.5">{item.name}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3 text-right">
                    <div className="flex items-center gap-1 text-[11px] text-text-muted">
                      <Tag className="h-3 w-3" />
                      <span>{item.sector}</span>
                    </div>
                    <ArrowRight className={`h-4 w-4 ${isSelected ? 'text-accent' : 'text-transparent'}`} />
                  </div>
                </div>
              );
            })}
        </div>

        {/* Footer info */}
        <div className="flex items-center justify-between border-t border-border px-4 py-2 bg-surface text-[11px] text-text-muted">
          <span>Deterministic search · Point-in-time security universe</span>
          <span>Use ↑ ↓ to navigate, Enter to select</span>
        </div>
      </div>
    </div>
  );
}
