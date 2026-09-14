import { useState, useEffect } from 'react';
import {
  ShieldCheck,
  RefreshCw,
  AlertCircle,
  Lock,
  ExternalLink,
  Trash2
} from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';
import { QLBadge } from '@/design-system/QLBadge';
import { QLSection } from '@/design-system/QLSection';

interface BrokerInfo {
  provider: string;
  name: string;
  status: string;
  is_connectable: boolean;
  description: string;
  supported_segments: string[];
}

interface BrokerConnection {
  id: string;
  provider: string;
  status: string;
  account_identifier: string;
  nse_enabled: boolean;
  bse_enabled: boolean;
  fno_enabled: boolean;
  created_at: string;
  last_sync_at: string;
}

export function BrokerConnectionPage() {
  const { token } = useAuth();
  const [brokers, setBrokers] = useState<BrokerInfo[]>([]);
  const [connections, setConnections] = useState<BrokerConnection[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedBroker, setSelectedBroker] = useState<string | null>(null);
  const [apiKey, setApiKey] = useState('');
  const [accessToken, setAccessToken] = useState('');
  const [connecting, setConnecting] = useState(false);
  const [syncingId, setSyncingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const brokerRes = await fetch('/api/v1/brokers');
      if (brokerRes.ok) {
        setBrokers(await brokerRes.json());
      }

      if (token) {
        const connRes = await fetch('/api/v1/brokers/connections', {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (connRes.ok) {
          setConnections(await connRes.json());
        }
      }
    } catch {
      // Safe fallback
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [token]);

  const handleConnect = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedBroker || !token) return;

    setError(null);
    setConnecting(true);

    try {
      const res = await fetch(`/api/v1/brokers/${selectedBroker}/connect`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ api_key: apiKey, access_token: accessToken }),
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.detail || 'Connection failed.');
      }

      setSuccessMsg(`Successfully connected to ${selectedBroker}!`);
      setSelectedBroker(null);
      setApiKey('');
      setAccessToken('');
      fetchData();
    } catch (err: any) {
      setError(err.message || 'Failed to connect broker.');
    } finally {
      setConnecting(false);
    }
  };

  const handleSync = async (connId: string) => {
    if (!token) return;
    setSyncingId(connId);
    try {
      const res = await fetch(`/api/v1/brokers/${connId}/sync`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        setSuccessMsg('Portfolio synchronized successfully.');
        fetchData();
      }
    } catch {
      // Safe fallback
    } finally {
      setSyncingId(null);
    }
  };

  const handleDisconnect = async (connId: string) => {
    if (!token) return;
    try {
      const res = await fetch(`/api/v1/brokers/${connId}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        setSuccessMsg('Broker disconnected.');
        fetchData();
      }
    } catch {
      // Safe fallback
    }
  };

  return (
    <div className="space-y-10 pb-12 font-sans">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border/80 pb-6">
        <div>
          <div className="flex items-center gap-2 mb-1 font-mono text-xs text-text-muted">
            <span>OFFICIAL BROKER INTEGRATION GATEWAY</span>
            <span>•</span>
            <span>ZERO SENSITIVE CREDENTIAL STORAGE</span>
          </div>
          <h1 className="text-2xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            BROKER CONNECTIONS
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Authorize official broker APIs to synchronize your actual portfolio holdings, positions, and live margin limits.
          </p>
        </div>

        <div className="flex items-center gap-3 font-mono text-xs">
          <QLBadge variant={connections.length > 0 ? 'positive' : 'neutral'} size="sm" dot>
            {connections.length > 0 ? `${connections.length} BROKER LINKED` : 'NO BROKER CONNECTED'}
          </QLBadge>
          <QLButton
            variant="outline"
            size="sm"
            onClick={fetchData}
            disabled={loading}
            icon={<RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />}
          >
            Refresh
          </QLButton>
        </div>
      </div>

      {/* Security Guarantee Banner */}
      <div className="p-4 rounded-xl border border-emerald-500/30 bg-emerald-950/20 flex flex-col sm:flex-row sm:items-center justify-between gap-4 text-xs font-mono text-emerald-300">
        <div className="flex items-start gap-3">
          <ShieldCheck className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
          <div className="space-y-0.5">
            <span className="font-bold text-text-primary block">STRICT SECURITY ENCLAVE:</span>
            <p className="text-text-secondary leading-relaxed font-sans text-xs">
              QuantLab never asks for or stores your trading PIN, Groww/broker password, MPIN, OTP, or bank credentials. All connections use official authorized API keys and OAuth tokens encrypted at rest.
            </p>
          </div>
        </div>
      </div>

      {successMsg && (
        <div className="p-3.5 rounded-lg border border-emerald-500/30 bg-emerald-500/10 text-emerald-300 text-xs font-mono flex items-center justify-between">
          <span>{successMsg}</span>
          <button onClick={() => setSuccessMsg(null)} className="text-text-muted hover:text-text-primary">✕</button>
        </div>
      )}

      {/* Active Connections */}
      {connections.length > 0 && (
        <QLSection
          number={1}
          title="Active Broker Connections"
          subtitle="Synchronized Live Portfolios & Order Channels"
        >
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {connections.map((conn) => (
              <QLPanel key={conn.id} variant="surface" padding="md" className="border-border">
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <span className="text-base font-bold font-mono text-text-primary block">
                        {conn.provider}
                      </span>
                      <span className="text-xs font-mono text-text-muted">
                        Account: {conn.account_identifier}
                      </span>
                    </div>
                    <QLBadge variant="positive" size="xs" dot>
                      CONNECTED
                    </QLBadge>
                  </div>

                  <div className="grid grid-cols-3 gap-2 font-mono text-xs pt-2 border-t border-border/50">
                    <div className="p-2 rounded bg-surface-elevated/40">
                      <span className="text-[10px] text-text-muted block">NSE</span>
                      <span className="font-bold text-emerald-400">{conn.nse_enabled ? 'ACTIVE' : 'DISABLED'}</span>
                    </div>
                    <div className="p-2 rounded bg-surface-elevated/40">
                      <span className="text-[10px] text-text-muted block">BSE</span>
                      <span className="font-bold text-emerald-400">{conn.bse_enabled ? 'ACTIVE' : 'DISABLED'}</span>
                    </div>
                    <div className="p-2 rounded bg-surface-elevated/40">
                      <span className="text-[10px] text-text-muted block">F&amp;O</span>
                      <span className="font-bold text-text-muted">{conn.fno_enabled ? 'ENABLED' : 'OFF'}</span>
                    </div>
                  </div>

                  <div className="pt-2 flex items-center justify-between text-xs font-mono text-text-muted border-t border-border/50">
                    <span>Synced: {new Date(conn.last_sync_at).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })}</span>
                    <div className="flex items-center gap-2">
                      <QLButton
                        variant="outline"
                        size="sm"
                        onClick={() => handleSync(conn.id)}
                        disabled={syncingId === conn.id}
                      >
                        <RefreshCw className={`w-3.5 h-3.5 ${syncingId === conn.id ? 'animate-spin text-accent' : ''}`} />
                        <span>Sync</span>
                      </QLButton>
                      <button
                        onClick={() => handleDisconnect(conn.id)}
                        className="p-1.5 rounded text-rose-400 hover:bg-rose-500/10 transition-colors"
                        title="Disconnect Broker"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              </QLPanel>
            ))}
          </div>
        </QLSection>
      )}

      {/* Available Adapters */}
      <QLSection
        number={connections.length > 0 ? 2 : 1}
        title="Supported Broker Adapters"
        subtitle="Official Trading Gateways & Integration Readiness"
      >
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {brokers.map((broker) => {
            const isConnected = connections.some((c) => c.provider === broker.provider);
            return (
              <QLPanel
                key={broker.provider}
                variant="surface"
                padding="md"
                className="border-border flex flex-col justify-between"
              >
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-base font-bold font-mono text-text-primary">
                      {broker.name}
                    </span>
                    <QLBadge
                      variant={isConnected ? 'positive' : broker.is_connectable ? 'neutral' : 'warning'}
                      size="xs"
                    >
                      {isConnected ? 'CONNECTED' : broker.status.replace('_', ' ')}
                    </QLBadge>
                  </div>

                  <p className="text-xs text-text-secondary font-sans leading-relaxed">
                    {broker.description}
                  </p>

                  <div className="flex flex-wrap gap-1.5 pt-1">
                    {broker.supported_segments.map((seg) => (
                      <span
                        key={seg}
                        className="text-[10px] font-mono px-2 py-0.5 rounded bg-surface-elevated/60 text-text-muted border border-border/40"
                      >
                        {seg}
                      </span>
                    ))}
                  </div>
                </div>

                <div className="pt-4 mt-4 border-t border-border/50">
                  {isConnected ? (
                    <span className="text-xs font-mono text-emerald-400 flex items-center gap-1.5 font-bold">
                      <ShieldCheck className="w-4 h-4" />
                      <span>Synchronized</span>
                    </span>
                  ) : broker.is_connectable ? (
                    <QLButton
                      variant="primary"
                      size="sm"
                      className="w-full justify-center font-mono"
                      onClick={() => {
                        setSelectedBroker(broker.provider);
                        setError(null);
                      }}
                    >
                      <span>Connect {broker.name}</span>
                      <ExternalLink className="w-3.5 h-3.5 ml-1.5" />
                    </QLButton>
                  ) : (
                    <span className="text-xs font-mono text-text-muted">Adapter in certification</span>
                  )}
                </div>
              </QLPanel>
            );
          })}
        </div>
      </QLSection>

      {/* Connect Modal / Panel */}
      {selectedBroker && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-surface border border-border rounded-xl p-6 space-y-6 shadow-2xl">
            <div className="flex items-center justify-between border-b border-border/60 pb-4">
              <div>
                <h3 className="text-lg font-bold font-mono text-text-primary">
                  CONNECT {selectedBroker}
                </h3>
                <p className="text-xs text-text-muted font-sans mt-0.5">
                  Enter your official API Key or OAuth Access Token
                </p>
              </div>
              <button
                onClick={() => setSelectedBroker(null)}
                className="text-text-muted hover:text-text-primary text-sm font-mono"
              >
                ✕
              </button>
            </div>

            {error && (
              <div className="p-3 rounded-lg border border-rose-500/30 bg-rose-500/10 text-rose-300 text-xs font-mono flex items-start gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleConnect} className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-xs font-mono font-bold text-text-secondary uppercase block">
                  API Key / Client ID
                </label>
                <input
                  type="text"
                  value={apiKey}
                  onChange={(e) => setApiKey(e.target.value)}
                  placeholder="e.g. groww_client_key_..."
                  className="w-full bg-surface-elevated border border-border rounded-lg px-3 py-2 text-sm text-text-primary placeholder:text-text-muted font-mono focus:outline-none focus:border-accent"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-mono font-bold text-text-secondary uppercase block">
                  OAuth Access Token (Bearer)
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
                  <input
                    type="password"
                    value={accessToken}
                    onChange={(e) => setAccessToken(e.target.value)}
                    placeholder="eyJhbGciOi..."
                    className="w-full bg-surface-elevated border border-border rounded-lg pl-9 pr-3 py-2 text-sm text-text-primary placeholder:text-text-muted font-mono focus:outline-none focus:border-accent"
                  />
                </div>
              </div>

              <div className="p-3 rounded-lg bg-surface-elevated/40 border border-border/40 text-[11px] font-mono text-text-muted leading-relaxed">
                Tokens are encrypted at rest using AES/HMAC before storage.
              </div>

              <div className="flex gap-3 pt-2">
                <QLButton
                  type="button"
                  variant="outline"
                  size="md"
                  className="flex-1 justify-center"
                  onClick={() => setSelectedBroker(null)}
                >
                  Cancel
                </QLButton>
                <QLButton
                  type="submit"
                  variant="primary"
                  size="md"
                  className="flex-1 justify-center font-mono font-bold"
                  disabled={connecting || (!apiKey && !accessToken)}
                >
                  <span>{connecting ? 'AUTHORIZING...' : 'AUTHORIZE LINK'}</span>
                </QLButton>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
