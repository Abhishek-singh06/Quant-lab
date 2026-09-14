import { Info } from 'lucide-react';
import { QLBadge } from '@/design-system/QLBadge';
import { QLPanel } from '@/design-system/QLPanel';
import { QLSection } from '@/design-system/QLSection';
import { QLEmptyState } from '@/design-system/QLEmptyState';

export function InstitutionalPage() {
  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>SMART MONEY &amp; INSTITUTIONAL DESK</span>
            <span>•</span>
            <span>NSE / BSE / SEBI REPORTS</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            INSTITUTIONAL INTELLIGENCE
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            FII/DII daily net flows, block and bulk deal activity, sector positioning, and smart-money accumulation traces.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <QLBadge variant="warning" size="sm" dot>
            FEED NOT CONNECTED
          </QLBadge>
        </div>
      </div>

      {/* 01 FEED CONNECTION STATUS */}
      <QLSection
        number={1}
        title="Data Provider Status & Lineage"
        subtitle="NSE Bulk Deals + SEBI FII Daily Clearing Feed"
      >
        <QLPanel variant="surface" padding="md" className="border-sky-500/20 bg-sky-950/10 space-y-3">
          <div className="flex items-start gap-3">
            <Info className="w-5 h-5 text-sky-400 shrink-0 mt-0.5" />
            <div className="space-y-1 text-xs">
              <span className="font-bold text-text-primary font-mono block">
                DATA SOURCE CONTRACT: NSE BULK/BLOCK API &amp; SEBI FII REPORTS
              </span>
              <p className="text-text-secondary leading-relaxed font-sans">
                Real-time institutional flow analysis requires authenticated ingestion from NSE bulk deal feeds and SEBI daily clearing reports.
                To maintain strict data honesty, QuantLab does not fabricate synthetic FII/DII figures.
              </p>
            </div>
          </div>
        </QLPanel>
      </QLSection>

      {/* 02 DAILY FLOW SUMMARY */}
      <QLSection
        number={2}
        title="Institutional Flow Metrics"
        subtitle="Aggregated Net Positioning"
      >
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 font-mono">
          {[
            { label: 'FII NET FLOW (INR)', status: 'FEED NOT CONNECTED', reason: 'Awaiting SEBI FII daily CSV' },
            { label: 'DII NET FLOW (INR)', status: 'FEED NOT CONNECTED', reason: 'Awaiting Mutual Fund daily report' },
            { label: 'BLOCK DEALS TODAY', status: 'FEED NOT CONNECTED', reason: 'Awaiting NSE block deal stream' },
            { label: 'BULK DEALS TODAY', status: 'FEED NOT CONNECTED', reason: 'Awaiting BSE/NSE bulk disclosures' },
          ].map((item) => (
            <QLPanel key={item.label} variant="surface" padding="md" className="space-y-2">
              <span className="text-[10px] text-text-muted uppercase block">{item.label}</span>
              <div className="py-2">
                <QLBadge variant="outline" size="sm">
                  {item.status}
                </QLBadge>
              </div>
              <span className="text-[10px] text-text-muted font-sans block">{item.reason}</span>
            </QLPanel>
          ))}
        </div>
      </QLSection>

      {/* 03 BLOCK & BULK DEALS TABLE */}
      <QLSection
        number={3}
        title="Block & Bulk Deal Disclosures"
        subtitle="Institutional Transfers & Stake Rebalancing"
      >
        <QLEmptyState
          title="INSTITUTIONAL FEED DISCONNECTED"
          description="Live authenticated connection to NSE/BSE institutional bulk-deal disclosures is currently awaiting credential authorization."
          source="NSE / SEBI FII FEED"
          type="unconnected"
          nextAction="Configure institutional provider API keys in Terminal Settings to activate live FII/DII telemetry."
        />
      </QLSection>
    </div>
  );
}
