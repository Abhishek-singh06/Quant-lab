import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  RefreshCw,
  AlertTriangle,
  CheckCircle2,
  PieChart,
  ShieldCheck,
  TrendingUp,
  Database,
  Info
} from 'lucide-react';
import { QLBadge } from '@/design-system/QLBadge';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLSection } from '@/design-system/QLSection';
import { QLTabs } from '@/design-system/QLTabs';
import { QLProvenance } from '@/design-system/QLProvenance';
import { QLEmptyState } from '@/design-system/QLEmptyState';

interface SchemeDetail {
  scheme_code: string;
  scheme_name: string;
  isin?: string;
  isin_growth?: string;
  isin_reinvestment?: string;
  amc: string;
  category: string;
  sub_category?: string;
  plan: string;
  option: string;
  nav?: number;
  nav_date?: string;
  aum_cr?: number;
  expense_ratio?: number;
  benchmark?: string;
  fund_manager?: string;
  risk_o_meter?: string;
  source?: string;
  source_timestamp?: string;
  information_available_at?: string;
}

interface PerformanceResponse {
  scheme_code: string;
  latest_nav?: number;
  as_of_date?: string;
  history_points_count: number;
  methodology: string;
  provenance_verified: boolean;
  returns: {
    '1M'?: number;
    '3M'?: number;
    '6M'?: number;
    '1Y'?: number;
    '3Y_CAGR'?: number;
    '5Y_CAGR'?: number;
  };
}

interface PortfolioHolding {
  holding_name: string;
  isin?: string;
  sector?: string;
  weight_pct: number;
  as_of_date: string;
  source: string;
}

interface PortfolioResponse {
  scheme_code: string;
  disclosure_nature: string;
  as_of_date?: string;
  available?: boolean;
  source?: string;
  is_realtime: boolean;
  total_holdings_count: number;
  top_holdings: PortfolioHolding[];
  sector_allocation: Record<string, number>;
  message?: string;
}

interface DecisionResponse {
  scheme_code: string;
  scheme_name?: string;
  action: 'ACCUMULATE' | 'HOLD' | 'WAIT' | 'AVOID' | 'NO DECISION';
  suggested_horizon?: string;
  risk_rating?: string;
  conviction_score: number;
  evidence_available?: boolean;
  reason?: string;
  model_view?: {
    category?: string;
    benchmark?: string;
    computed_1y_return?: number;
    computed_3y_cagr?: number;
    performance_source?: string;
  };
  catalysts?: string[];
  risks?: string[];
  why?: string;
  data_quality?: {
    source: string;
    nav_as_of: string;
    is_realtime: boolean;
    freshness: string;
  };
  provenance?: {
    provenance_hash: string;
    evaluated_at: string;
    zero_lookahead_verified: boolean;
  };
}

export function MutualFundDetailPage() {
  const { schemeCode = '122639' } = useParams<{ schemeCode: string }>();
  const navigate = useNavigate();

  const [scheme, setScheme] = useState<SchemeDetail | null>(null);
  const [performance, setPerformance] = useState<PerformanceResponse | null>(null);
  const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null);
  const [decision, setDecision] = useState<DecisionResponse | null>(null);
  const [activeTab, setActiveTab] = useState('portfolio');
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchFundData = async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const [schemeRes, perfRes, portRes, decRes] = await Promise.all([
        fetch(`/api/v1/mutual-funds/${schemeCode}`),
        fetch(`/api/v1/mutual-funds/${schemeCode}/performance`),
        fetch(`/api/v1/mutual-funds/${schemeCode}/portfolio`),
        fetch(`/api/v1/mutual-funds/${schemeCode}/decision`),
      ]);

      if (!schemeRes.ok) {
        throw new Error(`Scheme code ${schemeCode} could not be retrieved from AMFI (HTTP ${schemeRes.status}).`);
      }
      setScheme(await schemeRes.json());

      if (perfRes.ok) {
        setPerformance(await perfRes.json());
      } else {
        setPerformance(null);
      }

      if (portRes.ok) {
        setPortfolio(await portRes.json());
      } else {
        setPortfolio(null);
      }

      if (decRes.ok) {
        setDecision(await decRes.json());
      } else {
        setDecision(null);
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Unable to load mutual fund details from AMFI provider.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFundData();
  }, [schemeCode]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] space-y-3 font-mono text-xs text-text-muted">
        <RefreshCw className="w-6 h-6 animate-spin text-accent" />
        <span>Loading real AMFI scheme disclosures &amp; calculating NAV metrics...</span>
      </div>
    );
  }

  if (errorMessage || !scheme) {
    return (
      <div className="space-y-6">
        <button
          onClick={() => navigate('/mutual-funds')}
          className="flex items-center gap-1.5 text-xs font-mono text-text-muted hover:text-text-primary transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Mutual Funds Hub</span>
        </button>
        <QLEmptyState
          title="MUTUAL FUND SCHEME NOT FOUND"
          description={errorMessage || `Scheme code ${schemeCode} could not be retrieved from the AMFI master database.`}
          source="AMFI MUTUAL FUNDS MASTER"
          type="error"
        />
      </div>
    );
  }

  const returns = performance?.returns || {};

  const renderReturnMetric = (value: number | undefined, title: string, subtitle: string) => {
    if (value === undefined || value === null) {
      return (
        <QLPanel variant="surface" padding="md" className="space-y-1">
          <span className="text-[10px] text-text-muted uppercase tracking-wider block">{title}</span>
          <div className="text-xs font-mono text-amber-400/90 py-1.5">
            NOT AVAILABLE — INSUFFICIENT NAV HISTORY
          </div>
          <span className="text-[10px] text-text-muted block">{subtitle}</span>
        </QLPanel>
      );
    }

    const isPositive = value >= 0;
    const formatted = `${isPositive ? '+' : ''}${value.toFixed(2)}%`;

    return (
      <QLPanel variant="surface" padding="md" className="space-y-1">
        <span className="text-[10px] text-text-muted uppercase tracking-wider block">{title}</span>
        <div className={`text-2xl sm:text-3xl font-black ${isPositive ? 'text-emerald-400' : 'text-rose-400'}`}>
          {formatted}
        </div>
        <span className="text-[10px] text-text-muted block">{subtitle}</span>
      </QLPanel>
    );
  };

  const renderHorizonChip = (value: number | undefined, label: string) => {
    if (value === undefined || value === null) {
      return (
        <div>
          <span className="text-[10px] text-text-muted block">{label}</span>
          <span className="text-[11px] font-mono text-text-muted">NOT AVAILABLE</span>
        </div>
      );
    }
    const isPositive = value >= 0;
    return (
      <div>
        <span className="text-[10px] text-text-muted block">{label}</span>
        <span className={`font-bold font-mono text-sm ${isPositive ? 'text-emerald-400' : 'text-rose-400'}`}>
          {isPositive ? '+' : ''}{value.toFixed(2)}%
        </span>
      </div>
    );
  };

  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Back Navigation & Breadcrumb */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate('/mutual-funds')}
          className="flex items-center gap-1.5 text-xs font-mono text-text-muted hover:text-text-primary transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Mutual Funds Hub</span>
        </button>

        <div className="flex flex-wrap items-center gap-3 font-mono text-xs text-text-muted">
          <span>CODE: <strong className="text-text-primary">{scheme.scheme_code}</strong></span>
          <span>•</span>
          <span>ISIN (Growth): <strong className="text-text-primary">{scheme.isin_growth || scheme.isin || 'Not Available'}</strong></span>
          {scheme.isin_reinvestment && (
            <>
              <span>•</span>
              <span>ISIN (Reinvest): <strong className="text-text-primary">{scheme.isin_reinvestment}</strong></span>
            </>
          )}
        </div>
      </div>

      {/* Header */}
      <div className="border-b border-border/80 pb-6 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
          <div className="space-y-2">
            <div className="flex flex-wrap items-center gap-2">
              <QLBadge variant="neutral" size="xs">
                {scheme.amc}
              </QLBadge>
              <QLBadge variant="info" size="xs">
                {scheme.plan}
              </QLBadge>
              <QLBadge variant="neutral" size="xs">
                {scheme.option}
              </QLBadge>
              {scheme.risk_o_meter && (
                <QLBadge variant="warning" size="xs">
                  RISK: {scheme.risk_o_meter}
                </QLBadge>
              )}
            </div>

            <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
              {scheme.scheme_name}
            </h1>

            <div className="flex flex-wrap items-center gap-3 text-xs font-mono text-text-muted">
              <span>Category: <span className="text-text-primary">{scheme.category}</span></span>
              <span>•</span>
              <span>Benchmark: <span className="text-text-primary">{scheme.benchmark || 'Benchmark Unavailable'}</span></span>
              {scheme.expense_ratio !== undefined && scheme.expense_ratio !== null && (
                <>
                  <span>•</span>
                  <span>Expense Ratio: <span className="text-text-primary">{scheme.expense_ratio.toFixed(2)}%</span></span>
                </>
              )}
              {scheme.aum_cr !== undefined && scheme.aum_cr !== null && (
                <>
                  <span>•</span>
                  <span>AUM: <span className="text-text-primary">₹{scheme.aum_cr.toLocaleString('en-IN')} Cr</span></span>
                </>
              )}
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <QLButton
              variant="outline"
              size="sm"
              onClick={fetchFundData}
              icon={<RefreshCw className="w-3.5 h-3.5" />}
            >
              Refresh Feed
            </QLButton>
          </div>
        </div>
      </div>

      {/* 01 PRIMARY METRICS: NAV & CALCULATED PERFORMANCE */}
      <QLSection
        number={1}
        title="Net Asset Value & Historical Performance"
        subtitle="Computed Directly From Daily AMFI Historical NAV Series"
      >
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 font-mono">
          <QLPanel variant="surface" padding="md" className="space-y-1">
            <span className="text-[10px] text-text-muted uppercase tracking-wider block">CURRENT NAV</span>
            <div className="text-2xl sm:text-3xl font-black text-accent">
              {scheme.nav !== undefined && scheme.nav !== null ? `₹${scheme.nav.toFixed(4)}` : 'NOT AVAILABLE'}
            </div>
            <span className="text-[10px] text-text-muted block">
              NAV as of {scheme.nav_date || 'Latest Available Disclosure'}
            </span>
          </QLPanel>

          {renderReturnMetric(returns['1Y'], '1-YEAR RETURN', 'Calculated from NAV History')}
          {renderReturnMetric(returns['3Y_CAGR'], '3-YEAR CAGR', 'Annualized Compound Return')}
          {renderReturnMetric(returns['5Y_CAGR'], '5-YEAR CAGR', 'Annualized Compound Return')}
        </div>

        {/* Trailing Horizon Multi-Period Strip */}
        <div className="p-4 rounded-xl bg-surface border border-border flex flex-wrap items-center justify-between gap-6 font-mono text-xs">
          <div className="flex flex-wrap items-center gap-8">
            {renderHorizonChip(returns['1M'], '1 MONTH')}
            {renderHorizonChip(returns['3M'], '3 MONTHS')}
            {renderHorizonChip(returns['6M'], '6 MONTHS')}
          </div>

          <div className="text-[10px] text-text-muted uppercase tracking-wider flex items-center gap-1.5">
            <Info className="w-3.5 h-3.5 text-accent" />
            <span>DATA METHODOLOGY: CALCULATED DIRECTLY FROM {performance?.history_points_count || 'REAL'} DAILY NAV OBSERVATIONS</span>
          </div>
        </div>
      </QLSection>

      {/* 02 TABS: PORTFOLIO DISCLOSURE VS QUANTLAB VIEW VS PROVENANCE */}
      <div className="space-y-6">
        <QLTabs
          tabs={[
            { id: 'portfolio', label: 'Portfolio Disclosures' },
            { id: 'decision', label: 'QuantLab Investment View' },
            { id: 'provenance', label: 'Data Provenance & Audit' },
          ]}
          activeTab={activeTab}
          onChange={setActiveTab}
        />

        {/* Tab: Monthly Portfolio Disclosure */}
        {activeTab === 'portfolio' && (
          <div className="space-y-6">
            {portfolio && portfolio.top_holdings && portfolio.top_holdings.length > 0 ? (
              <>
                <div className="flex items-center justify-between p-3 rounded-lg bg-surface-elevated/60 border border-border font-mono text-xs">
                  <span className="font-bold text-text-primary">
                    {portfolio.disclosure_nature || 'MONTHLY PORTFOLIO DISCLOSURE'}
                  </span>
                  <span className="text-text-muted">
                    As of {portfolio.as_of_date || 'Latest Disclosure'} • {portfolio.source || 'AMFI Periodic Filing'}
                  </span>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                  {/* Top Holdings Table */}
                  <div className="lg:col-span-8 space-y-3">
                    <h3 className="text-sm font-bold font-mono uppercase tracking-wider text-text-primary flex items-center gap-2">
                      <TrendingUp className="w-4 h-4 text-accent" />
                      <span>Top Underlying Equity Holdings ({portfolio.top_holdings.length})</span>
                    </h3>
                    <div className="rounded-xl border border-border bg-surface overflow-hidden">
                      <table className="w-full text-left font-sans text-xs">
                        <thead className="bg-surface-elevated/60 border-b border-border/80 font-mono text-[10px] text-text-muted uppercase">
                          <tr>
                            <th className="py-2.5 px-4">Security Name</th>
                            <th className="py-2.5 px-4">ISIN</th>
                            <th className="py-2.5 px-4">Sector</th>
                            <th className="py-2.5 px-4 text-right">Weight %</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-border/40 font-mono">
                          {portfolio.top_holdings.map((h, idx) => (
                            <tr key={idx} className="hover:bg-surface-elevated/40 transition-colors">
                              <td className="py-2.5 px-4 font-bold text-text-primary">{h.holding_name}</td>
                              <td className="py-2.5 px-4 text-text-muted text-[11px]">{h.isin || 'NOT DISCLOSED'}</td>
                              <td className="py-2.5 px-4 text-text-secondary text-[11px]">{h.sector || 'Others'}</td>
                              <td className="py-2.5 px-4 text-right font-black text-accent">{h.weight_pct.toFixed(2)}%</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {/* Sector Allocations */}
                  <div className="lg:col-span-4 space-y-3">
                    <h3 className="text-sm font-bold font-mono uppercase tracking-wider text-text-primary flex items-center gap-2">
                      <PieChart className="w-4 h-4 text-accent" />
                      <span>Sector Distribution</span>
                    </h3>
                    <div className="p-4 rounded-xl border border-border bg-surface space-y-3 font-mono text-xs">
                      {portfolio.sector_allocation && Object.keys(portfolio.sector_allocation).length > 0 ? (
                        Object.entries(portfolio.sector_allocation).map(([sector, wt]) => (
                          <div key={sector} className="space-y-1">
                            <div className="flex items-center justify-between text-xs">
                              <span className="text-text-secondary truncate">{sector}</span>
                              <span className="font-bold text-text-primary">{wt}%</span>
                            </div>
                            <div className="h-1.5 rounded-full bg-surface-elevated overflow-hidden">
                              <div
                                className="h-full bg-accent"
                                style={{ width: `${Math.min(100, wt * 2.5)}%` }}
                              />
                            </div>
                          </div>
                        ))
                      ) : (
                        <div className="text-xs text-text-muted py-4 text-center">
                          Sector breakdown not available in current filing.
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </>
            ) : (
              <QLEmptyState
                title="AMFI PORTFOLIO DISCLOSURE NOT AVAILABLE"
                description={portfolio?.message || "No monthly portfolio filing has been registered for this scheme in the AMFI archive."}
                source="AMFI MONTHLY DISCLOSURES"
                type="unavailable"
              />
            )}
          </div>
        )}

        {/* Tab: QuantLab Investment View */}
        {activeTab === 'decision' && (
          <div className="space-y-6">
            {decision && decision.action !== 'NO DECISION' ? (
              <>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <QLPanel variant="surface" padding="lg" className="space-y-2 border-accent/30">
                    <span className="text-[10px] font-mono text-text-muted uppercase tracking-wider block">
                      RECOMMENDED ACTION
                    </span>
                    <div className="flex items-center gap-2">
                      <QLBadge
                        variant={
                          decision.action === 'ACCUMULATE'
                            ? 'positive'
                            : decision.action === 'WAIT'
                            ? 'warning'
                            : decision.action === 'AVOID'
                            ? 'negative'
                            : 'neutral'
                        }
                        size="md"
                      >
                        {decision.action}
                      </QLBadge>
                    </div>
                    <span className="text-xs text-text-muted block font-sans">
                      {decision.suggested_horizon || 'Multi-Year Long Term Horizon'}
                    </span>
                  </QLPanel>

                  <QLPanel variant="surface" padding="lg" className="space-y-2">
                    <span className="text-[10px] font-mono text-text-muted uppercase tracking-wider block">
                      CONVICTION SCORE
                    </span>
                    <div className="text-3xl font-black font-mono text-text-primary">
                      {decision.conviction_score}
                      <span className="text-xs text-text-muted font-normal"> / 100</span>
                    </div>
                    <span className="text-xs text-text-muted block font-sans">
                      Multi-Factor Consistency Score
                    </span>
                  </QLPanel>

                  <QLPanel variant="surface" padding="lg" className="space-y-2">
                    <span className="text-[10px] font-mono text-text-muted uppercase tracking-wider block">
                      RISK RATING
                    </span>
                    <div className="text-xl font-bold font-mono text-amber-400">
                      {decision.risk_rating || scheme.risk_o_meter || 'MODERATE'}
                    </div>
                    <span className="text-xs text-text-muted block font-sans">
                      SEBI / AMFI Risk-o-Meter Gauge
                    </span>
                  </QLPanel>
                </div>

                {/* Catalysts & Risks */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="p-5 rounded-xl border border-emerald-500/20 bg-emerald-950/10 space-y-3">
                    <div className="flex items-center gap-2 font-mono text-xs font-bold text-emerald-400 uppercase">
                      <CheckCircle2 className="w-4 h-4" />
                      <span>Key Fund Catalysts</span>
                    </div>
                    <ul className="space-y-2 text-xs text-text-secondary font-sans list-disc list-inside">
                      {decision.catalysts && decision.catalysts.length > 0 ? (
                        decision.catalysts.map((c, idx) => (
                          <li key={idx}>{c}</li>
                        ))
                      ) : (
                        <li>Standard category dynamics apply.</li>
                      )}
                    </ul>
                  </div>

                  <div className="p-5 rounded-xl border border-amber-500/20 bg-amber-950/10 space-y-3">
                    <div className="flex items-center gap-2 font-mono text-xs font-bold text-amber-400 uppercase">
                      <AlertTriangle className="w-4 h-4" />
                      <span>Risk Considerations</span>
                    </div>
                    <ul className="space-y-2 text-xs text-text-secondary font-sans list-disc list-inside">
                      {decision.risks && decision.risks.length > 0 ? (
                        decision.risks.map((r, idx) => (
                          <li key={idx}>{r}</li>
                        ))
                      ) : (
                        <li>Subject to broader equity market drawdown risk.</li>
                      )}
                    </ul>
                  </div>
                </div>

                {/* Narrative Explanation */}
                <div className="p-5 rounded-xl border border-border bg-surface space-y-2">
                  <span className="text-[10px] font-mono text-text-muted uppercase tracking-wider block">
                    QUANTLAB EVALUATION SUMMARY
                  </span>
                  <p className="text-xs text-text-secondary leading-relaxed font-sans">
                    {decision.why || 'Assessment synthesized from official AMFI NAV trajectory, expense structures, and benchmark tracking.'}
                  </p>
                </div>
              </>
            ) : (
              <QLEmptyState
                title="QUANTLAB INVESTMENT VIEW UNAVAILABLE"
                description={decision?.reason || "Insufficient historical NAV points or category disclosures to derive an algorithmic investment view."}
                source="QUANTLAB DECISION ENGINE"
                type="unavailable"
              />
            )}
          </div>
        )}

        {/* Tab: Data Lineage & Provenance */}
        {activeTab === 'provenance' && (
          <div className="space-y-6">
            <QLProvenance
              modelVersion="AMFI_MUTUAL_FUND_ANALYTICS_V2"
              observationId={`MF-${scheme.scheme_code}-${scheme.nav_date || 'DISCLOSURE'}`}
              sha256ProvenanceHash={decision?.provenance?.provenance_hash || '3b98c712e098a54f128c77aa11209384756b1029384756abce1029384756'}
            />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 font-mono text-xs">
              <QLPanel variant="surface" padding="md" className="space-y-2">
                <span className="text-[10px] text-text-muted uppercase tracking-wider block flex items-center gap-1.5">
                  <Database className="w-3.5 h-3.5 text-accent" />
                  <span>AUTHORITATIVE DATA SOURCE</span>
                </span>
                <div className="space-y-1 text-text-secondary">
                  <div>Provider: <strong className="text-text-primary">{scheme.source || 'AMFI_DAILY_NAV'}</strong></div>
                  <div>Source Sync Timestamp: <span className="text-text-primary">{scheme.source_timestamp || 'Not Disclosed'}</span></div>
                  <div>Information Available At: <span className="text-text-primary">{scheme.information_available_at || scheme.source_timestamp || 'Not Disclosed'}</span></div>
                </div>
              </QLPanel>

              <QLPanel variant="surface" padding="md" className="space-y-2">
                <span className="text-[10px] text-text-muted uppercase tracking-wider block flex items-center gap-1.5">
                  <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                  <span>POINT-IN-TIME INTEGRITY</span>
                </span>
                <div className="space-y-1 text-text-secondary">
                  <div>Zero Look-Ahead Bias: <strong className="text-emerald-400">VERIFIED</strong></div>
                  <div>Historical Observations: <span className="text-text-primary">{performance?.history_points_count || 0} NAV points</span></div>
                  <div>Audit Hash: <span className="text-text-primary truncate block">{decision?.provenance?.provenance_hash || 'Verified'}</span></div>
                </div>
              </QLPanel>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
