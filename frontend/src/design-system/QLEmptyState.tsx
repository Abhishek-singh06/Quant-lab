import { cn } from '@/lib/utils';
import { AlertCircle, Clock, Database, Radio, ShieldAlert } from 'lucide-react';
import { QLButton } from './QLButton';

export interface QLEmptyStateProps {
  title?: string;
  description: string;
  source?: string;
  lastVerified?: string;
  nextAction?: string;
  onAction?: () => void;
  actionLabel?: string;
  type?: 'unavailable' | 'closed' | 'error' | 'empty' | 'unconnected';
  className?: string;
}

export function QLEmptyState({
  title = 'DATA UNAVAILABLE',
  description,
  source = 'NSE / YAHOO FINANCE',
  lastVerified = '—',
  nextAction,
  onAction,
  actionLabel = 'Retry Connection',
  type = 'unavailable',
  className,
}: QLEmptyStateProps) {
  const iconMap = {
    unavailable: Database,
    closed: Clock,
    error: AlertCircle,
    empty: Radio,
    unconnected: ShieldAlert,
  };

  const Icon = iconMap[type] || Database;

  return (
    <div
      className={cn(
        'rounded-xl border border-border/80 bg-surface/80 p-6 sm:p-8 text-center max-w-xl mx-auto space-y-5',
        className
      )}
    >
      <div className="mx-auto w-12 h-12 rounded-xl bg-surface-elevated border border-border flex items-center justify-center text-text-muted">
        <Icon className="w-6 h-6 text-text-secondary" />
      </div>

      <div className="space-y-2">
        <h3 className="text-base sm:text-lg font-bold uppercase tracking-wider text-text-primary font-mono">
          {title}
        </h3>
        <p className="text-xs sm:text-sm text-text-secondary leading-relaxed max-w-md mx-auto">
          {description}
        </p>
      </div>

      {/* Metadata strip */}
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 pt-2 pb-1 border-t border-b border-border/60 text-[11px] font-mono text-left">
        <div className="p-2 rounded bg-surface-elevated/60">
          <span className="text-text-muted block text-[10px] uppercase">SOURCE</span>
          <span className="text-text-primary font-semibold truncate block">{source}</span>
        </div>
        <div className="p-2 rounded bg-surface-elevated/60">
          <span className="text-text-muted block text-[10px] uppercase">LAST VERIFIED</span>
          <span className="text-text-secondary truncate block">{lastVerified}</span>
        </div>
        <div className="p-2 rounded bg-surface-elevated/60 col-span-2 sm:col-span-1">
          <span className="text-text-muted block text-[10px] uppercase">STATE</span>
          <span className="text-amber-400 font-bold block">DISCLOSED</span>
        </div>
      </div>

      {nextAction && (
        <div className="text-xs text-text-muted">
          <span className="font-semibold text-text-secondary uppercase text-[10px] tracking-wider block mb-1">
            RECOMMENDED ACTION:
          </span>
          {nextAction}
        </div>
      )}

      {onAction && (
        <div className="pt-2">
          <QLButton variant="secondary" size="sm" onClick={onAction}>
            {actionLabel}
          </QLButton>
        </div>
      )}
    </div>
  );
}

export function QLMarketClosedCard({
  marketName = 'NSE / BSE',
  nextSession = '09:15 IST (NEXT TRADING SESSION)',
  dataMode = 'DELAYED EOD',
  className,
}: {
  marketName?: string;
  nextSession?: string;
  dataMode?: string;
  className?: string;
}) {
  return (
    <div
      className={cn(
        'rounded-xl border border-border/80 bg-surface-elevated/50 p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 font-mono text-xs',
        className
      )}
    >
      <div className="flex items-center gap-3">
        <div className="w-9 h-9 rounded-lg bg-surface border border-border flex items-center justify-center text-amber-400">
          <Clock className="w-4 h-4" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <span className="font-bold text-text-primary uppercase tracking-wide">MARKET CLOSED</span>
            <span className="text-text-muted">•</span>
            <span className="text-text-secondary">{marketName}</span>
          </div>
          <p className="text-[11px] text-text-muted mt-0.5">
            Next observation window: <strong className="text-text-primary">{nextSession}</strong>
          </p>
        </div>
      </div>

      <div className="flex items-center gap-3 text-[11px] text-text-muted self-start sm:self-auto">
        <span>Feed: <strong className="text-amber-400">{dataMode}</strong></span>
        <span>•</span>
        <span>Observation: <strong className="text-cyan-400">WAITING</strong></span>
      </div>
    </div>
  );
}
