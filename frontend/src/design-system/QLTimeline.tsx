import { cn } from '@/lib/utils';
import { ExternalLink } from 'lucide-react';
import { QLBadge } from './QLBadge';

export interface QLTimelineEvent {
  id: string | number;
  title: string;
  source: string;
  timestamp: string;
  summary?: string;
  sentiment?: 'positive' | 'negative' | 'neutral';
  relevanceScore?: number;
  url?: string;
}

export interface QLTimelineProps {
  events: QLTimelineEvent[];
  emptyMessage?: string;
  loading?: boolean;
  className?: string;
}

export function QLTimeline({
  events,
  emptyMessage = 'NO VERIFIED NEWS OR CORPORATE FILINGS RECORDED',
  loading = false,
  className,
}: QLTimelineProps) {
  if (loading) {
    return (
      <div className="p-8 text-center text-text-muted text-xs font-mono">
        <span className="inline-block w-4 h-4 border-2 border-accent border-t-transparent rounded-full animate-spin mr-2" />
        Ingesting market event timeline...
      </div>
    );
  }

  if (events.length === 0) {
    return (
      <div className="p-8 text-center text-text-muted text-xs font-mono border border-border/60 rounded-xl bg-surface">
        {emptyMessage}
      </div>
    );
  }

  return (
    <div className={cn('space-y-4', className)}>
      <div className="relative pl-6 space-y-6 before:absolute before:left-2.5 before:top-2 before:bottom-2 before:w-px before:bg-border/80">
        {events.map((ev) => (
          <div key={ev.id} className="relative group">
            {/* Timeline Dot */}
            <div className="absolute -left-6 mt-1 w-2.5 h-2.5 rounded-full bg-border group-hover:bg-accent transition-colors border-2 border-surface" />

            <div className="p-3.5 rounded-xl border border-border bg-surface hover:bg-surface-elevated transition-colors space-y-2">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2 text-xs font-mono">
                  <span className="text-text-muted uppercase text-[10px]">{ev.source}</span>
                  <span className="text-border">•</span>
                  <span className="text-text-muted text-[11px]">
                    {new Date(ev.timestamp).toLocaleTimeString('en-IN')} IST
                  </span>
                  {ev.sentiment && (
                    <QLBadge variant={ev.sentiment} size="xs">
                      {ev.sentiment}
                    </QLBadge>
                  )}
                </div>

                {ev.url && (
                  <a
                    href={ev.url}
                    target="_blank"
                    rel="noreferrer"
                    className="text-text-muted hover:text-accent transition-colors"
                  >
                    <ExternalLink className="w-3.5 h-3.5" />
                  </a>
                )}
              </div>

              <h4 className="text-sm font-bold text-text-primary leading-snug">
                {ev.title}
              </h4>

              {ev.summary && (
                <p className="text-xs text-text-secondary leading-relaxed font-sans">
                  {ev.summary}
                </p>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
