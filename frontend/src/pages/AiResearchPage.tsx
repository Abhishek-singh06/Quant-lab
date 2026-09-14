import { useState, useEffect } from 'react';
import { Search, RefreshCw, AlertCircle, Sparkles } from 'lucide-react';
import { ResearchReportView, type ResearchReportData } from '@/components/research/ResearchReportView';
import { RecommendationCard } from '@/components/dashboard/RecommendationCard';
import type { Signal } from '@/types/market';
import { api } from '@/lib/api';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLTabs } from '@/design-system/QLTabs';

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
    <div className="space-y-10 pb-12 font-sans">
      {/* Editorial Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>EQUITY RESEARCH LABORATORY</span>
            <span>•</span>
            <span>POINT-IN-TIME AI DEBATE</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            AI FUNDAMENTAL RESEARCH
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            20-Point Value Checklist &bull; Multi-Agent Bull/Bear Debate &bull; Point-in-Time Audited Provenance
          </p>
        </div>

        {/* Search Input Bar */}
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-2 rounded-lg border border-border bg-[#0b0c12] px-3 py-2">
            <Search className="w-3.5 h-3.5 text-text-muted" />
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && runAnalysis()}
              placeholder="Enter symbol (e.g. TCS)..."
              className="bg-transparent text-xs text-text-primary placeholder:text-text-muted outline-none font-mono w-32 sm:w-44"
            />
          </div>

          <QLButton
            variant="primary"
            size="sm"
            onClick={() => runAnalysis()}
            disabled={loading || !query.trim()}
            loading={loading}
            icon={<Sparkles className="w-3.5 h-3.5" />}
          >
            Synthesize
          </QLButton>
        </div>
      </div>

      {/* Universe Quick Lookup */}
      <div className="flex items-center gap-2 flex-wrap text-xs font-mono text-text-muted">
        <span>SECURITY QUICK SELECT:</span>
        {EXAMPLE_SYMBOLS.map((sym) => (
          <button
            key={sym}
            onClick={() => {
              setQuery(sym);
              runAnalysis(sym);
            }}
            className={`rounded-md border px-2.5 py-1 text-xs font-semibold transition-colors cursor-pointer ${
              sym.toUpperCase() === report?.symbol?.toUpperCase()
                ? 'bg-accent-muted text-accent border-accent/40 font-bold'
                : 'border-border bg-surface text-text-secondary hover:bg-surface-elevated hover:text-text-primary'
            }`}
          >
            {sym}
          </button>
        ))}
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-lg border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-xs text-amber-300 font-mono">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Sub-view Navigation */}
      <QLTabs<'report' | 'signal'>
        activeTab={activeTab}
        onChange={(t) => setActiveTab(t)}
        variant="underline"
        tabs={[
          { id: 'report', label: 'Full Fundamental Research Report' },
          { id: 'signal', label: 'Horizon Alpha Signal Card' },
        ]}
      />

      {/* Main Content Area */}
      {loading && (
        <QLPanel variant="surface" padding="lg" className="text-center space-y-3 font-mono text-xs">
          <RefreshCw className="w-6 h-6 animate-spin mx-auto text-accent" />
          <p className="font-bold text-text-primary">Generating Point-in-Time Research for {query.toUpperCase()}...</p>
          <p className="text-[11px] text-text-muted">Evaluating 20-point checklist · Running multi-agent debate · Detecting conflicts</p>
        </QLPanel>
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
