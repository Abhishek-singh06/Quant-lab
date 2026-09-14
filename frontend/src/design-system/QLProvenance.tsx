import { cn } from '@/lib/utils';
import { GitBranch, Shield, Key, FileCode, Database } from 'lucide-react';
import { QLBadge } from './QLBadge';

export interface QLLineageStep {
  step: string;
  name: string;
  timestamp?: string;
  status: 'VERIFIED' | 'COMPUTED' | 'DELAYED' | 'GATED';
  details?: string;
  hash?: string;
}

export interface QLProvenanceProps {
  observationId?: string;
  modelVersion?: string;
  datasetId?: string;
  sha256ProvenanceHash?: string;
  pipelineSteps?: QLLineageStep[];
  className?: string;
}

const DEFAULT_STEPS: QLLineageStep[] = [
  { step: '01', name: 'YAHOO FINANCE FEED', status: 'VERIFIED', details: 'Raw OHLCV data ingestion (~15m delay)' },
  { step: '02', name: 'NORMALIZATION', status: 'COMPUTED', details: 'Zero lookahead point-in-time standardization' },
  { step: '03', name: 'TECHNICAL FEATURES', status: 'COMPUTED', details: 'RSI(14), MACD, ATR, Volatility(20), Returns(1,5,20)' },
  { step: '04', name: 'FROZEN PREPROCESSOR', status: 'VERIFIED', details: 'Z-score scaling via canonical 8-feature order' },
  { step: '05', name: 'FROZEN RIDGE MODEL', status: 'COMPUTED', details: 'T+20 expected return inference (alpha=10)' },
  { step: '06', name: 'MULTI-FACTOR SYNTHESIS', status: 'COMPUTED', details: 'Synthesized with fundamentals, institutional flows & regime' },
  { step: '07', name: 'INVARIANT RISK GATING', status: 'GATED', details: 'Max 10% sizing, volatility budget & stop-loss rules' },
  { step: '08', name: 'PAPER EXECUTION DISPATCH', status: 'VERIFIED', details: 'Virtual simulated order creation with ₹0 real money' },
];

export function QLProvenance({
  observationId = 'OBS-PIT-2026-IND-01',
  modelVersion = 'PHASE_16_FROZEN_RIDGE_TOP8_V1',
  datasetId = 'quantlab_nifty50_2020_2024_v1',
  sha256ProvenanceHash = '1342f5d9134fd6cf25c625362a6b276a2700e668ddc63c5b54d3e75c4b738885',
  pipelineSteps = DEFAULT_STEPS,
  className,
}: QLProvenanceProps) {
  return (
    <div className={cn('space-y-6 font-mono text-xs', className)}>
      {/* Cryptographic Hash Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border space-y-1">
          <div className="text-[10px] text-text-muted uppercase tracking-wider flex items-center gap-1.5">
            <Key className="w-3 h-3 text-accent" />
            OBSERVATION ID
          </div>
          <div className="text-text-primary font-bold text-xs truncate" title={observationId}>
            {observationId}
          </div>
        </div>

        <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border space-y-1">
          <div className="text-[10px] text-text-muted uppercase tracking-wider flex items-center gap-1.5">
            <FileCode className="w-3 h-3 text-cyan-400" />
            MODEL ARTIFACT ID
          </div>
          <div className="text-text-primary font-bold text-xs truncate" title={modelVersion}>
            {modelVersion}
          </div>
        </div>

        <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border space-y-1">
          <div className="text-[10px] text-text-muted uppercase tracking-wider flex items-center gap-1.5">
            <Database className="w-3 h-3 text-emerald-400" />
            DATASET VERSION
          </div>
          <div className="text-text-primary font-bold text-xs truncate" title={datasetId}>
            {datasetId}
          </div>
        </div>

        <div className="p-3.5 rounded-lg bg-surface-elevated/70 border border-border space-y-1">
          <div className="text-[10px] text-text-muted uppercase tracking-wider flex items-center gap-1.5">
            <Shield className="w-3 h-3 text-amber-400" />
            SHA-256 PROVENANCE CHECKSUM
          </div>
          <div className="text-accent font-bold text-xs truncate" title={sha256ProvenanceHash}>
            {sha256ProvenanceHash}
          </div>
        </div>
      </div>

      {/* Visual Pipeline Flow */}
      <div className="p-5 rounded-xl bg-surface border border-border/80 space-y-4">
        <div className="flex items-center justify-between border-b border-border/60 pb-3">
          <div className="flex items-center gap-2">
            <GitBranch className="w-4 h-4 text-accent" />
            <h3 className="font-bold text-sm text-text-primary tracking-wider uppercase">
              End-to-End Decision Lineage & Transformations
            </h3>
          </div>
          <QLBadge variant="positive" size="xs">
            ZERO LOOKAHEAD VERIFIED
          </QLBadge>
        </div>

        <div className="relative pl-6 space-y-6 before:absolute before:left-2.5 before:top-2 before:bottom-2 before:w-px before:bg-border">
          {pipelineSteps.map((step, idx) => (
            <div key={idx} className="relative group">
              {/* Dot */}
              <div className="absolute -left-6 mt-1 w-2.5 h-2.5 rounded-full bg-accent border-2 border-surface" />

              <div className="flex flex-col sm:flex-row sm:items-baseline justify-between gap-1">
                <div className="flex items-center gap-2">
                  <span className="text-[10px] text-text-muted font-bold">{step.step}</span>
                  <span className="font-bold text-text-primary tracking-wide">{step.name}</span>
                  <QLBadge
                    variant={
                      step.status === 'VERIFIED'
                        ? 'positive'
                        : step.status === 'GATED'
                        ? 'accent'
                        : 'neutral'
                    }
                    size="xs"
                  >
                    {step.status}
                  </QLBadge>
                </div>
                {step.timestamp && (
                  <span className="text-[10px] text-text-muted">
                    {new Date(step.timestamp).toLocaleTimeString('en-IN')} IST
                  </span>
                )}
              </div>

              {step.details && (
                <p className="text-xs text-text-secondary mt-1 font-sans">{step.details}</p>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
