import React from 'react';
import { cn } from '@/lib/utils';

export interface QLSectionProps {
  number?: string | number;
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
  children: React.ReactNode;
  className?: string;
  badge?: React.ReactNode;
}

export function QLSection({
  number,
  title,
  subtitle,
  action,
  children,
  className,
  badge,
}: QLSectionProps) {
  const formattedNumber =
    number !== undefined
      ? typeof number === 'number' && number < 10
        ? `0${number}`
        : `${number}`
      : undefined;

  return (
    <section className={cn('space-y-4 pt-4 first:pt-0', className)}>
      {/* Editorial Header */}
      <div className="flex flex-col sm:flex-row sm:items-baseline justify-between gap-2 border-b border-border/60 pb-3">
        <div className="flex items-baseline gap-3 flex-wrap">
          {formattedNumber && (
            <span className="font-mono text-xs sm:text-sm font-bold text-text-muted tracking-widest uppercase">
              {formattedNumber}
            </span>
          )}
          <h2 className="text-lg sm:text-xl md:text-2xl font-bold tracking-tight text-text-primary uppercase flex items-center gap-2.5">
            {title}
            {badge && <span className="normal-case font-normal">{badge}</span>}
          </h2>
          {subtitle && (
            <span className="text-xs sm:text-sm text-text-muted font-normal">
              — {subtitle}
            </span>
          )}
        </div>

        {action && <div className="shrink-0">{action}</div>}
      </div>

      {/* Content */}
      <div>{children}</div>
    </section>
  );
}
