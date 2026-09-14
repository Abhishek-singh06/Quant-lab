import { useState, useEffect } from 'react';
import {
  TrendingUp,
  TrendingDown,
  Search,
} from 'lucide-react';
import { api } from '@/lib/api';
import { TechnicalFeaturesCard } from '@/components/dashboard/TechnicalFeaturesCard';
import { CompanyFundamentalsCard } from '@/components/dashboard/CompanyFundamentalsCard';
import { SignalTerminalCard } from '@/components/dashboard/SignalTerminalCard';
import { RiskTerminalCard } from '@/components/dashboard/RiskTerminalCard';
import { NewsTimelinePanel } from '@/components/terminal/NewsTimelinePanel';
import { QLBadge } from '@/design-system/QLBadge';
import { QLPanel } from '@/design-system/QLPanel';
import { QLTabs } from '@/design-system/QLTabs';

interface StockResearchWorkspaceProps {
  symbol: string;
  onOpenSearch: () => void;
}

type DeskTab = 'overview' | 'technicals' | 'fundamentals' | 'signals' | 'timeline' | 'risk';

export function StockResearchWorkspace({ symbol, onOpenSearch }: StockResearchWorkspaceProps) {
  const [tab, setTab] = useState<DeskTab>('overview');
  const [quote, setQuote] = useState<{
    close?: number;
    open?: number;
    high?: number;
    low?: number;
    volume?: number;
    source?: string;
    timestamp?: string;
    lastPrice?: number;
  } | null>(null);

  useEffect(() => {
    api.get<any>(`/v1/market-data/quotes/${symbol}`)
      .then((data) => setQuote(data))
      .catch(() => setQuote(null));
  }, [symbol]);

  const lastPrice = quote?.close ?? quote?.lastPrice ?? 0;
  const openPrice = quote?.open ?? 0;
  const change = lastPrice > 0 && openPrice > 0 ? lastPrice - openPrice : 0;
  const changePct = openPrice > 0 ? (change / openPrice) * 100 : 0;
  const isPos = change >= 0;

  return (
    <QLPanel variant="surface" padding="md" className="space-y-6">
      {/* Workspace Header & Action Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/60 pb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-surface-elevated border border-border flex items-center justify-center text-accent font-bold font-mono text-sm">
            {symbol.substring(0, 3)}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-bold text-text-primary tracking-wide font-mono">{symbol}</h2>
              <QLBadge variant="neutral" size="xs">
                NSE:EQ
              </QLBadge>
              <button
                onClick={onOpenSearch}
                className="text-text-muted hover:text-accent p-1 transition-colors cursor-pointer"
                title="Search another instrument"
              >
                <Search className="w-3.5 h-3.5" />
              </button>
            </div>
            <p className="text-xs text-text-muted mt-0.5">
              Point-in-Time Equity Desk · {quote?.timestamp ? `As of ${new Date(quote.timestamp).toLocaleTimeString('en-IN')} IST` : 'Yahoo Finance Delayed (~15m)'}
            </p>
          </div>
        </div>

        {/* Real-time Quote Card */}
        {lastPrice > 0 ? (
          <div className="flex items-center gap-4 bg-surface-elevated/60 border border-border/60 rounded-lg px-4 py-2 self-start sm:self-auto font-mono">
            <div>
              <span className="text-[10px] text-text-muted uppercase block">LTP (INR)</span>
              <span className="text-lg font-bold text-text-primary">
                ₹{lastPrice.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
              </span>
            </div>
            <div className="text-right">
              <span className="text-[10px] text-text-muted uppercase block">1D Change</span>
              <span className={`inline-flex items-center text-xs font-bold ${isPos ? 'text-emerald-400' : 'text-rose-400'}`}>
                {isPos ? <TrendingUp className="w-3.5 h-3.5 mr-0.5" /> : <TrendingDown className="w-3.5 h-3.5 mr-0.5" />}
                {isPos ? '+' : ''}{change.toFixed(2)} ({isPos ? '+' : ''}{changePct.toFixed(2)}%)
              </span>
            </div>
          </div>
        ) : (
          <div className="text-xs font-mono text-text-muted p-2 rounded bg-surface-elevated">
            Awaiting tick observation
          </div>
        )}
      </div>

      {/* Desk Navigation Tabs */}
      <QLTabs<DeskTab>
        activeTab={tab}
        onChange={(newTab) => setTab(newTab)}
        variant="underline"
        tabs={[
          { id: 'overview', label: 'Terminal Overview' },
          { id: 'technicals', label: 'Technical Indicators' },
          { id: 'fundamentals', label: 'Fundamentals & Ratios' },
          { id: 'signals', label: 'Quant Signals & Horizon' },
          { id: 'timeline', label: 'Corporate News & Filings' },
          { id: 'risk', label: 'Risk & Tail Exposures' },
        ]}
      />

      {/* Desk Content */}
      <div className="min-h-[400px]">
        {tab === 'overview' && (
          <div className="grid gap-6 lg:grid-cols-2">
            <SignalTerminalCard />
            <NewsTimelinePanel symbol={symbol} />
          </div>
        )}

        {tab === 'technicals' && <TechnicalFeaturesCard />}
        {tab === 'fundamentals' && <CompanyFundamentalsCard />}
        {tab === 'signals' && <SignalTerminalCard />}
        {tab === 'timeline' && <NewsTimelinePanel symbol={symbol} />}
        {tab === 'risk' && <RiskTerminalCard />}
      </div>
    </QLPanel>
  );
}
