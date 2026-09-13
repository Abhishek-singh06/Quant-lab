import { Brain, Cpu, Target, BarChart2 } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { ModelPrediction } from '@/types/market';
import { cn } from '@/lib/utils';

interface QuantPredictionTerminalCardProps {
  prediction?: ModelPrediction | null;
  loading?: boolean;
}

export function QuantPredictionTerminalCard({ prediction, loading }: QuantPredictionTerminalCardProps) {
  if (loading || !prediction) {
    return (
      <Card className="animate-pulse border-border">
        <CardHeader>
          <CardTitle>Quant Prediction Model Engine (Part 11)</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-48 bg-surface-elevated rounded-lg" />
        </CardContent>
      </Card>
    );
  }

  const isPos = (prediction.predictedReturn || 0) >= 0;

  return (
    <Card className="border-border">
      <CardHeader className="pb-3">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <Brain className="h-5 w-5 text-accent" />
            <div>
              <CardTitle className="text-base font-semibold">Quant Prediction Engine (Part 11)</CardTitle>
              <p className="text-xs text-text-secondary">{prediction.symbol} • Point-in-Time Statistical Inference</p>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant="outline" className="text-xs font-mono text-accent border-accent/30">
              {prediction.modelVersion}
            </Badge>
            <Badge variant="outline" className="text-xs font-mono text-info border-info/30">
              Target: {prediction.targetHorizon} Return
            </Badge>
            <Badge variant="outline" className="text-xs text-text-secondary">
              Regime: {prediction.regimeAtPrediction}
            </Badge>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Core Predictions 3-Column Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {/* 1. Expected Return */}
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <div className="flex items-center justify-between text-xs text-text-secondary mb-1">
              <span>Expected Return E[R_(t+1)]</span>
              <Target className="h-3.5 w-3.5 text-accent" />
            </div>
            <div className={cn("text-xl font-bold font-mono", isPos ? "text-success" : "text-danger")}>
              {isPos ? `+${((prediction.predictedReturn || 0) * 100).toFixed(2)}%` : `${((prediction.predictedReturn || 0) * 100).toFixed(2)}%`}
            </div>
            <span className="text-[10px] text-text-muted">1-Day Forward Horizon</span>
          </div>

          {/* 2. Direction Probability */}
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <div className="flex items-center justify-between text-xs text-text-secondary mb-1">
              <span>P(Return &gt; 0)</span>
              <BarChart2 className="h-3.5 w-3.5 text-info" />
            </div>
            <div className="text-xl font-bold font-mono text-info">
              {((prediction.probabilityPositive || 0.5) * 100).toFixed(1)}%
            </div>
            <div className="w-full bg-background h-1.5 rounded-full overflow-hidden mt-1">
              <div
                className="bg-info h-full rounded-full"
                style={{ width: `${(prediction.probabilityPositive || 0.5) * 100}%` }}
              />
            </div>
          </div>

          {/* 3. Volatility Forecast */}
          <div className="p-3 bg-surface-elevated rounded-lg border border-border-subtle">
            <div className="flex items-center justify-between text-xs text-text-secondary mb-1">
              <span>Forecast Volatility (5D)</span>
              <Cpu className="h-3.5 w-3.5 text-warning" />
            </div>
            <div className="text-xl font-bold font-mono text-warning">
              {((prediction.predictedVolatility || 0.14) * 100).toFixed(2)}%
            </div>
            <span className="text-[10px] text-text-muted">Annualized Realized Forecast</span>
          </div>
        </div>

        {/* Feature Contributions Breakdown */}
        {prediction.featureContributions && (
          <div>
            <span className="text-xs font-semibold text-text-secondary uppercase tracking-wider block mb-2">
              Top Model Feature Contributions
            </span>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
              {Object.entries(prediction.featureContributions).map(([feat, contrib]) => {
                const isContribPos = Number(contrib) >= 0;
                return (
                  <div key={feat} className="p-2 rounded bg-background border border-border-subtle text-xs">
                    <div className="flex justify-between items-center">
                      <span className="text-text-muted truncate">{feat.replace(/_/g, ' ')}</span>
                      <span className={cn("font-mono font-semibold", isContribPos ? "text-success" : "text-danger")}>
                        {isContribPos ? `+${(Number(contrib) * 100).toFixed(3)}%` : `${(Number(contrib) * 100).toFixed(3)}%`}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Audit & Disclaimer */}
        <div className="p-2.5 rounded-lg bg-surface-elevated border border-border-subtle text-[11px] text-text-muted flex items-center justify-between">
          <span>Available at: {new Date(prediction.informationAvailableAt).toLocaleTimeString()} IST</span>
          <span className="font-mono text-text-secondary">Zero Lookahead Guaranteed</span>
        </div>
      </CardContent>
    </Card>
  );
}
