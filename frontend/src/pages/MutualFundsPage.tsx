import { LineChart, Info } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';

export function MutualFundsPage() {
  return (
    <div className="space-y-6">
      <MockBanner />
      <div className="flex items-center gap-3">
        <LineChart className="h-6 w-6 text-accent" />
        <div>
          <h1 className="text-2xl font-bold gradient-text">Mutual Fund Intelligence</h1>
          <p className="text-sm text-text-muted mt-0.5">AMFI holdings, sector flows, and fund manager activity</p>
        </div>
      </div>

      <div className="rounded-xl border border-border bg-surface p-6 space-y-4">
        <div className="flex items-start gap-3 rounded-lg border border-blue-400/20 bg-blue-400/5 p-4">
          <Info className="h-5 w-5 text-blue-400 mt-0.5 shrink-0" />
          <div>
            <p className="text-sm font-semibold text-text-primary">Data Source: AMFI Monthly Portfolio Disclosures</p>
            <p className="text-xs text-text-muted mt-1">
              AMFI publishes MF portfolio data monthly. When parsed and ingested, this page will show sector allocation
              changes, top holdings by fund, and smart money accumulation/distribution patterns.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 lg:grid-cols-3">
          {['Top Held Stocks', 'Net Purchase (MoM)', 'Sector Allocation Change', 'New Entries', 'Full Exits', 'Avg Holding Period'].map((label) => (
            <div key={label} className="rounded-lg border border-border bg-background p-4 text-center">
              <p className="text-xs text-text-muted">{label}</p>
              <p className="text-lg font-bold text-text-muted mt-2">UNAVAILABLE</p>
              <p className="text-[10px] text-text-muted mt-1">AMFI feed not ingested</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
