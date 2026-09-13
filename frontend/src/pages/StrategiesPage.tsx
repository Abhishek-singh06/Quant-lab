import { GitFork } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import { HorizonAnalysisCard } from '@/components/dashboard/HorizonAnalysisCard';
import { SignalTerminalCard } from '@/components/dashboard/SignalTerminalCard';

export function StrategiesPage() {
  return (
    <div className="space-y-6">
      <MockBanner />
      <div className="flex items-center gap-3">
        <GitFork className="h-6 w-6 text-accent" />
        <div>
          <h1 className="text-2xl font-bold gradient-text">Trading Strategies</h1>
          <p className="text-sm text-text-muted mt-0.5">Multi-horizon strategy signals: Short, Medium &amp; Long-term</p>
        </div>
      </div>
      <div className="grid gap-6 lg:grid-cols-2">
        <HorizonAnalysisCard />
        <SignalTerminalCard />
      </div>
    </div>
  );
}
