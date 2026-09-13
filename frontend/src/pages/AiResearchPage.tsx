import { useState, useEffect } from 'react';
import { Search, Loader2, AlertCircle, FileText, TrendingUp, Sparkles } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import { ResearchReportView, type ResearchReportData } from '@/components/research/ResearchReportView';
import { RecommendationCard } from '@/components/dashboard/RecommendationCard';
import type { Signal } from '@/types/market';
import { api } from '@/lib/api';

const EXAMPLE_SYMBOLS = ['RELIANCE', 'TCS', 'HDFCBANK', 'INFY', 'ICICIBANK', 'ITC', 'BHARTIARTL', 'SBIN'];

export function AiResearchPage() {
  const [query, setQuery] = useState('RELIANCE');
  const [report, setReport] = useState<ResearchReportData | null>(null);
  const [signal, setSignal] = useState<Signal | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'report' | 'signal'>('report');

  const runAnalysis = (sym?: string) => {
    const target = (sym ?? query).toUpperCase().trim();
    if (!target) return;
    setLoading(true);
    setError(null);

    Promise.all([
      api.post<ResearchReportData>(`/v1/research/company/${target}`, {
        company_name: `${target} Ltd`,
        context_as_of: new Date().toISOString(),
      }).catch(() => null),
      api.get<Signal>(`/v1/signals/latest/${target}`).catch(() => null),
    ]).then(([rep, sig]) => {
      if (rep) {
        setReport(rep);
      } else {
        setError(`Unable to generate research report for ${target}. Please verify symbol and context parameters.`);
      }
      setSignal(sig);
    }).finally(() => setLoading(false));
  };

  useEffect(() => {
    runAnalysis('RELIANCE');
  }, []);

  return (
    <div className="space-y-6">
      <MockBanner />

      {/* Title & Desk Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-surface p-4 rounded-xl border border-border">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-extrabold gradient-text">Fundamental Intelligence & AI Research</h1>
            <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-accent/10 text-accent border border-accent/20 font-bold uppercase">
              Phase 10
            </span>
          </div>
          <p className="text-xs text-text-muted mt-1">
            20-Point Value Checklist · Multi-Agent Debate · Point-in-Time Audited Provenance
          </p>
        </div>

        {/* Search Input */}
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-2 rounded-lg border border-border bg-background px-3 py-1.5">
            <Search className="h-4 w-4 text-text-muted" />
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && runAnalysis()}
              placeholder="Enter symbol (e.g. TCS)..."
              className="bg-transparent text-xs text-text-primary placeholder:text-text-muted outline-none w-36 sm:w-48"
            />
          </div>

          <button
            onClick={() => runAnalysis()}
            disabled={loading || !query.trim()}
            className="flex items-center gap-1.5 rounded-lg bg-accent px-4 py-2 text-xs font-bold text-white hover:opacity-90 transition-opacity disabled:opacity-50"
          >
            {loading ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <Sparkles className="h-3.5 w-3.5" />}
            Analyze
          </button>
        </div>
      </div>

      {/* Quick Lookup Chips */}
      <div className="flex items-center gap-2 flex-wrap text-xs text-text-muted">
        <span>Universe Quick Lookup:</span>
        {EXAMPLE_SYMBOLS.map((sym) => (
          <button
            key={sym}
            onClick={() => {
              setQuery(sym);
              runAnalysis(sym);
            }}
            className={`rounded-full border px-3 py-1 text-xs font-semibold transition-colors ${
              sym.toUpperCase() === report?.symbol?.toUpperCase()
                ? 'bg-accent-muted text-accent border-accent/40'
                : 'border-border bg-surface text-text-secondary hover:bg-surface-elevated hover:text-text-primary'
            }`}
          >
            {sym}
          </button>
        ))}
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-lg border border-yellow-400/20 bg-yellow-400/10 px-4 py-3 text-xs text-yellow-400">
          <AlertCircle className="h-4 w-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Sub-view Navigation */}
      <div className="flex gap-2 border-b border-border pb-1">
        <button
          onClick={() => setActiveTab('report')}
          className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-xs font-bold transition-all ${
            activeTab === 'report'
              ? 'bg-accent text-white shadow-sm'
              : 'text-text-secondary hover:text-text-primary hover:bg-surface-elevated'
          }`}
        >
          <FileText className="h-4 w-4" />
          <span>Full Fundamental Research Report</span>
        </button>

        {signal && (
          <button
            onClick={() => setActiveTab('signal')}
            className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-xs font-bold transition-all ${
              activeTab === 'signal'
                ? 'bg-accent text-white shadow-sm'
                : 'text-text-secondary hover:text-text-primary hover:bg-surface-elevated'
            }`}
          >
            <TrendingUp className="h-4 w-4" />
            <span>Horizon Alpha Signal Card</span>
          </button>
        )}
      </div>

      {/* Main Content Area */}
      {loading && (
        <div className="rounded-xl border border-border bg-surface p-12 text-center text-xs text-text-muted space-y-3">
          <Loader2 className="h-8 w-8 animate-spin mx-auto text-accent" />
          <p className="font-semibold text-text-primary">Generating Point-in-Time Research Report for {query.toUpperCase()}...</p>
          <p className="text-[11px] text-text-muted">Evaluating 20-point checklist · Running multi-agent debate · Detecting conflicts</p>
        </div>
      )}

      {!loading && activeTab === 'report' && report && (
        <ResearchReportView report={report} />
      )}

      {!loading && activeTab === 'signal' && signal && (
        <div className="max-w-2xl">
          <RecommendationCard signal={signal} />
        </div>
      )}
    </div>
  );
}
