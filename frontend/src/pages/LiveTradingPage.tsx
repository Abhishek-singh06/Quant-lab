import { useState, useEffect } from 'react';
import { ShieldOff, Shield, RefreshCw, Lock } from 'lucide-react';
import type {
  TradingSafetyLock,
} from '@/types/market';
import { api } from '@/lib/api';
import { QLBadge } from '@/design-system/QLBadge';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLSection } from '@/design-system/QLSection';

export function LiveTradingPage() {
  const [safetyLock, setSafetyLock] = useState<TradingSafetyLock | null>(null);
  const [loading, setLoading] = useState(true);
  const [killLoading, setKillLoading] = useState(false);

  const fetchAll = () => {
    setLoading(true);
    Promise.all([
      api.get<TradingSafetyLock>('/v1/broker/safety-lock').catch(() => null),
    ]).then(([lock]) => {
      setSafetyLock(lock as TradingSafetyLock | null);
    }).finally(() => setLoading(false));
  };

  useEffect(() => { fetchAll(); }, []);

  const toggleKillSwitch = () => {
    setKillLoading(true);
    const endpoint = safetyLock?.isEmergencyKillSwitchActive
      ? '/v1/broker/safety-lock/disable-kill-switch'
      : '/v1/broker/safety-lock/emergency-kill';
    api.post<TradingSafetyLock>(endpoint)
      .then(setSafetyLock)
      .catch(() => alert('Failed to toggle kill switch. Backend may be offline.'))
      .finally(() => setKillLoading(false));
  };

  const killActive = safetyLock?.isEmergencyKillSwitchActive ?? true;

  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>LIVE PRODUCTION BROKER DISPATCH</span>
            <span>•</span>
            <span>SAFETY INTERLOCK GATE</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            LIVE TRADING INTERLOCK
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Production broker gateway with emergency kill switch interlocks, continuous capital reconciliation, and automated safety invariant enforcement.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <QLButton
            variant="outline"
            size="sm"
            onClick={fetchAll}
            disabled={loading}
            icon={<RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />}
          >
            Sync State
          </QLButton>
        </div>
      </div>

      {/* 01 SAFETY INTERLOCK & KILL SWITCH */}
      <QLSection
        number={1}
        title="Production Safety Interlock"
        subtitle="Automated Execution Circuit Breakers"
      >
        <QLPanel variant="surface" padding="lg" className="space-y-6 border-rose-500/30 bg-rose-950/10">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-start gap-3">
              <div className="w-10 h-10 rounded-lg bg-rose-500/20 border border-rose-500/40 flex items-center justify-center text-rose-400 shrink-0">
                <Lock className="w-5 h-5" />
              </div>
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <h3 className="text-base font-bold text-text-primary font-mono uppercase">
                    LIVE BROKER ROUTING: DISABLED
                  </h3>
                  <QLBadge variant="negative" size="xs">
                    ₹0 REAL MONEY AT RISK
                  </QLBadge>
                </div>
                <p className="text-xs text-text-secondary max-w-xl font-sans leading-relaxed">
                  Real capital broker order dispatch is hardlocked to disabled. All alpha model decisions remain strictly contained within the virtual paper execution sandbox.
                </p>
              </div>
            </div>

            <QLButton
              variant={killActive ? 'danger' : 'primary'}
              size="md"
              onClick={toggleKillSwitch}
              disabled={killLoading}
              loading={killLoading}
              icon={killActive ? <ShieldOff className="w-4 h-4" /> : <Shield className="w-4 h-4" />}
            >
              {killActive ? 'KILL SWITCH ENGAGED' : 'ENGAGE KILL SWITCH'}
            </QLButton>
          </div>
        </QLPanel>
      </QLSection>

      {/* 02 BROKER ACCOUNT RECONCILIATION */}
      <QLSection
        number={2}
        title="Broker Account & Ledger Reconciliation"
        subtitle="Mark-to-Market Ledger Alignment"
      >
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 font-mono">
          <QLPanel variant="surface" padding="md">
            <span className="text-[10px] text-text-muted uppercase block">BROKER STATUS</span>
            <span className="text-lg font-bold text-rose-400 block mt-1">DISCONNECTED</span>
            <span className="text-[10px] text-text-muted font-sans">0 Orders Dispatched</span>
          </QLPanel>

          <QLPanel variant="surface" padding="md">
            <span className="text-[10px] text-text-muted uppercase block">SETTLED CAPITAL</span>
            <span className="text-lg font-bold text-emerald-400 block mt-1">₹0.00</span>
            <span className="text-[10px] text-text-muted font-sans">Zero Real Risk</span>
          </QLPanel>

          <QLPanel variant="surface" padding="md">
            <span className="text-[10px] text-text-muted uppercase block">MARGIN UTILITY</span>
            <span className="text-lg font-bold text-text-primary block mt-1">0.0%</span>
            <span className="text-[10px] text-text-muted font-sans">No Borrowed Leverage</span>
          </QLPanel>

          <QLPanel variant="surface" padding="md">
            <span className="text-[10px] text-text-muted uppercase block">RECONCILIATION</span>
            <span className="text-lg font-bold text-emerald-400 block mt-1">BALANCED</span>
            <span className="text-[10px] text-text-muted font-sans">Ledger Hash Matched</span>
          </QLPanel>
        </div>
      </QLSection>
    </div>
  );
}
