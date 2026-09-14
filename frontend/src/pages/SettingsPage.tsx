import { QLBadge } from '@/design-system/QLBadge';
import { QLPanel } from '@/design-system/QLPanel';
import { QLSection } from '@/design-system/QLSection';

function SettingRow({ label, value, description }: { label: string; value: string; description?: string }) {
  return (
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 py-3 border-b border-border/50 last:border-0 font-mono text-xs">
      <div>
        <p className="font-semibold text-text-primary font-sans text-xs sm:text-sm">{label}</p>
        {description && <p className="text-[11px] text-text-muted mt-0.5 font-sans">{description}</p>}
      </div>
      <span className="text-xs font-mono text-accent bg-accent-muted border border-accent/20 rounded px-2.5 py-1 shrink-0 self-start sm:self-auto">
        {value}
      </span>
    </div>
  );
}

export function SettingsPage() {
  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>TERMINAL CONFIGURATION &amp; ENVIRONMENT</span>
            <span>•</span>
            <span>SYSTEM PREFERENCES</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            SETTINGS
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Data provider connections, latency budgets, quant model parameters, and safety interlock specifications.
          </p>
        </div>

        <div className="flex items-center gap-2 font-mono text-xs">
          <QLBadge variant="positive" size="sm" dot>
            SYSTEM CONFIGURED
          </QLBadge>
        </div>
      </div>

      {/* 01 DATA CONFIGURATION */}
      <QLSection
        number={1}
        title="Data Source &amp; Provider Feeds"
        subtitle="Ingestion Pipeline Specifications"
      >
        <div className="grid gap-6 lg:grid-cols-2">
          <QLPanel variant="surface" padding="md" title="Data Feed Ingestion">
            <SettingRow
              label="Primary Market Feed Provider"
              value="YAHOO_FINANCE (Polling)"
              description="Delayed market data with ~15 minute observation latency"
            />
            <SettingRow
              label="Staleness Detection Threshold"
              value="900s (15 min)"
              description="Maximum acceptable age before STALE invariant triggers"
            />
            <SettingRow
              label="Historical Data Warehouse"
              value="PostgreSQL / Point-in-Time"
              description="Adjusted OHLCV splits and corporate action normalization"
            />
            <SettingRow
              label="Execution Mode"
              value="PAPER_TRADING (100% Virtual)"
              description="Hardlocked to zero real-money order routing"
            />
          </QLPanel>

          <QLPanel variant="surface" padding="md" title="Quant Engine &amp; Inference">
            <SettingRow
              label="Quant Service Backend"
              value="Python FastAPI / Port 8000"
              description="Alpha generation, feature pipeline, and risk engine"
            />
            <SettingRow
              label="Active Model Artifact"
              value="PHASE_16_FROZEN_RIDGE_TOP8"
              description="Ridge regression alpha model with canonical 8-feature order"
            />
            <SettingRow
              label="Purge / Embargo Period"
              value="20 Trading Days"
              description="Strict cross-validation leakage barrier"
            />
            <SettingRow
              label="Target Forecast Horizon"
              value="T+20 Forward Return"
              description="Multi-day forward holding alpha target"
            />
          </QLPanel>
        </div>
      </QLSection>

      {/* 02 RISK CONTROLS & MONITORING */}
      <QLSection
        number={2}
        title="Risk Invariants &amp; Safety Controls"
        subtitle="Enforced Portfolio Constraints"
      >
        <div className="grid gap-6 lg:grid-cols-2">
          <QLPanel variant="surface" padding="md" title="Risk Engine Parameters">
            <SettingRow
              label="Max Single Position Cap"
              value="10.0% NAV (₹50,000)"
              description="Hard capital allocation ceiling per instrument"
            />
            <SettingRow
              label="Volatility Stop Loss"
              value="2.0 × ATR(14)"
              description="Dynamic trailing volatility threshold"
            />
            <SettingRow
              label="Max Portfolio Drawdown Circuit"
              value="10.0% NAV"
              description="Automatic trading halt trigger"
            />
            <SettingRow
              label="Emergency Kill Switch"
              value="MANUAL_ARMED"
              description="One-click instantaneous order cancellation & halt"
            />
          </QLPanel>

          <QLPanel variant="surface" padding="md" title="Observability &amp; Retention">
            <SettingRow
              label="Health Check Polling Frequency"
              value="30 Seconds"
              description="Automated system heartbeat interval"
            />
            <SettingRow
              label="Telemetry Event Retention"
              value="90 Days"
              description="Audit trail and cryptographic provenance log lifespan"
            />
            <SettingRow
              label="Feature Drift Detection"
              value="Kolmogorov-Smirnov"
              description="Real-time distribution non-stationarity monitor"
            />
            <SettingRow
              label="Build Version"
              value="QuantLab Institutional v1.0"
              description="Production release build"
            />
          </QLPanel>
        </div>
      </QLSection>
    </div>
  );
}
