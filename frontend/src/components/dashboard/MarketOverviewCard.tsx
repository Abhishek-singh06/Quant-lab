import { TrendingUp, TrendingDown } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import type { MarketIndex } from '@/types/market';

interface MarketOverviewCardProps {
  indices?: MarketIndex[];
  loading?: boolean;
}

const DEFAULT_INDICES: MarketIndex[] = [
  { symbol: 'NIFTY 50', name: 'Nifty 50', lastPrice: 24850.25, change: 112.50, changePercent: 0.45, timestamp: new Date().toISOString() },
  { symbol: 'SENSEX', name: 'BSE SENSEX', lastPrice: 81400.00, change: 320.00, changePercent: 0.39, timestamp: new Date().toISOString() },
  { symbol: 'NIFTY BANK', name: 'Nifty Bank', lastPrice: 51200.00, change: -85.00, changePercent: -0.17, timestamp: new Date().toISOString() },
  { symbol: 'NIFTY IT', name: 'Nifty IT', lastPrice: 38920.10, change: 312.45, changePercent: 0.81, timestamp: new Date().toISOString() },
];

export function MarketOverviewCard({ indices, loading }: MarketOverviewCardProps) {
  const displayIndices = indices && indices.length > 0 ? indices : DEFAULT_INDICES;

  if (loading) {
    return (
      <Card className="animate-pulse">
        <CardHeader>
          <CardTitle>Market Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-44 bg-surface-elevated rounded-lg" />
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Market Overview</CardTitle>
          <div className="flex items-center gap-2">
            <Badge variant="outline" className="text-[10px] font-mono border-amber-500/30 bg-amber-500/10 text-amber-300">
              DELAYED (~15m)
            </Badge>
            <Badge variant="outline" className="text-[10px] font-mono text-text-muted">
              YAHOO POLLING
            </Badge>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-2 gap-4">
          {displayIndices.slice(0, 4).map((index) => {
            const isPositive = index.change >= 0;
            return (
              <div
                key={index.symbol}
                className="rounded-lg border border-border-subtle bg-background p-4"
              >
                <p className="text-sm text-text-secondary mb-1">{index.symbol}</p>
                <p className="text-xl font-semibold font-mono">
                  {index.lastPrice.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </p>
                <div className={cn(
                  'flex items-center gap-1 mt-1 text-sm',
                  isPositive ? 'text-success' : 'text-danger'
                )}>
                  {isPositive ? (
                    <TrendingUp className="h-3.5 w-3.5" />
                  ) : (
                    <TrendingDown className="h-3.5 w-3.5" />
                  )}
                  <span className="font-mono">
                    {isPositive ? '+' : ''}{index.change.toFixed(2)} ({isPositive ? '+' : ''}{index.changePercent.toFixed(2)}%)
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}
