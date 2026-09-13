import { Globe, TrendingUp, TrendingDown, Clock } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import type { GlobalMarketSnapshot } from '@/types/market';
import { cn } from '@/lib/utils';

interface GlobalMarketsGridProps {
  snapshots?: GlobalMarketSnapshot[];
  loading?: boolean;
}

export function GlobalMarketsGrid({ snapshots = [], loading }: GlobalMarketsGridProps) {
  if (loading) {
    return (
      <Card className="animate-pulse">
        <CardHeader>
          <CardTitle>Global Markets</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-48 bg-surface-elevated rounded-lg" />
        </CardContent>
      </Card>
    );
  }

  const getFreshnessBadge = (freshness: string) => {
    switch (freshness) {
      case 'REAL_TIME':
        return <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-success/20 text-success">REAL-TIME</span>;
      case 'DELAYED':
        return <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-warning/20 text-warning">15M DELAYED</span>;
      case 'END_OF_DAY':
        return <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-info/20 text-info">END OF DAY</span>;
      default:
        return <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-surface-elevated text-text-muted">PERIODIC</span>;
    }
  };

  const getSessionBadge = (status: string) => {
    switch (status) {
      case 'OPEN':
        return <span className="inline-block w-2 h-2 rounded-full bg-success mr-1 animate-pulse" title="Session Open" />;
      case 'CLOSED':
        return <span className="inline-block w-2 h-2 rounded-full bg-text-muted mr-1" title="Session Closed" />;
      case 'WEEKEND':
      case 'HOLIDAY':
        return <span className="inline-block w-2 h-2 rounded-full bg-warning mr-1" title={status} />;
      default:
        return <span className="inline-block w-2 h-2 rounded-full bg-text-muted mr-1" />;
    }
  };

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
    <Card className="border-border">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Globe className="h-5 w-5 text-accent" />
            <CardTitle className="text-base font-semibold">Global Markets & Cross-Asset Intelligence</CardTitle>
          </div>
          <div className="text-xs text-text-muted flex items-center gap-2">
            <Clock className="h-3.5 w-3.5" />
            <span>Multi-Market Timestamps</span>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-3">
          {snapshots.map((snap) => {
            const isPositive = snap.change >= 0;
            return (
              <div
                key={snap.canonicalSymbol}
                className="rounded-lg border border-border-subtle bg-surface-elevated/30 p-3 hover:border-border transition-colors flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between gap-1 mb-1">
                    <span className="font-mono font-bold text-sm text-text-primary flex items-center">
                      {getSessionBadge(snap.sessionStatus)}
                      {snap.canonicalSymbol}
                    </span>
                    {getFreshnessBadge(snap.dataFreshness)}
                  </div>
                  <div className="text-[11px] text-text-secondary truncate mb-2" title={snap.instrumentName}>
                    {snap.instrumentName}
                  </div>
                  <div className="text-lg font-mono font-bold text-text-primary">
                    {formatValue(snap)}
                  </div>
                </div>

                <div className="mt-2 pt-2 border-t border-border-subtle/50 flex flex-col gap-1">
                  <div className={cn(
                    'flex items-center gap-1 text-xs font-mono font-medium',
                    isPositive ? 'text-success' : 'text-danger'
                  )}>
                    {isPositive ? <TrendingUp className="h-3 w-3" /> : <TrendingDown className="h-3 w-3" />}
                    <span>
                      {isPositive ? '+' : ''}{snap.change.toFixed(2)} ({isPositive ? '+' : ''}{snap.changePercent.toFixed(2)}%)
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
      </CardContent>
    </Card>
  );
}
