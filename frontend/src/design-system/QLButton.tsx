import React from 'react';
import { cn } from '@/lib/utils';

export interface QLButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger' | 'outline';
  size?: 'xs' | 'sm' | 'md' | 'lg';
  icon?: React.ReactNode;
  iconPosition?: 'left' | 'right';
  loading?: boolean;
}

export function QLButton({
  children,
  variant = 'secondary',
  size = 'md',
  icon,
  iconPosition = 'left',
  loading = false,
  className,
  disabled,
  ...props
}: QLButtonProps) {
  const baseStyles =
    'inline-flex items-center justify-center font-medium transition-all duration-150 select-none cursor-pointer disabled:cursor-not-allowed disabled:opacity-40 rounded-md';

  const sizeStyles = {
    xs: 'text-[11px] px-2 py-1 gap-1.5 font-mono',
    sm: 'text-xs px-2.5 py-1.5 gap-2',
    md: 'text-sm px-3.5 py-2 gap-2',
    lg: 'text-base px-5 py-2.5 gap-2.5',
  };

  const variantStyles = {
    primary:
      'bg-text-primary text-background hover:bg-white active:bg-gray-200 font-semibold shadow-sm',
    secondary:
      'bg-surface-elevated text-text-primary border border-border hover:bg-surface-hover hover:border-border-strong active:bg-surface',
    outline:
      'bg-transparent text-text-secondary border border-border hover:text-text-primary hover:border-border-strong hover:bg-surface-elevated/40',
    ghost:
      'bg-transparent text-text-secondary hover:text-text-primary hover:bg-surface-elevated/50',
    danger:
      'bg-rose-500/15 text-rose-400 border border-rose-500/30 hover:bg-rose-500/25 active:bg-rose-500/30',
  };

  return (
    <button
      className={cn(baseStyles, sizeStyles[size], variantStyles[variant], className)}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? (
        <span className="w-3.5 h-3.5 border-2 border-current border-t-transparent rounded-full animate-spin shrink-0" />
      ) : (
        icon && iconPosition === 'left' && <span className="shrink-0">{icon}</span>
      )}
      {children}
      {!loading && icon && iconPosition === 'right' && <span className="shrink-0">{icon}</span>}
    </button>
  );
}
