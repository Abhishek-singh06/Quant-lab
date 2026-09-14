import { useState, useEffect } from 'react';
import {
  Calendar,
  ShieldCheck
} from 'lucide-react';
import { QLPanel } from '@/design-system/QLPanel';
import { QLBadge } from '@/design-system/QLBadge';
import { QLEmptyState } from '@/design-system/QLEmptyState';
import { cn } from '@/lib/utils';

interface Statement {
  fiscalQuarter?: string;
  fiscalYear?: string;
  periodEnd: string;
  revenue: number;
  ebitda: number;
  netProfit: number;
  basicEps: number;
  freeCashFlow?: number;
}

interface Ratios {
  peRatio?: number;
  pbRatio?: number;
  evEbitda?: number;
  roe?: number;
  roce?: number;
  netProfitMargin?: number;
  debtToEquity?: number;
  revenueGrowthYoY?: number;
  profitGrowthYoY?: number;
}

interface CompanyFundamentals {
  symbol: string;
  companyName: string;
  sector: string;
  industry: string;
  latestPeriodEnd: string;
  availableAt: string;
  ageDays: number;
  source: string;
  dataQualityScore: string;
  reportingBasis: string;
  latestRatios?: Ratios;
  quarterlyStatements?: Statement[];
  annualStatements?: Statement[];
}

const SAMPLE_COMPANIES = ['RELIANCE', 'TCS', 'HDFCBANK', 'INFY', 'HINDZINC'];

export function CompanyFundamentalsCard() {
  const [selectedSymbol, setSelectedSymbol] = useState('RELIANCE');
  const [fundamentals, setFundamentals] = useState<CompanyFundamentals | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchFundamentals() {
      setLoading(true);
      try {
        const res = await fetch(`/api/v1/fundamentals/company/${selectedSymbol}`);
        if (res.ok) {
          const data = await res.json();
          setFundamentals(data);
        } else {
          setFundamentals(null);
        }
      } catch {
        setFundamentals(null);
      } finally {
        setLoading(false);
      }
    }

    fetchFundamentals();
  }, [selectedSymbol]);

  return (
    <QLPanel
      variant="surface"
      padding="md"
      title="POINT-IN-TIME CORPORATE FUNDAMENTALS"
      headerAction={
        <div className="flex items-center gap-2">
          <QLBadge variant="neutral" size="xs">
            {selectedSymbol}
          </QLBadge>
          <QLBadge variant="positive" size="xs" dot>
            CONSOLIDATED FILINGS
          </QLBadge>
        </div>
      }
    >
      {/* Symbol Switcher */}
      <div className="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-border/60">
        <div className="flex items-center gap-1.5 flex-wrap">
          {SAMPLE_COMPANIES.map((sym) => (
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
        </div>
      </div>

      {loading ? (
        <div className="py-8 space-y-4 animate-pulse">
          <div className="h-8 bg-surface-elevated/50 rounded w-1/3" />
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-20 bg-surface-elevated/50 rounded-lg" />
            ))}
          </div>
        </div>
      ) : !fundamentals ? (
        <div className="pt-4">
          <QLEmptyState
            title={`NO FUNDAMENTAL FILINGS FOUND FOR ${selectedSymbol}`}
            description="Statutory financial statements and corporate filings have not been ingested for this instrument."
            source="NSE / BSE CORPORATE FILINGS"
            type="unconnected"
            nextAction="Sync corporate filings connector in Settings or query a supported benchmark security."
          />
        </div>
      ) : (
        <div className="space-y-6 pt-4">
          {/* Header info */}
          <div className="flex flex-wrap items-center justify-between gap-4 bg-surface-elevated/30 p-4 rounded-lg border border-border/50 font-mono text-xs">
            <div>
              <span className="text-base font-bold text-text-primary font-sans block">{fundamentals.companyName}</span>
              <span className="text-text-muted text-[11px] font-sans mt-0.5 block">{fundamentals.sector} &bull; {fundamentals.industry}</span>
            </div>
            <div className="flex items-center gap-3">
              <span className="flex items-center gap-1 text-text-muted">
                <Calendar className="h-3.5 w-3.5 text-text-secondary" />
                Period: <strong className="text-text-primary">{fundamentals.latestPeriodEnd || 'N/A'}</strong>
              </span>
              <span className="flex items-center gap-1 text-emerald-400">
                <ShieldCheck className="h-3.5 w-3.5" />
                <span>Lag: {fundamentals.ageDays ?? 0}d</span>
              </span>
            </div>
          </div>

          {/* Ratios Grid */}
          <div className="space-y-2">
            <span className="text-xs font-mono font-bold text-text-secondary uppercase tracking-wider block">
              Valuation &amp; Profitability Multiples
            </span>
            <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-3">
              {[
                { label: 'P/E RATIO', val: fundamentals.latestRatios?.peRatio ? `${fundamentals.latestRatios.peRatio.toFixed(2)}x` : 'N/A' },
                { label: 'P/B RATIO', val: fundamentals.latestRatios?.pbRatio ? `${fundamentals.latestRatios.pbRatio.toFixed(2)}x` : 'N/A' },
                { label: 'EV/EBITDA', val: fundamentals.latestRatios?.evEbitda ? `${fundamentals.latestRatios.evEbitda.toFixed(2)}x` : 'N/A' },
                { label: 'ROE', val: fundamentals.latestRatios?.roe ? `${fundamentals.latestRatios.roe.toFixed(2)}%` : 'N/A' },
                { label: 'ROCE', val: fundamentals.latestRatios?.roce ? `${fundamentals.latestRatios.roce.toFixed(2)}%` : 'N/A' },
                { label: 'NET MARGIN', val: fundamentals.latestRatios?.netProfitMargin ? `${fundamentals.latestRatios.netProfitMargin.toFixed(2)}%` : 'N/A' },
                { label: 'DEBT / EQUITY', val: fundamentals.latestRatios?.debtToEquity !== undefined ? `${fundamentals.latestRatios.debtToEquity.toFixed(2)}` : 'N/A' },
              ].map((r) => (
                <div key={r.label} className="p-3 rounded-lg bg-surface-elevated/40 border border-border">
                  <span className="text-[10px] font-mono text-text-muted uppercase block">{r.label}</span>
                  <span className="text-lg font-black font-mono text-text-primary mt-1 block">{r.val}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Quarterly Statements Table */}
          {fundamentals.quarterlyStatements && fundamentals.quarterlyStatements.length > 0 && (
            <div className="space-y-2 font-mono text-xs">
              <span className="text-xs font-mono font-bold text-text-secondary uppercase tracking-wider block">
                Quarterly Statement Breakdown (₹ Crores)
              </span>
              <div className="overflow-x-auto rounded-lg border border-border">
                <table className="w-full text-left">
                  <thead className="bg-surface-elevated/80 border-b border-border text-text-muted text-[11px] uppercase">
                    <tr>
                      <th className="px-3.5 py-2.5">Period</th>
                      <th className="px-3.5 py-2.5 text-right">Revenue</th>
                      <th className="px-3.5 py-2.5 text-right">EBITDA</th>
                      <th className="px-3.5 py-2.5 text-right">Net Profit</th>
                      <th className="px-3.5 py-2.5 text-right">Basic EPS (₹)</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border/40">
                    {fundamentals.quarterlyStatements.map((stmt, idx) => (
                      <tr key={idx} className="hover:bg-surface-elevated/40">
                        <td className="px-3.5 py-2 font-bold text-text-primary">
                          {stmt.fiscalQuarter ? `${stmt.fiscalQuarter} ` : ''}{stmt.periodEnd}
                        </td>
                        <td className="px-3.5 py-2 text-right font-mono">₹{stmt.revenue?.toLocaleString('en-IN') ?? '--'}</td>
                        <td className="px-3.5 py-2 text-right font-mono">₹{stmt.ebitda?.toLocaleString('en-IN') ?? '--'}</td>
                        <td className="px-3.5 py-2 text-right font-mono">₹{stmt.netProfit?.toLocaleString('en-IN') ?? '--'}</td>
                        <td className="px-3.5 py-2 text-right font-mono font-bold text-accent">₹{stmt.basicEps?.toFixed(2) ?? '--'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}
    </QLPanel>
  );
}
