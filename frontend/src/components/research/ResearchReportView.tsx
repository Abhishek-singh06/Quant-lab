import { Star, CheckCircle2, Hash } from 'lucide-react';
import { ChecklistCard, type ChecklistItemData } from './ChecklistCard';
import { MultiAgentDebateCard, type AgentPerspectiveData } from './MultiAgentDebateCard';
import { ConflictDetectorCard, type ConflictItemData } from './ConflictDetectorCard';

export interface ResearchReportData {
  report_id: string;
  run_id: string;
  symbol: string;
  company_name: string;
  context_as_of: string;
  generated_at: string;
  business_quality_score: number;
  financial_health_score: number;
  valuation_score: number;
  risk_score: number;
  overall_score: number;
  checklist: ChecklistItemData[];
  checklist_passed: number;
  checklist_warnings: number;
  checklist_failed: number;
  checklist_unknown: number;
  agent_reviews: AgentPerspectiveData[];
  conflicts: ConflictItemData[];
  sections: Array<{
    section_id: string;
    title: string;
    content: string;
    claims: string[];
  }>;
  evidence_count: number;
  llm_provider: string;
  execution_mode: string;
  provenance_hash: string;
}

interface ResearchReportViewProps {
  report: ResearchReportData;
}

export function ResearchReportView({ report }: ResearchReportViewProps) {
  return (
    <div className="space-y-6">
      {/* Top Scorecard */}
      <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-border/60 pb-3">
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-black text-text-primary tracking-wide">
                {report.symbol} — {report.company_name}
              </h2>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-accent/10 text-accent border border-accent/20 font-bold">
                AUDITABLE REPORT
              </span>
            </div>
            <p className="text-xs text-text-muted mt-0.5 flex items-center gap-2">
              <span>Run ID: <strong className="text-text-secondary font-mono">{report.run_id}</strong></span>
              <span>·</span>
              <span>As of: <strong className="text-text-secondary">{new Date(report.context_as_of).toLocaleDateString('en-IN')}</strong></span>
            </p>
          </div>

          {/* Composite Score */}
          <div className="flex items-center gap-3 bg-surface-elevated rounded-xl p-3 border border-border/70">
            <div className="text-right">
              <span className="text-[10px] uppercase font-bold text-text-muted tracking-wider block">
                Composite Score
              </span>
              <span className="text-2xl font-black text-accent flex items-center justify-end">
                <Star className="h-5 w-5 fill-accent mr-1 inline" />
                {report.overall_score.toFixed(1)} <span className="text-xs text-text-muted font-normal">/ 5.0</span>
              </span>
            </div>
          </div>
        </div>

        {/* 4 Core Dimensions */}
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div className="p-3 rounded-lg border border-border bg-surface-elevated/40">
            <span className="text-[10px] text-text-muted uppercase font-bold tracking-wider block">Business Quality</span>
            <span className="text-lg font-bold text-text-primary mt-1 block">
              {report.business_quality_score.toFixed(1)} / 5.0
            </span>
            <span className="text-[10px] text-green-400">Moat & Reinvestment</span>
          </div>

          <div className="p-3 rounded-lg border border-border bg-surface-elevated/40">
            <span className="text-[10px] text-text-muted uppercase font-bold tracking-wider block">Financial Health</span>
            <span className="text-lg font-bold text-text-primary mt-1 block">
              {report.financial_health_score.toFixed(1)} / 5.0
            </span>
            <span className="text-[10px] text-blue-400">Solvency & Cash Flow</span>
          </div>

          <div className="p-3 rounded-lg border border-border bg-surface-elevated/40">
            <span className="text-[10px] text-text-muted uppercase font-bold tracking-wider block">Valuation Multiple</span>
            <span className="text-lg font-bold text-text-primary mt-1 block">
              {report.valuation_score.toFixed(1)} / 5.0
            </span>
            <span className="text-[10px] text-yellow-400">Margin of Safety</span>
          </div>

          <div className="p-3 rounded-lg border border-border bg-surface-elevated/40">
            <span className="text-[10px] text-text-muted uppercase font-bold tracking-wider block">Risk & Inversion</span>
            <span className="text-lg font-bold text-text-primary mt-1 block">
              {report.risk_score.toFixed(1)} / 5.0
            </span>
            <span className="text-[10px] text-purple-400">Downside Buffer</span>
          </div>
        </div>
      </div>

      {/* Detected Conflicts Alert (if any) */}
      <ConflictDetectorCard conflicts={report.conflicts} />

      {/* Multi-Agent Perspectives */}
      <MultiAgentDebateCard reviews={report.agent_reviews} />

      {/* 20-Point Checklist */}
      <ChecklistCard
        items={report.checklist}
        passedCount={report.checklist_passed}
        warningsCount={report.checklist_warnings}
        failedCount={report.checklist_failed}
        unknownCount={report.checklist_unknown}
      />

      {/* Structured Written Sections */}
      <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
        <h3 className="text-sm font-bold uppercase tracking-wider text-text-primary border-b border-border/60 pb-2.5">
          Detailed Investment Synthesis & Evidence Claims
        </h3>

        <div className="space-y-4">
          {report.sections.map((sec) => (
            <div key={sec.section_id} className="p-4 rounded-lg border border-border/70 bg-surface-elevated/30 space-y-2">
              <h4 className="text-xs font-bold text-text-primary">{sec.title}</h4>
              <p className="text-xs text-text-secondary leading-relaxed">{sec.content}</p>

              {sec.claims.length > 0 && (
                <div className="pt-2 border-t border-border/30">
                  <span className="text-[10px] font-semibold text-text-muted uppercase tracking-wider block mb-1">
                    Verified Ground-Truth Claims:
                  </span>
                  <ul className="space-y-1 text-[11px] text-text-secondary">
                    {sec.claims.map((clm, i) => (
                      <li key={i} className="flex items-start gap-1.5">
                        <CheckCircle2 className="h-3.5 w-3.5 text-accent shrink-0 mt-0.5" />
                        <span>{clm}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Provenance & Cryptographic Lineage Footer */}
      <div className="rounded-xl border border-border bg-surface p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-[11px] text-text-muted">
        <div className="flex items-center gap-2">
          <Hash className="h-4 w-4 text-accent" />
          <span>SHA-256 Provenance: <strong className="font-mono text-text-secondary">{report.provenance_hash.substring(0, 20)}...</strong></span>
        </div>
        <div className="flex items-center gap-3">
          <span>Provider: <strong className="text-text-secondary">{report.llm_provider}</strong></span>
          <span>Mode: <strong className="text-text-secondary">{report.execution_mode}</strong></span>
          <span>PIT Enforced: <strong className="text-green-400">YES</strong></span>
        </div>
      </div>
    </div>
  );
}
