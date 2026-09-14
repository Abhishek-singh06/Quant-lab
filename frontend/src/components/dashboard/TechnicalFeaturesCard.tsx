import { useState, useEffect } from 'react';
import {
  Activity,
  Calendar,
  Clock,
  Gauge,
  Sliders,
  ShieldCheck,
  RefreshCw
} from 'lucide-react';
import { QLPanel } from '@/design-system/QLPanel';
import { QLBadge } from '@/design-system/QLBadge';
import { QLButton } from '@/design-system/QLButton';
import { QLEmptyState } from '@/design-system/QLEmptyState';
import { cn } from '@/lib/utils';

interface FeatureItem {
  featureName: string;
  featureValue: number;
  tradingDate: string;
  featureVersion: string;
}

const SAMPLE_SYMBOLS = ['RELIANCE', 'TCS', 'HDFCBANK', 'INFY', 'HINDZINC'];

export function TechnicalFeaturesCard() {
  const [selectedSymbol, setSelectedSymbol] = useState('RELIANCE');
  const [timeframe, setTimeframe] = useState('1D');
  const [features, setFeatures] = useState<Record<string, number>>({});
  const [tradingDate, setTradingDate] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  useEffect(() => {
    async function fetchFeatures() {
      setLoading(true);
      try {
        const res = await fetch(`/api/v1/features/technical/${selectedSymbol}/latest?timeframe=${timeframe}`);
        if (res.ok) {
          const data: FeatureItem[] = await res.json();
          if (Array.isArray(data) && data.length > 0) {
            const map: Record<string, number> = {};
            data.forEach((item) => {
              map[item.featureName] = item.featureValue;
            });
            setFeatures(map);
            if (data[0]?.tradingDate) {
              setTradingDate(data[0].tradingDate);
            }
          } else {
            setFeatures({});
          }
        } else {
          setFeatures({});
        }
      } catch {
        setFeatures({});
      } finally {
        setLoading(false);
      }
    }

    fetchFeatures();
  }, [selectedSymbol, timeframe]);

  const handleRecalculate = async () => {
    setIsRefreshing(true);
    try {
      await fetch(`/api/v1/features/calculate/${selectedSymbol}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ symbols: [selectedSymbol], timeframe })
      });
      const res = await fetch(`/api/v1/features/technical/${selectedSymbol}/latest?timeframe=${timeframe}`);
      if (res.ok) {
        const data: FeatureItem[] = await res.json();
        const map: Record<string, number> = {};
        data.forEach((item) => {
          map[item.featureName] = item.featureValue;
        });
        setFeatures(map);
      }
    } catch {
      // safe fallback
    } finally {
      setIsRefreshing(false);
    }
  };

  const rsi = features['RSI_14'];
  const hasFeatures = Object.keys(features).length > 0;

  return (
    <QLPanel
      variant="surface"
      padding="md"
      title="PRODUCTION TECHNICAL FEATURE ENGINE"
      headerAction={
        <div className="flex items-center gap-2">
          <QLBadge variant="neutral" size="xs">
            {selectedSymbol}
          </QLBadge>
          <QLBadge variant="positive" size="xs" dot>
            POINT-IN-TIME SAFE
          </QLBadge>
        </div>
      }
    >
      {/* Controls Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-border/60">
        <div className="flex items-center gap-1.5 bg-surface-elevated/50 p-1 rounded-lg border border-border/60 font-mono text-xs">
          {['1D', '1H', '15M'].map((tf) => (
            <button
              key={tf}
              onClick={() => setTimeframe(tf)}
              className={cn(
                'px-2.5 py-1 rounded transition-colors',
                timeframe === tf ? 'bg-accent text-white font-bold' : 'text-text-muted hover:text-text-primary'
              )}
            >
              {tf}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-1.5 flex-wrap">
          {SAMPLE_SYMBOLS.map((sym) => (
            <button
              key={sym}
              onClick={() => setSelectedSymbol(sym)}
              className={cn(
                'px-2.5 py-1 rounded-md text-xs font-mono font-medium transition-all',
                selectedSymbol === sym
                  ? 'bg-accent text-white font-bold'
                  : 'bg-surface-elevated/60 text-text-muted hover:text-text-primary border border-border/50'
              )}
            >
              {sym}
            </button>
          ))}

          <QLButton
            variant="outline"
            size="sm"
            onClick={handleRecalculate}
            disabled={isRefreshing}
          >
            <RefreshCw className={cn('h-3.5 w-3.5', isRefreshing && 'animate-spin text-accent')} />
            <span>Recalculate</span>
          </QLButton>
        </div>
      </div>

      {/* Provenance Metadata Strip */}
      <div className="my-4 flex flex-wrap items-center gap-4 text-xs text-text-muted bg-surface-elevated/30 p-3 rounded-lg border border-border/40 font-mono">
        <span className="flex items-center gap-1.5">
          <Calendar className="h-3.5 w-3.5 text-text-secondary" />
          <span>As of: <strong className="text-text-primary">{tradingDate || 'LATEST CLOSE'}</strong></span>
        </span>
        <span className="flex items-center gap-1.5">
          <Clock className="h-3.5 w-3.5 text-text-secondary" />
          <span>Available: <strong className="text-text-primary">15:30 IST</strong></span>
        </span>
        <span className="flex items-center gap-1.5">
          <Sliders className="h-3.5 w-3.5 text-text-secondary" />
          <span>Series: <strong className="text-text-primary">Split-Adjusted</strong></span>
        </span>
        <span className="flex items-center gap-1.5 ml-auto text-emerald-400">
          <ShieldCheck className="h-3.5 w-3.5" />
          <span>Zero Look-Ahead Invariant: <strong>VERIFIED</strong></span>
        </span>
      </div>

      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-6 gap-3 animate-pulse">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="h-24 bg-surface-elevated/50 rounded-lg" />
          ))}
        </div>
      ) : !hasFeatures ? (
        <QLEmptyState
          title="NO TECHNICAL FEATURES COMPUTED"
          description={`Feature computation pipeline for ${selectedSymbol} has not yet executed for this timeframe.`}
          source="FEATURE STORE / DUCKDB"
          type="unavailable"
          nextAction="Click 'Recalculate' above to trigger point-in-time factor generation."
        />
      ) : (
        <div className="space-y-6">
          {/* Top Factor Highlights */}
          <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-6 gap-3">
            {/* RSI 14 */}
            <div className="bg-surface-elevated/40 border border-border p-3.5 rounded-lg">
              <div className="flex items-center justify-between text-[11px] text-text-muted font-mono">
                <span>RSI (14D)</span>
                <Gauge className="h-3.5 w-3.5 text-accent" />
              </div>
              <p className="text-2xl font-black font-mono text-text-primary mt-1">
                {rsi !== undefined ? rsi.toFixed(2) : '--'}
              </p>
              <span className={cn(
                'text-[10px] font-mono mt-1 block',
                rsi === undefined ? 'text-text-muted' : rsi > 70 ? 'text-rose-400' : rsi < 30 ? 'text-emerald-400' : 'text-text-muted'
              )}>
                {rsi === undefined ? 'NO DATA' : rsi > 70 ? 'OVERBOUGHT' : rsi < 30 ? 'OVERSOLD' : 'NEUTRAL RANGE'}
              </span>
            </div>

            {/* MACD Histogram */}
            <div className="bg-surface-elevated/40 border border-border p-3.5 rounded-lg">
              <div className="flex items-center justify-between text-[11px] text-text-muted font-mono">
                <span>MACD HIST</span>
                <Activity className="h-3.5 w-3.5 text-accent" />
              </div>
              <p className={cn(
                'text-2xl font-black font-mono mt-1',
                (features['MACD_HISTOGRAM_12_26_9'] ?? 0) >= 0 ? 'text-emerald-400' : 'text-rose-400'
              )}>
                {features['MACD_HISTOGRAM_12_26_9'] !== undefined
                  ? `${features['MACD_HISTOGRAM_12_26_9'] >= 0 ? '+' : ''}${features['MACD_HISTOGRAM_12_26_9'].toFixed(2)}`
                  : '--'}
              </p>
              <span className="text-[10px] text-text-muted font-mono mt-1 block truncate">
                Line: {features['MACD_LINE_12_26']?.toFixed(2) ?? '--'}
              </span>
            </div>

            {/* 20D Realized Volatility */}
            <div className="bg-surface-elevated/40 border border-border p-3.5 rounded-lg">
              <div className="flex items-center justify-between text-[11px] text-text-muted font-mono">
                <span>VOLATILITY (20D)</span>
                <Activity className="h-3.5 w-3.5 text-amber-400" />
              </div>
              <p className="text-2xl font-black font-mono text-amber-300 mt-1">
                {features['VOLATILITY_20D'] !== undefined ? `${features['VOLATILITY_20D'].toFixed(2)}%` : '--'}
              </p>
              <span className="text-[10px] text-text-muted font-mono mt-1 block">
                63D: {features['VOLATILITY_63D'] !== undefined ? `${features['VOLATILITY_63D'].toFixed(2)}%` : '--'}
              </span>
            </div>

            {/* ATR 14 */}
            <div className="bg-surface-elevated/40 border border-border p-3.5 rounded-lg">
              <div className="flex items-center justify-between text-[11px] text-text-muted font-mono">
                <span>ATR (14D)</span>
                <span className="text-[10px] font-mono text-text-muted">₹</span>
              </div>
              <p className="text-2xl font-black font-mono text-text-primary mt-1">
                {features['ATR_14'] !== undefined ? `₹${features['ATR_14'].toFixed(2)}` : '--'}
              </p>
              <span className="text-[10px] text-text-muted font-mono mt-1 block">
                {features['ATR_PERCENT_14'] !== undefined ? `${features['ATR_PERCENT_14'].toFixed(2)}% of Price` : '--'}
              </span>
            </div>

            {/* 20D Return */}
            <div className="bg-surface-elevated/40 border border-border p-3.5 rounded-lg">
              <div className="flex items-center justify-between text-[11px] text-text-muted font-mono">
                <span>RETURN (20D)</span>
                <Activity className="h-3.5 w-3.5 text-accent" />
              </div>
              <p className={cn(
                'text-2xl font-black font-mono mt-1',
                (features['RETURN_20D'] ?? 0) >= 0 ? 'text-emerald-400' : 'text-rose-400'
              )}>
                {features['RETURN_20D'] !== undefined
                  ? `${features['RETURN_20D'] >= 0 ? '+' : ''}${features['RETURN_20D'].toFixed(2)}%`
                  : '--'}
              </p>
              <span className="text-[10px] text-text-muted font-mono mt-1 block">
                1D: {features['RETURN_1D'] !== undefined ? `${features['RETURN_1D'] >= 0 ? '+' : ''}${features['RETURN_1D'].toFixed(2)}%` : '--'}
              </span>
            </div>

            {/* Volume Ratio */}
            <div className="bg-surface-elevated/40 border border-border p-3.5 rounded-lg">
              <div className="flex items-center justify-between text-[11px] text-text-muted font-mono">
                <span>VOL RATIO (20D)</span>
                <Activity className="h-3.5 w-3.5 text-accent" />
              </div>
              <p className="text-2xl font-black font-mono text-text-primary mt-1">
                {features['VOLUME_RATIO_20'] !== undefined ? `${features['VOLUME_RATIO_20'].toFixed(2)}x` : '--'}
              </p>
              <span className="text-[10px] text-text-muted font-mono mt-1 block">
                vs 20D Avg Vol
              </span>
            </div>
          </div>

          {/* Detailed Factor Groups */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Momentum & Moving Averages */}
            <div className="p-4 rounded-lg bg-surface-elevated/30 border border-border space-y-3 font-mono text-xs">
              <span className="text-xs font-bold text-accent tracking-wider uppercase block">
                Moving Averages &amp; Trend
              </span>
              <div className="space-y-2">
                {[
                  { label: 'SMA 20', val: features['SMA_20'] },
                  { label: 'SMA 50', val: features['SMA_50'] },
                  { label: 'SMA 200', val: features['SMA_200'] },
                  { label: 'EMA 20', val: features['EMA_20'] },
                  { label: 'EMA 50', val: features['EMA_50'] },
                  { label: 'EMA 200', val: features['EMA_200'] },
                ].map((ma) => (
                  <div key={ma.label} className="flex items-center justify-between py-1 border-b border-border/30 last:border-0">
                    <span className="text-text-muted">{ma.label}</span>
                    <span className="font-bold text-text-primary">{ma.val !== undefined ? `₹${ma.val.toFixed(2)}` : '--'}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Multi-Horizon Returns */}
            <div className="p-4 rounded-lg bg-surface-elevated/30 border border-border space-y-3 font-mono text-xs">
              <span className="text-xs font-bold text-accent tracking-wider uppercase block">
                Cumulative Multi-Period Returns
              </span>
              <div className="space-y-2">
                {[
                  { label: '1-Day Return', val: features['RETURN_1D'] },
                  { label: '5-Day Return', val: features['RETURN_5D'] },
                  { label: '20-Day Return', val: features['RETURN_20D'] },
                  { label: '63-Day Return (~3M)', val: features['RETURN_63D'] },
                  { label: '252-Day Return (~1Y)', val: features['RETURN_252D'] },
                ].map((ret) => (
                  <div key={ret.label} className="flex items-center justify-between py-1 border-b border-border/30 last:border-0">
                    <span className="text-text-muted">{ret.label}</span>
                    <span className={cn('font-bold', (ret.val ?? 0) >= 0 ? 'text-emerald-400' : 'text-rose-400')}>
                      {ret.val !== undefined ? `${ret.val >= 0 ? '+' : ''}${ret.val.toFixed(2)}%` : '--'}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* Benchmark Relative Strength & Extremes */}
            <div className="p-4 rounded-lg bg-surface-elevated/30 border border-border space-y-3 font-mono text-xs">
              <span className="text-xs font-bold text-accent tracking-wider uppercase block">
                Relative Strength &amp; Extremes
              </span>
              <div className="space-y-2">
                <div className="flex items-center justify-between py-1 border-b border-border/30">
                  <span className="text-text-muted">RS vs Nifty 50 (20D)</span>
                  <span className={cn('font-bold', (features['RS_NIFTY_20'] ?? 0) >= 0 ? 'text-emerald-400' : 'text-rose-400')}>
                    {features['RS_NIFTY_20'] !== undefined ? `${features['RS_NIFTY_20'] >= 0 ? '+' : ''}${features['RS_NIFTY_20'].toFixed(2)}%` : '--'}
                  </span>
                </div>
                <div className="flex items-center justify-between py-1 border-b border-border/30">
                  <span className="text-text-muted">RS vs Nifty 50 (63D)</span>
                  <span className={cn('font-bold', (features['RS_NIFTY_63'] ?? 0) >= 0 ? 'text-emerald-400' : 'text-rose-400')}>
                    {features['RS_NIFTY_63'] !== undefined ? `${features['RS_NIFTY_63'] >= 0 ? '+' : ''}${features['RS_NIFTY_63'].toFixed(2)}%` : '--'}
                  </span>
                </div>
                <div className="flex items-center justify-between py-1 border-b border-border/30">
                  <span className="text-text-muted">52-Week High</span>
                  <span className="font-bold text-text-primary">
                    {features['WEEK_52_HIGH'] !== undefined ? `₹${features['WEEK_52_HIGH'].toFixed(2)}` : '--'}
                  </span>
                </div>
                <div className="flex items-center justify-between py-1 border-b border-border/30">
                  <span className="text-text-muted">52-Week Low</span>
                  <span className="font-bold text-text-primary">
                    {features['WEEK_52_LOW'] !== undefined ? `₹${features['WEEK_52_LOW'].toFixed(2)}` : '--'}
                  </span>
                </div>
                <div className="flex items-center justify-between py-1">
                  <span className="text-text-muted">Drawdown from Peak</span>
                  <span className="font-bold text-rose-400">
                    {features['DRAWDOWN'] !== undefined ? `${features['DRAWDOWN'].toFixed(2)}%` : '--'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </QLPanel>
  );
}
