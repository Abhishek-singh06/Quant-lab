import React, { useState } from 'react';
import {
  Play,
  ShieldCheck,
  AlertTriangle,
  Layers,
  CheckCircle2
} from 'lucide-react';
import {
  AreaChart,
  Area,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend
} from 'recharts';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import type {
  BacktestTrade,
  BacktestEquityCurvePoint,
  BacktestPerformanceMetrics,
  BacktestRejectedSignal
} from '@/types/market';

// Realistic sample backtest result for interactive terminal display
const MOCK_EQUITY_CURVE: BacktestEquityCurvePoint[] = [
  { pointDate: '2024-01-02', strategyEquity: 1000000, strategyReturnPct: 0.0, strategyDrawdownPct: 0.0, buyAndHoldEquity: 1000000, buyAndHoldReturnPct: 0.0, benchmarkEquity: 1000000, benchmarkReturnPct: 0.0 },
  { pointDate: '2024-01-15', strategyEquity: 1024500, strategyReturnPct: 2.45, strategyDrawdownPct: 0.0, buyAndHoldEquity: 1012000, buyAndHoldReturnPct: 1.2, benchmarkEquity: 1008000, benchmarkReturnPct: 0.8 },
  { pointDate: '2024-02-01', strategyEquity: 1058000, strategyReturnPct: 5.8, strategyDrawdownPct: 0.0, buyAndHoldEquity: 1021000, buyAndHoldReturnPct: 2.1, benchmarkEquity: 1014000, benchmarkReturnPct: 1.4 },
  { pointDate: '2024-02-15', strategyEquity: 1042000, strategyReturnPct: 4.2, strategyDrawdownPct: 1.51, buyAndHoldEquity: 995000, buyAndHoldReturnPct: -0.5, benchmarkEquity: 998000, benchmarkReturnPct: -0.2 },
  { pointDate: '2024-03-01', strategyEquity: 1081000, strategyReturnPct: 8.1, strategyDrawdownPct: 0.0, buyAndHoldEquity: 1015000, buyAndHoldReturnPct: 1.5, benchmarkEquity: 1022000, benchmarkReturnPct: 2.2 },
  { pointDate: '2024-03-15', strategyEquity: 1073000, strategyReturnPct: 7.3, strategyDrawdownPct: 0.74, buyAndHoldEquity: 1008000, buyAndHoldReturnPct: 0.8, benchmarkEquity: 1019000, benchmarkReturnPct: 1.9 },
  { pointDate: '2024-04-01', strategyEquity: 1109000, strategyReturnPct: 10.9, strategyDrawdownPct: 0.0, buyAndHoldEquity: 1034000, buyAndHoldReturnPct: 3.4, benchmarkEquity: 1038000, benchmarkReturnPct: 3.8 },
  { pointDate: '2024-04-15', strategyEquity: 1098000, strategyReturnPct: 9.8, strategyDrawdownPct: 0.99, buyAndHoldEquity: 1029000, buyAndHoldReturnPct: 2.9, benchmarkEquity: 1032000, benchmarkReturnPct: 3.2 },
  { pointDate: '2024-05-01', strategyEquity: 1125000, strategyReturnPct: 12.5, strategyDrawdownPct: 0.0, buyAndHoldEquity: 1048000, buyAndHoldReturnPct: 4.8, benchmarkEquity: 1051000, benchmarkReturnPct: 5.1 },
  { pointDate: '2024-05-15', strategyEquity: 1118000, strategyReturnPct: 11.8, strategyDrawdownPct: 0.62, buyAndHoldEquity: 1041000, buyAndHoldReturnPct: 4.1, benchmarkEquity: 1046000, benchmarkReturnPct: 4.6 },
  { pointDate: '2024-06-03', strategyEquity: 1145200, strategyReturnPct: 14.52, strategyDrawdownPct: 0.0, buyAndHoldEquity: 1062000, buyAndHoldReturnPct: 6.2, benchmarkEquity: 1074000, benchmarkReturnPct: 7.4 },
];

const MOCK_METRICS: BacktestPerformanceMetrics = {
  totalReturnPct: 14.52,
  cagr: 18.4,
  annualizedVolatility: 11.2,
  sharpeRatio: 1.68,
  sortinoRatio: 2.15,
  maxDrawdownPct: 3.85,
  maxDrawdownDurationDays: 14,
  calmarRatio: 4.78,
  winRatePct: 68.75,
  profitFactor: 2.45,
  averageTradeReturnPct: 1.35,
  averageWinReturnPct: 2.85,
  averageLossReturnPct: -1.40,
  winLossRatio: 2.04,
  totalTradesCount: 16,
  winningTradesCount: 11,
  losingTradesCount: 5,
  annualizedTurnover: 3.2,
  betaToBenchmark: 0.78,
  alphaToBenchmark: 4.25,
  informationRatio: 1.12,
  regimeBreakdownMetrics: {
    BULL: { trade_count: 10, win_rate_pct: 70.0, total_net_pnl: 102400, avg_return_pct: 1.65 },
    SIDEWAYS: { trade_count: 6, win_rate_pct: 66.7, total_net_pnl: 42800, avg_return_pct: 0.85 }
  }
};

const MOCK_TRADES: BacktestTrade[] = [
  {
    id: 't-1',
    runId: 'r-1',
    symbol: 'RELIANCE',
    side: 'LONG',
    quantity: 80,
    entryTimestamp: '2024-01-08T09:15:00Z',
    exitTimestamp: '2024-01-22T09:15:00Z',
    entryPrice: 2540.0,
    exitPrice: 2685.0,
    grossPnl: 11600.0,
    netPnl: 11210.0,
    returnPct: 5.52,
    totalFees: 290.0,
    totalSlippage: 100.0,
    holdingPeriodDays: 14,
    exitReason: 'SIGNAL',
    regimeAtEntry: 'BULL',
    regimeAtExit: 'BULL'
  },
  {
    id: 't-2',
    runId: 'r-1',
    symbol: 'TCS',
    side: 'LONG',
    quantity: 50,
    entryTimestamp: '2024-01-16T09:15:00Z',
    exitTimestamp: '2024-01-30T09:15:00Z',
    entryPrice: 3820.0,
    exitPrice: 3960.0,
    grossPnl: 7000.0,
    netPnl: 6620.0,
    returnPct: 3.47,
    totalFees: 280.0,
    totalSlippage: 100.0,
    holdingPeriodDays: 14,
    exitReason: 'SIGNAL',
    regimeAtEntry: 'BULL',
    regimeAtExit: 'BULL'
  },
  {
    id: 't-3',
    runId: 'r-1',
    symbol: 'INFY',
    side: 'LONG',
    quantity: 120,
    entryTimestamp: '2024-02-05T09:15:00Z',
    exitTimestamp: '2024-02-14T09:15:00Z',
    entryPrice: 1650.0,
    exitPrice: 1610.0,
    grossPnl: -4800.0,
    netPnl: -5120.0,
    returnPct: -2.59,
    totalFees: 220.0,
    totalSlippage: 100.0,
    holdingPeriodDays: 9,
    exitReason: 'STOP_LOSS',
    regimeAtEntry: 'SIDEWAYS',
    regimeAtExit: 'SIDEWAYS'
  },
  {
    id: 't-4',
    runId: 'r-1',
    symbol: 'HDFCBANK',
    side: 'LONG',
    quantity: 100,
    entryTimestamp: '2024-02-20T09:15:00Z',
    exitTimestamp: '2024-03-12T09:15:00Z',
    entryPrice: 1420.0,
    exitPrice: 1530.0,
    grossPnl: 11000.0,
    netPnl: 10640.0,
    returnPct: 7.49,
    totalFees: 240.0,
    totalSlippage: 120.0,
    holdingPeriodDays: 21,
    exitReason: 'TAKE_PROFIT',
    regimeAtEntry: 'BULL',
    regimeAtExit: 'BULL'
  }
];

const MOCK_REJECTED: BacktestRejectedSignal[] = [
  {
    id: 'rej-1',
    runId: 'r-1',
    symbol: 'ICICIBANK',
    signalTimestamp: '2024-02-18T15:30:00Z',
    signalType: 'BUY',
    signalStrength: 0.65,
    rejectionReason: 'SECTOR_LIMIT_EXCEEDED',
    details: 'Financials sector allocation 34.2% would exceed max 35.0%'
  },
  {
    id: 'rej-2',
    runId: 'r-1',
    symbol: 'WIPRO',
    signalTimestamp: '2024-03-05T15:30:00Z',
    signalType: 'BUY',
    signalStrength: 0.40,
    rejectionReason: 'INSUFFICIENT_CASH',
    details: 'Available trade cash INR 14,200 below cash buffer constraint (5%)'
  }
];

export const BacktestTerminalCard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'curve' | 'trades' | 'regimes' | 'costs' | 'audit'>('curve');
  const [selectedHorizon, setSelectedHorizon] = useState<string>('SHORT_TERM');
  const [isRunning, setIsRunning] = useState<boolean>(false);

  const handleRunBacktest = () => {
    setIsRunning(true);
    setTimeout(() => {
      setIsRunning(false);
    }, 800);
  };

  return (
    <Card className="border-border bg-surface shadow-xl">
      <CardHeader className="pb-4">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <div className="flex items-center gap-2">
              <div className="p-1.5 rounded-lg bg-accent-muted text-accent">
                <Layers className="h-5 w-5" />
              </div>
              <CardTitle className="text-xl font-bold">Production Backtesting Engine</CardTitle>
              <Badge variant="outline" className="bg-success-muted text-success border-success/30 flex items-center gap-1">
                <ShieldCheck className="h-3.5 w-3.5" />
                PIT SAFE (Tavail ≤ Tdecision)
              </Badge>
            </div>
            <CardDescription className="mt-1">
              Realistic multi-asset simulation with Indian transaction costs (STT, GST, Stamp), slippage, next-bar execution, and cash ledger.
            </CardDescription>
          </div>

          <div className="flex items-center gap-3">
            <select
              value={selectedHorizon}
              onChange={(e) => setSelectedHorizon(e.target.value)}
              className="bg-background border border-border rounded-lg px-3 py-1.5 text-xs text-text-primary focus:outline-none focus:border-accent"
            >
              <option value="SHORT_TERM">Short-Term Horizon (1-5 Days)</option>
              <option value="MEDIUM_TERM">Medium-Term Horizon (1-4 Weeks)</option>
              <option value="LONG_TERM">Long-Term Horizon (3-12 Months)</option>
              <option value="MULTI_HORIZON">Multi-Horizon Composite</option>
            </select>

            <Button
              onClick={handleRunBacktest}
              disabled={isRunning}
              className="flex items-center gap-1.5 text-xs font-semibold px-4 py-2 bg-accent hover:bg-accent-hover text-white rounded-lg transition-all"
            >
              <Play className={`h-3.5 w-3.5 ${isRunning ? 'animate-spin' : ''}`} />
              {isRunning ? 'Simulating...' : 'Run Simulation'}
            </Button>
          </div>
        </div>

        {/* KPI Metrics Strip */}
        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-8 gap-3 mt-4 pt-3 border-t border-border-subtle">
          <div className="p-2.5 rounded-lg bg-background border border-border-subtle">
            <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">Total Net PnL</span>
            <div className="text-sm font-bold text-success font-mono mt-0.5">+INR 1,45,200</div>
            <span className="text-[10px] text-text-muted">Return: +{MOCK_METRICS.totalReturnPct}%</span>
          </div>
          <div className="p-2.5 rounded-lg bg-background border border-border-subtle">
            <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">CAGR</span>
            <div className="text-sm font-bold text-text-primary font-mono mt-0.5">{MOCK_METRICS.cagr}%</div>
            <span className="text-[10px] text-success">Alpha: +{MOCK_METRICS.alphaToBenchmark}%</span>
          </div>
          <div className="p-2.5 rounded-lg bg-background border border-border-subtle">
            <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">Sharpe Ratio</span>
            <div className="text-sm font-bold text-accent font-mono mt-0.5">{MOCK_METRICS.sharpeRatio}</div>
            <span className="text-[10px] text-text-muted">Sortino: {MOCK_METRICS.sortinoRatio}</span>
          </div>
          <div className="p-2.5 rounded-lg bg-background border border-border-subtle">
            <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">Max Drawdown</span>
            <div className="text-sm font-bold text-danger font-mono mt-0.5">-{MOCK_METRICS.maxDrawdownPct}%</div>
            <span className="text-[10px] text-text-muted">Dur: {MOCK_METRICS.maxDrawdownDurationDays}d</span>
          </div>
          <div className="p-2.5 rounded-lg bg-background border border-border-subtle">
            <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">Win Rate</span>
            <div className="text-sm font-bold text-text-primary font-mono mt-0.5">{MOCK_METRICS.winRatePct}%</div>
            <span className="text-[10px] text-text-muted">{MOCK_METRICS.winningTradesCount}W / {MOCK_METRICS.losingTradesCount}L</span>
          </div>
          <div className="p-2.5 rounded-lg bg-background border border-border-subtle">
            <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">Profit Factor</span>
            <div className="text-sm font-bold text-text-primary font-mono mt-0.5">{MOCK_METRICS.profitFactor}</div>
            <span className="text-[10px] text-text-muted">W/L: {MOCK_METRICS.winLossRatio}</span>
          </div>
          <div className="p-2.5 rounded-lg bg-background border border-border-subtle">
            <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">Turnover</span>
            <div className="text-sm font-bold text-text-primary font-mono mt-0.5">{MOCK_METRICS.annualizedTurnover}x / yr</div>
            <span className="text-[10px] text-text-muted">Trades: {MOCK_METRICS.totalTradesCount}</span>
          </div>
          <div className="p-2.5 rounded-lg bg-background border border-border-subtle">
            <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">Benchmark (NIFTY)</span>
            <div className="text-sm font-bold text-text-secondary font-mono mt-0.5">+7.40%</div>
            <span className="text-[10px] text-success font-semibold">+7.12% Outperf</span>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Navigation Tabs */}
        <div className="flex border-b border-border-subtle gap-4">
          <button
            onClick={() => setActiveTab('curve')}
            className={`pb-2 text-xs font-semibold transition-all border-b-2 ${
              activeTab === 'curve' ? 'border-accent text-accent' : 'border-transparent text-text-secondary hover:text-text-primary'
            }`}
          >
            Comparative Equity Curve
          </button>
          <button
            onClick={() => setActiveTab('trades')}
            className={`pb-2 text-xs font-semibold transition-all border-b-2 ${
              activeTab === 'trades' ? 'border-accent text-accent' : 'border-transparent text-text-secondary hover:text-text-primary'
            }`}
          >
            Trades Ledger ({MOCK_TRADES.length})
          </button>
          <button
            onClick={() => setActiveTab('regimes')}
            className={`pb-2 text-xs font-semibold transition-all border-b-2 ${
              activeTab === 'regimes' ? 'border-accent text-accent' : 'border-transparent text-text-secondary hover:text-text-primary'
            }`}
          >
            Regime Breakdown
          </button>
          <button
            onClick={() => setActiveTab('costs')}
            className={`pb-2 text-xs font-semibold transition-all border-b-2 ${
              activeTab === 'costs' ? 'border-accent text-accent' : 'border-transparent text-text-secondary hover:text-text-primary'
            }`}
          >
            Costs & Slippage Drag
          </button>
          <button
            onClick={() => setActiveTab('audit')}
            className={`pb-2 text-xs font-semibold transition-all border-b-2 ${
              activeTab === 'audit' ? 'border-accent text-accent' : 'border-transparent text-text-secondary hover:text-text-primary'
            }`}
          >
            Rejected Signals Audit ({MOCK_REJECTED.length})
          </button>
        </div>

        {/* Tab 1: Comparative Equity Curve */}
        {activeTab === 'curve' && (
          <div className="space-y-3">
            <div className="h-[280px] w-full pt-2">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={MOCK_EQUITY_CURVE}>
                  <defs>
                    <linearGradient id="strategyGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#6366f1" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#6366f1" stopOpacity={0.0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#2a2a3e" vertical={false} />
                  <XAxis dataKey="pointDate" stroke="#8888a0" fontSize={11} tickLine={false} />
                  <YAxis
                    stroke="#8888a0"
                    fontSize={11}
                    tickLine={false}
                    domain={['dataMin - 10000', 'dataMax + 10000']}
                    tickFormatter={(v: number) => `₹${(v / 100000).toFixed(1)}L`}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#12121a',
                      borderColor: '#2a2a3e',
                      borderRadius: '8px',
                      fontSize: '12px',
                      color: '#f0f0f5'
                    }}
                    formatter={(val: any) => [val != null ? `₹${Number(val).toLocaleString('en-IN')}` : '', '']}
                  />
                  <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
                  <Area
                    type="monotone"
                    name="QuantLab Strategy"
                    dataKey="strategyEquity"
                    stroke="#6366f1"
                    strokeWidth={2.5}
                    fillOpacity={1}
                    fill="url(#strategyGrad)"
                  />
                  <Line
                    type="monotone"
                    name="Equal Weight Buy & Hold"
                    dataKey="buyAndHoldEquity"
                    stroke="#22c55e"
                    strokeWidth={1.5}
                    strokeDasharray="4 4"
                    dot={false}
                  />
                  <Line
                    type="monotone"
                    name="Benchmark (NIFTY 50 TRI)"
                    dataKey="benchmarkEquity"
                    stroke="#f59e0b"
                    strokeWidth={1.5}
                    dot={false}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
            <div className="flex items-center justify-between text-xs text-text-secondary bg-background/50 p-2.5 rounded-lg border border-border-subtle">
              <span className="flex items-center gap-1.5">
                <CheckCircle2 className="h-3.5 w-3.5 text-success" />
                <strong>Point-in-Time Assurance:</strong> Orders placed at bar T close execute at bar T+1 Open. Zero same-bar lookahead.
              </span>
              <span className="font-mono text-text-muted">Initial: ₹10,00,000 | Current: ₹11,45,200</span>
            </div>
          </div>
        )}

        {/* Tab 2: Trades Ledger */}
        {activeTab === 'trades' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-border bg-surface-elevated/50 text-text-secondary">
                  <th className="p-2.5 font-semibold">Symbol</th>
                  <th className="p-2.5 font-semibold">Side</th>
                  <th className="p-2.5 font-semibold">Qty</th>
                  <th className="p-2.5 font-semibold">Entry Date / Price</th>
                  <th className="p-2.5 font-semibold">Exit Date / Price</th>
                  <th className="p-2.5 font-semibold">Holding</th>
                  <th className="p-2.5 font-semibold">Exit Reason</th>
                  <th className="p-2.5 font-semibold">Fees & Slippage</th>
                  <th className="p-2.5 font-semibold text-right">Net PnL</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-subtle">
                {MOCK_TRADES.map((t) => {
                  const isWin = t.netPnl > 0;
                  return (
                    <tr key={t.id} className="hover:bg-surface-elevated/30 transition-colors">
                      <td className="p-2.5 font-bold text-text-primary">{t.symbol}</td>
                      <td className="p-2.5">
                        <Badge variant="outline" className="text-[10px] bg-accent-muted text-accent border-accent/30">
                          {t.side}
                        </Badge>
                      </td>
                      <td className="p-2.5 font-mono">{t.quantity}</td>
                      <td className="p-2.5 font-mono">
                        <div>{new Date(t.entryTimestamp).toLocaleDateString()}</div>
                        <div className="text-text-muted">₹{t.entryPrice.toFixed(2)}</div>
                      </td>
                      <td className="p-2.5 font-mono">
                        <div>{new Date(t.exitTimestamp).toLocaleDateString()}</div>
                        <div className="text-text-muted">₹{t.exitPrice.toFixed(2)}</div>
                      </td>
                      <td className="p-2.5 text-text-secondary">{t.holdingPeriodDays} days</td>
                      <td className="p-2.5">
                        <Badge variant="outline" className="text-[10px]">
                          {t.exitReason}
                        </Badge>
                      </td>
                      <td className="p-2.5 font-mono text-text-muted">
                        ₹{(t.totalFees + t.totalSlippage).toFixed(1)}
                      </td>
                      <td className={`p-2.5 text-right font-mono font-bold ${isWin ? 'text-success' : 'text-danger'}`}>
                        {isWin ? '+' : ''}₹{t.netPnl.toLocaleString('en-IN', { minimumFractionDigits: 1 })}
                        <div className="text-[10px] font-normal">({isWin ? '+' : ''}{t.returnPct.toFixed(2)}%)</div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {/* Tab 3: Regime Breakdown */}
        {activeTab === 'regimes' && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="p-4 rounded-xl border border-border bg-background/50 space-y-3">
              <div className="flex items-center justify-between">
                <h4 className="text-sm font-semibold text-text-primary">BULL Regime Performance</h4>
                <Badge className="bg-success-muted text-success border-success/30">10 Trades</Badge>
              </div>
              <div className="space-y-2 text-xs">
                <div className="flex justify-between py-1 border-b border-border-subtle">
                  <span className="text-text-secondary">Win Rate:</span>
                  <span className="font-mono font-bold text-success">70.0%</span>
                </div>
                <div className="flex justify-between py-1 border-b border-border-subtle">
                  <span className="text-text-secondary">Net Realized PnL:</span>
                  <span className="font-mono font-bold text-success">+₹1,02,400</span>
                </div>
                <div className="flex justify-between py-1 border-b border-border-subtle">
                  <span className="text-text-secondary">Average Trade Return:</span>
                  <span className="font-mono font-bold">+1.65%</span>
                </div>
              </div>
            </div>

            <div className="p-4 rounded-xl border border-border bg-background/50 space-y-3">
              <div className="flex items-center justify-between">
                <h4 className="text-sm font-semibold text-text-primary">SIDEWAYS Regime Performance</h4>
                <Badge className="bg-warning-muted text-warning border-warning/30">6 Trades</Badge>
              </div>
              <div className="space-y-2 text-xs">
                <div className="flex justify-between py-1 border-b border-border-subtle">
                  <span className="text-text-secondary">Win Rate:</span>
                  <span className="font-mono font-bold text-text-primary">66.7%</span>
                </div>
                <div className="flex justify-between py-1 border-b border-border-subtle">
                  <span className="text-text-secondary">Net Realized PnL:</span>
                  <span className="font-mono font-bold text-success">+₹42,800</span>
                </div>
                <div className="flex justify-between py-1 border-b border-border-subtle">
                  <span className="text-text-secondary">Average Trade Return:</span>
                  <span className="font-mono font-bold">+0.85%</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Tab 4: Costs & Slippage Drag */}
        {activeTab === 'costs' && (
          <div className="space-y-3">
            <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
              <div className="p-3 rounded-lg border border-border-subtle bg-background">
                <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">STT (Delivery)</span>
                <div className="text-base font-bold font-mono mt-1 text-text-primary">₹1,450.00</div>
                <span className="text-[10px] text-text-muted">10 bps buy/sell turnover</span>
              </div>
              <div className="p-3 rounded-lg border border-border-subtle bg-background">
                <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">Brokerage & GST</span>
                <div className="text-base font-bold font-mono mt-1 text-text-primary">₹685.00</div>
                <span className="text-[10px] text-text-muted">3 bps + 18% GST</span>
              </div>
              <div className="p-3 rounded-lg border border-border-subtle bg-background">
                <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">Exchange & Stamp</span>
                <div className="text-base font-bold font-mono mt-1 text-text-primary">₹315.00</div>
                <span className="text-[10px] text-text-muted">NSE charges + stamp duty</span>
              </div>
              <div className="p-3 rounded-lg border border-border-subtle bg-background">
                <span className="text-[11px] text-text-secondary uppercase tracking-wider font-semibold">Slippage Impact</span>
                <div className="text-base font-bold font-mono mt-1 text-warning">₹1,120.00</div>
                <span className="text-[10px] text-text-muted">5 bps execution drag</span>
              </div>
            </div>
            <div className="p-3 rounded-lg bg-surface-elevated/40 border border-border-subtle text-xs text-text-secondary flex items-center justify-between">
              <span>Total Friction (Costs + Slippage Drag): <strong>₹3,570.00</strong> (~0.35% of capital)</span>
              <Badge variant="outline" className="text-text-muted">Deducted from Net Equity</Badge>
            </div>
          </div>
        )}

        {/* Tab 5: Rejected Signals Audit */}
        {activeTab === 'audit' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-border bg-surface-elevated/50 text-text-secondary">
                  <th className="p-2.5 font-semibold">Timestamp</th>
                  <th className="p-2.5 font-semibold">Symbol</th>
                  <th className="p-2.5 font-semibold">Signal Type</th>
                  <th className="p-2.5 font-semibold">Rejection Constraint</th>
                  <th className="p-2.5 font-semibold">Audit Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-subtle">
                {MOCK_REJECTED.map((r) => (
                  <tr key={r.id} className="hover:bg-surface-elevated/30 transition-colors">
                    <td className="p-2.5 font-mono text-text-muted">{new Date(r.signalTimestamp).toLocaleString()}</td>
                    <td className="p-2.5 font-bold text-text-primary">{r.symbol}</td>
                    <td className="p-2.5">
                      <Badge variant="outline" className="text-[10px]">{r.signalType}</Badge>
                    </td>
                    <td className="p-2.5">
                      <Badge variant="danger" className="text-[10px] bg-danger-muted text-danger border-danger/30">
                        {r.rejectionReason}
                      </Badge>
                    </td>
                    <td className="p-2.5 text-text-secondary">{r.details}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Methodological Disclaimers */}
        <div className="p-3 rounded-lg border border-border-subtle bg-surface-elevated/20 text-[11px] text-text-muted space-y-1">
          <div className="flex items-center gap-1.5 font-semibold text-text-secondary">
            <AlertTriangle className="h-3.5 w-3.5 text-warning" />
            QuantLab Quantitative Backtesting Disclaimers
          </div>
          <p>
            Past performance in point-in-time simulations is no guarantee of future results. Backtests simulate historical data under strict assumption of liquidity availability and execution at next bar open. No strategy is guaranteed or risk-free.
          </p>
        </div>
      </CardContent>
    </Card>
  );
};
