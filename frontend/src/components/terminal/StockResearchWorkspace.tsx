import { useState, useEffect } from 'react';
import { Building2, TrendingUp, TrendingDown, Layers, BarChart3, ShieldAlert, Cpu, FileText, Search } from 'lucide-react';
import { api } from '@/lib/api';
import { TechnicalFeaturesCard } from '@/components/dashboard/TechnicalFeaturesCard';
import { CompanyFundamentalsCard } from '@/components/dashboard/CompanyFundamentalsCard';
import { SignalTerminalCard } from '@/components/dashboard/SignalTerminalCard';
import { RiskTerminalCard } from '@/components/dashboard/RiskTerminalCard';
import { NewsTimelinePanel } from '@/components/terminal/NewsTimelinePanel';

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
  } | null>(null);

  useEffect(() => {
    api.get<any>(`/v1/market-data/quotes/${symbol}`)
      .then((data) => setQuote(data))
      .catch(() => setQuote(null));
  }, [symbol]);

  const lastPrice = quote?.close ?? 2450.0;
  const openPrice = quote?.open ?? 2440.0;
  const change = lastPrice - openPrice;
  const changePct = openPrice !== 0 ? (change / openPrice) * 100 : 0;
  const isPos = change >= 0;

  return (
    <div className="rounded-xl border border-border bg-surface p-5 space-y-5">
      {/* Workspace Header & Action Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-border/60 pb-4">
        <div className="flex items-center gap-3">
          <div className="h-11 w-11 rounded-xl bg-accent/10 border border-accent/20 flex items-center justify-center text-accent font-bold text-sm">
            {symbol.substring(0, 3)}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-extrabold text-text-primary tracking-wide">{symbol}</h2>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-surface-elevated border border-border text-text-secondary">
                NSE:EQ
              </span>
              <button
                onClick={onOpenSearch}
                className="text-text-muted hover:text-accent p-1 transition-colors"
                title="Search another instrument"
              >
                <Search className="h-4 w-4" />
              </button>
            </div>
            <p className="text-xs text-text-muted mt-0.5">
              Point-in-Time Indian Equity Analytics Desk · As of {quote?.timestamp ? new Date(quote.timestamp).toLocaleTimeString('en-IN') : 'Live Market'}
            </p>
          </div>
        </div>

        {/* Real-time Quote Card */}
        <div className="flex items-center gap-4 bg-surface-elevated/60 border border-border/60 rounded-xl px-4 py-2 self-start sm:self-auto">
          <div>
            <span className="text-[10px] text-text-muted uppercase tracking-wider block">LTP (INR)</span>
            <span className="text-lg font-bold text-text-primary">
              ₹{lastPrice.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </span>
          </div>
          <div className="text-right">
            <span className="text-[10px] text-text-muted uppercase tracking-wider block">1D Change</span>
            <span className={`inline-flex items-center text-sm font-bold ${isPos ? 'text-green-400' : 'text-red-400'}`}>
              {isPos ? <TrendingUp className="h-3.5 w-3.5 mr-0.5" /> : <TrendingDown className="h-3.5 w-3.5 mr-0.5" />}
              {isPos ? '+' : ''}{change.toFixed(2)} ({isPos ? '+' : ''}{changePct.toFixed(2)}%)
            </span>
          </div>
        </div>
      </div>

      {/* Desk Navigation Tabs */}
      <div className="flex gap-1.5 overflow-x-auto border-b border-border pb-1">
        {[
          { id: 'overview', label: 'Terminal Overview', icon: Layers },
          { id: 'technicals', label: 'Technical Indicators', icon: BarChart3 },
          { id: 'fundamentals', label: 'Fundamentals & Ratios', icon: Building2 },
          { id: 'signals', label: 'Quant Signals & Horizon', icon: Cpu },
          { id: 'timeline', label: 'Corporate Filings & Events', icon: FileText },
          { id: 'risk', label: 'Risk & Tail Exposures', icon: ShieldAlert },
        ].map((t) => {
          const Icon = t.icon;
          const isSelected = tab === t.id;
          return (
            <button
              key={t.id}
              onClick={() => setTab(t.id as DeskTab)}
              className={`flex items-center gap-2 px-3 py-2 rounded-lg text-xs font-semibold whitespace-nowrap transition-all ${
                isSelected
                  ? 'bg-accent-muted text-accent border border-accent/30'
                  : 'text-text-secondary hover:text-text-primary hover:bg-surface-elevated'
              }`}
            >
              <Icon className="h-3.5 w-3.5" />
              <span>{t.label}</span>
            </button>
          );
        })}
      </div>

      {/* Desk Content */}
      <div className="min-h-[420px]">
        {tab === 'overview' && (
          <div className="grid gap-5 lg:grid-cols-2">
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
    </div>
  );
}
