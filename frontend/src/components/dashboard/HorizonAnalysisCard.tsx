import { useState } from 'react';
import {
  Layers,
  Clock,
  TrendingUp,
  TrendingDown,
  Compass,
  AlertCircle,
  CheckCircle2,
  Calendar,
  Zap,
  Briefcase,
  BarChart3
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import type { CrossHorizonView, HorizonOutlook } from '@/types/market';

interface HorizonAnalysisCardProps {
  initialView?: CrossHorizonView;
}

const SAMPLE_HORIZON_VIEWS: Record<string, CrossHorizonView> = {
  RELIANCE: {
    symbol: 'RELIANCE',
    asOf: new Date().toISOString(),
    shortTerm: {
      predictionId: 'PRED-ST-REL',
      symbol: 'RELIANCE',
      predictionTimestamp: new Date().toISOString(),
      informationAvailableAt: new Date().toISOString(),
      calculatedAt: new Date().toISOString(),
      horizon: 'SHORT_TERM',
      horizonPeriod: '1D - 5D',
      expectedReturn: 0.0082,
      probabilityPositive: 0.61,
      probabilityNegative: 0.39,
      predictedClass: 1,
      expectedVolatility: 0.185,
      confidence: 0.68,
      outlook: 'BULLISH',
      modelVersion: 'ST_GB_v1.0.0',
      featureSetVersion: 'SHORT_TERM_FEATURE_SET_V1',
      targetSetVersion: 'SHORT_TERM_TARGET_SET_V1',
      featureContributions: {
        'rsi_14': 0.0035,
        'momentum_5d': 0.0028,
        'return_1d': 0.0019,
        'volatility_5d': -0.0008,
      }
    },
    mediumTerm: {
      predictionId: 'PRED-MT-REL',
      symbol: 'RELIANCE',
      predictionTimestamp: new Date().toISOString(),
      informationAvailableAt: new Date().toISOString(),
      calculatedAt: new Date().toISOString(),
      horizon: 'MEDIUM_TERM',
      horizonPeriod: '1W - 12W',
      expectedReturn: 0.0450,
      probabilityPositive: 0.67,
      probabilityNegative: 0.33,
      predictedClass: 1,
      expectedVolatility: 0.162,
      confidence: 0.72,
      outlook: 'BULLISH',
      modelVersion: 'MT_GB_v1.0.0',
      featureSetVersion: 'MEDIUM_TERM_FEATURE_SET_V1',
      targetSetVersion: 'MEDIUM_TERM_TARGET_SET_V1',
      featureContributions: {
        'price_vs_sma50': 0.015,
        'fii_flow_20d_norm': 0.012,
        'quarterly_eps_growth_yoy': 0.010,
        'relative_strength_nifty_63d': 0.008,
      }
    },
    longTerm: {
      predictionId: 'PRED-LT-REL',
      symbol: 'RELIANCE',
      predictionTimestamp: new Date().toISOString(),
      informationAvailableAt: new Date().toISOString(),
      calculatedAt: new Date().toISOString(),
      horizon: 'LONG_TERM',
      horizonPeriod: '6M - 5Y',
      expectedReturn: 0.1850,
      probabilityPositive: 0.75,
      probabilityNegative: 0.25,
      predictedClass: 1,
      expectedVolatility: 0.145,
      confidence: 0.84,
      outlook: 'BULLISH',
      modelVersion: 'LT_RIDGE_v1.0.0',
      featureSetVersion: 'LONG_TERM_FEATURE_SET_V1',
      targetSetVersion: 'LONG_TERM_TARGET_SET_V1',
      featureContributions: {
        'return_on_equity_ttm': 0.065,
        'ttm_eps_growth_3y_cagr': 0.052,
        'ebitda_margin_ttm': 0.038,
        'debt_to_equity': -0.015,
      }
    },
    conflict: {
      symbol: 'RELIANCE',
      timestamp: new Date().toISOString(),
      shortTermOutlook: 'BULLISH',
      mediumTermOutlook: 'BULLISH',
      longTermOutlook: 'BULLISH',
      conflictDetected: false,
      conflictSeverity: 'NONE',
      explanation: 'Full bullish confluence across active short, medium, and long term trading and investment horizons.'
    },
    shortTermStatus: 'AVAILABLE',
    mediumTermStatus: 'AVAILABLE',
    longTermStatus: 'AVAILABLE',
    modelVersions: {
      'SHORT_TERM': 'ST_GB_v1.0.0',
      'MEDIUM_TERM': 'MT_GB_v1.0.0',
      'LONG_TERM': 'LT_RIDGE_v1.0.0'
    }
  },
  TCS: {
    symbol: 'TCS',
    asOf: new Date().toISOString(),
    shortTerm: {
      predictionId: 'PRED-ST-TCS',
      symbol: 'TCS',
      predictionTimestamp: new Date().toISOString(),
      informationAvailableAt: new Date().toISOString(),
      calculatedAt: new Date().toISOString(),
      horizon: 'SHORT_TERM',
      horizonPeriod: '1D - 5D',
      expectedReturn: -0.0045,
      probabilityPositive: 0.42,
      probabilityNegative: 0.58,
      predictedClass: 0,
      expectedVolatility: 0.142,
      confidence: 0.70,
      outlook: 'BEARISH',
      modelVersion: 'ST_GB_v1.0.0',
      featureSetVersion: 'SHORT_TERM_FEATURE_SET_V1',
      targetSetVersion: 'SHORT_TERM_TARGET_SET_V1',
      featureContributions: {
        'return_1d': -0.0022,
        'rsi_14': -0.0018,
        'price_vs_high_20d': -0.0015
      }
    },
    mediumTerm: {
      predictionId: 'PRED-MT-TCS',
      symbol: 'TCS',
      predictionTimestamp: new Date().toISOString(),
      informationAvailableAt: new Date().toISOString(),
      calculatedAt: new Date().toISOString(),
      horizon: 'MEDIUM_TERM',
      horizonPeriod: '1W - 12W',
      expectedReturn: 0.0380,
      probabilityPositive: 0.64,
      probabilityNegative: 0.36,
      predictedClass: 1,
      expectedVolatility: 0.138,
      confidence: 0.75,
      outlook: 'BULLISH',
      modelVersion: 'MT_GB_v1.0.0',
      featureSetVersion: 'MEDIUM_TERM_FEATURE_SET_V1',
      targetSetVersion: 'MEDIUM_TERM_TARGET_SET_V1',
      featureContributions: {
        'quarterly_eps_growth_yoy': 0.015,
        'operating_margin_trend': 0.011,
        'price_vs_sma200': 0.008
      }
    },
    longTerm: {
      predictionId: 'PRED-LT-TCS',
      symbol: 'TCS',
      predictionTimestamp: new Date().toISOString(),
      informationAvailableAt: new Date().toISOString(),
      calculatedAt: new Date().toISOString(),
      horizon: 'LONG_TERM',
      horizonPeriod: '6M - 5Y',
      expectedReturn: 0.1620,
      probabilityPositive: 0.78,
      probabilityNegative: 0.22,
      predictedClass: 1,
      expectedVolatility: 0.128,
      confidence: 0.88,
      outlook: 'BULLISH',
      modelVersion: 'LT_RIDGE_v1.0.0',
      featureSetVersion: 'LONG_TERM_FEATURE_SET_V1',
      targetSetVersion: 'LONG_TERM_TARGET_SET_V1',
      featureContributions: {
        'return_on_equity_ttm': 0.072,
        'fcf_yield': 0.045,
        'net_profit_margin_ttm': 0.035
      }
    },
    conflict: {
      symbol: 'TCS',
      timestamp: new Date().toISOString(),
      shortTermOutlook: 'BEARISH',
      mediumTermOutlook: 'BULLISH',
      longTermOutlook: 'BULLISH',
      conflictDetected: true,
      conflictSeverity: 'LOW',
      explanation: 'Short-term technical dip (-0.45% 1-5D) within a solid multi-quarter and multi-year bullish quality thesis. Typical accumulation zone.'
    },
    shortTermStatus: 'AVAILABLE',
    mediumTermStatus: 'AVAILABLE',
    longTermStatus: 'AVAILABLE',
    modelVersions: {
      'SHORT_TERM': 'ST_GB_v1.0.0',
      'MEDIUM_TERM': 'MT_GB_v1.0.0',
      'LONG_TERM': 'LT_RIDGE_v1.0.0'
    }
  }
};

export function HorizonAnalysisCard({ initialView }: HorizonAnalysisCardProps) {
  const [selectedSymbol, setSelectedSymbol] = useState<string>('RELIANCE');
  const [selectedHorizonTab, setSelectedHorizonTab] = useState<'ALL' | 'SHORT' | 'MEDIUM' | 'LONG'>('ALL');

  const activeView = initialView || SAMPLE_HORIZON_VIEWS[selectedSymbol] || SAMPLE_HORIZON_VIEWS['RELIANCE'];

  const renderOutlookBadge = (outlook?: HorizonOutlook) => {
    if (!outlook) return null;
    const isBull = outlook === 'BULLISH';
    const isBear = outlook === 'BEARISH';
    return (
      <Badge
        className={cn(
          'px-2.5 py-0.5 text-xs font-semibold uppercase tracking-wider',
          isBull ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30' :
          isBear ? 'bg-rose-500/15 text-rose-400 border border-rose-500/30' :
          'bg-slate-500/15 text-slate-400 border border-slate-500/30'
        )}
      >
        {isBull ? <TrendingUp className="h-3 w-3 inline mr-1" /> : isBear ? <TrendingDown className="h-3 w-3 inline mr-1" /> : null}
        {outlook}
      </Badge>
    );
  };

  return (
    <Card className="border-border bg-surface text-text-primary">
      <CardHeader className="border-b border-border/50 pb-4">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
              <Compass className="h-5 w-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <CardTitle className="text-xl font-bold tracking-tight">Trading + Investing Multi-Horizon Models</CardTitle>
                <Badge variant="default" className="text-[10px] bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 font-mono">
                  PART 15 ARCHITECTURE
                </Badge>
              </div>
              <CardDescription className="text-xs text-text-secondary">
                Three independent quantitative engines: Short-Term Trading (1-5D), Medium-Term Trading (1-12W), and Long-Term Investing (6M-5Y)
              </CardDescription>
            </div>
          </div>

          {/* Symbol Selector */}
          <div className="flex items-center gap-2">
            <select
              value={selectedSymbol}
              onChange={(e) => setSelectedSymbol(e.target.value)}
              className="bg-surface-elevated border border-border text-xs rounded-md px-3 py-1.5 text-text-primary focus:outline-none focus:border-accent font-medium"
            >
              <option value="RELIANCE">RELIANCE (Full Confluence)</option>
              <option value="TCS">TCS (Short-Term Dip / Long-Term Bull)</option>
            </select>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-6 pt-5">
        {/* Cross-Horizon Conflict Banner */}
        {activeView.conflict && (
          <div className={cn(
            'p-4 rounded-xl border flex items-start gap-3.5',
            activeView.conflict.conflictDetected ? (
              activeView.conflict.conflictSeverity === 'HIGH' ? 'bg-rose-950/20 border-rose-800/40 text-rose-200' :
              'bg-amber-950/20 border-amber-800/40 text-amber-200'
            ) : 'bg-emerald-950/20 border-emerald-800/40 text-emerald-200'
          )}>
            <div className="p-1 rounded-full bg-surface/50 shrink-0 mt-0.5">
              {activeView.conflict.conflictDetected ? (
                <AlertCircle className="h-4 w-4 text-amber-400" />
              ) : (
                <CheckCircle2 className="h-4 w-4 text-emerald-400" />
              )}
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold tracking-wide uppercase">
                  {activeView.conflict.conflictDetected ? `Horizon Divergence (${activeView.conflict.conflictSeverity} Priority)` : 'Cross-Horizon Alignment'}
                </span>
                <Badge variant="outline" className="text-[10px] font-mono border-current px-1.5 py-0">
                  NO HARDCODED AVERAGING
                </Badge>
              </div>
              <p className="text-xs text-text-secondary mt-1 leading-relaxed">
                {activeView.conflict.explanation}
              </p>
            </div>
          </div>
        )}

        {/* Horizon Tabs Filter */}
        <div className="flex items-center gap-2 border-b border-border/60 pb-3">
          <span className="text-xs font-medium text-text-secondary mr-2 flex items-center gap-1">
            <Clock className="h-3.5 w-3.5" /> Filter Horizon:
          </span>
          {[
            { id: 'ALL', label: 'All 3 Horizons' },
            { id: 'SHORT', label: 'Short-Term (1-5 Days)' },
            { id: 'MEDIUM', label: 'Medium-Term (1-12 Weeks)' },
            { id: 'LONG', label: 'Long-Term (6M-5Y)' }
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setSelectedHorizonTab(tab.id as any)}
              className={cn(
                'px-3 py-1 rounded-md text-xs font-medium transition-colors',
                selectedHorizonTab === tab.id ? 'bg-accent text-white font-semibold' : 'bg-surface-elevated text-text-secondary hover:text-text-primary'
              )}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Three Independent Horizon Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {/* 1. SHORT-TERM TRADING */}
          {(selectedHorizonTab === 'ALL' || selectedHorizonTab === 'SHORT') && activeView.shortTerm && (
            <div className="rounded-xl border border-indigo-900/30 bg-gradient-to-b from-indigo-950/20 to-surface-elevated p-4 flex flex-col justify-between space-y-4">
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Zap className="h-4 w-4 text-amber-400" />
                    <span className="text-xs font-bold uppercase tracking-wider text-text-primary">Short-Term Trading</span>
                  </div>
                  {renderOutlookBadge(activeView.shortTerm.outlook)}
                </div>
                <div className="text-[11px] text-text-secondary font-mono flex items-center gap-1.5">
                  <Calendar className="h-3 w-3 text-indigo-400" /> Horizon: <strong>1 to 5 Trading Days</strong>
                </div>

                <div className="grid grid-cols-2 gap-2 pt-2">
                  <div className="bg-surface/80 p-2.5 rounded-lg border border-border/50">
                    <div className="text-[10px] text-text-secondary">Expected Return</div>
                    <div className={cn(
                      'text-lg font-bold font-mono',
                      (activeView.shortTerm.expectedReturn || 0) >= 0 ? 'text-emerald-400' : 'text-rose-400'
                    )}>
                      {((activeView.shortTerm.expectedReturn || 0) >= 0 ? '+' : '')}{((activeView.shortTerm.expectedReturn || 0) * 100).toFixed(2)}%
                    </div>
                  </div>
                  <div className="bg-surface/80 p-2.5 rounded-lg border border-border/50">
                    <div className="text-[10px] text-text-secondary">P(Return &gt; 0)</div>
                    <div className="text-lg font-bold font-mono text-text-primary">
                      {(((activeView.shortTerm.probabilityPositive || 0.5) * 100)).toFixed(0)}%
                    </div>
                  </div>
                </div>

                <div className="space-y-1.5 pt-1 text-xs">
                  <div className="flex justify-between text-text-secondary">
                    <span>Model Confidence:</span>
                    <span className="font-mono text-indigo-300 font-semibold">{((activeView.shortTerm.confidence || 0) * 100).toFixed(0)} / 100</span>
                  </div>
                  <div className="flex justify-between text-text-secondary">
                    <span>Model Version:</span>
                    <span className="font-mono text-text-muted">{activeView.shortTerm.modelVersion}</span>
                  </div>
                  <div className="flex justify-between text-text-secondary">
                    <span>Feature Focus:</span>
                    <span className="text-text-primary font-medium">Technicals, VIX, Breaking News</span>
                  </div>
                </div>
              </div>

              <div className="pt-2 border-t border-border/40 text-[11px] text-text-muted">
                Risk Policy: <strong className="text-amber-300 font-mono">1.5× ATR Stop (Tight)</strong> • Fast Invalidation
              </div>
            </div>
          )}

          {/* 2. MEDIUM-TERM TRADING */}
          {(selectedHorizonTab === 'ALL' || selectedHorizonTab === 'MEDIUM') && activeView.mediumTerm && (
            <div className="rounded-xl border border-sky-900/30 bg-gradient-to-b from-sky-950/20 to-surface-elevated p-4 flex flex-col justify-between space-y-4">
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <BarChart3 className="h-4 w-4 text-sky-400" />
                    <span className="text-xs font-bold uppercase tracking-wider text-text-primary">Medium-Term Trading</span>
                  </div>
                  {renderOutlookBadge(activeView.mediumTerm.outlook)}
                </div>
                <div className="text-[11px] text-text-secondary font-mono flex items-center gap-1.5">
                  <Calendar className="h-3 w-3 text-sky-400" /> Horizon: <strong>1 to 12 Weeks (Quarterly)</strong>
                </div>

                <div className="grid grid-cols-2 gap-2 pt-2">
                  <div className="bg-surface/80 p-2.5 rounded-lg border border-border/50">
                    <div className="text-[10px] text-text-secondary">Expected 4W Return</div>
                    <div className={cn(
                      'text-lg font-bold font-mono',
                      (activeView.mediumTerm.expectedReturn || 0) >= 0 ? 'text-emerald-400' : 'text-rose-400'
                    )}>
                      {((activeView.mediumTerm.expectedReturn || 0) >= 0 ? '+' : '')}{((activeView.mediumTerm.expectedReturn || 0) * 100).toFixed(2)}%
                    </div>
                  </div>
                  <div className="bg-surface/80 p-2.5 rounded-lg border border-border/50">
                    <div className="text-[10px] text-text-secondary">P(Trend &gt; 0)</div>
                    <div className="text-lg font-bold font-mono text-text-primary">
                      {(((activeView.mediumTerm.probabilityPositive || 0.5) * 100)).toFixed(0)}%
                    </div>
                  </div>
                </div>

                <div className="space-y-1.5 pt-1 text-xs">
                  <div className="flex justify-between text-text-secondary">
                    <span>Model Confidence:</span>
                    <span className="font-mono text-sky-300 font-semibold">{((activeView.mediumTerm.confidence || 0) * 100).toFixed(0)} / 100</span>
                  </div>
                  <div className="flex justify-between text-text-secondary">
                    <span>Model Version:</span>
                    <span className="font-mono text-text-muted">{activeView.mediumTerm.modelVersion}</span>
                  </div>
                  <div className="flex justify-between text-text-secondary">
                    <span>Feature Focus:</span>
                    <span className="text-text-primary font-medium">Momentum, SMA50/200, FII Flows</span>
                  </div>
                </div>
              </div>

              <div className="pt-2 border-t border-border/40 text-[11px] text-text-muted">
                Risk Policy: <strong className="text-sky-300 font-mono">2.5× ATR Stop</strong> • Volatility-Adjusted Sizing
              </div>
            </div>
          )}

          {/* 3. LONG-TERM INVESTING */}
          {(selectedHorizonTab === 'ALL' || selectedHorizonTab === 'LONG') && activeView.longTerm && (
            <div className="rounded-xl border border-emerald-900/30 bg-gradient-to-b from-emerald-950/20 to-surface-elevated p-4 flex flex-col justify-between space-y-4">
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Briefcase className="h-4 w-4 text-emerald-400" />
                    <span className="text-xs font-bold uppercase tracking-wider text-text-primary">Long-Term Investing</span>
                  </div>
                  {renderOutlookBadge(activeView.longTerm.outlook)}
                </div>
                <div className="text-[11px] text-text-secondary font-mono flex items-center gap-1.5">
                  <Calendar className="h-3 w-3 text-emerald-400" /> Horizon: <strong>6 Months to 5+ Years</strong>
                </div>

                <div className="grid grid-cols-2 gap-2 pt-2">
                  <div className="bg-surface/80 p-2.5 rounded-lg border border-border/50">
                    <div className="text-[10px] text-text-secondary">Expected 1Y Return</div>
                    <div className={cn(
                      'text-lg font-bold font-mono',
                      (activeView.longTerm.expectedReturn || 0) >= 0 ? 'text-emerald-400' : 'text-rose-400'
                    )}>
                      {((activeView.longTerm.expectedReturn || 0) >= 0 ? '+' : '')}{((activeView.longTerm.expectedReturn || 0) * 100).toFixed(1)}%
                    </div>
                  </div>
                  <div className="bg-surface/80 p-2.5 rounded-lg border border-border/50">
                    <div className="text-[10px] text-text-secondary">P(Value Growth)</div>
                    <div className="text-lg font-bold font-mono text-text-primary">
                      {(((activeView.longTerm.probabilityPositive || 0.5) * 100)).toFixed(0)}%
                    </div>
                  </div>
                </div>

                <div className="space-y-1.5 pt-1 text-xs">
                  <div className="flex justify-between text-text-secondary">
                    <span>Model Confidence:</span>
                    <span className="font-mono text-emerald-300 font-semibold">{((activeView.longTerm.confidence || 0) * 100).toFixed(0)} / 100</span>
                  </div>
                  <div className="flex justify-between text-text-secondary">
                    <span>Model Version:</span>
                    <span className="font-mono text-text-muted">{activeView.longTerm.modelVersion}</span>
                  </div>
                  <div className="flex justify-between text-text-secondary">
                    <span>Feature Focus:</span>
                    <span className="text-text-primary font-medium">ROE, 3Y CAGR, EBITDA Margin, D/E</span>
                  </div>
                </div>
              </div>

              <div className="pt-2 border-t border-border/40 text-[11px] text-text-muted">
                Risk Policy: <strong className="text-emerald-300 font-mono">12% Fixed Stop / Thesis Invalidation</strong>
              </div>
            </div>
          )}
        </div>

        {/* Methodological Provenance & Point-in-Time Assurance */}
        <div className="p-3 bg-surface-elevated/40 rounded-lg border border-border/50 flex flex-wrap items-center justify-between gap-3 text-[11px] text-text-secondary">
          <div className="flex items-center gap-2">
            <Layers className="h-3.5 w-3.5 text-accent" />
            <span>Dataset & Preprocessing isolation: <strong>Independent X, y, scaler & retrain cadence per horizon</strong></span>
          </div>
          <div className="flex items-center gap-3 font-mono text-[10px]">
            <span className="text-emerald-400">PIT Gating: Guaranteed</span>
            <span className="text-indigo-300">Walk-Forward: 4 Folds</span>
            <span className="text-text-muted">Version: HORIZON_v1.0.0</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
