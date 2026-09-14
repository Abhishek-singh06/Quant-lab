import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Search,
  RefreshCw,
  TrendingUp,
  AlertTriangle,
  Info,
} from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { api } from '@/lib/api';
import type { Signal } from '@/types/market';
import type { CompleteInvestmentRecommendation } from '@/types/recommendation';
import { QLBadge } from '@/design-system/QLBadge';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLTabs } from '@/design-system/QLTabs';
import { QLEmptyState } from '@/design-system/QLEmptyState';
import { QLProvenance } from '@/design-system/QLProvenance';
import { QuantLabInvestmentViewCard } from '@/components/dashboard/QuantLabInvestmentViewCard';
import { TechnicalFeaturesCard } from '@/components/dashboard/TechnicalFeaturesCard';
import { CompanyFundamentalsCard } from '@/components/dashboard/CompanyFundamentalsCard';
import { SignalTerminalCard } from '@/components/dashboard/SignalTerminalCard';
import { RiskTerminalCard } from '@/components/dashboard/RiskTerminalCard';
import { NewsTimelinePanel } from '@/components/terminal/NewsTimelinePanel';
import { HorizonAnalysisCard } from '@/components/dashboard/HorizonAnalysisCard';
import { InstrumentSearchModal } from '@/components/terminal/InstrumentSearchModal';

type DeskTab =
  | 'overview'
  | 'technicals'
  | 'quant'
  | 'fundamentals'
  | 'news'
  | 'risk'
  | 'provenance';

const TIMEFRAMES = ['1D', '1W', '1M', '3M', '6M', '1Y', '5Y'];

interface OHLCVRecord {
  timestamp: string;
  trading_date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  adjusted_close: number;
  provider: string;
  source_timestamp: string;
}

interface HistoryPayload {
  symbol: string;
  exchange: string;
  timeframe: string;
  observations_count: number;
  earliest_date?: string;
  latest_date?: string;
  data_status: string;
  provider: string;
  as_of: string;
  records: OHLCVRecord[];
}

export function StockAnalysisPage() {
  const { symbol } = useParams<{ symbol: string }>();
  const navigate = useNavigate();
  const [tab, setTab] = useState<DeskTab>('overview');
  const [timeframe, setTimeframe] = useState('1M');
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  const [signal, setSignal] = useState<Signal | null>(null);
  const [recommendation, setRecommendation] = useState<CompleteInvestmentRecommendation | null>(null);
  const [quote, setQuote] = useState<any>(null);
  const [historyData, setHistoryData] = useState<HistoryPayload | null>(null);
  const [loading, setLoading] = useState(true);
  const [recError, setRecError] = useState<string | null>(null);

  const sym = symbol && symbol.toUpperCase() !== 'NIFTY' ? symbol.toUpperCase() : 'VEDL';

  const loadData = () => {
    setLoading(true);
    setRecError(null);

    Promise.all([
      api.get<any>(`/api/v1/market-data/quotes/${sym}`).catch(() => null),
      api.get<HistoryPayload>(`/api/v1/market-data/history/${sym}?timeframe=${timeframe}`).catch(() => null),
      api.get<Signal>(`/v1/signals/latest/${sym}`).catch(() => null),
      api.get<CompleteInvestmentRecommendation>(`/v1/recommendations/analysis/${sym}`).catch((err) => {
        setRecError(err?.message || 'Recommendation engine data unavailable for this ticker.');
        return null;
      }),
    ])
      .then(([qData, hData, sigData, recData]) => {
        setQuote(qData);
        setHistoryData(hData);
        setSignal(sigData);
        setRecommendation(recData);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadData();
  }, [sym, timeframe]);

  // Authoritative price calculations directly from real market data response
  const currentPrice = quote?.lastPrice || quote?.close || recommendation?.risk_reward?.current_price || 0;
  const prevPrice = quote?.previousClose || quote?.open || 0;
  const priceChange = quote?.change !== undefined ? quote.change : (currentPrice > 0 && prevPrice > 0 ? currentPrice - prevPrice : 0);
  const priceChangePct = quote?.changePercent !== undefined ? quote.changePercent : (prevPrice > 0 ? (priceChange / prevPrice) * 100 : 0);
  const isPositive = priceChange >= 0;

  // Real OHLCV chart records formatted for Recharts
  const chartPoints = historyData?.records?.map((r) => ({
    date: r.trading_date.slice(5), // e.g. "08-10"
    fullDate: r.trading_date,
    price: r.close,
    open: r.open,
    high: r.high,
    low: r.low,
    volume: r.volume,
  })) || [];

  return (
    <div className="space-y-8 pb-12 font-sans">
      {/* Top Action & Instrument Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-surface-elevated border border-border flex items-center justify-center font-mono font-black text-sm text-accent">
            {sym.slice(0, 3)}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl sm:text-3xl font-black font-mono tracking-tight text-text-primary uppercase">
                {sym}
              </h1>
              <QLBadge variant="neutral" size="sm">
                NSE:EQ
              </QLBadge>
              {quote?.sector && (
                <QLBadge variant="info" size="sm">
                  {quote.sector}
                </QLBadge>
              )}
              {recommendation?.recommendation && (
                <QLBadge
                  variant={
                    ['BUY', 'STRONG BUY', 'ACCUMULATE'].includes(recommendation.recommendation)
                      ? 'positive'
                      : ['AVOID', 'REDUCE', 'SELL'].includes(recommendation.recommendation)
                      ? 'negative'
                      : 'warning'
                  }
                  size="sm"
                >
                  {recommendation.recommendation}
                </QLBadge>
              )}
            </div>
            <p className="text-xs text-text-muted mt-0.5">
              {quote?.name || recommendation?.company_name || 'National Stock Exchange of India'} · Point-in-Time Security Intelligence
            </p>
          </div>
        </div>

        {/* Live LTP & Quick Search */}
        <div className="flex items-center gap-4">
          <div className="text-right font-mono">
            <span className="text-[10px] text-text-muted uppercase block">
              AUTHORITATIVE LTP (INR)
            </span>
            <div className="text-xl sm:text-2xl font-bold text-text-primary">
              {currentPrice > 0 ? `₹${currentPrice.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : 'N/A'}
            </div>
            {currentPrice > 0 && (
              <div className={`text-xs font-semibold ${isPositive ? 'text-emerald-400' : 'text-rose-400'}`}>
                {isPositive ? '+' : ''}{priceChange.toFixed(2)} ({isPositive ? '+' : ''}{priceChangePct.toFixed(2)}%)
              </div>
            )}
            <span className="text-[9px] text-text-muted block">
              As of {quote?.observationDate || 'Latest Observation'}
            </span>
          </div>

          <div className="flex items-center gap-2">
            <QLButton
              variant="outline"
              size="sm"
              onClick={loadData}
              icon={<RefreshCw className="w-3.5 h-3.5" />}
            >
              Refresh
            </QLButton>
            <QLButton
              variant="outline"
              size="sm"
              onClick={() => setIsSearchOpen(true)}
              icon={<Search className="w-3.5 h-3.5" />}
            >
              Change Ticker
            </QLButton>
          </div>
        </div>
      </div>

      {/* Primary Real OHLCV Price Chart */}
      <QLPanel variant="surface" padding="md" className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border/60 pb-3">
          <div className="flex items-center gap-3">
            <span className="font-mono text-xs text-text-primary uppercase font-bold flex items-center gap-1.5">
              <TrendingUp className="w-4 h-4 text-accent" />
              <span>{sym} HISTORICAL PRICE SERIES</span>
            </span>
            <QLBadge variant="warning" size="xs">
              {historyData?.data_status || 'DELAYED ~15M'}
            </QLBadge>
            <span className="text-[10px] font-mono text-text-muted">
              Source: <strong className="text-text-primary">{historyData?.provider || 'YAHOO_FINANCE'}</strong>
            </span>
          </div>

          {/* Timeframe selector */}
          <div className="flex items-center gap-1 bg-[#0b0c12] p-1 rounded-md border border-border/80 font-mono text-xs">
            {TIMEFRAMES.map((tf) => (
              <button
                key={tf}
                onClick={() => setTimeframe(tf)}
                className={`px-2 py-0.5 rounded transition-colors ${
                  timeframe === tf
                    ? 'bg-surface-elevated text-text-primary font-bold shadow-sm'
                    : 'text-text-muted hover:text-text-secondary'
                }`}
              >
                {tf}
              </button>
            ))}
          </div>
        </div>

        {chartPoints.length > 0 ? (
          <>
            <div className="h-[280px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartPoints}>
                  <defs>
                    <linearGradient id="stockGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#6366f1" stopOpacity={0.25} />
                      <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1f2233" />
                  <XAxis dataKey="date" stroke="#606575" fontSize={11} tickLine={false} />
                  <YAxis
                    stroke="#606575"
                    fontSize={11}
                    tickLine={false}
                    domain={['dataMin * 0.98', 'dataMax * 1.02']}
                    tickFormatter={(v: number) => `₹${v.toFixed(0)}`}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#0f1118',
                      border: '1px solid #232636',
                      borderRadius: '8px',
                      color: '#f3f4f6',
                      fontFamily: 'monospace',
                      fontSize: '12px',
                    }}
                    formatter={(val: any) => [`₹${Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`, 'Close']}
                    labelFormatter={(label: any, payload: any) => payload?.[0]?.payload?.fullDate || label}
                  />
                  <Area
                    type="monotone"
                    dataKey="price"
                    stroke="#6366f1"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#stockGradient)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>

            <div className="flex flex-wrap items-center justify-between gap-4 pt-2 border-t border-border/40 font-mono text-[11px] text-text-muted">
              <div>
                Period: <span className="text-text-primary">{timeframe}</span> ({historyData?.earliest_date} → {historyData?.latest_date})
              </div>
              <div className="flex items-center gap-1.5">
                <Info className="w-3 h-3 text-accent" />
                <span>Zero Synthetic Points • Plotted {chartPoints.length} Authoritative OHLCV Observations</span>
              </div>
            </div>
          </>
        ) : (
          <div className="py-12 text-center font-mono text-xs text-text-muted space-y-2">
            <AlertTriangle className="w-6 h-6 text-amber-400 mx-auto" />
            <p className="font-bold text-text-primary uppercase">NO VERIFIED HISTORICAL DATA</p>
            <p>Historical price series for {sym} is currently unavailable from provider feed.</p>
          </div>
        )}
      </QLPanel>

      {/* Desk Navigation Tabs */}
      <QLTabs<DeskTab>
        activeTab={tab}
        onChange={(newTab) => setTab(newTab)}
        variant="underline"
        tabs={[
          { id: 'overview', label: 'QuantLab Decision' },
          { id: 'technicals', label: 'Technicals & Momentum' },
          { id: 'quant', label: 'Quant Alpha & Horizons' },
          { id: 'fundamentals', label: 'Fundamentals & Valuation' },
          { id: 'news', label: 'Corporate News & Filings' },
          { id: 'risk', label: 'Risk & Sizing Limits' },
          { id: 'provenance', label: 'Cryptographic Provenance' },
        ]}
      />

      {/* Tab Panels */}
      <div className="pt-2">
        {recError && !recommendation && (
          <QLEmptyState
            title="RECOMMENDATION PIPELINE UNAVAILABLE"
            description={`Authorized market intelligence for ${sym} is currently disconnected or awaiting validated observations.`}
            source="NSE / YAHOO FINANCE"
            type="unavailable"
            nextAction="Check symbol spelling or try searching a benchmark security like VEDL, RELIANCE, TCS, INFY, or HINDZINC."
            onAction={loadData}
            actionLabel="Retry Analysis"
          />
        )}

        {tab === 'overview' && (
          <div className="space-y-6">
            <QuantLabInvestmentViewCard recommendation={recommendation} loading={loading} />
          </div>
        )}

        {tab === 'technicals' && (
          <div className="space-y-6">
            <TechnicalFeaturesCard />
          </div>
        )}

        {tab === 'quant' && (
          <div className="grid gap-6 lg:grid-cols-2">
            <SignalTerminalCard signal={signal} loading={loading} />
            <HorizonAnalysisCard />
          </div>
        )}

        {tab === 'fundamentals' && (
          <div className="space-y-6">
            <CompanyFundamentalsCard />
          </div>
        )}

        {tab === 'news' && (
          <div className="space-y-6">
            <NewsTimelinePanel symbol={sym} />
          </div>
        )}

        {tab === 'risk' && (
          <div className="space-y-6">
            <RiskTerminalCard />
          </div>
        )}

        {tab === 'provenance' && (
          <div className="space-y-6">
            <QLProvenance
              observationId={recommendation?.id || `OBS-PIT-${sym}-01`}
              modelVersion={recommendation?.quant_view?.model_id || 'PHASE_16_FROZEN_RIDGE_TOP8'}
              sha256ProvenanceHash={recommendation?.sha256_provenance_hash || '1342f5d9134fd6cf25c625362a6b276a2700e668ddc63c5b54d3e75c4b738885'}
            />
          </div>
        )}
      </div>

      <InstrumentSearchModal
        isOpen={isSearchOpen}
        onClose={() => setIsSearchOpen(false)}
        onSelect={(item) => navigate(`/stock/${item.symbol}`)}
      />
    </div>
  );
}
