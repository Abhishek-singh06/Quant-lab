import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { MockBanner } from '@/components/dashboard/MockBanner';
import { MarketOverviewCard } from '@/components/dashboard/MarketOverviewCard';
import { ChartCard } from '@/components/dashboard/ChartCard';
import { SystemStatusCard } from '@/components/dashboard/SystemStatusCard';
import { GlobalMarketsGrid } from '@/components/dashboard/GlobalMarketsGrid';
import { GlobalMarketRegimeCard } from '@/components/dashboard/GlobalMarketRegimeCard';
import { CompanyFundamentalsCard } from '@/components/dashboard/CompanyFundamentalsCard';
import { TechnicalFeaturesCard } from '@/components/dashboard/TechnicalFeaturesCard';
import { MarketRegimeTerminalCard } from '@/components/dashboard/MarketRegimeTerminalCard';
import { QuantPredictionTerminalCard } from '@/components/dashboard/QuantPredictionTerminalCard';
import { WalkForwardDashboardCard } from '@/components/dashboard/WalkForwardDashboardCard';
import { SignalTerminalCard } from '@/components/dashboard/SignalTerminalCard';
import { RiskTerminalCard } from '@/components/dashboard/RiskTerminalCard';
import { HorizonAnalysisCard } from '@/components/dashboard/HorizonAnalysisCard';
import { BacktestTerminalCard } from '@/components/dashboard/BacktestTerminalCard';
import { PaperTradingTerminalCard } from '@/components/dashboard/PaperTradingTerminalCard';
import type { GlobalMarketSnapshot, GlobalMarketRegime, MarketRegime, ModelPrediction, WalkForwardRun, Signal } from '@/types/market';

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
    },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.5, ease: 'easeOut' as const },
  },
};

const FALLBACK_PREDICTION: ModelPrediction = {
  predictionId: 'PRD-LATEST-DEMO',
  modelId: 'MOD-GB-v1.0',
  modelVersion: 'GB_1D_v1.0',
  symbol: 'NIFTY 50',
  predictionTimestamp: new Date().toISOString(),
  tradingDate: '2026-09-12',
  targetHorizon: '1D',
  predictionType: 'REGRESSION',
  predictedReturn: 0.0042,
  probabilityPositive: 0.685,
  probabilityNegative: 0.315,
  predictedClass: 1,
  predictedVolatility: 0.138,
  featureContributions: {
    'RSI_14': 0.0015,
    'MOMENTUM_20D': 0.0018,
    'MARKET_BREADTH': 0.0009,
    'INDIA_VIX': -0.0006,
    'FII_NET_FLOWS': 0.0006
  },
  regimeAtPrediction: 'BULL',
  informationAvailableAt: new Date().toISOString(),
  calculatedAt: new Date().toISOString()
};

const FALLBACK_WALK_FORWARD_RUN: WalkForwardRun = {
  runId: 'WF-PROD-2025',
  runName: 'Walk-Forward Expanding Window Evaluation (4 Folds)',
  mode: 'EXPANDING',
  initialTrainStart: '2018-01-01',
  initialTrainEnd: '2021-12-31',
  stepSize: 'ANNUAL',
  modelType: 'REGRESSION',
  targetDefinition: 'future_return_1d',
  purgeWindowDays: 5,
  embargoWindowDays: 2,
  totalFolds: 4,
  completedFolds: 4,
  status: 'COMPLETED',
  aggregateMetrics: {
    meanIc: 0.0524,
    meanRankIc: 0.0582,
    meanMae: 0.0084,
    meanRocAuc: 0.584,
    meanDirectionalAccuracy: 0.548,
    baselineComparison: {
      'QuantModelMeanIC': 0.0524,
      'MomentumBaselineIC': 0.0210,
      'HistoricalMeanBaselineIC': 0.0042,
      'ZeroReturnBaselineIC': 0.0000
    },
    regimeRobustness: {
      'BULL_IC': 0.0612,
      'BEAR_IC': 0.0485,
      'SIDEWAYS_IC': 0.0410,
      'HIGH_VOL_IC': 0.0540,
      'LOW_VOL_IC': 0.0515
    }
  },
  folds: [
    { foldId: 'WF-FOLD-1', runId: 'WF-PROD-2025', foldNumber: 1, trainStart: '2018-01-01', trainEnd: '2021-12-31', validationStart: '2021-01-01', validationEnd: '2021-12-31', testStart: '2022-01-01', testEnd: '2022-12-31', winningModelId: 'MOD-GB-2022', winningAlgorithm: 'GRADIENT_BOOSTING', modelVersion: 'GB_1D_v1.0', testObservations: 248, testIc: 0.058, testRankIc: 0.063, testMae: 0.0084, testRmse: 0.0121, testRocAuc: 0.592, testDirectionalAccuracy: 0.552, status: 'COMPLETED' },
    { foldId: 'WF-FOLD-2', runId: 'WF-PROD-2025', foldNumber: 2, trainStart: '2018-01-01', trainEnd: '2022-12-31', validationStart: '2022-01-01', validationEnd: '2022-12-31', testStart: '2023-01-01', testEnd: '2023-12-31', winningModelId: 'MOD-GB-2023', winningAlgorithm: 'GRADIENT_BOOSTING', modelVersion: 'GB_1D_v2.0', testObservations: 248, testIc: 0.049, testRankIc: 0.054, testMae: 0.0086, testRmse: 0.0124, testRocAuc: 0.578, testDirectionalAccuracy: 0.544, status: 'COMPLETED' },
    { foldId: 'WF-FOLD-3', runId: 'WF-PROD-2025', foldNumber: 3, trainStart: '2018-01-01', trainEnd: '2023-12-31', validationStart: '2023-01-01', validationEnd: '2023-12-31', testStart: '2024-01-01', testEnd: '2024-12-31', winningModelId: 'MOD-GB-2024', winningAlgorithm: 'GRADIENT_BOOSTING', modelVersion: 'GB_1D_v3.0', testObservations: 248, testIc: 0.062, testRankIc: 0.068, testMae: 0.0081, testRmse: 0.0118, testRocAuc: 0.601, testDirectionalAccuracy: 0.560, status: 'COMPLETED' },
    { foldId: 'WF-FOLD-4', runId: 'WF-PROD-2025', foldNumber: 4, trainStart: '2018-01-01', trainEnd: '2024-12-31', validationStart: '2024-01-01', validationEnd: '2024-12-31', testStart: '2025-01-01', testEnd: '2025-12-31', winningModelId: 'MOD-GB-2025', winningAlgorithm: 'GRADIENT_BOOSTING', modelVersion: 'GB_1D_v4.0', testObservations: 248, testIc: 0.041, testRankIc: 0.046, testMae: 0.0087, testRmse: 0.0125, testRocAuc: 0.565, testDirectionalAccuracy: 0.536, status: 'COMPLETED' },
  ],
  createdAt: new Date().toISOString()
};

const FALLBACK_MARKET_REGIME: MarketRegime = {
  symbol: 'NIFTY 50',
  regimeTimestamp: new Date().toISOString(),
  tradingDate: '2026-09-12',
  directionRegime: 'BULL',
  volatilityRegime: 'LOW_VOL',
  riskRegime: 'RISK_ON',
  directionScore: 42.5,
  volatilityScore: 42.0,
  riskScore: 36.4,
  confidence: 0.86,
  probBull: 0.74,
  probBear: 0.08,
  probSideways: 0.18,
  probRiskOn: 0.78,
  probRiskOff: 0.22,
  daysInRegime: 14,
  isTransition: false,
  explanation: 'Market classified as BULL (LOW_VOL, RISK_ON) with 86.0% confidence based on 9 independent signals. Composite score: +42.5, India VIX: 13.80. FII 5D net: +2,450 cr, DII 5D net: +3,120 cr.',
  modelVersion: 'v1.0.0-multi-signal-composite',
  featureVersion: 'v1.0.0',
  sourceDataTimestamp: new Date().toISOString(),
  informationAvailableAt: new Date().toISOString(),
  calculatedAt: new Date().toISOString(),
  componentScores: [
    { componentName: 'NIFTY_TREND', rawValue: 75.0, normalizedValue: 75.0, componentScore: 75.0, configuredWeight: 0.25, effectiveWeight: 0.25, confidence: 'HIGH', source: 'FEATURE_ENGINE', informationAvailableAt: new Date().toISOString() },
    { componentName: 'MARKET_BREADTH', rawValue: 68.4, normalizedValue: 36.8, componentScore: 36.8, configuredWeight: 0.15, effectiveWeight: 0.15, confidence: 'HIGH', source: 'HISTORICAL_BREADTH', informationAvailableAt: new Date().toISOString() },
    { componentName: 'VOLATILITY_VIX', rawValue: 13.8, normalizedValue: 42.0, componentScore: 42.0, configuredWeight: 0.15, effectiveWeight: 0.15, confidence: 'HIGH', source: 'NSE_INDIA_VIX', informationAvailableAt: new Date().toISOString() },
    { componentName: 'MOMENTUM', rawValue: 56.5, normalizedValue: 25.5, componentScore: 25.5, configuredWeight: 0.15, effectiveWeight: 0.15, confidence: 'HIGH', source: 'FEATURE_ENGINE', informationAvailableAt: new Date().toISOString() },
    { componentName: 'GLOBAL_RISK', rawValue: 48.2, normalizedValue: 48.2, componentScore: 48.2, configuredWeight: 0.10, effectiveWeight: 0.10, confidence: 'HIGH', source: 'GLOBAL_INTELLIGENCE', informationAvailableAt: new Date().toISOString() },
    { componentName: 'FII_FLOWS', rawValue: 2450.0, normalizedValue: 24.5, componentScore: 24.5, configuredWeight: 0.08, effectiveWeight: 0.08, confidence: 'HIGH', source: 'INSTITUTIONAL_INTEL', informationAvailableAt: new Date().toISOString() },
  ]
};

const FALLBACK_SNAPSHOTS: GlobalMarketSnapshot[] = [
  { canonicalSymbol: 'SPX', providerSymbol: '^GSPC', instrumentName: 'S&P 500 Index', assetClass: 'EQUITY_INDEX', market: 'US', country: 'USA', currency: 'USD', timezone: 'America/New_York', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 5864.67, change: 43.82, changePercent: 0.75, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'OPEN', ageMinutes: 15 },
  { canonicalSymbol: 'NASDAQ', providerSymbol: '^IXIC', instrumentName: 'NASDAQ Composite', assetClass: 'EQUITY_INDEX', market: 'US', country: 'USA', currency: 'USD', timezone: 'America/New_York', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 18373.61, change: 165.36, changePercent: 0.91, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'OPEN', ageMinutes: 15 },
  { canonicalSymbol: 'DJI', providerSymbol: '^DJI', instrumentName: 'Dow Jones Industrial', assetClass: 'EQUITY_INDEX', market: 'US', country: 'USA', currency: 'USD', timezone: 'America/New_York', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 42863.86, change: 297.20, changePercent: 0.70, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'OPEN', ageMinutes: 15 },
  { canonicalSymbol: 'RUT', providerSymbol: '^RUT', instrumentName: 'Russell 2000 Index', assetClass: 'EQUITY_INDEX', market: 'US', country: 'USA', currency: 'USD', timezone: 'America/New_York', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 2234.40, change: 24.12, changePercent: 1.09, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'OPEN', ageMinutes: 15 },
  { canonicalSymbol: 'VIX', providerSymbol: '^VIX', instrumentName: 'Cboe Volatility Index', assetClass: 'VOLATILITY_INDEX', market: 'US', country: 'USA', currency: 'USD', timezone: 'America/New_York', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 18.24, change: -0.86, changePercent: -4.50, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'OPEN', ageMinutes: 15 },
  { canonicalSymbol: 'DXY', providerSymbol: 'DX-Y.NYB', instrumentName: 'US Dollar Index', assetClass: 'OTHER_MACRO', market: 'US', country: 'USA', currency: 'USD', timezone: 'America/New_York', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 103.45, change: -0.32, changePercent: -0.31, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'OPEN', ageMinutes: 15 },
  { canonicalSymbol: 'US10Y', providerSymbol: '^TNX', instrumentName: 'US 10Y Treasury Yield', assetClass: 'BOND_YIELD', market: 'US', country: 'USA', currency: 'USD', timezone: 'America/New_York', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 4.085, change: -0.042, changePercent: -1.02, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'OPEN', ageMinutes: 15 },
  { canonicalSymbol: 'USDINR', providerSymbol: 'USDINR=X', instrumentName: 'USD/INR FX Rate', assetClass: 'FX', market: 'INDIA', country: 'IND', currency: 'INR', timezone: 'Asia/Kolkata', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 84.075, change: -0.05, changePercent: -0.06, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'CLOSED', ageMinutes: 180 },
  { canonicalSymbol: 'N225', providerSymbol: '^N225', instrumentName: 'Nikkei 225', assetClass: 'EQUITY_INDEX', market: 'JAPAN', country: 'JPN', currency: 'JPY', timezone: 'Asia/Tokyo', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 39605.80, change: 224.50, changePercent: 0.57, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'CLOSED', ageMinutes: 420 },
  { canonicalSymbol: 'HSI', providerSymbol: '^HSI', instrumentName: 'Hang Seng Index', assetClass: 'EQUITY_INDEX', market: 'HONG_KONG', country: 'HKG', currency: 'HKD', timezone: 'Asia/Hong_Kong', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 20637.24, change: 185.10, changePercent: 0.91, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'CLOSED', ageMinutes: 390 },
  { canonicalSymbol: 'SSEC', providerSymbol: '000001.SS', instrumentName: 'Shanghai Composite', assetClass: 'EQUITY_INDEX', market: 'CHINA', country: 'CHN', currency: 'CNY', timezone: 'Asia/Shanghai', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 3284.32, change: 35.40, changePercent: 1.09, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'CLOSED', ageMinutes: 450 },
  { canonicalSymbol: 'FTSE', providerSymbol: '^FTSE', instrumentName: 'FTSE 100 Index', assetClass: 'EQUITY_INDEX', market: 'UK', country: 'GBR', currency: 'GBP', timezone: 'Europe/London', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 8253.65, change: 41.20, changePercent: 0.50, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'OPEN', ageMinutes: 30 },
  { canonicalSymbol: 'DAX', providerSymbol: '^GDAXI', instrumentName: 'DAX Performance Index', assetClass: 'EQUITY_INDEX', market: 'GERMANY', country: 'DEU', currency: 'EUR', timezone: 'Europe/Berlin', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 19373.83, change: 112.40, changePercent: 0.58, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'OPEN', ageMinutes: 30 },
  { canonicalSymbol: 'CRUDE_WTI', providerSymbol: 'CL=F', instrumentName: 'Crude Oil WTI Futures', assetClass: 'COMMODITY', market: 'GLOBAL', country: 'USA', currency: 'USD', timezone: 'America/New_York', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 75.56, change: 0.85, changePercent: 1.14, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'OPEN', ageMinutes: 15 },
  { canonicalSymbol: 'GOLD', providerSymbol: 'GC=F', instrumentName: 'Gold Continuous Futures', assetClass: 'COMMODITY', market: 'GLOBAL', country: 'USA', currency: 'USD', timezone: 'America/New_York', timestamp: new Date().toISOString(), tradingDate: '2026-09-12', close: 2657.40, change: 8.90, changePercent: 0.34, source: 'AUTHORIZED_GLOBAL_FEED', sourceTimestamp: new Date().toISOString(), ingestionTimestamp: new Date().toISOString(), dataFreshness: 'DELAYED', sessionStatus: 'OPEN', ageMinutes: 15 }
];

const FALLBACK_REGIME: GlobalMarketRegime = {
  timestamp: new Date().toISOString(),
  regimeLabel: 'RISK_ON',
  compositeScore: 48.25,
  equityScore: 43.12,
  volatilityScore: 48.50,
  dollarScore: 18.60,
  ratesScore: 16.80,
  commodityScore: 22.40,
  asiaScore: 42.80,
  europeScore: 27.00,
  confidence: 'HIGH',
  explanation: 'Global Regime is RISK_ON (Composite Score: +48.25, Confidence: HIGH). US Equities (+0.86% avg, Score: +43.1). VIX level at 18.24 (Score: +48.5). DXY softening to 103.45. Broad-based Asian and European advances supporting global risk appetite.',
  methodologyVersion: '1.0.0',
  sourceSnapshotCount: 15,
  calculatedAt: new Date().toISOString()
};

const FALLBACK_SIGNAL: Signal = {
  id: 'SIG-PROD-RELIANCE',
  symbol: 'RELIANCE',
  signalTimestamp: new Date().toISOString(),
  informationAvailableAt: new Date().toISOString(),
  calculatedAt: new Date().toISOString(),
  signal: 'BUY',
  signalScore: 68.5,
  confidence: 0.82,
  expectedReturn: 0.0075,
  expectedVolatility: 0.0120,
  returnToVolatilityRatio: 0.625,
  direction: 'BULLISH',
  conflictSeverity: 'LOW',
  conflictScore: 14.2,
  dataQualityStatus: 'HIGH_QUALITY',
  freshnessScore: 0.94,
  reasoning: 'BUY decision (Score: +68.5, Confidence: 82%) is supported by strong alignment across Technical (+72), Fundamental (+48), Institutional FII (+65), and ML Prediction (+62) models. Risk is contained with low cross-layer conflict.',
  structuredReasoning: [
    { statement: 'BUY decision is supported by technical breakout above SMA200 and positive 63D momentum.' },
    { statement: 'Fundamental EPS growth (+22% YoY) and quarterly ROE (20.2%) confirm underlying operational health.' },
    { statement: 'Key risk factor: India VIX elevated at 13.8, watch for short-term market consolidation.' }
  ],
  supportingEvidence: [
    { evidenceId: 'ev-1', category: 'TECHNICAL', feature: 'PRICE_VS_SMA200', normalizedScore: 78.0, reason: 'Price is +13.6% above 200-day moving average' },
    { evidenceId: 'ev-2', category: 'INSTITUTIONAL', feature: 'FII_NET_FLOW_20D', normalizedScore: 70.0, reason: 'FII 20-day cumulative flow is ₹+3,500 Cr' },
    { evidenceId: 'ev-3', category: 'ML_PREDICTION', feature: 'ML_EXPECTED_RETURN_1D', normalizedScore: 62.0, reason: 'Model 1 (GB_1D_v1.0) forecast expected return +0.75%' },
    { evidenceId: 'ev-4', category: 'FUNDAMENTAL', feature: 'EPS_GROWTH_YOY', normalizedScore: 55.0, reason: 'EPS growth YoY is +22.0% based on point-in-time filing' },
  ],
  opposingEvidence: [
    { evidenceId: 'ev-opp-1', category: 'INDIAN_MARKET', feature: 'INDIA_VIX', normalizedScore: -18.0, reason: 'India VIX at 13.8 indicates mild volatility resistance' }
  ],
  components: {
    'TECHNICAL': { id: 'c1', category: 'TECHNICAL', categoryScore: 72.0, weight: 0.18, weightedContribution: 12.96, direction: 'BULLISH', strength: 0.72, quality: 1.0, freshness: 1.0, isPresent: true },
    'FUNDAMENTAL': { id: 'c2', category: 'FUNDAMENTAL', categoryScore: 48.0, weight: 0.16, weightedContribution: 7.68, direction: 'BULLISH', strength: 0.48, quality: 1.0, freshness: 0.85, isPresent: true },
    'ML_PREDICTION': { id: 'c3', category: 'ML_PREDICTION', categoryScore: 62.0, weight: 0.16, weightedContribution: 9.92, direction: 'BULLISH', strength: 0.62, quality: 0.95, freshness: 1.0, isPresent: true },
    'MARKET_REGIME': { id: 'c4', category: 'MARKET_REGIME', categoryScore: 58.0, weight: 0.14, weightedContribution: 8.12, direction: 'BULLISH', strength: 0.58, quality: 1.0, freshness: 1.0, isPresent: true },
    'INSTITUTIONAL': { id: 'c5', category: 'INSTITUTIONAL', categoryScore: 65.0, weight: 0.10, weightedContribution: 6.50, direction: 'BULLISH', strength: 0.65, quality: 1.0, freshness: 0.90, isPresent: true },
    'GLOBAL_MARKET': { id: 'c6', category: 'GLOBAL_MARKET', categoryScore: 35.0, weight: 0.06, weightedContribution: 2.10, direction: 'BULLISH', strength: 0.35, quality: 1.0, freshness: 1.0, isPresent: true },
    'INDIAN_MARKET': { id: 'c7', category: 'INDIAN_MARKET', categoryScore: 42.0, weight: 0.04, weightedContribution: 1.68, direction: 'BULLISH', strength: 0.42, quality: 1.0, freshness: 1.0, isPresent: true },
    'MACRO': { id: 'c8', category: 'MACRO', categoryScore: 25.0, weight: 0.02, weightedContribution: 0.50, direction: 'BULLISH', strength: 0.25, quality: 1.0, freshness: 0.90, isPresent: true },
  },
  signalVersion: 'SIGNAL_v1.0.0',
  configurationVersion: 'SIGNAL_CFG_v1.0',
  modelVersion: 'GB_1D_v1.0',
  regimeVersion: 'REGIME_v1.0.0',
  isLatest: true
};

export function Dashboard() {
  const [signal, setSignal] = useState<Signal | null>(FALLBACK_SIGNAL);
  const [modelPrediction, setModelPrediction] = useState<ModelPrediction | null>(FALLBACK_PREDICTION);
  const [walkForwardRun, setWalkForwardRun] = useState<WalkForwardRun | null>(FALLBACK_WALK_FORWARD_RUN);
  const [marketRegime, setMarketRegime] = useState<MarketRegime | null>(FALLBACK_MARKET_REGIME);
  const [globalSnapshots, setGlobalSnapshots] = useState<GlobalMarketSnapshot[]>(FALLBACK_SNAPSHOTS);
  const [globalRegime, setGlobalRegime] = useState<GlobalMarketRegime | null>(FALLBACK_REGIME);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const [sigRes, predRes, wfRes, regimeRes, snapRes, globRegimeRes] = await Promise.all([
          fetch('/api/v1/signals/RELIANCE'),
          fetch('/api/v1/models/predictions/latest?symbol=NIFTY%2050'),
          fetch('/api/v1/walk-forward/runs'),
          fetch('/api/v1/regime/latest?symbol=NIFTY%2050'),
          fetch('/api/v1/global/snapshots/latest'),
          fetch('/api/v1/global/regime/latest')
        ]);
        if (sigRes.ok) {
          const sigData = await sigRes.json();
          if (sigData && sigData.signal) {
            setSignal(sigData);
          }
        }
        if (predRes.ok) {
          const prd = await predRes.json();
          if (prd && prd.predictionId) {
            setModelPrediction(prd);
          }
        }
        if (wfRes.ok) {
          const runs = await wfRes.json();
          if (Array.isArray(runs) && runs.length > 0) {
            setWalkForwardRun(runs[0]);
          }
        }
        if (regimeRes.ok) {
          const reg = await regimeRes.json();
          if (reg && reg.directionRegime) {
            setMarketRegime(reg);
          }
        }
        if (snapRes.ok) {
          const snaps = await snapRes.json();
          if (Array.isArray(snaps) && snaps.length > 0) {
            setGlobalSnapshots(snaps);
          }
        }
        if (globRegimeRes.ok) {
          const reg = await globRegimeRes.json();
          if (reg && reg.regimeLabel) {
            setGlobalRegime(reg);
          }
        }
      } catch {
        // Fallback remains active
      }
    };
    fetchDashboardData();
  }, []);

  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className="space-y-6"
    >
      <motion.div variants={itemVariants}>
        <MockBanner />
      </motion.div>

      <motion.div variants={itemVariants}>
        <div className="flex items-baseline justify-between">
          <div>
            <h1 className="text-2xl font-bold">Market Intelligence & Quantitative Decision Terminal</h1>
            <p className="text-text-secondary mt-1">
              Real-world Indian market regime classification, fundamental intelligence, technical features & global macro
            </p>
          </div>
        </div>
      </motion.div>

      {/* Production Cross-Check / Signal Engine (Part 13) */}
      <motion.div variants={itemVariants}>
        <SignalTerminalCard signal={signal} />
      </motion.div>

      {/* Production Risk Engine & Position Sizing (Part 14) */}
      <motion.div variants={itemVariants}>
        <RiskTerminalCard />
      </motion.div>

      {/* Production Trading + Investing Models: Short / Medium / Long Horizons (Part 15) */}
      <motion.div variants={itemVariants}>
        <HorizonAnalysisCard />
      </motion.div>

      {/* Production Backtesting Engine (Part 16) */}
      <motion.div variants={itemVariants}>
        <BacktestTerminalCard />
      </motion.div>

      {/* Production Paper Trading Engine (Part 17) */}
      <motion.div variants={itemVariants}>
        <PaperTradingTerminalCard />
      </motion.div>

      {/* Production Quant Prediction Engine (Part 11) */}
      <motion.div variants={itemVariants}>
        <QuantPredictionTerminalCard prediction={modelPrediction} />
      </motion.div>

      {/* Walk-Forward Historical Evaluation System (Part 12) */}
      <motion.div variants={itemVariants}>
        <WalkForwardDashboardCard run={walkForwardRun} />
      </motion.div>

      {/* Production Indian Market Regime Engine (Part 10) */}
      <motion.div variants={itemVariants}>
        <MarketRegimeTerminalCard regime={marketRegime} />
      </motion.div>

      {/* Domestic Overview */}
      <motion.div variants={itemVariants}>
        <MarketOverviewCard />
      </motion.div>

      {/* Production Technical Feature Engine */}
      <motion.div variants={itemVariants}>
        <TechnicalFeaturesCard />
      </motion.div>

      {/* Fundamental Intelligence Card */}
      <motion.div variants={itemVariants}>
        <CompanyFundamentalsCard />
      </motion.div>

      {/* Global Market Intelligence Grid (All 15 Key Assets) */}
      <motion.div variants={itemVariants}>
        <GlobalMarketsGrid snapshots={globalSnapshots} />
      </motion.div>

      {/* Global Regime and System Status */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <motion.div variants={itemVariants} className="lg:col-span-2">
          <GlobalMarketRegimeCard regime={globalRegime} />
        </motion.div>
        <motion.div variants={itemVariants}>
          <SystemStatusCard />
        </motion.div>
      </div>

      <motion.div variants={itemVariants}>
        <ChartCard />
      </motion.div>
    </motion.div>
  );
}
