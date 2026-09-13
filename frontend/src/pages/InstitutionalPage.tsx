import { Building2, Info } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';

export function InstitutionalPage() {
  return (
    <div className="space-y-6">
      <MockBanner />
      <div className="flex items-center gap-3">
        <Building2 className="h-6 w-6 text-accent" />
        <div>
          <h1 className="text-2xl font-bold gradient-text">Institutional Intelligence</h1>
          <p className="text-sm text-text-muted mt-0.5">FII/DII flows, smart-money positioning, block deal analysis</p>
        </div>
      </div>

      <div className="rounded-xl border border-border bg-surface p-6 space-y-4">
        <div className="flex items-start gap-3 rounded-lg border border-blue-400/20 bg-blue-400/5 p-4">
          <Info className="h-5 w-5 text-blue-400 mt-0.5 shrink-0" />
          <div>
            <p className="text-sm font-semibold text-text-primary">Data Source: NSE/BSE Bulk Deal Data + SEBI FII Reports</p>
            <p className="text-xs text-text-muted mt-1">
              Live institutional flow data requires authenticated access to NSE bulk-deal feed and SEBI FII daily CSV.
              When feeds are available, this page surfaces aggregated FII/DII net buy/sell, block deal activity, and
              sector-level smart money flows.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
          {['FII Net Flow', 'DII Net Flow', 'Block Deals Today', 'Bulk Deals Today'].map((label) => (
            <div key={label} className="rounded-lg border border-border bg-background p-4 text-center">
              <p className="text-xs text-text-muted">{label}</p>
              <p className="text-lg font-bold text-text-muted mt-2">UNAVAILABLE</p>
              <p className="text-[10px] text-text-muted mt-1">Live feed not connected</p>
            </div>
          ))}
        </div>

        <p className="text-xs text-text-muted text-center">
          Backend Part 6 (Mutual Fund + Institutional Intelligence) implemented. Connect live data feed to populate.
        </p>
      </div>
    </div>
  );
}
