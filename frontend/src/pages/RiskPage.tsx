import { Shield } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import { RiskTerminalCard } from '@/components/dashboard/RiskTerminalCard';

export function RiskPage() {
  return (
    <div className="space-y-6">
      <MockBanner />
      <div className="flex items-center gap-3">
        <Shield className="h-6 w-6 text-accent" />
        <div>
          <h1 className="text-2xl font-bold gradient-text">Risk Engine</h1>
          <p className="text-sm text-text-muted mt-0.5">Position sizing, risk budgets, stop-loss, and portfolio constraints</p>
        </div>
      </div>
      <RiskTerminalCard />
    </div>
  );
}
