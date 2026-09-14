import React from 'react';
import { cn } from '@/lib/utils';
import type { SignalType } from './tokens';

export interface QLBadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: SignalType | 'outline' | 'ghost';
  size?: 'xs' | 'sm' | 'md';
  mono?: boolean;
  dot?: boolean;
}

export function QLBadge({
  children,
  variant = 'neutral',
  size = 'sm',
  mono = true,
  dot = false,
  className,
  ...props
}: QLBadgeProps) {
  const variantStyles: Record<string, string> = {
    neutral: 'bg-surface-elevated text-text-secondary border-border/80',
    positive: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/25',
    negative: 'bg-rose-500/10 text-rose-400 border-rose-500/25',
    warning: 'bg-amber-500/10 text-amber-400 border-amber-500/25',
    info: 'bg-sky-500/10 text-sky-400 border-sky-500/25',
    accent: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/25',
    outline: 'bg-transparent text-text-muted border-border',
    ghost: 'bg-transparent text-text-secondary border-transparent',
  };

  const dotStyles: Record<string, string> = {
    neutral: 'bg-text-muted',
    positive: 'bg-emerald-400',
    negative: 'bg-rose-400',
    warning: 'bg-amber-400',
    info: 'bg-sky-400',
    accent: 'bg-indigo-400',
    outline: 'bg-text-muted',
    ghost: 'bg-text-muted',
  };

  const sizeStyles: Record<string, string> = {
    xs: 'text-[10px] px-1.5 py-0.5 tracking-wider',
    sm: 'text-[11px] px-2 py-0.5 tracking-wide',
    md: 'text-xs px-2.5 py-1 tracking-wide',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 font-medium border rounded transition-colors uppercase select-none',
        mono ? 'font-mono' : 'font-sans',
        sizeStyles[size],
        variantStyles[variant],
        className
      )}
      {...props}
    >
      {dot && (
        <span
          className={cn(
            'w-1.5 h-1.5 rounded-full shrink-0',
            dotStyles[variant]
          )}
        />
      )}
      {children}
    </span>
  );
}
