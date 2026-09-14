import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  CheckCircle2,
  ArrowRight,
  Sparkles,
  Link as LinkIcon
} from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLBadge } from '@/design-system/QLBadge';

export function OnboardingPage() {
  const navigate = useNavigate();
  const { user, token } = useAuth();
  const [currentStep, setCurrentStep] = useState(2); // Step 1 is account creation (done)
  const [riskTolerance, setRiskTolerance] = useState('MODERATE');
  const [capital, setCapital] = useState('500000');
  const [maxStockAlloc, setMaxStockAlloc] = useState('10');
  const [isSaving, setIsSaving] = useState(false);

  const handleSavePreferences = async () => {
    setIsSaving(true);
    try {
      if (token) {
        await fetch('/api/v1/settings/investment-profile', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`
          },
          body: JSON.stringify({
            risk_tolerance: riskTolerance,
            capital_available: parseFloat(capital) || 500000.0,
            max_single_stock_alloc_pct: parseFloat(maxStockAlloc) || 10.0,
            max_sector_alloc_pct: 25.0,
            preferred_horizons: ['1-4 WEEKS', '1-3 MONTHS'],
            preferred_asset_classes: ['EQUITY', 'ETF', 'MUTUAL_FUNDS'],
            investment_objective: 'BALANCED_GROWTH'
          })
        });
      }
    } catch {
      // Safe continue
    } finally {
      setIsSaving(false);
      setCurrentStep(5);
    }
  };

  const steps = [
    { num: '01', title: 'CREATE ACCOUNT', desc: 'Secure profile initialized', done: true },
    { num: '02', title: 'CONNECT BROKER', desc: 'Groww / Zerodha official API', done: currentStep > 2 },
    { num: '03', title: 'IMPORT PORTFOLIO', desc: 'Point-in-time holding synchronization', done: currentStep > 3 },
    { num: '04', title: 'SET PREFERENCES', desc: 'Risk tolerance & capital bounds', done: currentStep > 4 },
    { num: '05', title: 'START TERMINAL', desc: 'Personalized market intelligence', done: currentStep === 5 },
  ];

  return (
    <div className="max-w-4xl mx-auto py-10 px-4 space-y-10 font-sans">
      {/* Header */}
      <div className="text-center space-y-2 border-b border-border/80 pb-6">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-accent-muted border border-accent/20 text-accent text-xs font-mono mb-1">
          <Sparkles className="w-3.5 h-3.5" />
          <span>ONBOARDING WORKSPACE</span>
        </div>
        <h1 className="text-3xl sm:text-5xl font-black font-mono tracking-tight text-text-primary uppercase">
          WELCOME TO QUANTLAB{user ? `, ${user.name.split(' ')[0].toUpperCase()}` : ''}
        </h1>
        <p className="text-xs sm:text-sm font-mono text-text-muted">
          5-STEP INITIALIZATION TO PERSONALIZED QUANTITATIVE INTELLIGENCE
        </p>
      </div>

      {/* Step Progress Tracker */}
      <div className="grid grid-cols-2 sm:grid-cols-5 gap-2 font-mono text-xs">
        {steps.map((s, idx) => (
          <div
            key={s.num}
            className={`p-3 rounded-lg border transition-all ${
              s.done
                ? 'border-emerald-500/40 bg-emerald-950/20 text-emerald-300'
                : currentStep === idx + 1
                ? 'border-accent bg-accent-muted/40 text-text-primary font-bold'
                : 'border-border/60 bg-surface text-text-muted'
            }`}
          >
            <div className="flex items-center justify-between mb-1">
              <span className="text-[10px] uppercase font-bold">{s.num}</span>
              {s.done && <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />}
            </div>
            <p className="text-xs font-bold truncate">{s.title}</p>
            <p className="text-[10px] text-text-muted mt-0.5 truncate">{s.desc}</p>
          </div>
        ))}
      </div>

      {/* Step 02: Connect Broker */}
      {currentStep === 2 && (
        <QLPanel variant="surface" padding="lg" title="STEP 02: CONNECT BROKER (OPTIONAL)">
          <div className="space-y-6">
            <p className="text-xs sm:text-sm text-text-secondary leading-relaxed font-sans">
              Connect your official broker account to import your actual portfolio holdings, track true mark-to-market performance, and receive personalized holding recommendations.
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="p-4 rounded-xl border border-border bg-surface-elevated/40 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="font-mono font-bold text-sm text-text-primary">GROWW</span>
                  <QLBadge variant="positive" size="xs">OFFICIAL API</QLBadge>
                </div>
                <p className="text-xs text-text-muted font-sans">
                  Official OAuth &amp; API access token integration. Demat holdings, positions, and cash balances.
                </p>
                <QLButton
                  variant="primary"
                  size="sm"
                  className="w-full justify-center"
                  onClick={() => navigate('/connect-broker')}
                >
                  <LinkIcon className="w-3.5 h-3.5 mr-1.5" />
                  <span>Connect Groww</span>
                </QLButton>
              </div>

              <div className="p-4 rounded-xl border border-border bg-surface-elevated/40 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="font-mono font-bold text-sm text-text-primary">ZERODHA KITE</span>
                  <QLBadge variant="neutral" size="xs">CONNECTABLE</QLBadge>
                </div>
                <p className="text-xs text-text-muted font-sans">
                  Kite Connect 3.0 API provider connector for equities, ETFs, and derivatives.
                </p>
                <QLButton
                  variant="outline"
                  size="sm"
                  className="w-full justify-center"
                  onClick={() => navigate('/connect-broker')}
                >
                  <span>Connect Zerodha</span>
                </QLButton>
              </div>
            </div>

            <div className="pt-4 border-t border-border/50 flex flex-col sm:flex-row items-center justify-between gap-3">
              <span className="text-xs font-mono text-text-muted">
                Broker integration is strictly optional. You can explore market intelligence without connecting a broker.
              </span>
              <QLButton
                variant="outline"
                size="md"
                onClick={() => setCurrentStep(4)}
              >
                <span>Skip for now</span>
                <ArrowRight className="w-4 h-4 ml-1.5" />
              </QLButton>
            </div>
          </div>
        </QLPanel>
      )}

      {/* Step 04: Investment Preferences */}
      {currentStep === 4 && (
        <QLPanel variant="surface" padding="lg" title="STEP 04: SET INVESTMENT PREFERENCES">
          <div className="space-y-6">
            <p className="text-xs sm:text-sm text-text-secondary leading-relaxed font-sans">
              Calibrate your risk bounds and capital constraints. These parameters govern the personalized decision engine and position sizing caps.
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 font-mono text-xs">
              <div className="space-y-1.5">
                <label className="font-bold text-text-secondary uppercase">Risk Tolerance</label>
                <select
                  value={riskTolerance}
                  onChange={(e) => setRiskTolerance(e.target.value)}
                  className="w-full bg-surface-elevated border border-border rounded-lg px-3 py-2 text-sm text-text-primary focus:outline-none focus:border-accent"
                >
                  <option value="CONSERVATIVE">Conservative (0.5x Sizing)</option>
                  <option value="MODERATE">Moderate (1.0x Sizing)</option>
                  <option value="AGGRESSIVE">Aggressive (1.4x Sizing)</option>
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="font-bold text-text-secondary uppercase">Available Capital (₹)</label>
                <input
                  type="number"
                  value={capital}
                  onChange={(e) => setCapital(e.target.value)}
                  className="w-full bg-surface-elevated border border-border rounded-lg px-3 py-2 text-sm text-text-primary focus:outline-none focus:border-accent"
                />
              </div>

              <div className="space-y-1.5">
                <label className="font-bold text-text-secondary uppercase">Max Single-Stock Cap (%)</label>
                <input
                  type="number"
                  value={maxStockAlloc}
                  onChange={(e) => setMaxStockAlloc(e.target.value)}
                  className="w-full bg-surface-elevated border border-border rounded-lg px-3 py-2 text-sm text-text-primary focus:outline-none focus:border-accent"
                />
              </div>
            </div>

            <div className="pt-4 border-t border-border/50 flex justify-end">
              <QLButton
                variant="primary"
                size="lg"
                onClick={handleSavePreferences}
                disabled={isSaving}
              >
                <span>{isSaving ? 'SAVING...' : 'SAVE & START INTELLIGENCE'}</span>
                {!isSaving && <ArrowRight className="w-4 h-4 ml-1.5" />}
              </QLButton>
            </div>
          </div>
        </QLPanel>
      )}

      {/* Step 05: Completed Ready */}
      {currentStep === 5 && (
        <QLPanel variant="surface" padding="lg" title="STEP 05: READY FOR MARKET INTELLIGENCE">
          <div className="text-center py-6 space-y-4">
            <div className="w-12 h-12 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <h3 className="text-2xl font-bold font-mono text-text-primary">
              INITIALIZATION COMPLETE
            </h3>
            <p className="text-xs sm:text-sm text-text-secondary max-w-md mx-auto font-sans leading-relaxed">
              Your personalized quantitative terminal is ready. Explore your connected portfolio, search universal market instruments, and inspect model-backed decisions.
            </p>

            <div className="flex flex-wrap justify-center gap-3 pt-4">
              <QLButton
                variant="primary"
                size="lg"
                onClick={() => navigate('/portfolio')}
              >
                <span>Open Portfolio Desk</span>
                <ArrowRight className="w-4 h-4 ml-1.5" />
              </QLButton>
              <QLButton
                variant="outline"
                size="lg"
                onClick={() => navigate('/markets')}
              >
                <span>Universal Marketplace</span>
              </QLButton>
            </div>
          </div>
        </QLPanel>
      )}
    </div>
  );
}
