import { TrendingUp, TrendingDown, Clock, ShieldCheck, Activity } from 'lucide-react';
import type { TerminalIndexSummary } from '@/types/terminal';

interface MarketIndexRibbonProps {
  indices: TerminalIndexSummary[];
  provider: string;
  providerStatus: string;
  marketState?: string;
  onSelectIndex?: (symbol: string) => void;
}

export function MarketIndexRibbon({
  indices,
  provider,
  providerStatus,
  marketState = 'OPEN',
  onSelectIndex,
}: MarketIndexRibbonProps) {
  return (
    <div className="rounded-xl border border-border bg-surface p-3 shadow-sm">
      <div className="flex items-center justify-between border-b border-border/50 pb-2.5 mb-2.5 px-1">
        <div className="flex items-center gap-2">
          <Activity className="h-4 w-4 text-accent" />
          <span className="text-xs font-bold text-text-primary tracking-wider uppercase">Market Benchmark Ticker</span>
          <span className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${
            marketState === 'OPEN'
              ? 'bg-green-500/10 text-green-400 border-green-500/20'
              : 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20'
          }`}>
            NSE: {marketState}
          </span>
        </div>

        <div className="flex items-center gap-3 text-[11px] text-text-muted">
          <div className="flex items-center gap-1">
            <ShieldCheck className="h-3.5 w-3.5 text-blue-400" />
            <span>Feed: <strong className="text-text-secondary">{provider}</strong> ({providerStatus})</span>
          </div>
          <div className="flex items-center gap-1">
            <Clock className="h-3.5 w-3.5" />
            <span>IST (UTC+05:30)</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
        {indices.map((idx) => {
          const isPositive = idx.change >= 0;
          return (
            <div
              key={idx.symbol}
              onClick={() => onSelectIndex?.(idx.symbol)}
              className="flex flex-col justify-between rounded-lg border border-border/60 bg-surface-elevated/60 p-3 hover:border-accent/40 hover:bg-surface-elevated transition-all cursor-pointer"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-text-primary">{idx.symbol}</span>
                <span className={`flex items-center text-[10px] font-medium ${isPositive ? 'text-green-400' : 'text-red-400'}`}>
                  {isPositive ? <TrendingUp className="h-3 w-3 mr-0.5" /> : <TrendingDown className="h-3 w-3 mr-0.5" />}
                  {isPositive ? '+' : ''}{idx.changePercent.toFixed(2)}%
                </span>
              </div>

              <div className="flex items-baseline justify-between mt-1.5">
                <span className="text-base font-bold text-text-primary">
                  {idx.lastPrice.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </span>
                <span className={`text-[11px] font-mono ${isPositive ? 'text-green-400' : 'text-red-400'}`}>
                  {isPositive ? '+' : ''}{idx.change.toFixed(2)}
                </span>
              </div>

              <div className="flex items-center justify-between mt-1 text-[9px] text-text-muted border-t border-border/30 pt-1">
                <span>{idx.source}</span>
                <span>{new Date(idx.timestamp).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })}</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
