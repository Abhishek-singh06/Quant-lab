import { Users, Star } from 'lucide-react';

export interface AgentPerspectiveData {
  agent_name: string;
  role: string;
  score: number;
  thesis: string;
  key_positives: string[];
  key_risks: string[];
  recommendation: string;
}

interface MultiAgentDebateCardProps {
  reviews: AgentPerspectiveData[];
}

export function MultiAgentDebateCard({ reviews }: MultiAgentDebateCardProps) {
  const getBadgeColor = (rec: string) => {
    switch (rec) {
      case 'FAVORABLE':
        return 'bg-green-500/10 text-green-400 border-green-500/20';
      case 'CAUTIOUS':
        return 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20';
      case 'AVOID':
        return 'bg-red-500/10 text-red-400 border-red-500/20';
      default:
        return 'bg-blue-500/10 text-blue-400 border-blue-500/20';
    }
  };

  return (
    <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-border/60 pb-3">
        <div className="flex items-center gap-2">
          <Users className="h-4 w-4 text-accent" />
          <h3 className="text-sm font-bold uppercase tracking-wider text-text-primary">
            Multi-Agent Analytical Review & Committee Debate
          </h3>
        </div>
        <span className="text-xs text-text-muted">4 Independent Analytical Perspectives</span>
      </div>

      {/* Agents Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {reviews.map((rev) => (
          <div
            key={rev.agent_name}
            className="rounded-xl border border-border/70 bg-surface-elevated/40 p-4 space-y-3 flex flex-col justify-between"
          >
            <div>
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-sm font-bold text-text-primary">{rev.agent_name}</h4>
                  <p className="text-[11px] text-text-muted">{rev.role}</p>
                </div>

                <div className="flex items-center gap-2">
                  <div className="flex items-center text-xs font-bold text-yellow-400">
                    <Star className="h-3.5 w-3.5 fill-yellow-400 mr-1" />
                    {rev.score.toFixed(1)}/5.0
                  </div>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded border ${getBadgeColor(rev.recommendation)}`}>
                    {rev.recommendation}
                  </span>
                </div>
              </div>

              {/* Thesis */}
              <p className="text-xs text-text-secondary mt-2.5 italic border-l-2 border-accent/40 pl-2.5">
                &ldquo;{rev.thesis}&rdquo;
              </p>
            </div>

            {/* Positives & Risks */}
            <div className="space-y-2 pt-2 border-t border-border/40 text-[11px]">
              {rev.key_positives.length > 0 && (
                <div>
                  <span className="font-semibold text-green-400 block mb-1">Key Strengths:</span>
                  <ul className="space-y-1 text-text-secondary">
                    {rev.key_positives.map((pos, idx) => (
                      <li key={idx} className="flex items-start gap-1.5">
                        <span className="text-green-400 font-bold">✓</span>
                        <span>{pos}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {rev.key_risks.length > 0 && (
                <div>
                  <span className="font-semibold text-yellow-400 block mb-1">Vulnerabilities & Blindspots:</span>
                  <ul className="space-y-1 text-text-secondary">
                    {rev.key_risks.map((risk, idx) => (
                      <li key={idx} className="flex items-start gap-1.5">
                        <span className="text-yellow-400 font-bold">⚠</span>
                        <span>{risk}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
