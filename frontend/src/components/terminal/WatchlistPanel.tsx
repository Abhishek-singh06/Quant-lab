import { useState, useEffect } from 'react';
import { List, Plus, Trash2, CheckCircle2, ChevronRight, Search } from 'lucide-react';
import { api } from '@/lib/api';
import type { WatchlistDTO } from '@/types/terminal';

interface WatchlistPanelProps {
  activeSymbol: string;
  onSelectSymbol: (symbol: string) => void;
  onOpenSearch: () => void;
}

export function WatchlistPanel({ activeSymbol, onSelectSymbol, onOpenSearch }: WatchlistPanelProps) {
  const [watchlists, setWatchlists] = useState<WatchlistDTO[]>([]);
  const [selectedWlId, setSelectedWlId] = useState<number | null>(null);
  const [newWlName, setNewWlName] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchWatchlists = () => {
    setLoading(true);
    api.get<WatchlistDTO[]>('/v1/watchlists')
      .then((data) => {
        if (data && data.length > 0) {
          setWatchlists(data);
          if (!selectedWlId) {
            setSelectedWlId(data[0].id);
          }
        }
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchWatchlists();
  }, []);

  const currentWl = watchlists.find((w) => w.id === selectedWlId) || watchlists[0];

  const handleCreateWatchlist = () => {
    if (!newWlName.trim()) return;
    api.post<WatchlistDTO>('/v1/watchlists', {
      name: newWlName.trim(),
      symbols: ['RELIANCE', 'TCS', 'HDFCBANK'],
    }).then((created) => {
      setNewWlName('');
      setShowCreate(false);
      fetchWatchlists();
      if (created) setSelectedWlId(created.id);
    });
  };

  const handleRemoveSymbol = (symbol: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!currentWl) return;
    api.delete<WatchlistDTO>(`/v1/watchlists/${currentWl.id}/symbols/${symbol}`)
      .then(() => {
        fetchWatchlists();
      });
  };

  return (
    <div className="rounded-xl border border-border bg-surface p-4 flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-border/50 pb-2.5 mb-3">
        <div className="flex items-center gap-2">
          <List className="h-4 w-4 text-accent" />
          <h3 className="text-xs font-bold uppercase tracking-wider text-text-primary">
            Research Watchlists
          </h3>
        </div>
        <button
          onClick={() => setShowCreate(!showCreate)}
          className="text-xs text-accent hover:text-accent/80 flex items-center gap-1 font-medium"
        >
          <Plus className="h-3.5 w-3.5" />
          New List
        </button>
      </div>

      {showCreate && (
        <div className="mb-3 p-2.5 rounded-lg border border-border bg-surface-elevated flex items-center gap-2">
          <input
            type="text"
            value={newWlName}
            onChange={(e) => setNewWlName(e.target.value)}
            placeholder="Watchlist name..."
            className="flex-1 bg-surface border border-border rounded px-2.5 py-1 text-xs text-text-primary focus:outline-none"
          />
          <button
            onClick={handleCreateWatchlist}
            className="px-2.5 py-1 bg-accent text-white rounded text-xs font-medium hover:bg-accent/90"
          >
            Create
          </button>
        </div>
      )}

      {/* Watchlist selector tabs */}
      {watchlists.length > 1 && (
        <div className="flex gap-1 overflow-x-auto pb-2 mb-2 scrollbar-thin">
          {watchlists.map((wl) => (
            <button
              key={wl.id}
              onClick={() => setSelectedWlId(wl.id)}
              className={`px-2.5 py-1 rounded text-xs whitespace-nowrap font-medium transition-colors ${
                wl.id === currentWl?.id
                  ? 'bg-accent-muted text-accent border border-accent/20'
                  : 'text-text-muted hover:text-text-primary hover:bg-surface-elevated'
              }`}
            >
              {wl.name} ({wl.itemCount})
            </button>
          ))}
        </div>
      )}

      {/* Symbol List */}
      <div className="flex-1 overflow-y-auto space-y-1.5 pr-1 max-h-[360px]">
        {loading && (
          <div className="py-6 text-center text-xs text-text-muted animate-pulse">
            Loading watchlist...
          </div>
        )}

        {!loading && currentWl && currentWl.symbols.length === 0 && (
          <div className="py-6 text-center text-xs text-text-muted">
            No symbols in this watchlist. Add some below!
          </div>
        )}

        {!loading &&
          currentWl?.symbols.map((sym) => {
            const isActive = sym.toUpperCase() === activeSymbol.toUpperCase();
            return (
              <div
                key={sym}
                onClick={() => onSelectSymbol(sym)}
                className={`flex items-center justify-between p-2.5 rounded-lg border cursor-pointer transition-all ${
                  isActive
                    ? 'bg-accent-muted/40 border-accent/40 shadow-sm'
                    : 'bg-surface-elevated/40 border-border/60 hover:bg-surface-elevated hover:border-border'
                }`}
              >
                <div className="flex items-center gap-2">
                  <div className="flex items-center justify-center h-6 w-6 rounded bg-surface border border-border text-[10px] font-bold text-text-secondary">
                    {sym.substring(0, 2)}
                  </div>
                  <div>
                    <span className="text-xs font-bold text-text-primary tracking-wide">{sym}</span>
                    <span className="text-[10px] text-text-muted block">NSE Equity</span>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  {isActive && <CheckCircle2 className="h-3.5 w-3.5 text-accent" />}
                  <button
                    onClick={(e) => handleRemoveSymbol(sym, e)}
                    className="opacity-0 group-hover:opacity-100 hover:text-red-400 text-text-muted p-1 rounded transition-opacity"
                    title="Remove symbol"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                  <ChevronRight className="h-3.5 w-3.5 text-text-muted" />
                </div>
              </div>
            );
          })}
      </div>

      {/* Quick Add Symbol Bar */}
      <div className="mt-3 pt-3 border-t border-border/50">
        <button
          onClick={onOpenSearch}
          className="w-full flex items-center justify-center gap-2 py-2 rounded-lg border border-dashed border-border hover:border-accent hover:bg-surface-elevated text-xs text-text-secondary hover:text-text-primary transition-all"
        >
          <Search className="h-3.5 w-3.5 text-accent" />
          <span>Search & Add Security</span>
        </button>
      </div>
    </div>
  );
}
