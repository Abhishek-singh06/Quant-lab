import { cn } from '@/lib/utils';
import { Database } from 'lucide-react';
import { QLBadge } from './QLBadge';

export interface QLDataQualityProps {
  sourcesVerified?: number;
  totalSources?: number;
  provider?: string;
  asOfTimestamp?: string;
  status?: 'HIGH_QUALITY' | 'DELAYED' | 'TEST_FIXTURE' | 'PARTIAL' | 'UNAVAILABLE';
  hash?: string;
  className?: string;
}

export function QLDataQuality({
  sourcesVerified = 4,
  totalSources = 4,
  provider = 'YAHOO FINANCE (DELAYED ~15M)',
  asOfTimestamp,
  status = 'DELAYED',
  hash,
  className,
}: QLDataQualityProps) {
  const statusVariant =
    status === 'HIGH_QUALITY'
      ? 'positive'
      : status === 'DELAYED'
      ? 'warning'
      : status === 'TEST_FIXTURE'
      ? 'info'
      : 'negative';

  return (
    <div
      className={cn(
        'rounded-lg border border-border/80 bg-surface-elevated/40 p-3 flex flex-wrap items-center justify-between gap-3 text-xs font-mono',
        className
      )}
    >
      <div className="flex items-center gap-2">
        <Database className="w-3.5 h-3.5 text-text-muted" />
        <span className="text-text-muted uppercase text-[10px]">Data Quality:</span>
        <QLBadge variant={statusVariant} size="xs" dot>
          {status}
        </QLBadge>
        <span className="text-text-secondary text-[11px]">
          ({sourcesVerified}/{totalSources} inputs verified)
        </span>
      </div>

      <div className="flex items-center gap-3 text-[11px] text-text-muted">
        <span>Provider: <strong className="text-text-secondary">{provider}</strong></span>
        {asOfTimestamp && (
          <>
            <span>•</span>
            <span>As of: {new Date(asOfTimestamp).toLocaleTimeString('en-IN')} IST</span>
          </>
        )}
        {hash && (
          <>
            <span>•</span>
            <span className="text-text-muted truncate max-w-[120px]" title={hash}>
              SHA: {hash.slice(0, 8)}...
            </span>
          </>
        )}
      </div>
    </div>
  );
}
