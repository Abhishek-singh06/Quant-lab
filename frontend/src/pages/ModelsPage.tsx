import { Brain } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import { QuantPredictionTerminalCard } from '@/components/dashboard/QuantPredictionTerminalCard';
import { WalkForwardDashboardCard } from '@/components/dashboard/WalkForwardDashboardCard';
import { MarketRegimeTerminalCard } from '@/components/dashboard/MarketRegimeTerminalCard';
import { DeepLearningSequenceCard } from '@/components/dashboard/DeepLearningSequenceCard';

export function ModelsPage() {
  return (
    <div className="space-y-6">
      <MockBanner />
      <div className="flex items-center gap-3">
        <Brain className="h-6 w-6 text-accent" />
        <div>
          <h1 className="text-2xl font-bold gradient-text">Quant Models</h1>
          <p className="text-sm text-text-muted mt-0.5">ML prediction models, temporal deep learning, walk-forward validation, and regime detection</p>
        </div>
      </div>
      <DeepLearningSequenceCard />
      <QuantPredictionTerminalCard />
      <WalkForwardDashboardCard />
      <MarketRegimeTerminalCard />
    </div>
  );
}
