import { cn } from '@/lib/utils';
import { QLBadge } from './QLBadge';

export interface QLStatusProps {
  label: string;
  status: string;
  substatus?: string;
  type?: 'market' | 'data' | 'paper' | 'model' | 'broker' | 'generic';
  state?: 'positive' | 'negative' | 'warning' | 'info' | 'neutral';
  className?: string;
}

export function QLStatus({
  label,
  status,
  substatus,
  state = 'neutral',
  className,
}: QLStatusProps) {
  return (
    <div
      className={cn(
        'inline-flex items-center gap-2 px-2.5 py-1 rounded bg-surface-elevated/80 border border-border/80 font-mono text-[11px]',
        className
      )}
    >
      <span className="text-text-muted uppercase text-[10px] tracking-wider">{label}</span>
      <span className="text-border-strong">/</span>
      <QLBadge variant={state} size="xs" dot>
        {status}
      </QLBadge>
      {substatus && <span className="text-text-muted text-[10px]">({substatus})</span>}
    </div>
  );
}

export function QLTerminalStatusBar({
  marketOpen = false,
  dataProvider = 'YAHOO FINANCE',
  dataFreshness = 'DELAYED (~15m)',
  paperMode = true,
  realMoneyRisk = 0,
  liveBroker = 'DISABLED',
  modelVersion = 'PHASE_16_FROZEN_RIDGE_TOP8',
  className,
}: {
  marketOpen?: boolean;
  dataProvider?: string;
  dataFreshness?: string;
  paperMode?: boolean;
  realMoneyRisk?: number;
  liveBroker?: string;
  modelVersion?: string;
  className?: string;
}) {
  return (
    <div
      className={cn(
        'flex flex-wrap items-center gap-2 py-1.5 px-3 bg-surface-elevated/40 border-b border-border/60 text-[11px] font-mono text-text-secondary overflow-x-auto',
        className
      )}
    >
      <div className="flex items-center gap-1.5 shrink-0">
        <span className="text-text-muted text-[10px] uppercase">NSE</span>
        <QLBadge variant={marketOpen ? 'positive' : 'warning'} size="xs" dot>
          {marketOpen ? 'OPEN' : 'CLOSED'}
        </QLBadge>
      </div>

      <span className="text-border-strong shrink-0">•</span>

      <div className="flex items-center gap-1.5 shrink-0">
        <span className="text-text-muted text-[10px] uppercase">DATA</span>
        <QLBadge variant="warning" size="xs">
          {dataFreshness}
        </QLBadge>
        <span className="text-text-muted text-[10px]">({dataProvider})</span>
      </div>

      <span className="text-border-strong shrink-0">•</span>

      <div className="flex items-center gap-1.5 shrink-0">
        <span className="text-text-muted text-[10px] uppercase">EXECUTION</span>
        <QLBadge variant={paperMode ? 'positive' : 'negative'} size="xs" dot>
          {paperMode ? 'PAPER ACTIVE' : 'SIMULATION PAUSED'}
        </QLBadge>
      </div>

      <span className="text-border-strong shrink-0">•</span>

      <div className="flex items-center gap-1.5 shrink-0">
        <span className="text-text-muted text-[10px] uppercase">REAL CAPITAL</span>
        <span className="font-bold text-emerald-400">
          ₹{realMoneyRisk.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
        </span>
      </div>

      <span className="text-border-strong shrink-0">•</span>

      <div className="flex items-center gap-1.5 shrink-0">
        <span className="text-text-muted text-[10px] uppercase">BROKER</span>
        <QLBadge variant="negative" size="xs">
          {liveBroker}
        </QLBadge>
      </div>

      <span className="text-border-strong shrink-0 hidden lg:inline">•</span>

      <div className="hidden lg:flex items-center gap-1.5 shrink-0">
        <span className="text-text-muted text-[10px] uppercase">MODEL</span>
        <span className="text-text-primary text-[10px] truncate max-w-[200px]" title={modelVersion}>
          {modelVersion}
        </span>
      </div>
    </div>
  );
}
