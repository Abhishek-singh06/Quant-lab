import { Shield } from 'lucide-react';
import { QLBadge } from '@/design-system/QLBadge';
import { PaperTradingTerminalCard } from '@/components/dashboard/PaperTradingTerminalCard';

export function PaperTradingPage() {
  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>ISOLATED SIMULATION TERMINAL</span>
            <span>•</span>
            <span>NO REAL BROKER ORDERS</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            PAPER TRADING
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Strictly simulated execution environment &bull; Virtual portfolio management, live fills, and zero-lookahead calibration.
          </p>
        </div>

        <div className="flex items-center gap-2 font-mono text-xs">
          <QLBadge variant="positive" size="sm" dot>
            SIMULATION ACTIVE (₹0 RISK)
          </QLBadge>
        </div>
      </div>

      {/* Safety Notice Strip */}
      <div className="p-3.5 rounded-xl border border-amber-500/30 bg-amber-500/10 flex items-center justify-between gap-3 text-xs text-amber-300 font-mono">
        <div className="flex items-center gap-2">
          <Shield className="w-4 h-4 text-amber-400 shrink-0" />
          <span>
            <strong>VIRTUAL ISOLATION ACTIVE:</strong> All orders and P&amp;L shown here are simulated with ₹0 real money. Live broker dispatch is hardlocked to <strong>DISABLED</strong>.
          </span>
        </div>
      </div>

      {/* Main Paper Execution Terminal */}
      <PaperTradingTerminalCard />
    </div>
  );
}
