import { MockBanner } from '@/components/dashboard/MockBanner';
import { GlobalMarketsGrid } from '@/components/dashboard/GlobalMarketsGrid';
import { GlobalMarketRegimeCard } from '@/components/dashboard/GlobalMarketRegimeCard';
import { MarketRegimeTerminalCard } from '@/components/dashboard/MarketRegimeTerminalCard';

export function MarketPulsePage() {
  return (
    <div className="space-y-6">
      <MockBanner />
      <div>
        <h1 className="text-2xl font-bold gradient-text">Market Pulse</h1>
        <p className="text-sm text-text-muted mt-1">Global macro regime, breadth indicators, and market structure</p>
      </div>
      <GlobalMarketRegimeCard />
      <MarketRegimeTerminalCard />
      <GlobalMarketsGrid />
    </div>
  );
}
