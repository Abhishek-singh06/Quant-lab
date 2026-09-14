import React, { useState, useEffect } from 'react';
import { Save, CheckCircle2, AlertCircle } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLBadge } from '@/design-system/QLBadge';
import { QLSection } from '@/design-system/QLSection';

export function InvestmentProfilePage() {
  const { token, updatePreferences } = useAuth();
  const [riskTolerance, setRiskTolerance] = useState('MODERATE');
  const [capital, setCapital] = useState('500000');
  const [maxStockAlloc, setMaxStockAlloc] = useState('10');
  const [maxSectorAlloc, setMaxSectorAlloc] = useState('25');
  const [horizons, setHorizons] = useState<string[]>(['1-4 WEEKS', '1-3 MONTHS']);
  const [assetClasses, setAssetClasses] = useState<string[]>(['EQUITY', 'ETF', 'MUTUAL_FUNDS']);
  const [objective, setObjective] = useState('BALANCED_GROWTH');
  const [saving, setSaving] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadPreferences() {
      if (!token) return;
      try {
        const res = await fetch('/api/v1/settings/investment-profile', {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (res.ok) {
          const data = await res.json();
          setRiskTolerance(data.risk_tolerance || 'MODERATE');
          setCapital(String(data.capital_available || 500000));
          setMaxStockAlloc(String(data.max_single_stock_alloc_pct || 10));
          setMaxSectorAlloc(String(data.max_sector_alloc_pct || 25));
          if (data.preferred_horizons) setHorizons(data.preferred_horizons);
          if (data.preferred_asset_classes) setAssetClasses(data.preferred_asset_classes);
          if (data.investment_objective) setObjective(data.investment_objective);
        }
      } catch {
        // Safe fallback
      }
    }

    loadPreferences();
  }, [token]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    setSaving(true);
    setError(null);
    setSuccessMsg(null);

    const payload = {
      risk_tolerance: riskTolerance,
      capital_available: parseFloat(capital) || 500000.0,
      max_single_stock_alloc_pct: parseFloat(maxStockAlloc) || 10.0,
      max_sector_alloc_pct: parseFloat(maxSectorAlloc) || 25.0,
      preferred_horizons: horizons,
      preferred_asset_classes: assetClasses,
      investment_objective: objective
    };

    try {
      const res = await fetch('/api/v1/settings/investment-profile', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        throw new Error('Failed to save investment profile.');
      }

      updatePreferences(payload);
      setSuccessMsg('Investment profile and risk bounds calibrated successfully.');
    } catch (err: any) {
      setError(err.message || 'Error saving preferences.');
    } finally {
      setSaving(false);
    }
  };

  const toggleHorizon = (h: string) => {
    setHorizons((prev) =>
      prev.includes(h) ? prev.filter((item) => item !== h) : [...prev, h]
    );
  };

  const toggleAssetClass = (ac: string) => {
    setAssetClasses((prev) =>
      prev.includes(ac) ? prev.filter((item) => item !== ac) : [...prev, ac]
    );
  };

  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>PERSONAL RISK PROFILE &amp; CAPITAL BOUNDS</span>
            <span>•</span>
            <span>DECISION ENGINE PARAMETERS</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            INVESTMENT PROFILE
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Configure your capital risk constraints, preferred holding horizons, and single-stock allocation bounds.
          </p>
        </div>

        <div className="flex items-center gap-2 font-mono text-xs">
          <QLBadge variant="positive" size="sm" dot>
            PROFILE ACTIVE: {riskTolerance}
          </QLBadge>
        </div>
      </div>

      {successMsg && (
        <div className="p-3.5 rounded-lg border border-emerald-500/30 bg-emerald-500/10 text-emerald-300 text-xs font-mono flex items-center justify-between">
          <span className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            {successMsg}
          </span>
          <button onClick={() => setSuccessMsg(null)} className="text-text-muted hover:text-text-primary">✕</button>
        </div>
      )}

      {error && (
        <div className="p-3.5 rounded-lg border border-rose-500/30 bg-rose-500/10 text-rose-300 text-xs font-mono flex items-center gap-2">
          <AlertCircle className="w-4 h-4 text-rose-400" />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSave} className="space-y-8">
        {/* 01 RISK & CAPITAL ENVELOPE */}
        <QLSection
          number={1}
          title="Risk Envelope & Capital Allocation Bounds"
          subtitle="Governs Position Sizing Multipliers & Maximum Tranche Caps"
        >
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <QLPanel variant="surface" padding="md" title="RISK TOLERANCE">
              <div className="space-y-3 font-mono text-xs">
                <select
                  value={riskTolerance}
                  onChange={(e) => setRiskTolerance(e.target.value)}
                  className="w-full bg-surface-elevated border border-border rounded-lg px-3 py-2 text-sm text-text-primary font-bold focus:outline-none focus:border-accent"
                >
                  <option value="CONSERVATIVE">Conservative (0.6x Risk Budget)</option>
                  <option value="MODERATE">Moderate (1.0x Base Envelope)</option>
                  <option value="AGGRESSIVE">Aggressive (1.4x Conviction Sizing)</option>
                </select>
                <p className="text-[11px] text-text-muted font-sans leading-relaxed">
                  Controls position sizing sensitivity relative to 1-Day VaR (95%) and historical asset volatility.
                </p>
              </div>
            </QLPanel>

            <QLPanel variant="surface" padding="md" title="AVAILABLE CAPITAL (₹)">
              <div className="space-y-3 font-mono text-xs">
                <input
                  type="number"
                  value={capital}
                  onChange={(e) => setCapital(e.target.value)}
                  className="w-full bg-surface-elevated border border-border rounded-lg px-3 py-2 text-sm text-text-primary font-bold focus:outline-none focus:border-accent"
                />
                <p className="text-[11px] text-text-muted font-sans leading-relaxed">
                  Total capital available across liquid assets and deployed trading allocations.
                </p>
              </div>
            </QLPanel>

            <QLPanel variant="surface" padding="md" title="MAX SINGLE-STOCK CAP (%)">
              <div className="space-y-3 font-mono text-xs">
                <input
                  type="number"
                  value={maxStockAlloc}
                  onChange={(e) => setMaxStockAlloc(e.target.value)}
                  className="w-full bg-surface-elevated border border-border rounded-lg px-3 py-2 text-sm text-text-primary font-bold focus:outline-none focus:border-accent"
                />
                <p className="text-[11px] text-text-muted font-sans leading-relaxed">
                  Hard concentration ceiling. Beyond this threshold, engine recommends 'AVOID ADDING'.
                </p>
              </div>
            </QLPanel>
          </div>
        </QLSection>

        {/* 02 PREFERRED HORIZONS & ASSETS */}
        <QLSection
          number={2}
          title="Investment Horizons & Asset Classes"
          subtitle="Model Calibration Scope & Universe Filtering"
        >
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <QLPanel variant="surface" padding="md" title="PREFERRED HOLDING HORIZONS">
              <div className="grid grid-cols-2 gap-2 pt-1 font-mono text-xs">
                {['INTRADAY', '1-5 DAYS', '1-4 WEEKS', '1-3 MONTHS', '3-12 MONTHS', 'LONG TERM'].map((h) => {
                  const active = horizons.includes(h);
                  return (
                    <button
                      type="button"
                      key={h}
                      onClick={() => toggleHorizon(h)}
                      className={`p-2.5 rounded-lg border text-left transition-all ${
                        active
                          ? 'border-accent bg-accent-muted/40 text-text-primary font-bold'
                          : 'border-border/60 bg-surface text-text-muted hover:text-text-primary'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <span>{h}</span>
                        {active && <CheckCircle2 className="w-3.5 h-3.5 text-accent" />}
                      </div>
                    </button>
                  );
                })}
              </div>
            </QLPanel>

            <QLPanel variant="surface" padding="md" title="PREFERRED ASSET CLASSES">
              <div className="grid grid-cols-2 gap-2 pt-1 font-mono text-xs">
                {['EQUITY', 'ETF', 'MUTUAL_FUNDS', 'COMMODITIES', 'F&O'].map((ac) => {
                  const active = assetClasses.includes(ac);
                  return (
                    <button
                      type="button"
                      key={ac}
                      onClick={() => toggleAssetClass(ac)}
                      className={`p-2.5 rounded-lg border text-left transition-all ${
                        active
                          ? 'border-accent bg-accent-muted/40 text-text-primary font-bold'
                          : 'border-border/60 bg-surface text-text-muted hover:text-text-primary'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <span>{ac}</span>
                        {active && <CheckCircle2 className="w-3.5 h-3.5 text-accent" />}
                      </div>
                    </button>
                  );
                })}
              </div>
            </QLPanel>
          </div>
        </QLSection>

        {/* Action Button */}
        <div className="pt-4 border-t border-border/80 flex justify-end">
          <QLButton
            type="submit"
            variant="primary"
            size="lg"
            className="font-mono font-bold"
            disabled={saving}
          >
            <Save className="w-4 h-4 mr-1.5" />
            <span>{saving ? 'CALIBRATING PROFILE...' : 'SAVE INVESTMENT PROFILE'}</span>
          </QLButton>
        </div>
      </form>
    </div>
  );
}
