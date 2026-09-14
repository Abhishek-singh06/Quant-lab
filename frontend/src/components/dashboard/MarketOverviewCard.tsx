import { TrendingUp, TrendingDown } from 'lucide-react';
import type { MarketIndex } from '@/types/market';
import { cn } from '@/lib/utils';
import { QLPanel } from '@/design-system/QLPanel';
import { QLBadge } from '@/design-system/QLBadge';
import { QLEmptyState } from '@/design-system/QLEmptyState';

interface MarketOverviewCardProps {
  indices?: MarketIndex[];
  loading?: boolean;
}

export function MarketOverviewCard({ indices, loading }: MarketOverviewCardProps) {
  if (loading) {
    return (
      <QLPanel variant="surface" padding="md" title="MARKET OVERVIEW">
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 animate-pulse">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-24 bg-surface-elevated/50 rounded-lg" />
          ))}
        </div>
      </QLPanel>
    );
  }

  if (!indices || indices.length === 0) {
    return (
      <QLPanel variant="surface" padding="md" title="MARKET OVERVIEW">
        <QLEmptyState
          title="NO INDEX DATA AVAILABLE"
          description="Live market benchmark feeds are currently offline or polling is unconfigured."
          source="NSE / YAHOO FINANCE"
          type="unavailable"
          nextAction="Check Market Data provider connectivity in Settings."
        />
      </QLPanel>
    );
  }

  return (
    <QLPanel
      variant="surface"
      padding="md"
      title="MARKET OVERVIEW"
      headerAction={
        <div className="flex items-center gap-2">
          <QLBadge variant="warning" size="xs">
            DELAYED (~15M)
          </QLBadge>
          <QLBadge variant="neutral" size="xs">
            YAHOO POLLING
          </QLBadge>
        </div>
      }
    >
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {indices.map((index) => {
          const isPositive = (index.change ?? 0) >= 0;
          return (
            <div
              key={index.symbol}
              className="rounded-lg border border-border bg-surface-elevated/40 p-4 flex flex-col justify-between"
            >
              <div>
                <span className="text-xs font-mono font-bold text-text-secondary uppercase tracking-wider block mb-1">
                  {index.symbol}
                </span>
                <span className="text-xl sm:text-2xl font-black font-mono text-text-primary tracking-tight block">
                  {index.lastPrice ? `₹${index.lastPrice.toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : 'N/A'}
                </span>
              </div>

              <div className="mt-3 pt-2 border-t border-border/50 flex items-center justify-between text-xs font-mono">
                <div
                  className={cn(
                    'flex items-center gap-1 font-semibold',
                    isPositive ? 'text-emerald-400' : 'text-rose-400'
                  )}
                >
                  {isPositive ? <TrendingUp className="h-3.5 w-3.5" /> : <TrendingDown className="h-3.5 w-3.5" />}
                  <span>
                    {isPositive ? '+' : ''}{index.change?.toFixed(2) ?? '0.00'} ({isPositive ? '+' : ''}{index.changePercent?.toFixed(2) ?? '0.00'}%)
                  </span>
                </div>
                <span className="text-[10px] text-text-muted">
                  {index.timestamp ? new Date(index.timestamp).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' }) : ''}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </QLPanel>
  );
}
