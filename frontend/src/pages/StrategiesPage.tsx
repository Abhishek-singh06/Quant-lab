import { QLBadge } from '@/design-system/QLBadge';
import { QLSection } from '@/design-system/QLSection';
import { HorizonAnalysisCard } from '@/components/dashboard/HorizonAnalysisCard';
import { SignalTerminalCard } from '@/components/dashboard/SignalTerminalCard';

export function StrategiesPage() {
  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>STRATEGY ENGINE &amp; MULTI-HORIZON SIGNALS</span>
            <span>•</span>
            <span>INDEPENDENT EVALUATION</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            QUANT STRATEGIES
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Short, Medium, and Long-Term multi-horizon quantitative strategy execution engines with cross-horizon conflict detectors.
          </p>
        </div>

        <div className="flex items-center gap-2 font-mono text-xs">
          <QLBadge variant="positive" size="sm" dot>
            MULTI-HORIZON ACTIVE
          </QLBadge>
        </div>
      </div>

      {/* 01 HORIZON ALPHA DECOMPOSITION */}
      <QLSection
        number={1}
        title="Multi-Horizon Alpha Signals"
        subtitle="Independent Short (T+1), Medium (T+5), and Long (T+20) Framework"
      >
        <div className="grid gap-6 lg:grid-cols-2">
          <HorizonAnalysisCard />
          <SignalTerminalCard />
        </div>
      </QLSection>
    </div>
  );
}
