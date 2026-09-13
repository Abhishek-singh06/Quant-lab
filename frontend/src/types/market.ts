/**
 * Core market data types for QuantLab.
 * These types represent real market data structures
 * for Indian equity markets and Global Market Intelligence.
 */

export interface MarketIndex {
  symbol: string;
  name: string;
  lastPrice: number;
  change: number;
  changePercent: number;
  timestamp: string;
}

export interface StockQuote {
  symbol: string;
  name: string;
  exchange: 'NSE' | 'BSE';
  lastPrice: number;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  change: number;
  changePercent: number;
  timestamp: string;
}

export interface OHLCV {
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface WatchlistItem {
  id: string;
  symbol: string;
  name: string;
  exchange: 'NSE' | 'BSE';
  addedAt: string;
}

export interface GlobalMarketSnapshot {
  canonicalSymbol: string;
  providerSymbol: string;
  instrumentName: string;
  assetClass: 'EQUITY_INDEX' | 'VOLATILITY_INDEX' | 'FX' | 'BOND_YIELD' | 'COMMODITY' | 'OTHER_MACRO';
  market: 'US' | 'JAPAN' | 'HONG_KONG' | 'CHINA' | 'UK' | 'GERMANY' | 'INDIA' | 'GLOBAL';
  country: string;
  currency: string;
  timezone: string;
  timestamp: string;
  tradingDate: string;
  open?: number;
  high?: number;
  low?: number;
  close: number;
  previousClose?: number;
  change: number;
  changePercent: number;
  volume?: number;
  yieldRate?: number;
  source: string;
  sourceTimestamp: string;
  ingestionTimestamp: string;
  dataFreshness: 'REAL_TIME' | 'DELAYED' | 'END_OF_DAY' | 'PERIODIC';
  sessionStatus: 'OPEN' | 'CLOSED' | 'HOLIDAY' | 'WEEKEND' | 'STALE' | 'NOT_AVAILABLE';
  ageMinutes?: number;
}

export interface GlobalMarketRegime {
  timestamp: string;
  regimeLabel: 'RISK_ON' | 'RISK_OFF' | 'NEUTRAL' | 'HIGH_VOLATILITY' | 'LOW_VOLATILITY' | 'TRANSITION';
  compositeScore: number;
  equityScore: number;
  volatilityScore: number;
  ratesScore: number;
  dollarScore: number;
  commodityScore: number;
  asiaScore: number;
  europeScore: number;
  confidence: 'HIGH' | 'MEDIUM' | 'LOW';
  explanation: string;
  methodologyVersion: string;
  sourceSnapshotCount: number;
  calculatedAt: string;
  componentBreakdown?: Record<string, number>;
}

export interface GlobalMarketStatus {
  market: string;
  country: string;
  timezone: string;
  sessionStatus: 'OPEN' | 'CLOSED' | 'HOLIDAY' | 'WEEKEND' | 'STALE' | 'NOT_AVAILABLE';
  evaluatedAt: string;
  localTimeFormatted: string;
  marketSessionHours: string;
  isTradingDay: boolean;
}

export interface RegimeComponentScore {
  componentName: string;
  rawValue?: number;
  normalizedValue?: number;
  componentScore: number;
  configuredWeight: number;
  effectiveWeight: number;
  confidence: string;
  source: string;
  informationAvailableAt: string;
}

export interface MarketRegime {
  symbol: string;
  regimeTimestamp: string;
  tradingDate: string;
  directionRegime: 'BULL' | 'BEAR' | 'SIDEWAYS' | 'TRANSITION';
  volatilityRegime: 'LOW_VOL' | 'NORMAL_VOL' | 'HIGH_VOL' | 'EXTREME_VOL';
  riskRegime: 'RISK_ON' | 'NEUTRAL' | 'RISK_OFF';
  directionScore: number;
  volatilityScore: number;
  riskScore: number;
  confidence: number;
  probBull: number;
  probBear: number;
  probSideways: number;
  probRiskOn: number;
  probRiskOff: number;
  previousDirectionRegime?: string;
  daysInRegime: number;
  isTransition: boolean;
  explanation: string;
  modelVersion: string;
  featureVersion: string;
  sourceDataTimestamp: string;
  informationAvailableAt: string;
  calculatedAt: string;
  componentScores?: RegimeComponentScore[];
}

export interface ModelPrediction {
  predictionId: string;
  modelId: string;
  modelVersion: string;
  symbol: string;
  predictionTimestamp: string;
  tradingDate: string;
  targetHorizon: string;
  predictionType: 'REGRESSION' | 'CLASSIFICATION' | 'VOLATILITY';
  predictedReturn?: number;
  probabilityPositive?: number;
  probabilityNegative?: number;
  predictedClass?: number;
  predictedVolatility?: number;
  actualReturn?: number;
  actualVolatility?: number;
  featureContributions?: Record<string, number>;
  regimeAtPrediction: string;
  informationAvailableAt: string;
  calculatedAt: string;
}

export interface WalkForwardFold {
  foldId: string;
  runId: string;
  foldNumber: number;
  trainStart: string;
  trainEnd: string;
  validationStart: string;
  validationEnd: string;
  testStart: string;
  testEnd: string;
  winningModelId: string;
  winningAlgorithm: string;
  modelVersion: string;
  testObservations: number;
  testIc: number;
  testRankIc: number;
  testMae: number;
  testRmse: number;
  testRocAuc?: number;
  testDirectionalAccuracy?: number;
  baselineComparison?: Record<string, number>;
  regimeBreakdown?: Record<string, number>;
  sectorBreakdown?: Record<string, number>;
  status: string;
}

export interface WalkForwardRun {
  runId: string;
  runName: string;
  mode: 'EXPANDING' | 'ROLLING';
  initialTrainStart: string;
  initialTrainEnd: string;
  stepSize: string;
  modelType: 'REGRESSION' | 'CLASSIFICATION' | 'VOLATILITY';
  targetDefinition: string;
  purgeWindowDays: number;
  embargoWindowDays: number;
  totalFolds: number;
  completedFolds: number;
  status: string;
  aggregateMetrics: {
    meanIc?: number;
    meanRankIc?: number;
    meanMae?: number;
    meanRocAuc?: number;
    meanDirectionalAccuracy?: number;
    baselineComparison?: Record<string, number>;
    regimeRobustness?: Record<string, number>;
  };
  folds?: WalkForwardFold[];
  createdAt: string;
  completedAt?: string;
}

export interface SignalComponent {
  id: string;
  category: string;
  categoryScore: number;
  weight: number;
  weightedContribution: number;
  direction: 'BULLISH' | 'BEARISH' | 'NEUTRAL';
  strength: number;
  quality: number;
  freshness: number;
  isPresent: boolean;
  missingReason?: string;
}

export interface SignalEvidence {
  id: string;
  category: string;
  featureName: string;
  rawValue?: number;
  rawValueStr?: string;
  normalizedScore: number;
  direction: 'BULLISH' | 'BEARISH' | 'NEUTRAL';
  strength: number;
  quality: number;
  freshness: number;
  confidence: number;
  source: string;
  sourceTimestamp: string;
  availableAt: string;
  reason: string;
}

export interface SignalTransition {
  id: string;
  symbol: string;
  previousSignal?: 'BUY' | 'HOLD' | 'SELL' | 'NO_SIGNAL';
  newSignal: 'BUY' | 'HOLD' | 'SELL' | 'NO_SIGNAL';
  previousScore?: number;
  newScore: number;
  transitionTimestamp: string;
  transitionReason: string;
}

export interface Signal {
  id: string;
  symbol: string;
  signalTimestamp: string;
  informationAvailableAt: string;
  calculatedAt: string;
  signal: 'BUY' | 'HOLD' | 'SELL' | 'NO_SIGNAL';
  signalScore: number;
  confidence: number;
  expectedReturn?: number;
  expectedVolatility?: number;
  returnToVolatilityRatio?: number;
  direction: 'BULLISH' | 'BEARISH' | 'NEUTRAL';
  conflictSeverity: 'LOW' | 'MEDIUM' | 'HIGH';
  conflictScore: number;
  dataQualityStatus: 'HIGH_QUALITY' | 'MEDIUM_QUALITY' | 'LOW_QUALITY' | 'INSUFFICIENT_DATA';
  freshnessScore: number;
  reasoning: string;
  structuredReasoning?: Array<{
    statement: string;
    traceableEvidenceIds?: string[];
  }>;
  supportingEvidence?: Array<{
    evidenceId: string;
    category: string;
    feature: string;
    normalizedScore: number;
    reason: string;
  }>;
  opposingEvidence?: Array<{
    evidenceId: string;
    category: string;
    feature: string;
    normalizedScore: number;
    reason: string;
  }>;
  components?: Record<string, SignalComponent>;
  signalVersion: string;
  configurationVersion: string;
  modelVersion?: string;
  regimeVersion?: string;
  isLatest: boolean;
}

export type RiskProfileType = 'CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE' | 'CUSTOM';
export type StopMethod = 'ATR_MULTIPLE' | 'FIXED_PERCENTAGE' | 'SUPPORT_RESISTANCE' | 'VOLATILITY_BAND' | 'USER_DEFINED';
export type PositionSizingMethod = 'FIXED_ALLOCATION' | 'FIXED_RISK' | 'VOLATILITY_ADJUSTED' | 'PORTFOLIO_AWARE' | 'CUSTOM';
export type RiskDecision = 'APPROVED' | 'REDUCED_ALLOCATION' | 'REJECTED_RISK_BUDGET' | 'REJECTED_SECTOR_LIMIT' | 'REJECTED_SINGLE_SECURITY_LIMIT' | 'REJECTED_PORTFOLIO_VOLATILITY' | 'REJECTED_CORRELATION' | 'REJECTED_DRAWDOWN' | 'REJECTED_LIQUIDITY' | 'REJECTED_SIGNAL_SCORE' | 'REJECTED_LOW_CONFIDENCE' | 'REJECTED_SHORT_NOT_ALLOWED' | 'ZERO_ALLOCATION';
export type RiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL';

export interface RiskProfile {
  id: string;
  name: string;
  profileType: RiskProfileType;
  maxPortfolioRisk: number;
  maxPositionRisk: number;
  maxPositionAllocation: number;
  maxSectorAllocation: number;
  maxIndustryAllocation: number;
  maxSingleSecurityAllocation: number;
  maxCorrelationExposure: number;
  maxDrawdownTolerance: number;
  maxPortfolioVolatility: number;
  minimumLiquidityRequirement: number;
  defaultStopMethod: StopMethod;
  defaultPositionSizingMethod: PositionSizingMethod;
  allowShortSelling: boolean;
  allowLeverage: boolean;
  maxLeverage: number;
  cashBuffer: number;
  minimumConfidence: number;
  minimumSignalScore: number;
  riskBudgetMethod: string;
  active: boolean;
  version: string;
  createdAt: string;
  updatedAt: string;
}

export interface PortfolioPosition {
  id: string;
  portfolioId: string;
  instrumentId?: number;
  symbol: string;
  sector?: string;
  industry?: string;
  quantity: number;
  averageEntryPrice: number;
  currentPrice: number;
  marketValue: number;
  weight: number;
  currentStopPrice?: number;
  positionRiskAmount?: number;
  positionRiskPercent?: number;
  entryTimestamp: string;
  lastUpdatedTimestamp: string;
  active: boolean;
  createdAt: string;
}

export interface Portfolio {
  id: string;
  name: string;
  currency: string;
  riskProfileId?: string;
  currentCash: number;
  currentPortfolioValue: number;
  peakPortfolioValue: number;
  peakTimestamp: string;
  currentDrawdown: number;
  maxDrawdown: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  positions?: PortfolioPosition[];
}

export interface RiskAdjustment {
  id?: string;
  adjustmentType: string;
  multiplier: number;
  baseAllocation: number;
  adjustedAllocation: number;
  reason: string;
  createdAt?: string;
}

export interface RiskWarning {
  id?: string;
  warningCode: string;
  severity: 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL';
  message: string;
  createdAt?: string;
}

export interface RiskTrace {
  symbol: string;
  entryPrice: number;
  stopPrice: number;
  stopDistance: number;
  stopDistancePct: number;
  stopMethod: StopMethod;
  sizingMethod: PositionSizingMethod;
  unconstrainedAllocation: number;
  constrainedAllocation: number;
  finalSuggestedAllocation: number;
  finalRecommendedQuantity: number;
  constraintLimits: Record<string, number>;
  limitingConstraint: string;
  adjustments: RiskAdjustment[];
  warnings: RiskWarning[];
}

export interface RiskAssessment {
  id: string;
  timestamp: string;
  informationAvailableAt: string;
  calculatedAt: string;
  portfolioId?: string;
  riskProfileId?: string;
  signalId?: string;
  symbol: string;
  signalType: string;
  signalScore: number;
  signalConfidence: number;
  suggestedAllocation: number;
  maximumAllocation: number;
  recommendedQuantity: number;
  entryPrice: number;
  stopPrice: number;
  targetPrice?: number;
  stopDistance: number;
  stopDistancePct: number;
  stopMethod: StopMethod;
  positionRiskAmount: number;
  positionRiskPercent: number;
  estimatedDownside: number;
  portfolioValue: number;
  remainingRiskBudget: number;
  portfolioVolatility?: number;
  securityVolatility: number;
  expectedVolatility?: number;
  maxCorrelation?: number;
  sectorExposureAfterTrade?: number;
  currentDrawdown: number;
  riskRewardRatio?: number;
  riskDecision: RiskDecision;
  riskLevel: RiskLevel;
  riskTrace?: RiskTrace;
  limitingConstraints?: Record<string, number>;
  riskWarnings?: RiskWarning[];
  adjustments?: RiskAdjustment[];
  reasoning: string;
  dataQualityStatus: string;
  riskEngineVersion: string;
  riskProfileVersion: string;
  signalVersion?: string;
  dataVersion: string;
  createdAt: string;
}

export type TradingHorizon = 'SHORT_TERM' | 'MEDIUM_TERM' | 'LONG_TERM';
export type HorizonOutlook = 'BULLISH' | 'BEARISH' | 'NEUTRAL';

export interface HorizonPrediction {
  id?: string;
  predictionId: string;
  symbol: string;
  predictionTimestamp: string;
  informationAvailableAt: string;
  calculatedAt: string;
  horizon: TradingHorizon;
  horizonPeriod: string;
  expectedReturn?: number;
  probabilityPositive?: number;
  probabilityNegative?: number;
  predictedClass?: number;
  expectedVolatility?: number;
  expectedDrawdown?: number;
  relativeReturn?: number;
  confidence: number;
  outlook: HorizonOutlook;
  featureContributions?: Record<string, number>;
  modelVersion: string;
  featureSetVersion: string;
  targetSetVersion: string;
  dataVersion?: string;
}

export interface HorizonConflict {
  id?: string;
  symbol: string;
  timestamp: string;
  shortTermOutlook: HorizonOutlook;
  mediumTermOutlook: HorizonOutlook;
  longTermOutlook: HorizonOutlook;
  conflictDetected: boolean;
  conflictSeverity: 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH';
  explanation: string;
}

export interface CrossHorizonView {
  symbol: string;
  asOf: string;
  shortTerm?: HorizonPrediction;
  mediumTerm?: HorizonPrediction;
  longTerm?: HorizonPrediction;
  conflict?: HorizonConflict;
  shortTermStatus: string;
  mediumTermStatus: string;
  longTermStatus: string;
  modelVersions?: Record<string, string>;
}

// Backtesting Types (Part 16)
export interface BacktestConfig {
  id?: string;
  name: string;
  description?: string;
  horizon: string;
  universeType: string;
  symbols: string[];
  startDate: string;
  endDate: string;
  initialCapital: number;
  cashBufferPct: number;
  rebalanceFrequency: string;
  executionTiming: string;
  costModelType: 'ZERO' | 'FIXED_BPS' | 'REALISTIC_INDIAN';
  slippageModelType: 'NONE' | 'FIXED_BPS' | 'SPREAD_AND_VOLUME';
  brokerageBps: number;
  sttDeliveryBps: number;
  sttIntradayBps: number;
  exchangeChargesBps: number;
  gstRate: number;
  stampDutyBps: number;
  slippageBps: number;
  maxPositionWeight: number;
  maxSectorWeight: number;
  maxDrawdownLimit: number;
  benchmarkSymbol: string;
  version: string;
}

export interface BacktestRun {
  id: string;
  configId: string;
  name: string;
  status: 'CREATED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  engineVersion: string;
  startDate: string;
  endDate: string;
  totalBarsProcessed: number;
  totalTradesCount: number;
  initialCapital: number;
  finalEquity?: number;
  totalNetPnl?: number;
  totalFeesPaid?: number;
  totalSlippagePaid?: number;
  totalDividendsReceived?: number;
  errorMessage?: string;
  executionDurationMs?: number;
  dataQualityTrustLevel: 'PRODUCTION_READY' | 'DEGRADED' | 'REJECTED';
  dataQualityReport?: Record<string, any>;
  createdAt: string;
  completedAt?: string;
}

export interface BacktestTrade {
  id: string;
  runId: string;
  symbol: string;
  side: 'LONG' | 'SHORT';
  quantity: number;
  entryOrderId?: string;
  exitOrderId?: string;
  entryTimestamp: string;
  exitTimestamp: string;
  entryPrice: number;
  exitPrice: number;
  grossPnl: number;
  netPnl: number;
  returnPct: number;
  totalFees: number;
  totalSlippage: number;
  holdingPeriodDays: number;
  exitReason: 'SIGNAL' | 'STOP_LOSS' | 'TAKE_PROFIT' | 'MAX_HOLDING_TIME' | 'REBALANCE' | 'RISK_CIRCUIT' | 'FORCE_CLOSE_END';
  maxFavorableExcursion?: number;
  maxAdverseExcursion?: number;
  regimeAtEntry?: string;
  regimeAtExit?: string;
}

export interface BacktestEquityCurvePoint {
  pointDate: string;
  strategyEquity: number;
  strategyReturnPct: number;
  strategyDrawdownPct: number;
  buyAndHoldEquity: number;
  buyAndHoldReturnPct: number;
  benchmarkEquity: number;
  benchmarkReturnPct: number;
}

export interface BacktestPerformanceMetrics {
  totalReturnPct: number;
  cagr: number;
  annualizedVolatility: number;
  sharpeRatio: number;
  sortinoRatio: number;
  maxDrawdownPct: number;
  maxDrawdownDurationDays: number;
  calmarRatio: number;
  winRatePct: number;
  profitFactor: number;
  averageTradeReturnPct: number;
  averageWinReturnPct: number;
  averageLossReturnPct: number;
  winLossRatio: number;
  totalTradesCount: number;
  winningTradesCount: number;
  losingTradesCount: number;
  annualizedTurnover: number;
  betaToBenchmark?: number;
  alphaToBenchmark?: number;
  informationRatio?: number;
  subperiodMetrics?: Record<string, any>;
  regimeBreakdownMetrics?: Record<string, any>;
  sectorBreakdownMetrics?: Record<string, any>;
}

export interface BenchmarkComparison {
  benchmarkSymbol: string;
  strategyTotalReturn: number;
  benchmarkTotalReturn: number;
  strategyCagr: number;
  benchmarkCagr: number;
  strategySharpe: number;
  benchmarkSharpe: number;
  strategyMaxDd: number;
  benchmarkMaxDd: number;
  alpha: number;
  beta: number;
  trackingError: number;
  informationRatio: number;
}

export interface BacktestRejectedSignal {
  id: string;
  runId: string;
  symbol: string;
  signalTimestamp: string;
  signalType: string;
  signalStrength: number;
  rejectionReason: string;
  details?: string;
}

export interface BacktestResult {
  runId: string;
  config?: BacktestConfig;
  run?: BacktestRun;
  metrics?: BacktestPerformanceMetrics;
  benchmarkComparison?: BenchmarkComparison;
  equityCurve: BacktestEquityCurvePoint[];
  trades: BacktestTrade[];
  snapshots?: any[];
  rejectedSignals?: BacktestRejectedSignal[];
}

// ==========================================
// PART 17: Production Paper Trading Engine
// ==========================================

export interface PaperTradingSession {
  id: string;
  name: string;
  executionMode: 'PAPER_TRADING' | 'BACKTEST';
  status: 'CREATED' | 'STARTING' | 'RUNNING' | 'PAUSED' | 'DATA_DEGRADED' | 'DISCONNECTED' | 'STOPPED' | 'FAILED';
  clockType: 'LIVE_CLOCK' | 'REPLAY_CLOCK';
  dataProvider: string;
  dataFreshnessStatus: 'REAL_TIME' | 'DELAYED' | 'STALE' | 'NOT_AVAILABLE' | 'UNKNOWN';
  startTime?: string;
  endTime?: string;
  configurationVersion: string;
  engineVersion: string;
  totalDecisionsCount: number;
  totalOrdersCount: number;
  totalFillsCount: number;
  errorMessage?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaperPortfolio {
  id: string;
  sessionId?: string;
  name: string;
  horizon: 'SHORT_TERM' | 'MEDIUM_TERM' | 'LONG_TERM';
  riskProfileId?: string;
  currency: string;
  initialVirtualCapital: number;
  cashBalance: number;
  availableCash: number;
  reservedCash: number;
  investedValue: number;
  totalPortfolioValue: number;
  peakPortfolioValue: number;
  currentDrawdownPct: number;
  maxDrawdownPct: number;
  grossExposure: number;
  netExposure: number;
  leverage: number;
  totalRealizedPnl: number;
  totalUnrealizedPnl: number;
  totalFeesPaid: number;
  totalSlippagePaid: number;
  totalDividendsReceived: number;
  status: 'ACTIVE' | 'PAUSED' | 'CLOSED';
  createdAt: string;
  updatedAt: string;
}

export interface PaperPosition {
  id: string;
  portfolioId: string;
  symbol: string;
  instrumentId?: number;
  horizon: string;
  quantity: number;
  averageEntryPrice: number;
  currentMarketPrice: number;
  costBasis: number;
  marketValue: number;
  unrealizedPnl: number;
  unrealizedReturnPct: number;
  realizedPnl: number;
  portfolioWeight: number;
  stopPrice?: number;
  targetPrice?: number;
  stopMethod?: string;
  highestPriceSeen: number;
  lowestPriceSeen: number;
  entryTimestamp: string;
  lastUpdatedAt: string;
  signalId?: string;
  riskAssessmentId?: string;
  modelVersion?: string;
  isActive: boolean;
  createdAt: string;
}

export interface PaperTradingDecision {
  id: string;
  portfolioId: string;
  sessionId?: string;
  symbol: string;
  timestamp: string;
  horizon: string;
  decision: 'BUY' | 'HOLD' | 'SELL' | 'NO_TRADE';
  decisionReason: string;
  signalId?: string;
  signalVersion?: string;
  signalScore: number;
  signalConfidence: number;
  expectedReturn?: number;
  expectedVolatility?: number;
  predictedDirection?: string;
  predictedProbability?: number;
  predictionId?: string;
  modelVersion?: string;
  riskAssessmentId?: string;
  riskEngineVersion?: string;
  suggestedAllocation: number;
  maximumAllocation: number;
  recommendedQuantity: number;
  entryPrice: number;
  stopPrice?: number;
  targetPrice?: number;
  riskLevel: string;
  supportingEvidence?: string;
  opposingEvidence?: string;
  dataQualityStatus: string;
  dataVersion: string;
  featureVersion: string;
  informationAvailableAt: string;
  calculatedAt: string;
  status: string;
  createdAt: string;
}

export interface PaperOrder {
  id: string;
  decisionId?: string;
  portfolioId: string;
  sessionId?: string;
  symbol: string;
  side: 'BUY' | 'SELL';
  orderType: 'MARKET' | 'LIMIT' | 'STOP';
  quantity: number;
  requestedPrice: number;
  executedPrice?: number;
  signalTimestamp: string;
  orderSubmittedTimestamp: string;
  orderExecutedTimestamp?: string;
  status: 'PENDING' | 'FILLED' | 'REJECTED' | 'CANCELLED';
  rejectionReason?: string;
  slippageBps?: number;
  slippageAmount?: number;
  feesAmount?: number;
  createdAt: string;
}

export interface PaperFill {
  id: string;
  orderId: string;
  portfolioId: string;
  symbol: string;
  side: 'BUY' | 'SELL';
  quantity: number;
  requestedPrice: number;
  fillPrice: number;
  slippageBps: number;
  slippageAmount: number;
  brokerage: number;
  stt: number;
  exchangeCharges: number;
  gst: number;
  stampDuty: number;
  totalFees: number;
  executionTimestamp: string;
  createdAt: string;
}

export interface PaperLedger {
  id: string;
  portfolioId: string;
  sessionId?: string;
  transactionTimestamp: string;
  eventType: string;
  symbol?: string;
  amount: number;
  cashBalanceBefore: number;
  cashBalanceAfter: number;
  description: string;
  referenceId?: string;
  createdAt: string;
}

export interface PaperEquityCurve {
  id: string;
  portfolioId: string;
  snapshotTimestamp: string;
  portfolioValue: number;
  cashBalance: number;
  investedValue: number;
  dailyReturnPct: number;
  cumulativeReturnPct: number;
  drawdownPct: number;
  createdAt: string;
}

export interface PaperSignalOutcome {
  id: string;
  decisionId: string;
  symbol: string;
  horizon: string;
  signalTimestamp: string;
  evaluationTimestamp: string;
  expectedDirection: string;
  realizedDirection: string;
  expectedReturn: number;
  realizedReturn: number;
  outcomeStatus: 'CORRECT_DIRECTION' | 'WRONG_DIRECTION' | 'PARTIAL' | 'EXPIRED' | 'NO_OUTCOME_YET' | 'INSUFFICIENT_DATA';
  attribution?: string;
  createdAt: string;
}

export interface PaperPredictionOutcome {
  id: string;
  decisionId: string;
  predictionId?: string;
  modelVersion: string;
  symbol: string;
  horizon: string;
  predictionTimestamp: string;
  evaluationTimestamp: string;
  expectedReturn: number;
  realizedReturn: number;
  predictionError: number;
  absoluteError: number;
  squaredError: number;
  expectedVolatility?: number;
  realizedVolatility?: number;
  isDirectionCorrect: boolean;
  createdAt: string;
}

export interface PaperModelMonitoring {
  id: string;
  modelVersion: string;
  horizon: string;
  evaluationWindowStart: string;
  evaluationWindowEnd: string;
  sampleSize: number;
  directionalAccuracy: number;
  mae: number;
  rmse: number;
  ic?: number;
  rankIc?: number;
  driftStatus: 'NORMAL' | 'WARNING' | 'DRIFT' | 'INSUFFICIENT_DATA';
  modelStatus: 'ACTIVE' | 'WARNING' | 'DEGRADED' | 'PAUSED' | 'RETIRED';
  evaluatedAt: string;
  createdAt: string;
}

export interface LiveDataHealth {
  id: string;
  provider: string;
  connectionStatus: 'HEALTHY' | 'DEGRADED' | 'STALE' | 'DISCONNECTED' | 'UNAVAILABLE';
  dataFreshnessStatus: 'REAL_TIME' | 'DELAYED' | 'STALE' | 'NOT_AVAILABLE' | 'UNKNOWN';
  lastSuccessfulUpdate?: string;
  lastMarketTimestamp?: string;
  latencyMs?: number;
  dataAgeSeconds?: number;
  errorCount: number;
  rateLimitStatus?: string;
  universeCoveragePct?: number;
  checkedAt: string;
}

export interface PaperTradingHealthReport {
  overallStatus: 'HEALTHY' | 'DEGRADED' | 'BLOCKED';
  executionMode: 'PAPER_TRADING' | 'BACKTEST';
  liveDataAvailable: boolean;
  liveDataProvider: string;
  subsystemStatus: Record<string, string>;
  message: string;
  evaluatedAt: string;
}

// ==========================================
// PART 19: Production Monitoring & Reliability
// ==========================================

export interface SystemOverviewHealth {
  overallSystemStatus: 'HEALTHY' | 'DEGRADED' | 'WARNING' | 'CRITICAL' | 'UNKNOWN' | 'UNAVAILABLE' | 'NOT_CONFIGURED';
  subsystemStatus: Record<string, string>;
  activeAlertsCount: number;
  criticalAlertsCount: number;
  averageApiLatencyMs: number;
  databasePoolUsagePct: number;
  liveTradingKillSwitchActive: boolean;
  evaluatedAt: string;
}

export interface MonitoringHealthCheck {
  id: string;
  component: string;
  status: string;
  latencyMs?: number;
  message?: string;
  checkedAt: string;
}

export interface MonitoringAlert {
  id: string;
  ruleId: string;
  ruleName: string;
  alertType: string;
  component: string;
  severity: string;
  status: 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED' | 'SUPPRESSED';
  observedValue?: string;
  thresholdValue?: string;
  message: string;
  runbookRef?: string;
  acknowledgedBy?: string;
  acknowledgedAt?: string;
  resolvedAt?: string;
  createdAt: string;
}

export interface MonitoringIncident {
  id: string;
  incidentNumber: string;
  title: string;
  severity: string;
  status: string;
  affectedComponents: string;
  rootCause?: string;
  createdAt: string;
  resolvedAt?: string;
}

export interface ProviderHealthDTO {
  id: string;
  provider: string;
  connectionStatus: string;
  dataFreshnessStatus: string;
  lastSuccessfulUpdate?: string;
  lastMarketTimestamp?: string;
  latencyMs?: number;
  dataAgeSeconds?: number;
  consecutiveFailures: number;
  healthScore: number;
  universeCoveragePct: number;
  isFailingOver: boolean;
  fallbackProvider?: string;
  updatedAt: string;
}

export interface DataQualityEventDTO {
  id: string;
  provider: string;
  symbol?: string;
  eventType: string;
  severity: string;
  description: string;
  affectedRecordsCount: number;
  sourceTimestamp?: string;
  availableTimestamp?: string;
  detectedAt: string;
}

export interface FeatureDriftEventDTO {
  id: string;
  featureName: string;
  metricType: string;
  observedValue: number;
  threshold: number;
  driftStatus: string;
  sampleSize: number;
  referenceWindow?: string;
  evaluationWindow?: string;
  detectedAt: string;
}

export interface SignalAnomalyEventDTO {
  id: string;
  signalType: string;
  anomalyCategory: string;
  observedRate: number;
  expectedRate: number;
  affectedSector?: string;
  severity: string;
  description: string;
  detectedAt: string;
}

export interface MonitoringRuleDTO {
  id: string;
  ruleVersion: string;
  name: string;
  targetComponent: string;
  metricName: string;
  conditionOperator: string;
  thresholdValue: number;
  windowSeconds: number;
  cooldownSeconds: number;
  severity: string;
  isEnabled: boolean;
  runbookRef?: string;
  updatedAt: string;
}

// ==========================================
// PART 20: Broker Integration & Live Trading
// ==========================================

export interface BrokerAccount {
  id: string;
  brokerName: 'ZERODHA' | 'UPSTOX' | 'DHAN' | 'ICICI_DIRECT' | 'MOCK';
  accountId: string;
  accountName: string;
  status: 'CONNECTED' | 'AUTHENTICATED' | 'DISCONNECTED' | 'ERROR';
  authExpiry?: string;
  cashBalance: number;
  collateralBalance: number;
  marginAvailable: number;
  marginUsed: number;
  createdAt: string;
  updatedAt: string;
}

export interface LiveOrder {
  id: string;
  brokerOrderId?: string;
  symbol: string;
  exchange: 'NSE' | 'BSE';
  side: 'BUY' | 'SELL';
  orderType: 'MARKET' | 'LIMIT' | 'STOP_LOSS';
  productType: 'CNC' | 'MIS' | 'NRML';
  quantity: number;
  limitPrice?: number;
  stopPrice?: number;
  executedQuantity: number;
  averagePrice?: number;
  status: 'PENDING_APPROVAL' | 'SUBMITTED' | 'OPEN' | 'TRIGGER_PENDING' | 'COMPLETE' | 'REJECTED' | 'CANCELLED';
  rejectionReason?: string;
  orderPlacedAt: string;
  orderExecutedAt?: string;
  manualConfirmationBy?: string;
  manualConfirmationAt?: string;
  signalId?: string;
  riskAssessmentId?: string;
  checksumVerified: boolean;
}

export interface LivePosition {
  id: string;
  brokerAccountId: string;
  symbol: string;
  exchange: string;
  productType: string;
  quantity: number;
  buyAveragePrice: number;
  sellAveragePrice: number;
  lastPrice: number;
  pnl: number;
  pnlPct: number;
  unrealizedPnl: number;
  realizedPnl: number;
  lastSyncedAt: string;
}

export interface TradingSafetyLock {
  id: string;
  isLiveTradingEnabled: boolean;
  isEmergencyKillSwitchActive: boolean;
  reason?: string;
  lockedBy?: string;
  lockedAt?: string;
  requireManualConfirmation: boolean;
  maxSingleOrderValue: number;
  maxDailyLossLimit: number;
  currentDailyLoss: number;
  updatedAt: string;
}

export interface LivePortfolioReconciliation {
  id: string;
  reconciliationTimestamp: string;
  brokerAccountId: string;
  totalInternalPositions: number;
  totalBrokerPositions: number;
  matchedPositions: number;
  mismatchedPositions: number;
  status: 'MATCHED' | 'DISCREPANCY_DETECTED' | 'CRITICAL_MISMATCH';
  discrepancyDetails?: string;
  resolved: boolean;
}

export interface LiveOrderPreview {
  symbol: string;
  exchange: string;
  side: 'BUY' | 'SELL';
  orderType: 'MARKET' | 'LIMIT';
  quantity: number;
  requestedPrice: number;
  estimatedValue: number;
  estimatedBrokerage: number;
  estimatedStt: number;
  estimatedTotalCharges: number;
  estimatedMarginRequired: number;
  riskCheckPassed: boolean;
  riskWarnings: string[];
  killSwitchActive: boolean;
}
