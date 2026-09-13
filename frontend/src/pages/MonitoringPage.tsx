import { useEffect, useState } from 'react';
import {
  AlertTriangle,
  RefreshCw,
  CheckCircle2,
  AlertCircle,
  Activity,
  Cpu,
  ShieldAlert,
} from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import type {
  SystemOverviewHealth,
  MonitoringHealthCheck,
  MonitoringAlert,
  MonitoringIncident,
  ProviderHealthDTO,
  DataQualityEventDTO,
  FeatureDriftEventDTO,
  SignalAnomalyEventDTO,
} from '@/types/market';
import { api } from '@/lib/api';
import { cn } from '@/lib/utils';

type Tab = 'system' | 'providers' | 'data-quality' | 'drift' | 'signals' | 'alerts' | 'incidents';

const TABS: { id: Tab; label: string }[] = [
  { id: 'system', label: 'System Health' },
  { id: 'providers', label: 'Feed Providers' },
  { id: 'data-quality', label: 'Data Quality' },
  { id: 'drift', label: 'Feature Drift' },
  { id: 'signals', label: 'Signal Anomalies' },
  { id: 'alerts', label: 'Alerts' },
  { id: 'incidents', label: 'Incidents' },
];

function StatusBadge({ status }: { status: string }) {
  const s = (status || 'UNKNOWN').toUpperCase();
  const cfg: Record<string, { bg: string; text: string; border: string }> = {
    HEALTHY: { bg: 'bg-green-500/10', text: 'text-green-400', border: 'border-green-500/20' },
    REAL_TIME: { bg: 'bg-green-500/10', text: 'text-green-400', border: 'border-green-500/20' },
    NORMAL: { bg: 'bg-green-500/10', text: 'text-green-400', border: 'border-green-500/20' },
    DEGRADED: { bg: 'bg-yellow-500/10', text: 'text-yellow-400', border: 'border-yellow-500/20' },
    DELAYED: { bg: 'bg-yellow-500/10', text: 'text-yellow-400', border: 'border-yellow-500/20' },
    WARNING: { bg: 'bg-yellow-500/10', text: 'text-yellow-400', border: 'border-yellow-500/20' },
    WATCH: { bg: 'bg-yellow-500/10', text: 'text-yellow-400', border: 'border-yellow-500/20' },
    CRITICAL: { bg: 'bg-red-500/10', text: 'text-red-400', border: 'border-red-500/20' },
    STALE: { bg: 'bg-red-500/10', text: 'text-red-400', border: 'border-red-500/20' },
    UNAVAILABLE: { bg: 'bg-red-500/10', text: 'text-red-400', border: 'border-red-500/20' },
    NOT_AVAILABLE: { bg: 'bg-red-500/10', text: 'text-red-400', border: 'border-red-500/20' },
    NOT_CONFIGURED: { bg: 'bg-purple-500/10', text: 'text-purple-400', border: 'border-purple-500/20' },
    UNKNOWN: { bg: 'bg-zinc-500/10', text: 'text-zinc-400', border: 'border-zinc-500/20' },
  };

  const style = cfg[s] || cfg.UNKNOWN;
  return (
    <span className={cn('inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-semibold', style.bg, style.text, style.border)}>
      <span className={cn('h-1.5 w-1.5 rounded-full', style.text.replace('text-', 'bg-'))} />
      {s}
    </span>
  );
}

function SeverityBadge({ severity }: { severity: string }) {
  const sev = (severity || 'INFO').toUpperCase();
  const cfg: Record<string, string> = {
    INFO: 'text-blue-400 bg-blue-400/10 border-blue-400/20',
    WARNING: 'text-yellow-400 bg-yellow-400/10 border-yellow-400/20',
    ERROR: 'text-orange-400 bg-orange-400/10 border-orange-400/20',
    CRITICAL: 'text-red-400 bg-red-400/10 border-red-400/20',
  };
  return (
    <span className={cn('rounded-full border px-2 py-0.5 text-[10px] font-bold', cfg[sev] ?? 'text-text-muted border-border')}>
      {sev}
    </span>
  );
}

export function MonitoringPage() {
  const [tab, setTab] = useState<Tab>('system');
  const [overview, setOverview] = useState<SystemOverviewHealth | null>(null);
  const [healthChecks, setHealthChecks] = useState<MonitoringHealthCheck[]>([]);
  const [alerts, setAlerts] = useState<MonitoringAlert[]>([]);
  const [incidents, setIncidents] = useState<MonitoringIncident[]>([]);
  const [providers, setProviders] = useState<ProviderHealthDTO[]>([]);
  const [dataQualityEvents, setDataQualityEvents] = useState<DataQualityEventDTO[]>([]);
  const [driftEvents, setDriftEvents] = useState<FeatureDriftEventDTO[]>([]);
  const [signalAnomalies, setSignalAnomalies] = useState<SignalAnomalyEventDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [lastRefreshed, setLastRefreshed] = useState<Date | null>(null);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const fetchAll = () => {
    setLoading(true);
    Promise.all([
      api.get<SystemOverviewHealth>('/v1/monitoring/overview').catch(() => null),
      api.get<MonitoringHealthCheck[]>('/v1/monitoring/health-checks').catch(() => []),
      api.get<MonitoringAlert[]>('/v1/monitoring/alerts').catch(() => []),
      api.get<MonitoringIncident[]>('/v1/monitoring/incidents').catch(() => []),
      api.get<ProviderHealthDTO[]>('/v1/monitoring/providers').catch(() => []),
      api.get<DataQualityEventDTO[]>('/v1/monitoring/data-quality-events').catch(() => []),
      api.get<FeatureDriftEventDTO[]>('/v1/monitoring/feature-drift-events').catch(() => []),
      api.get<SignalAnomalyEventDTO[]>('/v1/monitoring/signal-anomaly-events').catch(() => []),
    ]).then(([ov, hc, al, inc, prov, dq, drift, sig]) => {
      setOverview(ov as SystemOverviewHealth | null);
      setHealthChecks(hc as MonitoringHealthCheck[]);
      setAlerts(al as MonitoringAlert[]);
      setIncidents(inc as MonitoringIncident[]);
      setProviders(prov as ProviderHealthDTO[]);
      setDataQualityEvents(dq as DataQualityEventDTO[]);
      setDriftEvents(drift as FeatureDriftEventDTO[]);
      setSignalAnomalies(sig as SignalAnomalyEventDTO[]);
      setLastRefreshed(new Date());
    }).finally(() => setLoading(false));
  };

  useEffect(() => { fetchAll(); }, []);

  const handleAcknowledge = async (id: string) => {
    setActionLoading(id);
    try {
      await api.post(`/v1/monitoring/alerts/${id}/acknowledge`, { user: 'OPERATOR' });
      fetchAll();
    } finally {
      setActionLoading(null);
    }
  };

  const handleResolve = async (id: string) => {
    setActionLoading(id);
    try {
      await api.post(`/v1/monitoring/alerts/${id}/resolve`, {});
      fetchAll();
    } finally {
      setActionLoading(null);
    }
  };

  const criticalAlerts = alerts.filter((a) => a.status === 'OPEN' && (a.severity === 'CRITICAL' || a.severity === 'ERROR')).length;
  const openIncidents = incidents.filter((i) => i.status !== 'RESOLVED' && i.status !== 'POSTMORTEM').length;

  return (
    <div className="space-y-6">
      <MockBanner />
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <AlertTriangle className="h-6 w-6 text-accent" />
          <div>
            <h1 className="text-2xl font-bold gradient-text">Production Monitoring</h1>
            <p className="text-sm text-text-muted mt-0.5">Part 19 — Real-time reliability, data quality, drift calculation &amp; alerting</p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          {lastRefreshed && (
            <span className="text-xs text-text-muted">Last evaluated: {lastRefreshed.toLocaleTimeString('en-IN')}</span>
          )}
          <button onClick={fetchAll} disabled={loading} className="flex items-center gap-2 rounded-lg border border-border px-3 py-1.5 text-sm text-text-secondary hover:bg-surface-elevated transition-colors disabled:opacity-50">
            <RefreshCw className={cn('h-4 w-4', loading && 'animate-spin')} />
            Refresh
          </button>
        </div>
      </div>

      {/* Overview Stat Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="rounded-xl border border-border bg-surface p-4 flex items-center gap-4">
          <div className="rounded-lg bg-surface-elevated p-2">
            <Activity className="h-5 w-5 text-accent" />
          </div>
          <div>
            <p className="text-xs text-text-muted">Overall System</p>
            <div className="mt-1">
              <StatusBadge status={overview?.overallSystemStatus ?? 'UNKNOWN'} />
            </div>
          </div>
        </div>

        <div className="rounded-xl border border-border bg-surface p-4 flex items-center gap-4">
          <div className="rounded-lg bg-surface-elevated p-2">
            <AlertCircle className="h-5 w-5 text-red-400" />
          </div>
          <div>
            <p className="text-2xl font-bold text-red-400">{criticalAlerts}</p>
            <p className="text-xs text-text-muted">Critical Open Alerts</p>
          </div>
        </div>

        <div className="rounded-xl border border-border bg-surface p-4 flex items-center gap-4">
          <div className="rounded-lg bg-surface-elevated p-2">
            <ShieldAlert className="h-5 w-5 text-orange-400" />
          </div>
          <div>
            <p className="text-2xl font-bold text-orange-400">{openIncidents}</p>
            <p className="text-xs text-text-muted">Active Incidents</p>
          </div>
        </div>

        <div className="rounded-xl border border-border bg-surface p-4 flex items-center gap-4">
          <div className="rounded-lg bg-surface-elevated p-2">
            <Cpu className="h-5 w-5 text-blue-400" />
          </div>
          <div>
            <p className="text-2xl font-bold text-text-primary">{overview ? `${overview.averageApiLatencyMs.toFixed(1)} cores` : '—'}</p>
            <p className="text-xs text-text-muted">JVM Host Load Average</p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 overflow-x-auto rounded-xl border border-border bg-surface p-1">
        {TABS.map((t) => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            className={cn(
              'flex-1 whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium transition-all',
              tab === t.id ? 'bg-accent-muted text-accent' : 'text-text-secondary hover:text-text-primary hover:bg-surface-elevated'
            )}
          >
            {t.label}
            {t.id === 'alerts' && criticalAlerts > 0 && (
              <span className="ml-1.5 rounded-full bg-red-400 px-1.5 py-0.5 text-[10px] font-bold text-white">{criticalAlerts}</span>
            )}
            {t.id === 'incidents' && openIncidents > 0 && (
              <span className="ml-1.5 rounded-full bg-orange-400 px-1.5 py-0.5 text-[10px] font-bold text-white">{openIncidents}</span>
            )}
          </button>
        ))}
      </div>

      {/* System Health Tab */}
      {tab === 'system' && (
        <div className="space-y-6">
          {/* Subsystems Map */}
          <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
            <h3 className="text-sm font-semibold text-text-primary">Subsystem Health Matrix</h3>
            {overview?.subsystemStatus ? (
              <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
                {Object.entries(overview.subsystemStatus).map(([sub, status]) => (
                  <div key={sub} className="rounded-lg border border-border bg-surface-elevated p-3 space-y-1.5">
                    <p className="text-xs font-mono text-text-muted">{sub}</p>
                    <StatusBadge status={status} />
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-text-muted">No subsystem telemetry available.</p>
            )}
          </div>

          {/* Health Checks List */}
          <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
            <h3 className="text-sm font-semibold text-text-primary">Component Health Checks</h3>
            {healthChecks.length === 0 ? (
              <p className="text-xs text-text-muted">No individual health checks recorded yet.</p>
            ) : (
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {healthChecks.map((hc) => (
                  <div key={hc.id} className="rounded-xl border border-border bg-surface-elevated p-4 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-text-primary">{hc.component}</span>
                      <StatusBadge status={hc.status} />
                    </div>
                    {hc.latencyMs !== undefined && (
                      <p className="text-xs text-text-secondary">Latency: <span className="font-mono">{hc.latencyMs}ms</span></p>
                    )}
                    {hc.message && <p className="text-xs text-text-muted italic">{hc.message}</p>}
                    <p className="text-[10px] text-text-muted">Checked: {new Date(hc.checkedAt).toLocaleString('en-IN')}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Feed Providers Tab */}
      {tab === 'providers' && (
        <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
          <h3 className="text-sm font-semibold text-text-primary">Market Data Provider Telemetry</h3>
          {providers.length === 0 ? (
            <div className="py-8 text-center text-text-muted text-sm">No provider health data recorded.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead>
                  <tr className="border-b border-border text-text-muted">
                    {['Provider', 'Connection', 'Freshness', 'Latency', 'Age (s)', 'Failures', 'Coverage', 'Updated'].map((h) => (
                      <th key={h} className="text-left py-2 pr-4 font-medium">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {providers.map((p) => (
                    <tr key={p.id} className="border-b border-border/50">
                      <td className="py-2.5 pr-4 font-medium text-text-primary">{p.provider}</td>
                      <td className="py-2.5 pr-4"><StatusBadge status={p.connectionStatus} /></td>
                      <td className="py-2.5 pr-4"><StatusBadge status={p.dataFreshnessStatus} /></td>
                      <td className="py-2.5 pr-4 font-mono">{p.latencyMs ? `${p.latencyMs.toFixed(0)}ms` : '—'}</td>
                      <td className="py-2.5 pr-4 font-mono">{p.dataAgeSeconds ? `${p.dataAgeSeconds.toFixed(0)}s` : '—'}</td>
                      <td className={cn('py-2.5 pr-4 font-mono', p.consecutiveFailures > 0 ? 'text-red-400 font-bold' : 'text-green-400')}>{p.consecutiveFailures}</td>
                      <td className="py-2.5 pr-4 font-mono">{p.universeCoveragePct.toFixed(1)}%</td>
                      <td className="py-2.5 pr-4 text-text-muted">{new Date(p.updatedAt).toLocaleTimeString('en-IN')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Data Quality Tab */}
      {tab === 'data-quality' && (
        <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
          <h3 className="text-sm font-semibold text-text-primary">Recorded Data Quality &amp; Integrity Events</h3>
          {dataQualityEvents.length === 0 ? (
            <div className="py-8 text-center text-text-muted text-sm">
              <CheckCircle2 className="h-8 w-8 text-green-400 mx-auto mb-2" />
              No data quality anomalies recorded.
            </div>
          ) : (
            <div className="space-y-3">
              {dataQualityEvents.map((dq) => (
                <div key={dq.id} className="rounded-lg border border-border bg-surface-elevated p-3.5 space-y-1.5">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <SeverityBadge severity={dq.severity} />
                      <span className="text-xs font-mono font-bold text-text-primary">{dq.eventType}</span>
                      {dq.symbol && <span className="rounded bg-accent/10 px-1.5 py-0.5 text-[10px] font-mono text-accent">{dq.symbol}</span>}
                    </div>
                    <span className="text-[10px] text-text-muted">{new Date(dq.detectedAt).toLocaleString('en-IN')}</span>
                  </div>
                  <p className="text-xs text-text-secondary">{dq.description}</p>
                  <p className="text-[10px] text-text-muted">Provider: {dq.provider} | Affected Records: {dq.affectedRecordsCount}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Feature Drift Tab */}
      {tab === 'drift' && (
        <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
          <h3 className="text-sm font-semibold text-text-primary">Statistical Feature Drift Monitoring (PSI / KS-Test / Wasserstein)</h3>
          {driftEvents.length === 0 ? (
            <div className="py-8 text-center text-text-muted text-sm">No feature drift events recorded.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead>
                  <tr className="border-b border-border text-text-muted">
                    {['Feature Name', 'Metric', 'Observed Value', 'Threshold', 'Status', 'Sample Size', 'Detected At'].map((h) => (
                      <th key={h} className="text-left py-2 pr-4 font-medium">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {driftEvents.map((d) => (
                    <tr key={d.id} className="border-b border-border/50">
                      <td className="py-2.5 pr-4 font-mono font-medium text-text-primary">{d.featureName}</td>
                      <td className="py-2.5 pr-4 font-mono text-text-muted">{d.metricType}</td>
                      <td className="py-2.5 pr-4 font-mono font-bold">{d.observedValue.toFixed(4)}</td>
                      <td className="py-2.5 pr-4 font-mono text-text-muted">{d.threshold.toFixed(4)}</td>
                      <td className="py-2.5 pr-4"><StatusBadge status={d.driftStatus} /></td>
                      <td className="py-2.5 pr-4 font-mono">{d.sampleSize}</td>
                      <td className="py-2.5 pr-4 text-text-muted">{new Date(d.detectedAt).toLocaleString('en-IN')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Signal Anomalies Tab */}
      {tab === 'signals' && (
        <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
          <h3 className="text-sm font-semibold text-text-primary">Signal Distribution Anomaly Events</h3>
          {signalAnomalies.length === 0 ? (
            <div className="py-8 text-center text-text-muted text-sm">No signal distribution anomalies detected.</div>
          ) : (
            <div className="space-y-3">
              {signalAnomalies.map((s) => (
                <div key={s.id} className="rounded-lg border border-border bg-surface-elevated p-3.5 space-y-1.5">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <SeverityBadge severity={s.severity} />
                      <span className="text-xs font-mono font-bold text-text-primary">{s.anomalyCategory}</span>
                      {s.affectedSector && <span className="rounded bg-accent/10 px-1.5 py-0.5 text-[10px] text-accent">{s.affectedSector}</span>}
                    </div>
                    <span className="text-[10px] text-text-muted">{new Date(s.detectedAt).toLocaleString('en-IN')}</span>
                  </div>
                  <p className="text-xs text-text-secondary">{s.description}</p>
                  <p className="text-[10px] font-mono text-text-muted">Observed: {s.observedRate.toFixed(4)} | Expected: {s.expectedRate.toFixed(4)}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Alerts Tab */}
      {tab === 'alerts' && (
        <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
          <h3 className="text-sm font-semibold text-text-primary">Monitoring Alerts Lifecycle</h3>
          {alerts.length === 0 ? (
            <div className="py-8 text-center text-text-muted text-sm">
              <CheckCircle2 className="h-8 w-8 text-green-400 mx-auto mb-2" />
              No active or historic alerts.
            </div>
          ) : (
            <div className="space-y-3">
              {alerts.map((a) => (
                <div key={a.id} className="rounded-xl border border-border bg-surface-elevated p-4 space-y-2.5">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="flex items-center gap-2">
                        <SeverityBadge severity={a.severity} />
                        <span className="text-xs font-mono text-text-muted">{a.ruleId}</span>
                        <StatusBadge status={a.status} />
                      </div>
                      <p className="text-sm font-bold text-text-primary mt-1">{a.ruleName}</p>
                    </div>
                    <span className="text-[10px] text-text-muted">{new Date(a.createdAt).toLocaleString('en-IN')}</span>
                  </div>
                  <p className="text-xs text-text-secondary">{a.message}</p>
                  <div className="flex items-center justify-between pt-2 border-t border-border/50 text-[10px] text-text-muted">
                    <span>Component: <span className="font-mono text-text-primary">{a.component}</span> | Observed: <span className="font-mono text-text-primary">{a.observedValue ?? '—'}</span></span>
                    <div className="flex gap-2">
                      {a.status === 'OPEN' && (
                        <button
                          onClick={() => handleAcknowledge(a.id)}
                          disabled={actionLoading === a.id}
                          className="rounded bg-yellow-500/10 border border-yellow-500/20 px-2.5 py-1 text-yellow-400 font-semibold hover:bg-yellow-500/20 transition-colors"
                        >
                          Acknowledge
                        </button>
                      )}
                      {a.status !== 'RESOLVED' && (
                        <button
                          onClick={() => handleResolve(a.id)}
                          disabled={actionLoading === a.id}
                          className="rounded bg-green-500/10 border border-green-500/20 px-2.5 py-1 text-green-400 font-semibold hover:bg-green-500/20 transition-colors"
                        >
                          Resolve
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Incidents Tab */}
      {tab === 'incidents' && (
        <div className="rounded-xl border border-border bg-surface p-5 space-y-4">
          <h3 className="text-sm font-semibold text-text-primary">Escalated Incidents</h3>
          {incidents.length === 0 ? (
            <div className="py-8 text-center text-text-muted text-sm">
              <CheckCircle2 className="h-8 w-8 text-green-400 mx-auto mb-2" />
              No active or escalated incidents.
            </div>
          ) : (
            <div className="space-y-3">
              {incidents.map((inc) => (
                <div key={inc.id} className="rounded-xl border border-border bg-surface-elevated p-4 space-y-2">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="flex items-center gap-2">
                        <SeverityBadge severity={inc.severity} />
                        <span className="text-xs font-mono font-bold text-accent">{inc.incidentNumber}</span>
                        <StatusBadge status={inc.status} />
                      </div>
                      <p className="text-sm font-bold text-text-primary mt-1">{inc.title}</p>
                    </div>
                    <span className="text-[10px] text-text-muted">{new Date(inc.createdAt).toLocaleString('en-IN')}</span>
                  </div>
                  {inc.rootCause && (
                    <div className="rounded bg-surface p-2.5 text-xs text-text-secondary whitespace-pre-line">
                      <strong className="text-text-primary">Root Cause:</strong> {inc.rootCause}
                    </div>
                  )}
                  <p className="text-[10px] text-text-muted">Affected: <span className="font-mono text-text-primary">{inc.affectedComponents}</span></p>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
