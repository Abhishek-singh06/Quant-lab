import { HelpCircle } from 'lucide-react';
import type { TraderQuestionsAnalysis } from '@/types/recommendation';

interface Props {
  traderQA: TraderQuestionsAnalysis;
}

export function TraderQASection({ traderQA }: Props) {

  const questions = [
    {
      q: '1. Is the stock attractive at the CURRENT price?',
      a: traderQA.is_attractive_at_current_price,
      tag: 'PRICING',
    },
    {
      q: '2. Is the current momentum favorable?',
      a: traderQA.is_momentum_favorable,
      tag: 'MOMENTUM',
    },
    {
      q: '3. Is the stock overextended?',
      a: traderQA.is_stock_overextended,
      tag: 'TECHNICAL',
    },
    {
      q: '4. Is there a better entry opportunity?',
      a: traderQA.is_there_better_entry,
      tag: 'ENTRY',
    },
    {
      q: '5. What is the downside if the thesis is wrong?',
      a: traderQA.downside_if_thesis_wrong,
      tag: 'RISK',
    },
    {
      q: '6. What is the upside if the thesis works?',
      a: traderQA.upside_if_thesis_works,
      tag: 'TARGET',
    },
    {
      q: '7. What is the strongest reason to buy?',
      a: traderQA.strongest_reason_to_buy,
      tag: 'THESIS',
    },
    {
      q: '8. What is the strongest reason NOT to buy?',
      a: traderQA.strongest_reason_not_to_buy,
      tag: 'CAUTION',
    },
    {
      q: '9. What could invalidate the thesis?',
      a: traderQA.what_could_invalidate_thesis,
      tag: 'INVALIDATION',
    },
    {
      q: '10. What event should the investor watch next?',
      a: traderQA.what_event_to_watch_next,
      tag: 'CATALYST',
    },
    {
      q: '11. Should capital be deployed immediately or gradually?',
      a: traderQA.deploy_capital_mode,
      tag: 'EXECUTION',
    },
    {
      q: '12. What is the appropriate risk level?',
      a: `Assessed Risk: ${traderQA.appropriate_risk_level}`,
      tag: 'RISK_PROFILE',
    },
    {
      q: '13. What is the expected holding period?',
      a: `Optimal Horizon: ${traderQA.expected_holding_period}`,
      tag: 'TIMEFRAME',
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2">
        <HelpCircle className="w-5 h-5 text-accent" />
        <div>
          <h3 className="text-lg font-bold text-text-primary">Experienced Trader Analysis</h3>
          <p className="text-xs text-text-muted">Rigorous assessment of the 13 canonical investment and trade viability questions.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {questions.map((item, idx) => (
          <div
            key={idx}
            className="rounded-xl border border-border bg-surface-elevated/50 p-4 transition-all hover:border-accent/40"
          >
              <div className="flex items-center justify-between gap-2 mb-2">
                <span className="text-xs font-bold text-accent uppercase tracking-wider">
                  {item.tag}
                </span>
                <span className="text-[11px] text-text-muted font-mono">Q{idx + 1}</span>
              </div>
              <h4 className="text-sm font-semibold text-text-primary mb-2">{item.q}</h4>
              <p className="text-xs text-text-secondary leading-relaxed bg-surface/80 p-3 rounded-lg border border-border/50">
                {item.a}
              </p>
            </div>
        ))}
      </div>
    </div>
  );
}
