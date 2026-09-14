import React from 'react';
import { cn } from '@/lib/utils';

export interface QLTableColumn<T> {
  key: string;
  header: string | React.ReactNode;
  align?: 'left' | 'center' | 'right';
  className?: string;
  render?: (item: T, index: number) => React.ReactNode;
  width?: string;
}

export interface QLTableProps<T> {
  columns: QLTableColumn<T>[];
  data: T[];
  keyExtractor?: (item: T, index: number) => string | number;
  emptyMessage?: string | React.ReactNode;
  loading?: boolean;
  onRowClick?: (item: T) => void;
  className?: string;
  compact?: boolean;
}

export function QLTable<T extends Record<string, any>>({
  columns,
  data,
  keyExtractor,
  emptyMessage = 'No validated records found.',
  loading = false,
  onRowClick,
  className,
  compact = false,
}: QLTableProps<T>) {
  const alignStyles = {
    left: 'text-left',
    center: 'text-center',
    right: 'text-right',
  };

  const padStyles = compact ? 'px-3 py-2 text-xs' : 'px-4 py-3 text-xs sm:text-sm';

  return (
    <div className={cn('w-full overflow-x-auto border border-border/80 rounded-lg bg-surface', className)}>
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b border-border/80 bg-surface-elevated/60">
            {columns.map((col) => (
              <th
                key={col.key}
                style={{ width: col.width }}
                className={cn(
                  'font-mono text-[10px] sm:text-[11px] font-semibold text-text-muted uppercase tracking-wider select-none whitespace-nowrap',
                  padStyles,
                  alignStyles[col.align || 'left'],
                  col.className
                )}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-border/40">
          {loading ? (
            <tr>
              <td colSpan={columns.length} className="text-center py-12 text-text-muted text-xs">
                <span className="inline-block w-4 h-4 border-2 border-accent border-t-transparent rounded-full animate-spin mr-2" />
                Retrieving point-in-time records...
              </td>
            </tr>
          ) : data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="text-center py-12 text-text-muted text-xs">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((item, index) => {
              const key = keyExtractor ? keyExtractor(item, index) : item.id || item.symbol || index;
              return (
                <tr
                  key={key}
                  onClick={() => onRowClick && onRowClick(item)}
                  className={cn(
                    'transition-colors font-mono',
                    onRowClick ? 'cursor-pointer hover:bg-surface-elevated/70' : 'hover:bg-surface-elevated/30'
                  )}
                >
                  {columns.map((col) => (
                    <td
                      key={col.key}
                      className={cn(
                        padStyles,
                        alignStyles[col.align || 'left'],
                        'text-text-primary whitespace-nowrap',
                        col.className
                      )}
                    >
                      {col.render ? col.render(item, index) : item[col.key] ?? '—'}
                    </td>
                  ))}
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}
