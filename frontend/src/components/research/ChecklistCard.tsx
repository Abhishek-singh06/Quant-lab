import { CheckCircle2, AlertTriangle, XCircle, HelpCircle } from 'lucide-react';

export interface ChecklistItemData {
  item_id: number;
  category: string;
  name: string;
  metric_name: string;
  value?: any;
  threshold: string;
  status: 'PASS' | 'WARNING' | 'FAIL' | 'UNKNOWN';
  explanation: string;
  confidence: number;
  as_of?: string;
}

interface ChecklistCardProps {
  items: ChecklistItemData[];
  passedCount: number;
  warningsCount: number;
  failedCount: number;
  unknownCount: number;
}

export function ChecklistCard({
  items,
  passedCount,
  warningsCount,
  failedCount,
  unknownCount,
}: ChecklistCardProps) {
  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PASS':
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded bg-green-500/10 text-green-400 border border-green-500/20">
            <CheckCircle2 className="h-3 w-3" /> PASS
          </span>
        );
      case 'WARNING':
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded bg-yellow-500/10 text-yellow-400 border border-yellow-500/20">
            <AlertTriangle className="h-3 w-3" /> WARNING
          </span>
        );
      case 'FAIL':
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded bg-red-500/10 text-red-400 border border-red-500/20">
            <XCircle className="h-3 w-3" /> FAIL
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded bg-gray-500/10 text-text-muted border border-border">
            <HelpCircle className="h-3 w-3" /> UNKNOWN
          </span>
        );
    }
  };

  return (
    <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
      {/* Header & Stats Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-border/60 pb-3">
        <div>
          <h3 className="text-sm font-bold uppercase tracking-wider text-text-primary">
            20-Point Value & Quant Investment Checklist
          </h3>
          <p className="text-xs text-text-muted mt-0.5">
            Strict point-in-time evaluation of balance sheet, profitability, valuation, and governance
          </p>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs font-mono px-2 py-1 rounded bg-green-500/10 text-green-400 border border-green-500/20 font-bold">
            {passedCount} Passed
          </span>
          {warningsCount > 0 && (
            <span className="text-xs font-mono px-2 py-1 rounded bg-yellow-500/10 text-yellow-400 border border-yellow-500/20 font-bold">
              {warningsCount} Warnings
            </span>
          )}
          {failedCount > 0 && (
            <span className="text-xs font-mono px-2 py-1 rounded bg-red-500/10 text-red-400 border border-red-500/20 font-bold">
              {failedCount} Failed
            </span>
          )}
          {unknownCount > 0 && (
            <span className="text-xs font-mono px-2 py-1 rounded bg-surface-elevated text-text-muted border border-border font-bold">
              {unknownCount} Unknown
            </span>
          )}
        </div>
      </div>

      {/* Checklist Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-border bg-surface-elevated/40 text-text-muted font-medium">
              <th className="py-2.5 px-3">#</th>
              <th className="py-2.5 px-3">Checklist Item</th>
              <th className="py-2.5 px-3">Category</th>
              <th className="py-2.5 px-3">Benchmark Threshold</th>
              <th className="py-2.5 px-3">Status</th>
              <th className="py-2.5 px-3">Finding & Explanation</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border/40">
            {items.map((item) => (
              <tr key={item.item_id} className="hover:bg-surface-elevated/30 transition-colors">
                <td className="py-2.5 px-3 font-mono text-text-muted">{item.item_id}</td>
                <td className="py-2.5 px-3 font-bold text-text-primary whitespace-nowrap">{item.name}</td>
                <td className="py-2.5 px-3 text-text-secondary whitespace-nowrap">
                  <span className="px-1.5 py-0.5 rounded bg-surface-elevated border border-border text-[10px]">
                    {item.category}
                  </span>
                </td>
                <td className="py-2.5 px-3 font-mono text-text-muted whitespace-nowrap">{item.threshold}</td>
                <td className="py-2.5 px-3">{getStatusBadge(item.status)}</td>
                <td className="py-2.5 px-3 text-text-secondary max-w-md">{item.explanation}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
