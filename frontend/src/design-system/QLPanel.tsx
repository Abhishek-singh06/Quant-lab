import React from 'react';
import { cn } from '@/lib/utils';

export interface QLPanelProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'surface' | 'elevated' | 'ghost' | 'contrast';
  border?: 'default' | 'subtle' | 'none' | 'accent';
  padding?: 'none' | 'sm' | 'md' | 'lg';
  header?: React.ReactNode;
  headerAction?: React.ReactNode;
  title?: string;
  subtitle?: string;
}

export function QLPanel({
  children,
  variant = 'surface',
  border = 'default',
  padding = 'md',
  header,
  headerAction,
  title,
  subtitle,
  className,
  ...props
}: QLPanelProps) {
  const variantStyles = {
    surface: 'bg-surface',
    elevated: 'bg-surface-elevated',
    ghost: 'bg-transparent',
    contrast: 'bg-[#0b0c12]',
  };

  const borderStyles = {
    default: 'border border-border',
    subtle: 'border border-border-subtle',
    none: 'border-0',
    accent: 'border border-accent/40',
  };

  const paddingStyles = {
    none: 'p-0',
    sm: 'p-3 sm:p-4',
    md: 'p-4 sm:p-6',
    lg: 'p-6 sm:p-8',
  };

  const hasHeader = header || title;

  return (
    <div
      className={cn(
        'rounded-xl transition-all duration-150',
        variantStyles[variant],
        borderStyles[border],
        className
      )}
      {...props}
    >
      {hasHeader && (
        <div className="flex flex-wrap items-center justify-between gap-3 px-4 sm:px-6 py-3.5 border-b border-border/70">
          {header || (
            <div>
              {title && (
                <h3 className="text-sm sm:text-base font-semibold text-text-primary tracking-tight">
                  {title}
                </h3>
              )}
              {subtitle && (
                <p className="text-xs text-text-muted mt-0.5">{subtitle}</p>
              )}
            </div>
          )}
          {headerAction && <div className="shrink-0">{headerAction}</div>}
        </div>
      )}

      <div className={cn(paddingStyles[padding])}>{children}</div>
    </div>
  );
}
