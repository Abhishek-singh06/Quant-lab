import { Terminal } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import { PaperTradingTerminalCard } from '@/components/dashboard/PaperTradingTerminalCard';

export function PaperTradingPage() {
  return (
    <div className="space-y-6">
      <MockBanner />
      <div className="flex items-center gap-3">
        <Terminal className="h-6 w-6 text-accent" />
        <div>
          <h1 className="text-2xl font-bold gradient-text">Paper Trading</h1>
          <p className="text-sm text-text-muted mt-0.5">
            Strictly simulated execution &mdash; NO real money, NO broker orders. For strategy validation only.
          </p>
        </div>
      </div>
      <div className="rounded-lg border border-yellow-400/20 bg-yellow-400/5 px-4 py-3 text-xs text-yellow-400 font-medium">
        ⚠️ PAPER TRADING MODE &mdash; All orders and P&amp;L shown here are simulated. No real capital is at risk.
      </div>
      <PaperTradingTerminalCard />
    </div>
  );
}
