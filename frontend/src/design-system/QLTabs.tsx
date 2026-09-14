import React from 'react';
import { cn } from '@/lib/utils';

export interface QLTabItem<T extends string = string> {
  id: T;
  label: string;
  count?: number | string;
  badge?: React.ReactNode;
  icon?: React.ReactNode;
}

export interface QLTabsProps<T extends string = string> {
  tabs: QLTabItem<T>[];
  activeTab: T;
  onChange: (tabId: T) => void;
  variant?: 'pill' | 'underline' | 'segment';
  className?: string;
}

export function QLTabs<T extends string = string>({
  tabs,
  activeTab,
  onChange,
  variant = 'underline',
  className,
}: QLTabsProps<T>) {
  if (variant === 'underline') {
    return (
      <div
        className={cn(
          'flex items-center gap-6 border-b border-border/70 overflow-x-auto select-none no-scrollbar',
          className
        )}
      >
        {tabs.map((tab) => {
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onChange(tab.id)}
              className={cn(
                'group flex items-center gap-2 pb-3 pt-1 text-xs sm:text-sm font-medium transition-all relative whitespace-nowrap cursor-pointer',
                isActive
                  ? 'text-text-primary font-semibold'
                  : 'text-text-muted hover:text-text-secondary'
              )}
            >
              {tab.icon && <span className="shrink-0">{tab.icon}</span>}
              <span>{tab.label}</span>
              {tab.count !== undefined && (
                <span
                  className={cn(
                    'font-mono text-[10px] px-1.5 py-0.2 rounded',
                    isActive
                      ? 'bg-surface-elevated text-text-primary border border-border'
                      : 'bg-surface text-text-muted'
                  )}
                >
                  {tab.count}
                </span>
              )}
              {tab.badge}
              {isActive && (
                <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-accent rounded-full" />
              )}
            </button>
          );
        })}
      </div>
    );
  }

  if (variant === 'segment') {
    return (
      <div
        className={cn(
          'inline-flex items-center p-1 rounded-lg bg-[#0b0c12] border border-border/80 overflow-x-auto select-none',
          className
        )}
      >
        {tabs.map((tab) => {
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onChange(tab.id)}
              className={cn(
                'flex items-center gap-2 px-3 py-1.5 rounded-md text-xs font-medium transition-all whitespace-nowrap cursor-pointer',
                isActive
                  ? 'bg-surface-elevated text-text-primary font-semibold shadow-sm border border-border/60'
                  : 'text-text-muted hover:text-text-secondary'
              )}
            >
              {tab.icon && <span className="shrink-0">{tab.icon}</span>}
              <span>{tab.label}</span>
              {tab.count !== undefined && (
                <span className="font-mono text-[10px] text-text-muted">({tab.count})</span>
              )}
              {tab.badge}
            </button>
          );
        })}
      </div>
    );
  }

  // Pill variant
  return (
    <div
      className={cn(
        'flex items-center gap-1.5 overflow-x-auto select-none no-scrollbar',
        className
      )}
    >
      {tabs.map((tab) => {
        const isActive = activeTab === tab.id;
        return (
          <button
            key={tab.id}
            onClick={() => onChange(tab.id)}
            className={cn(
              'flex items-center gap-2 px-3 py-1.5 rounded-md text-xs font-medium transition-all whitespace-nowrap cursor-pointer border',
              isActive
                ? 'bg-accent-muted text-accent border-accent/40 font-semibold'
                : 'bg-surface-elevated text-text-secondary border-border hover:bg-surface-hover hover:text-text-primary'
            )}
          >
            {tab.icon && <span className="shrink-0">{tab.icon}</span>}
            <span>{tab.label}</span>
            {tab.count !== undefined && (
              <span className="font-mono text-[10px] text-text-muted">({tab.count})</span>
            )}
            {tab.badge}
          </button>
        );
      })}
    </div>
  );
}
