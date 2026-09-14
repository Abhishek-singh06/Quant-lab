import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '@/context/AuthContext';
import { MainLayout } from '@/components/layout/MainLayout';
import { OverviewPage } from '@/pages/OverviewPage';
import { MarketIntelligenceTerminalPage } from '@/pages/MarketIntelligenceTerminalPage';
import { UniversalMarketplacePage } from '@/pages/UniversalMarketplacePage';
import { UniversalInstrumentDetailPage } from '@/pages/UniversalInstrumentDetailPage';
import { MarketsPage } from '@/pages/MarketsPage';
import { StockAnalysisPage } from '@/pages/StockAnalysisPage';
import { MarketPulsePage } from '@/pages/MarketPulsePage';
import { InstitutionalPage } from '@/pages/InstitutionalPage';
import { MutualFundsPage } from '@/pages/MutualFundsPage';
import { MutualFundDetailPage } from '@/pages/MutualFundDetailPage';
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
import { LoginPage } from '@/pages/LoginPage';
import { SignupPage } from '@/pages/SignupPage';
import { OnboardingPage } from '@/pages/OnboardingPage';
import { BrokerConnectionPage } from '@/pages/BrokerConnectionPage';
import { InvestmentProfilePage } from '@/pages/InvestmentProfilePage';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <MainLayout>
          <Routes>
            <Route path="/" element={<OverviewPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/onboarding" element={<OnboardingPage />} />
            <Route path="/connect-broker" element={<BrokerConnectionPage />} />
            <Route path="/settings/brokers" element={<BrokerConnectionPage />} />
            <Route path="/settings/investment-profile" element={<InvestmentProfilePage />} />
            <Route path="/terminal" element={<MarketIntelligenceTerminalPage />} />
            <Route path="/markets" element={<UniversalMarketplacePage />} />
            <Route path="/markets/indices" element={<MarketsPage />} />
            <Route path="/instrument/:exchange/:symbol" element={<UniversalInstrumentDetailPage />} />
            <Route path="/stock" element={<StockAnalysisPage />} />
            <Route path="/stock/:symbol" element={<StockAnalysisPage />} />
            <Route path="/stock-analysis" element={<StockAnalysisPage />} />
            <Route path="/stock-analysis/:symbol" element={<StockAnalysisPage />} />
            <Route path="/market-pulse" element={<MarketPulsePage />} />
            <Route path="/institutional" element={<InstitutionalPage />} />
            <Route path="/mutual-funds" element={<MutualFundsPage />} />
            <Route path="/mutual-funds/:schemeCode" element={<MutualFundDetailPage />} />
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
    </AuthProvider>
  );
}

export default App;
