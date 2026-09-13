import { cn } from '@/lib/utils';

type FreshnessStatus = 'REAL_TIME' | 'DELAYED' | 'STALE' | 'NOT_AVAILABLE' | 'UNKNOWN' | 'END_OF_DAY' | 'PERIODIC';

interface DataFreshnessBadgeProps {
  status: FreshnessStatus;
  className?: string;
  showIcon?: boolean;
}

export function DataFreshnessBadge({ status, className, showIcon = true }: DataFreshnessBadgeProps) {
  const config: Record<FreshnessStatus, { label: string; color: string; dot: string }> = {
    REAL_TIME:     { label: 'REAL-TIME',   color: 'text-green-400 bg-green-400/10 border-green-400/20',     dot: 'bg-green-400'    },
    DELAYED:       { label: 'DELAYED',     color: 'text-yellow-400 bg-yellow-400/10 border-yellow-400/20', dot: 'bg-yellow-400'   },
    STALE:         { label: 'STALE',       color: 'text-orange-400 bg-orange-400/10 border-orange-400/20', dot: 'bg-orange-400'   },
    END_OF_DAY:    { label: 'END-OF-DAY',  color: 'text-blue-400 bg-blue-400/10 border-blue-400/20',       dot: 'bg-blue-400'     },
    PERIODIC:      { label: 'PERIODIC',    color: 'text-purple-400 bg-purple-400/10 border-purple-400/20', dot: 'bg-purple-400'   },
    NOT_AVAILABLE: { label: 'UNAVAILABLE', color: 'text-red-400 bg-red-400/10 border-red-400/20',          dot: 'bg-red-400'      },
    UNKNOWN:       { label: 'UNKNOWN',     color: 'text-text-muted bg-surface-elevated border-border',     dot: 'bg-text-muted'   },
  };

  const c = config[status] ?? config.UNKNOWN;

  return (
    <span className={cn('inline-flex items-center gap-1.5 rounded-full border px-2 py-0.5 text-[10px] font-semibold', c.color, className)}>
      {showIcon && <span className={cn('h-1.5 w-1.5 rounded-full', c.dot)} />}
      {c.label}
    </span>
  );
}
