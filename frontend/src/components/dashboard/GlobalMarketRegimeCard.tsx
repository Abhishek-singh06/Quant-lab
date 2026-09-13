import { ShieldCheck, ShieldAlert, Activity, AlertCircle, Compass } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { GlobalMarketRegime } from '@/types/market';
import { cn } from '@/lib/utils';

interface GlobalMarketRegimeCardProps {
  regime?: GlobalMarketRegime | null;
  loading?: boolean;
}

export function GlobalMarketRegimeCard({ regime, loading }: GlobalMarketRegimeCardProps) {
  if (loading || !regime) {
    return (
      <Card className="animate-pulse">
        <CardHeader>
          <CardTitle>Global Market Regime</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-40 bg-surface-elevated rounded-lg" />
        </CardContent>
      </Card>
    );
  }

  const isRiskOn = regime.regimeLabel === 'RISK_ON';
  const isRiskOff = regime.regimeLabel === 'RISK_OFF';
  const isHighVol = regime.regimeLabel === 'HIGH_VOLATILITY';

  const getRegimeColor = () => {
    if (isRiskOn) return 'text-success border-success/30 bg-success/10';
    if (isRiskOff) return 'text-danger border-danger/30 bg-danger/10';
    if (isHighVol) return 'text-warning border-warning/30 bg-warning/10';
    return 'text-info border-info/30 bg-info/10';
  };

  const getScoreColor = (score: number) => {
    if (score > 15) return 'text-success';
    if (score < -15) return 'text-danger';
    return 'text-text-secondary';
  };

  return (
    <Card className="border-border">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Compass className="h-5 w-5 text-accent" />
            <CardTitle className="text-base font-semibold">Global Market Regime</CardTitle>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="outline" className="text-xs uppercase">
              Conf: {regime.confidence}
            </Badge>
            <Badge variant="outline" className="text-xs font-mono">
              v{regime.methodologyVersion}
            </Badge>
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Regime Indicator Banner */}
        <div className={cn('flex items-center justify-between p-3.5 rounded-lg border', getRegimeColor())}>
          <div className="flex items-center gap-3">
            {isRiskOn && <ShieldCheck className="h-6 w-6 text-success shrink-0" />}
            {isRiskOff && <ShieldAlert className="h-6 w-6 text-danger shrink-0" />}
            {isHighVol && <AlertCircle className="h-6 w-6 text-warning shrink-0" />}
            {!isRiskOn && !isRiskOff && !isHighVol && <Activity className="h-6 w-6 text-info shrink-0" />}
            <div>
              <div className="text-sm font-bold tracking-wider">{regime.regimeLabel.replace('_', ' ')}</div>
              <div className="text-xs opacity-80">Composite Risk Score</div>
            </div>
          </div>
          <div className="text-right">
            <div className="text-2xl font-mono font-bold">{regime.compositeScore > 0 ? `+${regime.compositeScore.toFixed(1)}` : regime.compositeScore.toFixed(1)}</div>
            <div className="text-[10px] opacity-70">Scale: -100 to +100</div>
          </div>
        </div>

        {/* Explainable Component Breakdown */}
        <div>
          <div className="text-xs font-medium text-text-secondary mb-2 flex items-center justify-between">
            <span>Factor Attribution</span>
            <span className="text-[10px] text-text-muted">{regime.sourceSnapshotCount} active markets</span>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs">
            <div className="bg-background/60 p-2 rounded border border-border-subtle">
              <span className="text-text-muted text-[10px] block">US Equities</span>
              <span className={cn('font-mono font-semibold', getScoreColor(regime.equityScore))}>
                {regime.equityScore > 0 ? `+${regime.equityScore.toFixed(1)}` : regime.equityScore.toFixed(1)}
              </span>
            </div>
            <div className="bg-background/60 p-2 rounded border border-border-subtle">
              <span className="text-text-muted text-[10px] block">Volatility (VIX)</span>
              <span className={cn('font-mono font-semibold', getScoreColor(regime.volatilityScore))}>
                {regime.volatilityScore > 0 ? `+${regime.volatilityScore.toFixed(1)}` : regime.volatilityScore.toFixed(1)}
              </span>
            </div>
            <div className="bg-background/60 p-2 rounded border border-border-subtle">
              <span className="text-text-muted text-[10px] block">Dollar (DXY)</span>
              <span className={cn('font-mono font-semibold', getScoreColor(regime.dollarScore))}>
                {regime.dollarScore > 0 ? `+${regime.dollarScore.toFixed(1)}` : regime.dollarScore.toFixed(1)}
              </span>
            </div>
            <div className="bg-background/60 p-2 rounded border border-border-subtle">
              <span className="text-text-muted text-[10px] block">US 10Y Yield</span>
              <span className={cn('font-mono font-semibold', getScoreColor(regime.ratesScore))}>
                {regime.ratesScore > 0 ? `+${regime.ratesScore.toFixed(1)}` : regime.ratesScore.toFixed(1)}
              </span>
            </div>
            <div className="bg-background/60 p-2 rounded border border-border-subtle">
              <span className="text-text-muted text-[10px] block">Commodities</span>
              <span className={cn('font-mono font-semibold', getScoreColor(regime.commodityScore))}>
                {regime.commodityScore > 0 ? `+${regime.commodityScore.toFixed(1)}` : regime.commodityScore.toFixed(1)}
              </span>
            </div>
            <div className="bg-background/60 p-2 rounded border border-border-subtle">
              <span className="text-text-muted text-[10px] block">Asia Breadth</span>
              <span className={cn('font-mono font-semibold', getScoreColor(regime.asiaScore))}>
                {regime.asiaScore > 0 ? `+${regime.asiaScore.toFixed(1)}` : regime.asiaScore.toFixed(1)}
              </span>
            </div>
            <div className="bg-background/60 p-2 rounded border border-border-subtle">
              <span className="text-text-muted text-[10px] block">Europe Breadth</span>
              <span className={cn('font-mono font-semibold', getScoreColor(regime.europeScore))}>
                {regime.europeScore > 0 ? `+${regime.europeScore.toFixed(1)}` : regime.europeScore.toFixed(1)}
              </span>
            </div>
            <div className="bg-background/60 p-2 rounded border border-border-subtle">
              <span className="text-text-muted text-[10px] block">Confidence</span>
              <span className="font-mono font-semibold text-text-primary">{regime.confidence}</span>
            </div>
          </div>
        </div>

        {/* Explainability Rationale */}
        <div className="text-xs text-text-secondary bg-surface-elevated/40 p-2.5 rounded border border-border-subtle">
          <span className="font-semibold text-text-primary">Rationale: </span>
          {regime.explanation}
        </div>
      </CardContent>
    </Card>
  );
}
