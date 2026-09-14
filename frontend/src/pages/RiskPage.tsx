import { QLBadge } from '@/design-system/QLBadge';
import { QLSection } from '@/design-system/QLSection';
import { QLRisk } from '@/design-system/QLRisk';
import { RiskTerminalCard } from '@/components/dashboard/RiskTerminalCard';

export function RiskPage() {
  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>PORTFOLIO RISK &amp; SIZING COMMAND CENTER</span>
            <span>•</span>
            <span>INVARIANT ENVELOPE</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            RISK CENTER
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Automated capital allocation bounds, 1-Day Value-at-Risk (95%), 10% hard position sizing cap, and scenario stress testing.
          </p>
        </div>

        <div className="flex items-center gap-2 font-mono text-xs">
          <QLBadge variant="positive" size="sm" dot>
            ENVELOPE ACTIVE
          </QLBadge>
        </div>
      </div>

      {/* 01 RISK SUMMARY & INVARIANTS */}
      <QLSection
        number={1}
        title="Portfolio Risk Envelope"
        subtitle="Active Sizing Bounds & Exposure Constraints"
      >
        <QLRisk
          portfolioRisk="MODERATE"
          var95Pct={1.85}
          maxDrawdownPct={3.20}
          cashAllocationPct={84.3}
          grossExposurePct={15.7}
          suggestedAllocationInr={45000}
          maxAllocationInr={50000}
          bindingConstraint="VOLATILITY_CAP (10% MAX PORTFOLIO CAP)"
        />
      </QLSection>

      {/* 02 RISK TERMINAL & STRESS TESTS */}
      <QLSection
        number={2}
        title="Detailed Risk Diagnostics & Factor Sensitivity"
        subtitle="Volatility Surface & Scenario Loss Budgets"
      >
        <RiskTerminalCard />
      </QLSection>
    </div>
  );
}
