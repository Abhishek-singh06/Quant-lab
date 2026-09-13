import { useState, useEffect } from 'react';
import { Zap, ShieldOff, Shield, AlertTriangle, CheckCircle2, XCircle, RefreshCw, Loader2, Eye } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import type {
  BrokerAccount,
  LiveOrder,
  LivePosition,
  TradingSafetyLock,
  LivePortfolioReconciliation,
} from '@/types/market';
import { api } from '@/lib/api';
import { cn } from '@/lib/utils';

function KillSwitchButton({ lock, onToggle, loading }: { lock: TradingSafetyLock | null; onToggle: () => void; loading: boolean }) {
  const active = lock?.isEmergencyKillSwitchActive ?? false;
  return (
    <button
      onClick={onToggle}
      disabled={loading}
      className={cn(
        'flex items-center gap-2 rounded-xl border-2 px-6 py-3 text-sm font-bold transition-all disabled:opacity-50',
        active
          ? 'border-red-500 bg-red-500/10 text-red-400 hover:bg-red-500/20'
          : 'border-yellow-500 bg-yellow-500/10 text-yellow-400 hover:bg-yellow-500/20'
      )}
    >
      {loading ? <Loader2 className="h-5 w-5 animate-spin" /> : active ? <ShieldOff className="h-5 w-5" /> : <Shield className="h-5 w-5" />}
      {active ? '⛔ KILL SWITCH ACTIVE — Trading Halted' : '🟢 Live Trading Enabled — Click to Halt'}
    </button>
  );
}

export function LiveTradingPage() {
  const [brokerAccount, setBrokerAccount] = useState<BrokerAccount | null>(null);
  const [safetyLock, setSafetyLock] = useState<TradingSafetyLock | null>(null);
  const [liveOrders, setLiveOrders] = useState<LiveOrder[]>([]);
  const [livePositions, setLivePositions] = useState<LivePosition[]>([]);
  const [reconciliation, setReconciliation] = useState<LivePortfolioReconciliation | null>(null);
  const [loading, setLoading] = useState(true);
  const [killLoading, setKillLoading] = useState(false);
  const [lastRefreshed, setLastRefreshed] = useState<Date | null>(null);

  const fetchAll = () => {
    setLoading(true);
    Promise.all([
      api.get<BrokerAccount>('/v1/broker/account').catch(() => null),
      api.get<TradingSafetyLock>('/v1/broker/safety-lock').catch(() => null),
      api.get<LiveOrder[]>('/v1/broker/orders?status=OPEN').catch(() => []),
      api.get<LivePosition[]>('/v1/broker/positions').catch(() => []),
      api.get<LivePortfolioReconciliation>('/v1/broker/reconciliation/latest').catch(() => null),
    ]).then(([acc, lock, orders, positions, recon]) => {
      setBrokerAccount(acc as BrokerAccount | null);
      setSafetyLock(lock as TradingSafetyLock | null);
      setLiveOrders(orders as LiveOrder[]);
      setLivePositions(positions as LivePosition[]);
      setReconciliation(recon as LivePortfolioReconciliation | null);
      setLastRefreshed(new Date());
    }).finally(() => setLoading(false));
  };

  useEffect(() => { fetchAll(); }, []);

  const toggleKillSwitch = () => {
    setKillLoading(true);
    const endpoint = safetyLock?.isEmergencyKillSwitchActive
      ? '/v1/broker/safety-lock/disable-kill-switch'
      : '/v1/broker/safety-lock/emergency-kill';
    api.post<TradingSafetyLock>(endpoint)
      .then(setSafetyLock)
      .catch(() => alert('Failed to toggle kill switch. Backend may be offline.'))
      .finally(() => setKillLoading(false));
  };

  const killActive = safetyLock?.isEmergencyKillSwitchActive ?? false;

  return (
    <div className="space-y-6">
      <MockBanner />

      {/* Page header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Zap className="h-6 w-6 text-accent" />
          <div>
            <h1 className="text-2xl font-bold gradient-text">Live Trading</h1>
            <p className="text-sm text-text-muted mt-0.5">Part 20 — Manual order review, live positions, and reconciliation</p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          {lastRefreshed && <span className="text-xs text-text-muted">Last: {lastRefreshed.toLocaleTimeString('en-IN')}</span>}
          <button onClick={fetchAll} disabled={loading} className="flex items-center gap-2 rounded-lg border border-border px-3 py-1.5 text-sm text-text-secondary hover:bg-surface-elevated transition-colors disabled:opacity-50">
            <RefreshCw className={cn('h-4 w-4', loading && 'animate-spin')} />
            Refresh
          </button>
        </div>
      </div>

      {/* Critical compliance disclosure */}
      <div className="rounded-xl border-2 border-red-500/30 bg-red-500/5 p-5 space-y-2">
        <div className="flex items-start gap-3">
          <AlertTriangle className="h-5 w-5 text-red-400 mt-0.5 shrink-0" />
          <div className="space-y-1">
            <p className="text-sm font-bold text-red-400">LIVE TRADING — REAL MONEY AT RISK</p>
            <p className="text-xs text-text-secondary">
              This module connects to a real broker account and executes orders with real capital.
              <strong className="text-text-primary"> Automated execution is permanently disabled.</strong>{' '}
              All orders require explicit two-stage manual review and confirmation.
              The emergency kill switch immediately halts all pending and future order placement.
            </p>
            <p className="text-xs text-text-muted">
              <strong>System constraint:</strong> <code className="text-xs">AUTOMATED_LIVE_TRADING_ENABLED=false</code> is hard-coded and cannot be overridden at runtime.
            </p>
          </div>
        </div>
      </div>

      {/* Kill switch */}
      <div className="rounded-xl border border-border bg-surface p-5 space-y-3">
        <h2 className="text-sm font-semibold text-text-primary">Emergency Kill Switch</h2>
        <KillSwitchButton lock={safetyLock} onToggle={toggleKillSwitch} loading={killLoading} />
        {safetyLock && (
          <div className="grid grid-cols-2 gap-3 text-xs sm:grid-cols-4">
            <div className="rounded-lg bg-background p-2">
              <p className="text-text-muted">Daily Loss Limit</p>
              <p className="text-text-primary font-semibold">₹{safetyLock.maxDailyLossLimit.toLocaleString('en-IN')}</p>
            </div>
            <div className="rounded-lg bg-background p-2">
              <p className="text-text-muted">Current Daily Loss</p>
              <p className={cn('font-semibold', safetyLock.currentDailyLoss < 0 ? 'text-red-400' : 'text-text-primary')}>
                ₹{Math.abs(safetyLock.currentDailyLoss).toLocaleString('en-IN')}
              </p>
            </div>
            <div className="rounded-lg bg-background p-2">
              <p className="text-text-muted">Max Single Order</p>
              <p className="text-text-primary font-semibold">₹{safetyLock.maxSingleOrderValue.toLocaleString('en-IN')}</p>
            </div>
            <div className="rounded-lg bg-background p-2">
              <p className="text-text-muted">Manual Confirm Required</p>
              <p className={cn('font-semibold', safetyLock.requireManualConfirmation ? 'text-green-400' : 'text-red-400')}>
                {safetyLock.requireManualConfirmation ? 'YES' : 'NO'}
              </p>
            </div>
          </div>
        )}
        {killActive && safetyLock?.reason && (
          <p className="text-xs text-red-400"><strong>Reason:</strong> {safetyLock.reason}</p>
        )}
      </div>

      {/* Broker account */}
      <div className="rounded-xl border border-border bg-surface p-5 space-y-3">
        <h2 className="text-sm font-semibold text-text-primary">Broker Account</h2>
        {!brokerAccount ? (
          <div className="flex items-center gap-2 text-sm text-text-muted">
            <XCircle className="h-4 w-4" />
            No broker account connected. Configure in Settings.
          </div>
        ) : (
          <div className="space-y-3">
            <div className="flex items-center gap-3">
              <span className="rounded-full border border-border bg-background px-3 py-1 text-xs font-bold">{brokerAccount.brokerName}</span>
              <span className={cn('text-xs font-semibold', brokerAccount.status === 'CONNECTED' || brokerAccount.status === 'AUTHENTICATED' ? 'text-green-400' : 'text-red-400')}>
                {brokerAccount.status}
              </span>
              {brokerAccount.authExpiry && (
                <span className="text-xs text-text-muted">Token expires: {new Date(brokerAccount.authExpiry).toLocaleString('en-IN')}</span>
              )}
            </div>
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              {[
                { label: 'Cash Balance', val: `₹${brokerAccount.cashBalance.toLocaleString('en-IN')}` },
                { label: 'Margin Available', val: `₹${brokerAccount.marginAvailable.toLocaleString('en-IN')}` },
                { label: 'Margin Used', val: `₹${brokerAccount.marginUsed.toLocaleString('en-IN')}` },
                { label: 'Collateral', val: `₹${brokerAccount.collateralBalance.toLocaleString('en-IN')}` },
              ].map((m) => (
                <div key={m.label} className="rounded-lg bg-background p-3">
                  <p className="text-[10px] text-text-muted">{m.label}</p>
                  <p className="text-sm font-bold text-text-primary mt-1">{m.val}</p>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Live orders */}
      <div className="rounded-xl border border-border bg-surface p-5 space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold text-text-primary">Live Orders (Open)</h2>
          <span className="text-xs text-text-muted">{liveOrders.length} order(s)</span>
        </div>
        {liveOrders.length === 0 ? (
          <div className="flex items-center gap-2 text-sm text-text-muted">
            <CheckCircle2 className="h-4 w-4 text-green-400" /> No open live orders.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead>
                <tr className="text-text-muted border-b border-border">
                  {['Symbol', 'Side', 'Type', 'Qty', 'Price', 'Filled', 'Status', 'Placed', 'Confirmed by'].map((h) => (
                    <th key={h} className="text-left py-1.5 pr-4 font-medium">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {liveOrders.map((o) => (
                  <tr key={o.id} className="border-b border-border/50">
                    <td className="py-1.5 pr-4 font-medium text-text-primary">{o.symbol}</td>
                    <td className={cn('py-1.5 pr-4 font-bold', o.side === 'BUY' ? 'text-green-400' : 'text-red-400')}>{o.side}</td>
                    <td className="py-1.5 pr-4 text-text-secondary">{o.orderType}</td>
                    <td className="py-1.5 pr-4">{o.quantity}</td>
                    <td className="py-1.5 pr-4">{o.limitPrice?.toFixed(2) ?? 'MKT'}</td>
                    <td className="py-1.5 pr-4">{o.executedQuantity}</td>
                    <td className="py-1.5 pr-4">
                      <span className={cn(
                        'rounded-full px-2 py-0.5 font-semibold',
                        o.status === 'COMPLETE' ? 'text-green-400' :
                        o.status === 'REJECTED' || o.status === 'CANCELLED' ? 'text-red-400' :
                        'text-yellow-400'
                      )}>
                        {o.status}
                      </span>
                    </td>
                    <td className="py-1.5 pr-4 text-text-muted">{new Date(o.orderPlacedAt).toLocaleTimeString('en-IN')}</td>
                    <td className="py-1.5 pr-4 text-text-muted">{o.manualConfirmationBy ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Live positions */}
      <div className="rounded-xl border border-border bg-surface p-5 space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold text-text-primary">Live Positions</h2>
          <span className="text-xs text-text-muted">{livePositions.length} position(s)</span>
        </div>
        {livePositions.length === 0 ? (
          <div className="flex items-center gap-2 text-sm text-text-muted">
            <Eye className="h-4 w-4" /> No live positions.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead>
                <tr className="text-text-muted border-b border-border">
                  {['Symbol', 'Qty', 'Buy Avg', 'Last Price', 'Unrealized P&L', 'Realized P&L', 'Total P&L', 'Synced'].map((h) => (
                    <th key={h} className="text-left py-1.5 pr-4 font-medium">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {livePositions.map((p) => (
                  <tr key={p.id} className="border-b border-border/50">
                    <td className="py-1.5 pr-4 font-medium text-text-primary">{p.symbol}</td>
                    <td className="py-1.5 pr-4">{p.quantity}</td>
                    <td className="py-1.5 pr-4">{p.buyAveragePrice.toFixed(2)}</td>
                    <td className="py-1.5 pr-4">{p.lastPrice.toFixed(2)}</td>
                    <td className={cn('py-1.5 pr-4 font-semibold', p.unrealizedPnl >= 0 ? 'text-green-400' : 'text-red-400')}>
                      {p.unrealizedPnl >= 0 ? '+' : ''}₹{p.unrealizedPnl.toFixed(0)}
                    </td>
                    <td className={cn('py-1.5 pr-4', p.realizedPnl >= 0 ? 'text-green-400' : 'text-red-400')}>
                      {p.realizedPnl >= 0 ? '+' : ''}₹{p.realizedPnl.toFixed(0)}
                    </td>
                    <td className={cn('py-1.5 pr-4 font-bold', p.pnl >= 0 ? 'text-green-400' : 'text-red-400')}>
                      {p.pnl >= 0 ? '+' : ''}₹{p.pnl.toFixed(0)} ({p.pnlPct >= 0 ? '+' : ''}{p.pnlPct.toFixed(2)}%)
                    </td>
                    <td className="py-1.5 pr-4 text-text-muted">{new Date(p.lastSyncedAt).toLocaleTimeString('en-IN')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Reconciliation status */}
      <div className="rounded-xl border border-border bg-surface p-5 space-y-3">
        <h2 className="text-sm font-semibold text-text-primary">Portfolio Reconciliation</h2>
        {!reconciliation ? (
          <div className="text-sm text-text-muted">No reconciliation data available.</div>
        ) : (
          <div className="space-y-3">
            <div className="flex items-center gap-3">
              <span className={cn(
                'rounded-full px-3 py-1 text-xs font-bold',
                reconciliation.status === 'MATCHED' ? 'bg-green-400/10 text-green-400' :
                reconciliation.status === 'CRITICAL_MISMATCH' ? 'bg-red-400/10 text-red-400' :
                'bg-yellow-400/10 text-yellow-400'
              )}>
                {reconciliation.status}
              </span>
              <span className="text-xs text-text-muted">
                Last run: {new Date(reconciliation.reconciliationTimestamp).toLocaleString('en-IN')}
              </span>
            </div>
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              {[
                { label: 'Internal Positions', val: reconciliation.totalInternalPositions },
                { label: 'Broker Positions', val: reconciliation.totalBrokerPositions },
                { label: 'Matched', val: reconciliation.matchedPositions },
                { label: 'Mismatched', val: reconciliation.mismatchedPositions },
              ].map((m) => (
                <div key={m.label} className="rounded-lg bg-background p-3 text-center">
                  <p className="text-[10px] text-text-muted">{m.label}</p>
                  <p className={cn('text-lg font-bold mt-1', m.label === 'Mismatched' && m.val > 0 ? 'text-red-400' : 'text-text-primary')}>{m.val}</p>
                </div>
              ))}
            </div>
            {reconciliation.discrepancyDetails && (
              <div className="rounded-lg border border-red-400/20 bg-red-400/5 p-3">
                <p className="text-xs text-red-400 font-semibold">Discrepancy Details</p>
                <p className="text-xs text-text-secondary mt-1">{reconciliation.discrepancyDetails}</p>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Compliance badge */}
      <div className="rounded-xl border border-green-400/20 bg-green-400/5 p-4">
        <div className="flex items-center gap-3">
          <Shield className="h-5 w-5 text-green-400" />
          <div>
            <p className="text-sm font-semibold text-green-400">Compliance Gate: ACTIVE</p>
            <p className="text-xs text-text-secondary mt-0.5">
              Automated live trading is permanently disabled. All orders require explicit manual approval.
              Emergency kill switch operational. Daily loss limits enforced.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
