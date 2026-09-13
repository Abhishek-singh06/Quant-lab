import { useState } from 'react';
import {
  ShieldAlert,
  TrendingDown,
  Target,
  Percent,
  Layers,
  Activity,
  Info,
  CheckCircle2
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import type { RiskProfileType, StopMethod, PositionSizingMethod, RiskDecision, RiskLevel } from '@/types/market';

interface StockRiskScenario {
  symbol: string;
  name: string;
  signalType: 'BUY' | 'HOLD' | 'SELL';
  signalScore: number;
  confidence: number;
  entryPrice: number;
  targetPrice: number;
  atr: number;
  securityVol: number;
  sector: string;
}

const SAMPLE_SCENARIOS: StockRiskScenario[] = [
  {
    symbol: 'RELIANCE',
    name: 'Reliance Industries Ltd.',
    signalType: 'BUY',
    signalScore: 68.5,
    confidence: 0.82,
    entryPrice: 2950.0,
    targetPrice: 3250.0,
    atr: 45.0,
    securityVol: 0.18,
    sector: 'Energy / Oil & Gas',
  },
  {
    symbol: 'TCS',
    name: 'Tata Consultancy Services Ltd.',
    signalType: 'BUY',
    signalScore: 74.0,
    confidence: 0.88,
    entryPrice: 3850.0,
    targetPrice: 4200.0,
    atr: 55.0,
    securityVol: 0.15,
    sector: 'Information Technology',
  },
  {
    symbol: 'HDFCBANK',
    name: 'HDFC Bank Ltd.',
    signalType: 'BUY',
    signalScore: 58.0,
    confidence: 0.65,
    entryPrice: 1650.0,
    targetPrice: 1780.0,
    atr: 28.0,
    securityVol: 0.22,
    sector: 'Financial Services',
  },
  {
    symbol: 'INFY',
    name: 'Infosys Ltd.',
    signalType: 'HOLD',
    signalScore: 42.0,
    confidence: 0.52,
    entryPrice: 1780.0,
    targetPrice: 1850.0,
    atr: 32.0,
    securityVol: 0.20,
    sector: 'Information Technology',
  }
];

export function RiskTerminalCard() {
  const [selectedSymbol, setSelectedSymbol] = useState<string>('RELIANCE');
  const [riskProfile, setRiskProfile] = useState<RiskProfileType>('MODERATE');
  const [stopMethod, setStopMethod] = useState<StopMethod>('ATR_MULTIPLE');
  const [sizingMethod, setSizingMethod] = useState<PositionSizingMethod>('FIXED_RISK');
  const [portfolioValue] = useState<number>(1000000); // 10 Lakh INR
  const [currentDrawdownPct] = useState<number>(4.5); // 4.5%

  const scenario = SAMPLE_SCENARIOS.find(s => s.symbol === selectedSymbol) || SAMPLE_SCENARIOS[0];

  // Profile parameter multipliers
  const profileConfig = {
    CONSERVATIVE: {
      maxPortfolioRisk: 0.03,
      maxPositionRisk: 0.005,
      maxSingleSecurity: 0.05,
      maxSector: 0.15,
      cashBuffer: 0.10,
      minScore: 45.0,
      minConf: 0.60,
    },
    MODERATE: {
      maxPortfolioRisk: 0.05,
      maxPositionRisk: 0.01,
      maxSingleSecurity: 0.10,
      maxSector: 0.25,
      cashBuffer: 0.05,
      minScore: 35.0,
      minConf: 0.50,
    },
    AGGRESSIVE: {
      maxPortfolioRisk: 0.08,
      maxPositionRisk: 0.02,
      maxSingleSecurity: 0.20,
      maxSector: 0.35,
      cashBuffer: 0.02,
      minScore: 25.0,
      minConf: 0.40,
    },
    CUSTOM: {
      maxPortfolioRisk: 0.05,
      maxPositionRisk: 0.01,
      maxSingleSecurity: 0.10,
      maxSector: 0.25,
      cashBuffer: 0.05,
      minScore: 35.0,
      minConf: 0.50,
    }
  }[riskProfile];

  // Stop Price Calculation
  let stopPrice = 0;
  if (stopMethod === 'ATR_MULTIPLE') {
    stopPrice = scenario.entryPrice - (2.0 * scenario.atr);
  } else if (stopMethod === 'FIXED_PERCENTAGE') {
    stopPrice = scenario.entryPrice * 0.95;
  } else {
    stopPrice = scenario.entryPrice - (1.5 * scenario.atr);
  }
  const stopDistance = Math.max(0.01, scenario.entryPrice - stopPrice);
  const stopDistancePct = stopDistance / scenario.entryPrice;

  // Unconstrained Sizing
  const riskBudgetCapital = portfolioValue * profileConfig.maxPositionRisk;
  let unconstrainedQty = 0;
  if (sizingMethod === 'FIXED_RISK') {
    unconstrainedQty = stopDistance > 0 ? riskBudgetCapital / stopDistance : 0;
  } else if (sizingMethod === 'FIXED_ALLOCATION') {
    unconstrainedQty = (portfolioValue * profileConfig.maxSingleSecurity) / scenario.entryPrice;
  } else if (sizingMethod === 'VOLATILITY_ADJUSTED') {
    const volRatio = 0.15 / Math.max(0.05, scenario.securityVol);
    unconstrainedQty = ((portfolioValue * profileConfig.maxPositionRisk * volRatio) / stopDistance);
  } else {
    unconstrainedQty = (riskBudgetCapital / stopDistance) * 0.9;
  }

  const unconstrainedAlloc = (unconstrainedQty * scenario.entryPrice) / portfolioValue;

  // Constraints Binding
  const maxSingleAlloc = profileConfig.maxSingleSecurity;
  const cashAvailableAlloc = 1.0 - profileConfig.cashBuffer;
  const sectorRoomAlloc = profileConfig.maxSector;

  let constrainedAlloc = Math.min(unconstrainedAlloc, maxSingleAlloc);
  constrainedAlloc = Math.min(constrainedAlloc, cashAvailableAlloc);
  constrainedAlloc = Math.min(constrainedAlloc, sectorRoomAlloc);

  // Drawdown scaling penalty
  let drawdownMultiplier = 1.0;
  if (currentDrawdownPct > 5.0) {
    const excessDd = (currentDrawdownPct - 5.0) / 10.0;
    drawdownMultiplier = Math.max(0.3, 1.0 - (excessDd * 0.6));
    constrainedAlloc = constrainedAlloc * drawdownMultiplier;
  }

  // Decision & Gating
  let decision: RiskDecision = 'APPROVED';
  let riskLevel: RiskLevel = 'MODERATE';

  if (scenario.signalScore < profileConfig.minScore) {
    decision = 'REJECTED_SIGNAL_SCORE';
    constrainedAlloc = 0;
  } else if (scenario.confidence < profileConfig.minConf) {
    decision = 'REJECTED_LOW_CONFIDENCE';
    constrainedAlloc = 0;
  } else if (constrainedAlloc < 0.005) {
    decision = 'ZERO_ALLOCATION';
  } else if (constrainedAlloc < unconstrainedAlloc) {
    decision = 'REDUCED_ALLOCATION';
  }

  const finalQuantity = Math.floor((constrainedAlloc * portfolioValue) / scenario.entryPrice);
  const finalSuggestedAlloc = (finalQuantity * scenario.entryPrice) / portfolioValue;
  const estimatedDownside = finalQuantity * stopDistance;
  const positionRiskPct = (estimatedDownside / portfolioValue);

  if (positionRiskPct > 0.015 || currentDrawdownPct > 10.0) {
    riskLevel = 'HIGH';
  } else if (positionRiskPct < 0.005 && currentDrawdownPct < 4.0) {
    riskLevel = 'LOW';
  }

  const riskRewardRatio = (scenario.targetPrice - scenario.entryPrice) / stopDistance;

  return (
    <Card className="border-border bg-surface text-text-primary">
      <CardHeader className="border-b border-border/50 pb-4">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/10 text-accent border border-accent/20">
              <ShieldAlert className="h-5 w-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <CardTitle className="text-xl font-bold tracking-tight">Risk Engine & Position Sizing Terminal</CardTitle>
                <Badge variant="default" className="text-[10px] bg-accent/20 text-accent border border-accent/30 font-mono">
                  PART 14 ENGINE
                </Badge>
              </div>
              <CardDescription className="text-xs text-text-secondary">
                Point-in-Time quantitative risk allocation, downside estimation, constraint resolution & drawdown protection
              </CardDescription>
            </div>
          </div>

          {/* Quick Decision Badge */}
          <div className="flex items-center gap-2">
            <Badge
              className={cn(
                'px-3 py-1 text-xs font-semibold tracking-wide uppercase',
                decision === 'APPROVED' ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30' :
                decision === 'REDUCED_ALLOCATION' ? 'bg-amber-500/15 text-amber-400 border border-amber-500/30' :
                'bg-rose-500/15 text-rose-400 border border-rose-500/30'
              )}
            >
              {decision.replace(/_/g, ' ')}
            </Badge>
            <Badge
              className={cn(
                'px-2.5 py-1 text-xs font-mono',
                riskLevel === 'LOW' ? 'bg-emerald-950 text-emerald-300 border border-emerald-800' :
                riskLevel === 'MODERATE' ? 'bg-blue-950 text-blue-300 border border-blue-800' :
                'bg-amber-950 text-amber-300 border border-amber-800'
              )}
            >
              RISK: {riskLevel}
            </Badge>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-6 pt-5">
        {/* Scenario Controls & Risk Profile Bar */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3 bg-surface-elevated/40 p-3 rounded-lg border border-border/40">
          <div>
            <label className="text-[11px] font-medium text-text-secondary uppercase tracking-wider block mb-1">Target Security</label>
            <select
              value={selectedSymbol}
              onChange={(e) => setSelectedSymbol(e.target.value)}
              className="w-full bg-surface-elevated border border-border text-sm rounded-md px-2.5 py-1.5 text-text-primary focus:outline-none focus:border-accent font-medium"
            >
              {SAMPLE_SCENARIOS.map(s => (
                <option key={s.symbol} value={s.symbol}>
                  {s.symbol} ({s.signalType} Score: {s.signalScore})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="text-[11px] font-medium text-text-secondary uppercase tracking-wider block mb-1">Risk Profile</label>
            <select
              value={riskProfile}
              onChange={(e) => setRiskProfile(e.target.value as RiskProfileType)}
              className="w-full bg-surface-elevated border border-border text-sm rounded-md px-2.5 py-1.5 text-text-primary focus:outline-none focus:border-accent font-medium"
            >
              <option value="CONSERVATIVE">Conservative (0.5% pos risk)</option>
              <option value="MODERATE">Moderate (1.0% pos risk)</option>
              <option value="AGGRESSIVE">Aggressive (2.0% pos risk)</option>
            </select>
          </div>

          <div>
            <label className="text-[11px] font-medium text-text-secondary uppercase tracking-wider block mb-1">Sizing Strategy</label>
            <select
              value={sizingMethod}
              onChange={(e) => setSizingMethod(e.target.value as PositionSizingMethod)}
              className="w-full bg-surface-elevated border border-border text-sm rounded-md px-2.5 py-1.5 text-text-primary focus:outline-none focus:border-accent font-medium"
            >
              <option value="FIXED_RISK">Fixed Risk (Risk Budget / Stop)</option>
              <option value="FIXED_ALLOCATION">Fixed Capital Allocation</option>
              <option value="VOLATILITY_ADJUSTED">Volatility-Adjusted (ATR/Vol)</option>
              <option value="PORTFOLIO_AWARE">Portfolio-Aware Covariance</option>
            </select>
          </div>

          <div>
            <label className="text-[11px] font-medium text-text-secondary uppercase tracking-wider block mb-1">Stop Methodology</label>
            <select
              value={stopMethod}
              onChange={(e) => setStopMethod(e.target.value as StopMethod)}
              className="w-full bg-surface-elevated border border-border text-sm rounded-md px-2.5 py-1.5 text-text-primary focus:outline-none focus:border-accent font-medium"
            >
              <option value="ATR_MULTIPLE">ATR Multiple (2.0 × ATR)</option>
              <option value="FIXED_PERCENTAGE">Fixed 5% Capital Stop</option>
              <option value="SUPPORT_RESISTANCE">Support/Resistance Level</option>
            </select>
          </div>
        </div>

        {/* Primary Allocation & Downside Dashboard */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-gradient-to-br from-indigo-950/40 to-surface-elevated border border-indigo-900/30 rounded-xl p-4">
            <div className="flex items-center justify-between text-indigo-300 text-xs mb-1">
              <span>Suggested Allocation</span>
              <Percent className="h-3.5 w-3.5" />
            </div>
            <div className="text-2xl font-bold font-mono text-text-primary">
              {(finalSuggestedAlloc * 100).toFixed(2)}%
            </div>
            <div className="text-[11px] text-text-secondary mt-1">
              Capital: <span className="font-mono text-indigo-200">₹{(finalQuantity * scenario.entryPrice).toLocaleString('en-IN')}</span> ({finalQuantity} shares)
            </div>
          </div>

          <div className="bg-gradient-to-br from-rose-950/30 to-surface-elevated border border-rose-900/30 rounded-xl p-4">
            <div className="flex items-center justify-between text-rose-300 text-xs mb-1">
              <span>Estimated Downside</span>
              <TrendingDown className="h-3.5 w-3.5" />
            </div>
            <div className="text-2xl font-bold font-mono text-rose-400">
              ₹{estimatedDownside.toLocaleString('en-IN', { maximumFractionDigits: 0 })}
            </div>
            <div className="text-[11px] text-text-secondary mt-1">
              Downside Risk: <span className="font-mono text-rose-300">{(positionRiskPct * 100).toFixed(2)}%</span> of portfolio
            </div>
          </div>

          <div className="bg-surface-elevated/80 border border-border/60 rounded-xl p-4">
            <div className="flex items-center justify-between text-text-secondary text-xs mb-1">
              <span>Stop Price & Distance</span>
              <Target className="h-3.5 w-3.5 text-accent" />
            </div>
            <div className="text-2xl font-bold font-mono text-text-primary">
              ₹{stopPrice.toFixed(1)}
            </div>
            <div className="text-[11px] text-text-secondary mt-1">
              Distance: <span className="font-mono text-amber-300">{(stopDistancePct * 100).toFixed(2)}%</span> (₹{stopDistance.toFixed(1)})
            </div>
          </div>

          <div className="bg-surface-elevated/80 border border-border/60 rounded-xl p-4">
            <div className="flex items-center justify-between text-text-secondary text-xs mb-1">
              <span>Risk / Reward Ratio</span>
              <Activity className="h-3.5 w-3.5 text-emerald-400" />
            </div>
            <div className="text-2xl font-bold font-mono text-emerald-400">
              1 : {riskRewardRatio > 0 ? riskRewardRatio.toFixed(2) : 'N/A'}
            </div>
            <div className="text-[11px] text-text-secondary mt-1">
              Target: <span className="font-mono text-text-primary">₹{scenario.targetPrice.toFixed(1)}</span> (+{(((scenario.targetPrice - scenario.entryPrice) / scenario.entryPrice) * 100).toFixed(1)}%)
            </div>
          </div>
        </div>

        {/* Traceable Constraint Breakdown Ladder */}
        <div className="bg-surface-elevated/30 border border-border/50 rounded-xl p-4 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Layers className="h-4 w-4 text-accent" />
              <h4 className="text-xs font-semibold uppercase tracking-wider text-text-primary">
                Traceable Constraint Resolution Pipeline
              </h4>
            </div>
            <span className="text-[11px] text-text-secondary font-mono">
              Binding Constraint: <span className="text-amber-300 font-semibold">{constrainedAlloc < unconstrainedAlloc ? 'Single Security / Drawdown Bound' : 'None'}</span>
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-2 text-xs">
            <div className="p-3 bg-surface rounded border border-border/60">
              <div className="text-text-secondary text-[11px]">1. Unconstrained Sizer</div>
              <div className="font-mono font-bold text-text-primary text-sm mt-0.5">{(unconstrainedAlloc * 100).toFixed(2)}%</div>
              <div className="text-[10px] text-text-muted mt-1">Sizing formula raw quantity</div>
            </div>

            <div className="p-3 bg-surface rounded border border-border/60">
              <div className="text-text-secondary text-[11px]">2. Single Security Limit</div>
              <div className="font-mono font-bold text-indigo-300 text-sm mt-0.5">{(maxSingleAlloc * 100).toFixed(1)}% max</div>
              <div className="text-[10px] text-text-muted mt-1">{riskProfile} profile cap</div>
            </div>

            <div className="p-3 bg-surface rounded border border-border/60">
              <div className="text-text-secondary text-[11px]">3. Drawdown Defense</div>
              <div className="font-mono font-bold text-amber-300 text-sm mt-0.5">{drawdownMultiplier.toFixed(2)}× scalar</div>
              <div className="text-[10px] text-text-muted mt-1">Portfolio in {currentDrawdownPct.toFixed(1)}% DD</div>
            </div>

            <div className="p-3 bg-surface rounded border border-accent/40 bg-accent/5">
              <div className="text-accent text-[11px] font-semibold">4. Final Suggested</div>
              <div className="font-mono font-bold text-accent text-sm mt-0.5">{(finalSuggestedAlloc * 100).toFixed(2)}%</div>
              <div className="text-[10px] text-text-secondary mt-1">{finalQuantity} shares @ ₹{scenario.entryPrice}</div>
            </div>
          </div>
        </div>

        {/* Risk Warnings & Explanatory Reasoning */}
        <div className="rounded-lg bg-surface-elevated/40 border border-border/60 p-4 space-y-2">
          <div className="flex items-center gap-2 text-xs font-semibold text-text-secondary">
            <Info className="h-4 w-4 text-accent" />
            <span>Risk Assessment & Trace Audit</span>
          </div>
          <p className="text-xs text-text-secondary leading-relaxed font-sans">
            Risk evaluation for <strong className="text-text-primary">{scenario.symbol}</strong> ({scenario.sector}): Recommended allocation is{' '}
            <strong className="text-accent">{(finalSuggestedAlloc * 100).toFixed(2)}%</strong> (₹{(finalQuantity * scenario.entryPrice).toLocaleString('en-IN')}) with strict stop at{' '}
            <strong className="text-amber-300">₹{stopPrice.toFixed(1)}</strong> ({stopMethod.replace(/_/g, ' ')}). Total estimated downside is bounded to ₹{estimatedDownside.toFixed(0)} ({ (positionRiskPct * 100).toFixed(2) }% of portfolio value), fully complying with the {riskProfile} risk profile limits.
          </p>

          <div className="pt-2 flex flex-wrap items-center gap-2 text-[11px]">
            <span className="inline-flex items-center gap-1 text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-800/60">
              <CheckCircle2 className="h-3 w-3" /> Point-in-Time Verified
            </span>
            <span className="inline-flex items-center gap-1 text-indigo-400 bg-indigo-950/60 px-2 py-0.5 rounded border border-indigo-800/60">
              Cash Buffer: {(profileConfig.cashBuffer * 100).toFixed(0)}% Retained
            </span>
            <span className="inline-flex items-center gap-1 text-text-muted bg-surface px-2 py-0.5 rounded border border-border">
              Engine Version: RISK_v1.0.0
            </span>
          </div>
        </div>

        {/* Disclaimer Footer */}
        <div className="text-[10px] text-text-muted border-t border-border/40 pt-3 flex items-center justify-between">
          <span>* Suggested allocations and downside estimates are mathematical risk management outputs and do not represent guarantees or investment advice.</span>
          <span className="font-mono text-text-secondary">QuantLab Risk Engine v1.0</span>
        </div>
      </CardContent>
    </Card>
  );
}
