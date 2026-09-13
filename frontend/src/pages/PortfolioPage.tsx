import { useEffect, useState } from 'react';
import { Briefcase, RefreshCw, AlertCircle, TrendingUp, TrendingDown } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import type { Portfolio } from '@/types/market';
import { api } from '@/lib/api';

export function PortfolioPage() {
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPortfolios = () => {
    setLoading(true);
    setError(null);
    api.get<Portfolio[]>('/v1/portfolios')
      .then(setPortfolios)
      .catch(() => setError('Portfolio data unavailable.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchPortfolios(); }, []);

  return (
    <div className="space-y-6">
      <MockBanner />
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Briefcase className="h-6 w-6 text-accent" />
          <div>
            <h1 className="text-2xl font-bold gradient-text">Portfolio</h1>
            <p className="text-sm text-text-muted mt-0.5">Position tracking, P&amp;L, and allocation breakdown</p>
          </div>
        </div>
        <button
          onClick={fetchPortfolios}
          disabled={loading}
          className="flex items-center gap-2 rounded-lg border border-border px-3 py-1.5 text-sm text-text-secondary hover:bg-surface-elevated transition-colors disabled:opacity-50"
        >
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-lg border border-red-400/20 bg-red-400/10 px-4 py-3 text-sm text-red-400">
          <AlertCircle className="h-4 w-4" /> {error}
        </div>
      )}

      {portfolios.length === 0 && !loading && !error && (
        <div className="rounded-xl border border-border bg-surface p-12 text-center">
          <Briefcase className="h-10 w-10 text-text-muted mx-auto mb-3" />
          <p className="text-text-muted">No portfolios found. Create one via the Risk Engine.</p>
        </div>
      )}

      <div className="space-y-4">
        {portfolios.map((p) => (
          <div key={p.id} className="rounded-xl border border-border bg-surface p-5 space-y-4">
            {/* Header */}
            <div className="flex items-start justify-between">
              <div>
                <h2 className="text-base font-bold text-text-primary">{p.name}</h2>
                <p className="text-xs text-text-muted">{p.currency} &bull; {p.active ? 'Active' : 'Closed'}</p>
              </div>
              <div
                className={`text-sm font-semibold px-3 py-1 rounded-full ${
                  p.currentDrawdown < -0.1 ? 'text-red-400 bg-red-400/10' : 'text-green-400 bg-green-400/10'
                }`}
              >
                DD: {(p.currentDrawdown * 100).toFixed(1)}%
              </div>
            </div>

            {/* Metric tiles */}
            <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
              {[
                { label: 'Portfolio Value', val: `₹${p.currentPortfolioValue.toLocaleString('en-IN')}` },
                { label: 'Cash Balance',   val: `₹${p.currentCash.toLocaleString('en-IN')}` },
                { label: 'Peak Value',     val: `₹${p.peakPortfolioValue.toLocaleString('en-IN')}` },
                { label: 'Max Drawdown',   val: `${(p.maxDrawdown * 100).toFixed(1)}%` },
              ].map((m) => (
                <div key={m.label} className="rounded-lg bg-background p-3">
                  <p className="text-xs text-text-muted">{m.label}</p>
                  <p className="text-sm font-bold text-text-primary mt-1">{m.val}</p>
                </div>
              ))}
            </div>

            {/* Positions table */}
            {p.positions && p.positions.length > 0 && (
              <div>
                <p className="text-xs font-semibold text-text-muted mb-2 uppercase tracking-wider">
                  Positions ({p.positions.length})
                </p>
                <div className="overflow-x-auto">
                  <table className="w-full text-xs">
                    <thead>
                      <tr className="text-text-muted border-b border-border">
                        {['Symbol', 'Qty', 'Entry', 'Current', 'Mkt Value', 'P&L'].map((h) => (
                          <th key={h} className="text-left py-1.5 pr-4 font-medium">{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {p.positions.map((pos) => {
                        const pnl = (pos.currentPrice - pos.averageEntryPrice) * pos.quantity;
                        return (
                          <tr key={pos.id} className="border-b border-border/50">
                            <td className="py-1.5 pr-4 font-medium text-text-primary">{pos.symbol}</td>
                            <td className="py-1.5 pr-4 text-text-secondary">{pos.quantity}</td>
                            <td className="py-1.5 pr-4 text-text-secondary">{pos.averageEntryPrice.toFixed(2)}</td>
                            <td className="py-1.5 pr-4 text-text-secondary">{pos.currentPrice.toFixed(2)}</td>
                            <td className="py-1.5 pr-4 text-text-secondary">₹{pos.marketValue.toLocaleString('en-IN')}</td>
                            <td className={`py-1.5 font-semibold ${pnl >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                              {pnl >= 0
                                ? <TrendingUp className="inline h-3 w-3 mr-1" />
                                : <TrendingDown className="inline h-3 w-3 mr-1" />
                              }
                              {pnl >= 0 ? '+' : ''}{pnl.toFixed(0)}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
