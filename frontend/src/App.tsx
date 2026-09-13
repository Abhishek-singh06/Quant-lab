import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { MainLayout } from '@/components/layout/MainLayout';
import { OverviewPage } from '@/pages/OverviewPage';
import { MarketIntelligenceTerminalPage } from '@/pages/MarketIntelligenceTerminalPage';
import { MarketsPage } from '@/pages/MarketsPage';
import { StockAnalysisPage } from '@/pages/StockAnalysisPage';
import { MarketPulsePage } from '@/pages/MarketPulsePage';
import { InstitutionalPage } from '@/pages/InstitutionalPage';
import { MutualFundsPage } from '@/pages/MutualFundsPage';
import { StrategiesPage } from '@/pages/StrategiesPage';
import { BacktestsPage } from '@/pages/BacktestsPage';
import { ModelsPage } from '@/pages/ModelsPage';
import { AiResearchPage } from '@/pages/AiResearchPage';
import { PaperTradingPage } from '@/pages/PaperTradingPage';
import { PortfolioPage } from '@/pages/PortfolioPage';
import { RiskPage } from '@/pages/RiskPage';
import { MonitoringPage } from '@/pages/MonitoringPage';
import { LiveTradingPage } from '@/pages/LiveTradingPage';
import { SettingsPage } from '@/pages/SettingsPage';

function App() {
  return (
    <BrowserRouter>
      <MainLayout>
        <Routes>
          <Route path="/" element={<OverviewPage />} />
          <Route path="/terminal" element={<MarketIntelligenceTerminalPage />} />
          <Route path="/markets" element={<MarketsPage />} />
          <Route path="/stock" element={<StockAnalysisPage />} />
          <Route path="/stock/:symbol" element={<StockAnalysisPage />} />
          <Route path="/stock-analysis" element={<StockAnalysisPage />} />
          <Route path="/stock-analysis/:symbol" element={<StockAnalysisPage />} />
          <Route path="/market-pulse" element={<MarketPulsePage />} />
          <Route path="/institutional" element={<InstitutionalPage />} />
          <Route path="/mutual-funds" element={<MutualFundsPage />} />
          <Route path="/strategies" element={<StrategiesPage />} />
          <Route path="/backtests" element={<BacktestsPage />} />
          <Route path="/models" element={<ModelsPage />} />
          <Route path="/ai-research" element={<AiResearchPage />} />
          <Route path="/paper-trading" element={<PaperTradingPage />} />
          <Route path="/portfolio" element={<PortfolioPage />} />
          <Route path="/risk" element={<RiskPage />} />
          <Route path="/monitoring" element={<MonitoringPage />} />
          <Route path="/live-trading" element={<LiveTradingPage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Routes>
      </MainLayout>
    </BrowserRouter>
  );
}

export default App;
