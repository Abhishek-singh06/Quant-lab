import { TrendingUp, TrendingDown } from 'lucide-react';
import type { GlobalMarketSnapshot } from '@/types/market';
import { cn } from '@/lib/utils';
import { QLPanel } from '@/design-system/QLPanel';
import { QLBadge } from '@/design-system/QLBadge';
import { QLEmptyState } from '@/design-system/QLEmptyState';

interface GlobalMarketsGridProps {
  snapshots?: GlobalMarketSnapshot[];
  loading?: boolean;
}

export function GlobalMarketsGrid({ snapshots = [], loading }: GlobalMarketsGridProps) {
  if (loading) {
    return (
      <QLPanel variant="surface" padding="md" title="GLOBAL MARKETS & CROSS-ASSET INTELLIGENCE">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3 animate-pulse">
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="h-28 bg-surface-elevated/50 rounded-lg" />
          ))}
        </div>
      </QLPanel>
    );
  }

  if (snapshots.length === 0) {
    return (
      <QLPanel variant="surface" padding="md" title="GLOBAL MARKETS & CROSS-ASSET INTELLIGENCE">
        <QLEmptyState
          title="NO GLOBAL SNAPSHOT DATA"
          description="Cross-asset market snapshots from global exchanges are not currently available."
          source="GLOBAL EXCHANGES / YAHOO FINANCE"
          type="unavailable"
          nextAction="Verify global market feed ingestors in Settings."
        />
      </QLPanel>
    );
  }

  const formatValue = (snap: GlobalMarketSnapshot) => {
    if (snap.assetClass === 'BOND_YIELD') {
      return `${snap.close.toFixed(3)}%`;
    }
    if (snap.assetClass === 'FX') {
      return `₹${snap.close.toFixed(3)}`;
    }
    return snap.close.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  };

  return (
    <QLPanel
      variant="surface"
      padding="md"
      title="GLOBAL MARKETS & CROSS-ASSET INTELLIGENCE"
      headerAction={
        <QLBadge variant="neutral" size="xs">
          CROSS-ASSET RADAR
        </QLBadge>
      }
    >
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-3">
        {snapshots.map((snap) => {
          const isPositive = (snap.change ?? 0) >= 0;
          return (
            <div
              key={snap.canonicalSymbol}
              className="rounded-lg border border-border bg-surface-elevated/40 p-3.5 hover:border-border-strong transition-colors flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between gap-1 mb-1">
                  <span className="font-mono font-bold text-sm text-text-primary flex items-center gap-1.5">
                    <span
                      className={cn(
                        'w-2 h-2 rounded-full',
                        snap.sessionStatus === 'OPEN'
                          ? 'bg-emerald-400 animate-pulse'
                          : 'bg-zinc-600'
                      )}
                    />
                    {snap.canonicalSymbol}
                  </span>
                  <QLBadge
                    variant={snap.dataFreshness === 'REAL_TIME' ? 'positive' : 'warning'}
                    size="xs"
                  >
                    {snap.dataFreshness === 'REAL_TIME' ? 'LIVE' : 'DELAYED'}
                  </QLBadge>
                </div>
                <div className="text-[11px] text-text-muted truncate mb-2 font-sans" title={snap.instrumentName}>
                  {snap.instrumentName}
                </div>
                <div className="text-lg font-mono font-black text-text-primary">
                  {formatValue(snap)}
                </div>
              </div>

              <div className="mt-3 pt-2 border-t border-border/50 flex flex-col gap-1 font-mono text-xs">
                <div
                  className={cn(
                    'flex items-center gap-1 font-semibold',
                    isPositive ? 'text-emerald-400' : 'text-rose-400'
                  )}
                >
                  {isPositive ? <TrendingUp className="h-3 w-3" /> : <TrendingDown className="h-3 w-3" />}
                  <span>
                    {isPositive ? '+' : ''}{snap.change?.toFixed(2) ?? '0.00'} ({isPositive ? '+' : ''}{snap.changePercent?.toFixed(2) ?? '0.00'}%)
                  </span>
                </div>

                <div className="flex items-center justify-between text-[10px] text-text-muted mt-0.5">
                  <span className="truncate max-w-[80px]" title={snap.source}>{snap.source}</span>
                  <span>{snap.ageMinutes !== undefined ? `${snap.ageMinutes}m ago` : snap.tradingDate}</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </QLPanel>
  );
}
