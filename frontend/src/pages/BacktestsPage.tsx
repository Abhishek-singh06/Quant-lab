import { FlaskConical } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import { BacktestTerminalCard } from '@/components/dashboard/BacktestTerminalCard';
import { WalkForwardDashboardCard } from '@/components/dashboard/WalkForwardDashboardCard';

export function BacktestsPage() {
  return (
    <div className="space-y-6">
      <MockBanner />
      <div className="flex items-center gap-3">
        <FlaskConical className="h-6 w-6 text-accent" />
        <div>
          <h1 className="text-2xl font-bold gradient-text">Backtests</h1>
          <p className="text-sm text-text-muted mt-0.5">Historical strategy performance with realistic Indian market cost models</p>
        </div>
      </div>
      <BacktestTerminalCard />
      <WalkForwardDashboardCard />
    </div>
  );
}
