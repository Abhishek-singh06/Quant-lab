import { AlertOctagon, ArrowRight, ShieldAlert } from 'lucide-react';

export interface ConflictItemData {
  dimension_a: string;
  dimension_b: string;
  description: string;
  severity: string;
  recommended_action: string;
}

interface ConflictDetectorCardProps {
  conflicts: ConflictItemData[];
}

export function ConflictDetectorCard({ conflicts }: ConflictDetectorCardProps) {
  if (!conflicts || conflicts.length === 0) {
    return (
      <div className="rounded-xl border border-green-500/20 bg-green-500/5 p-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="rounded-lg bg-green-500/10 p-2 text-green-400">
            <ShieldAlert className="h-5 w-5" />
          </div>
          <div>
            <h4 className="text-xs font-bold text-text-primary">Zero Structural Evidence Conflicts</h4>
            <p className="text-[11px] text-text-muted mt-0.5">
              Fundamental quality, valuation multiples, solvency, and macro regime are in directional alignment.
            </p>
          </div>
        </div>
        <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-green-500/10 text-green-400 border border-green-500/20 font-bold">
          HARMONIZED
        </span>
      </div>
    );
  }

  return (
    <div className="rounded-xl border border-yellow-500/30 bg-yellow-500/5 p-5 space-y-3">
      <div className="flex items-center justify-between border-b border-yellow-500/20 pb-2.5">
        <div className="flex items-center gap-2">
          <AlertOctagon className="h-4 w-4 text-yellow-400" />
          <h3 className="text-xs font-bold uppercase tracking-wider text-yellow-400">
            Detected Evidence Conflicts ({conflicts.length})
          </h3>
        </div>
        <span className="text-[10px] font-mono text-text-muted">
          Explicit Uncertainty Acknowledged
        </span>
      </div>

      <div className="space-y-2.5">
        {conflicts.map((c, idx) => (
          <div
            key={idx}
            className="p-3.5 rounded-lg border border-yellow-500/20 bg-surface/80 space-y-2"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-xs font-bold text-text-primary">
                <span className="text-accent">{c.dimension_a}</span>
                <span className="text-text-muted">vs.</span>
                <span className="text-red-400">{c.dimension_b}</span>
              </div>
              <span className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
                c.severity === 'HIGH'
                  ? 'bg-red-500/10 text-red-400 border-red-500/20'
                  : 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20'
              }`}>
                {c.severity} SEVERITY
              </span>
            </div>

            <p className="text-xs text-text-secondary">{c.description}</p>

            <div className="flex items-center gap-1.5 text-[11px] text-text-muted pt-1.5 border-t border-border/30">
              <ArrowRight className="h-3 w-3 text-accent shrink-0" />
              <span>Recommended Action: <strong className="text-text-primary">{c.recommended_action}</strong></span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
