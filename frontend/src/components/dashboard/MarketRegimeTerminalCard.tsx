import { Layers } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { MarketRegime } from '@/types/market';
import { cn } from '@/lib/utils';

interface MarketRegimeTerminalCardProps {
  regime?: MarketRegime | null;
  loading?: boolean;
}

export function MarketRegimeTerminalCard({ regime, loading }: MarketRegimeTerminalCardProps) {
  if (loading || !regime) {
    return (
      <Card className="animate-pulse border-border">
        <CardHeader>
          <CardTitle>Market Regime Engine (Multi-Signal)</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-48 bg-surface-elevated rounded-lg" />
        </CardContent>
      </Card>
    );
  }

  const isBull = regime.directionRegime === 'BULL';
  const isBear = regime.directionRegime === 'BEAR';
  const isSideways = regime.directionRegime === 'SIDEWAYS';

  const getDirectionBadge = () => {
    if (isBull) return <Badge className="bg-success/20 text-success border-success/40">BULL REGIME</Badge>;
    if (isBear) return <Badge className="bg-danger/20 text-danger border-danger/40">BEAR REGIME</Badge>;
    if (isSideways) return <Badge className="bg-info/20 text-info border-info/40">SIDEWAYS</Badge>;
    return <Badge className="bg-warning/20 text-warning border-warning/40">TRANSITION</Badge>;
  };

  const getVolBadge = () => {
    if (regime.volatilityRegime === 'LOW_VOL') return <Badge variant="outline" className="text-success border-success/30">LOW VOL</Badge>;
    if (regime.volatilityRegime === 'NORMAL_VOL') return <Badge variant="outline" className="text-info border-info/30">NORMAL VOL</Badge>;
    if (regime.volatilityRegime === 'HIGH_VOL') return <Badge variant="outline" className="text-warning border-warning/30">HIGH VOL</Badge>;
    return <Badge variant="outline" className="text-danger border-danger/30">EXTREME VOL</Badge>;
  };

  const getRiskBadge = () => {
    if (regime.riskRegime === 'RISK_ON') return <Badge variant="outline" className="text-success border-success/30">RISK-ON</Badge>;
    if (regime.riskRegime === 'RISK_OFF') return <Badge variant="outline" className="text-danger border-danger/30">RISK-OFF</Badge>;
    return <Badge variant="outline" className="text-text-secondary border-border">NEUTRAL RISK</Badge>;
  };

  return (
    <Card className="border-border">
      <CardHeader className="pb-3">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <Layers className="h-5 w-5 text-accent" />
            <div>
              <CardTitle className="text-base font-semibold">Indian Market Regime Engine</CardTitle>
              <p className="text-xs text-text-secondary">{regime.symbol} • Point-in-Time Calibrated</p>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            {getDirectionBadge()}
            {getVolBadge()}
            {getRiskBadge()}
            <Badge variant="outline" className="text-xs font-mono text-accent border-accent/30">
              {(regime.confidence * 100).toFixed(0)}% Conf
            </Badge>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Composite Score Meter */}
        <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs text-text-secondary">Composite Direction Score</span>
            <span className={cn(
              "text-base font-bold font-mono",
              regime.directionScore > 15 ? "text-success" : regime.directionScore < -15 ? "text-danger" : "text-info"
            )}>
              {regime.directionScore > 0 ? `+${regime.directionScore.toFixed(1)}` : regime.directionScore.toFixed(1)} / 100
            </span>
          </div>

          <div className="relative w-full h-2.5 bg-background rounded-full overflow-hidden flex border border-border">
            {/* Center zero indicator */}
            <div className="w-1/2 flex justify-end bg-transparent">
              {regime.directionScore < 0 && (
                <div
                  className="h-full bg-danger rounded-l-full"
                  style={{ width: `${Math.min(100, Math.abs(regime.directionScore))}%` }}
                />
              )}
            </div>
            <div className="w-1/2 flex justify-start bg-transparent">
              {regime.directionScore > 0 && (
                <div
                  className="h-full bg-success rounded-r-full"
                  style={{ width: `${Math.min(100, regime.directionScore)}%` }}
                />
              )}
            </div>
          </div>
          <div className="flex justify-between text-[10px] text-text-muted mt-1 font-mono">
            <span>-100 (Extreme Bear)</span>
            <span>0 (Neutral)</span>
            <span>+100 (Extreme Bull)</span>
          </div>
        </div>

        {/* Probabilities Breakdown */}
        <div className="grid grid-cols-3 gap-2">
          <div className="p-2 rounded-lg bg-background border border-border-subtle text-center">
            <span className="text-[11px] text-text-muted block">P(Bull)</span>
            <span className="text-sm font-bold font-mono text-success">
              {((regime.probBull || 0) * 100).toFixed(1)}%
            </span>
          </div>
          <div className="p-2 rounded-lg bg-background border border-border-subtle text-center">
            <span className="text-[11px] text-text-muted block">P(Sideways)</span>
            <span className="text-sm font-bold font-mono text-info">
              {((regime.probSideways || 0) * 100).toFixed(1)}%
            </span>
          </div>
          <div className="p-2 rounded-lg bg-background border border-border-subtle text-center">
            <span className="text-[11px] text-text-muted block">P(Bear)</span>
            <span className="text-sm font-bold font-mono text-danger">
              {((regime.probBear || 0) * 100).toFixed(1)}%
            </span>
          </div>
        </div>

        {/* Component Scores */}
        {regime.componentScores && regime.componentScores.length > 0 && (
          <div>
            <span className="text-xs font-semibold text-text-secondary uppercase tracking-wider block mb-2">
              Independent Signal Decomposition ({regime.componentScores.length} signals)
            </span>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
              {regime.componentScores.slice(0, 6).map((comp) => {
                const isPos = comp.componentScore >= 0;
                return (
                  <div key={comp.componentName} className="p-2 rounded bg-surface-elevated border border-border-subtle">
                    <div className="flex justify-between items-center text-[11px]">
                      <span className="text-text-muted truncate">{comp.componentName.replace(/_/g, ' ')}</span>
                      <span className={cn("font-mono font-semibold", isPos ? "text-success" : "text-danger")}>
                        {isPos ? `+${comp.componentScore.toFixed(0)}` : comp.componentScore.toFixed(0)}
                      </span>
                    </div>
                    <div className="flex justify-between text-[10px] text-text-muted mt-1">
                      <span>Weight: {(comp.effectiveWeight * 100).toFixed(0)}%</span>
                      <span className="truncate max-w-[80px]">{comp.source}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Regime Continuity & Explanation */}
        <div className="text-xs text-text-secondary bg-background p-3 rounded-lg border border-border-subtle space-y-1">
          <div className="flex justify-between items-center">
            <span className="font-semibold text-text-primary">Regime Tenure: {regime.daysInRegime} Days</span>
            <span className="text-[11px] text-text-muted">Model: {regime.modelVersion}</span>
          </div>
          <p className="text-text-muted leading-relaxed">{regime.explanation}</p>
        </div>
      </CardContent>
    </Card>
  );
}
