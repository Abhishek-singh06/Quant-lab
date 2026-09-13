export interface TerminalIndexSummary {
  symbol: string;
  name: string;
  lastPrice: number;
  change: number;
  changePercent: number;
  timestamp: string;
  source: string;
  status: string;
}

export interface SectorSummary {
  sectorName: string;
  performance1D: number;
  advancingCount: number;
  decliningCount: number;
  topGainer: string;
  topLoser: string;
}

export interface TerminalOverview {
  asOf: string;
  provider: string;
  providerStatus: string;
  nseMarketStatus: {
    exchange: string;
    marketState: string;
    isOpen: boolean;
    sessionName: string;
    timestamp: string;
    source: string;
  };
  globalRegime?: {
    timestamp: string;
    regimeLabel: string;
    compositeScore: number;
    equityScore: number;
    volatilityScore: number;
    ratesScore: number;
    dollarScore: number;
    commodityScore: number;
    asiaScore: number;
    explanation: string;
    realTime: boolean;
  };
  majorIndices: TerminalIndexSummary[];
  sectors: SectorSummary[];
  metadata: {
    pointInTimeGuaranteed: boolean;
    survivorshipBiasFree: boolean;
    marketCenter: string;
  };
}

export interface WatchlistDTO {
  id: number;
  name: string;
  userId: string;
  description?: string;
  symbols: string[];
  createdAt: string;
  updatedAt: string;
  itemCount: number;
}

export interface InstrumentSearchResult {
  symbol: string;
  name: string;
  exchange: string;
  sector: string;
  assetClass: string;
  isin?: string;
}

export interface CorporateEventDTO {
  id: number;
  symbol: string;
  eventType: string;
  eventDate: string;
  headline: string;
  details?: string;
  impactScore?: number;
  source: string;
  informationAvailableAt: string;
}

export interface NewsArticleDTO {
  id: number;
  headline: string;
  summary?: string;
  source: string;
  url?: string;
  publishedAt: string;
  informationAvailableAt: string;
  sentimentScore?: number;
  relevanceScore?: number;
}
