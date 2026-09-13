import { Layers, ArrowUpRight, ArrowDownRight } from 'lucide-react';
import type { SectorSummary } from '@/types/terminal';

interface SectorHeatmapPanelProps {
  sectors: SectorSummary[];
  onSelectSector?: (sectorName: string) => void;
}

export function SectorHeatmapPanel({ sectors, onSelectSector }: SectorHeatmapPanelProps) {
  return (
    <div className="rounded-xl border border-border bg-surface p-4">
      <div className="flex items-center justify-between border-b border-border/50 pb-2.5 mb-3">
        <div className="flex items-center gap-2">
          <Layers className="h-4 w-4 text-accent" />
          <h3 className="text-xs font-bold uppercase tracking-wider text-text-primary">
            NSE Sectoral Heatmap & Market Breadth
          </h3>
        </div>
        <span className="text-[11px] text-text-muted">1-Day Performance</span>
      </div>

      <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2 lg:grid-cols-3">
        {sectors.map((sec) => {
          const isPos = sec.performance1D >= 0;
          const totalBreadth = sec.advancingCount + sec.decliningCount;
          const advancePct = totalBreadth > 0 ? (sec.advancingCount / totalBreadth) * 100 : 50;

          return (
            <div
              key={sec.sectorName}
              onClick={() => onSelectSector?.(sec.sectorName)}
              className="rounded-lg border border-border/60 bg-surface-elevated/40 p-3 hover:border-accent/40 hover:bg-surface-elevated transition-all cursor-pointer"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-text-primary">{sec.sectorName}</span>
                <span
                  className={`inline-flex items-center text-xs font-bold ${
                    isPos ? 'text-green-400' : 'text-red-400'
                  }`}
                >
                  {isPos ? (
                    <ArrowUpRight className="h-3.5 w-3.5 mr-0.5" />
                  ) : (
                    <ArrowDownRight className="h-3.5 w-3.5 mr-0.5" />
                  )}
                  {isPos ? '+' : ''}
                  {sec.performance1D.toFixed(2)}%
                </span>
              </div>

              {/* Breadth progress bar */}
              <div className="mt-2">
                <div className="flex justify-between text-[10px] text-text-muted mb-1">
                  <span>Adv: {sec.advancingCount}</span>
                  <span>Dec: {sec.decliningCount}</span>
                </div>
                <div className="h-1.5 w-full rounded-full bg-red-500/30 overflow-hidden flex">
                  <div
                    className="h-full bg-green-500"
                    style={{ width: `${advancePct}%` }}
                  />
                </div>
              </div>

              <div className="mt-2.5 flex items-center justify-between text-[10px] border-t border-border/30 pt-1.5">
                <span className="text-text-muted truncate max-w-[48%]">
                  ▲ <strong className="text-green-400">{sec.topGainer}</strong>
                </span>
                <span className="text-text-muted truncate max-w-[48%] text-right">
                  ▼ <strong className="text-red-400">{sec.topLoser}</strong>
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
