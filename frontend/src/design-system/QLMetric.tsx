import React from 'react';
import { cn } from '@/lib/utils';
import { TrendingUp, TrendingDown, Minus } from 'lucide-react';

export interface QLMetricProps {
  label: string;
  value: React.ReactNode;
  change?: number | string;
  changePercent?: number | string;
  subtext?: string;
  provenance?: string;
  size?: 'sm' | 'md' | 'lg' | 'hero';
  trend?: 'up' | 'down' | 'neutral' | 'auto';
  prefix?: string;
  suffix?: string;
  className?: string;
}

export function QLMetric({
  label,
  value,
  change,
  changePercent,
  subtext,
  provenance,
  size = 'md',
  trend = 'auto',
  prefix,
  suffix,
  className,
}: QLMetricProps) {
  // Determine trend if auto
  let derivedTrend: 'up' | 'down' | 'neutral' = 'neutral';
  if (trend === 'auto') {
    if (typeof change === 'number') {
      derivedTrend = change > 0 ? 'up' : change < 0 ? 'down' : 'neutral';
    } else if (typeof changePercent === 'number') {
      derivedTrend = changePercent > 0 ? 'up' : changePercent < 0 ? 'down' : 'neutral';
    }
  } else {
    derivedTrend = trend;
  }

  const valueSizes = {
    sm: 'text-lg sm:text-xl font-bold',
    md: 'text-2xl sm:text-3xl font-extrabold tracking-tight',
    lg: 'text-3xl sm:text-4xl lg:text-5xl font-extrabold tracking-tight',
    hero: 'text-4xl sm:text-6xl lg:text-7xl font-black tracking-tighter',
  };

  const trendColor =
    derivedTrend === 'up'
      ? 'text-emerald-400'
      : derivedTrend === 'down'
      ? 'text-rose-400'
      : 'text-text-muted';

  return (
    <div className={cn('flex flex-col space-y-1', className)}>
      {/* Label */}
      <span className="text-[11px] font-semibold text-text-muted tracking-widest uppercase font-mono">
        {label}
      </span>

      {/* Numerical Value with Prefix/Suffix */}
      <div className="flex items-baseline gap-1.5 flex-wrap">
        {prefix && (
          <span className="text-text-secondary text-sm font-mono font-normal">{prefix}</span>
        )}
        <span className={cn('text-text-primary mono-number', valueSizes[size])}>
          {value}
        </span>
        {suffix && (
          <span className="text-text-muted text-xs font-mono">{suffix}</span>
        )}
      </div>

      {/* Change & Subtext */}
      {(change !== undefined || changePercent !== undefined || subtext) && (
        <div className="flex items-center gap-2 pt-0.5 text-xs font-mono">
          {(change !== undefined || changePercent !== undefined) && (
            <div className={cn('flex items-center gap-1 font-semibold', trendColor)}>
              {derivedTrend === 'up' && <TrendingUp className="w-3.5 h-3.5 shrink-0" />}
              {derivedTrend === 'down' && <TrendingDown className="w-3.5 h-3.5 shrink-0" />}
              {derivedTrend === 'neutral' && <Minus className="w-3.5 h-3.5 shrink-0" />}
              <span>
                {change !== undefined && (typeof change === 'number' && change > 0 ? `+${change}` : change)}
                {change !== undefined && changePercent !== undefined && ' '}
                {changePercent !== undefined && (
                  typeof changePercent === 'number'
                    ? `(${changePercent > 0 ? '+' : ''}${changePercent.toFixed(2)}%)`
                    : `(${changePercent})`
                )}
              </span>
            </div>
          )}

          {subtext && (
            <span className="text-text-muted text-[11px] truncate">{subtext}</span>
          )}
        </div>
      )}

      {/* Provenance note if provided */}
      {provenance && (
        <span className="text-[10px] text-text-subdued font-mono pt-0.5 uppercase tracking-wider">
          {provenance}
        </span>
      )}
    </div>
  );
}
