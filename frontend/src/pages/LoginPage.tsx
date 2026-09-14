import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldCheck, ArrowRight, AlertCircle, Lock, Mail } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const res = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.detail || 'Login failed. Please verify your credentials.');
      }

      login(data.token, data.user);
      navigate('/portfolio');
    } catch (err: any) {
      setError(err.message || 'An error occurred during authentication.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[85vh] flex flex-col justify-center items-center px-4 py-12">
      <div className="w-full max-w-md space-y-8">
        {/* Brand Header */}
        <div className="text-center space-y-2">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-accent-muted border border-accent/20 text-accent text-xs font-mono mb-2">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>QUANTLAB SECURE TERMINAL</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            QUANTLAB
          </h1>
          <p className="text-xs sm:text-sm font-mono text-text-muted tracking-wide">
            MARKET INTELLIGENCE WITHOUT THE NOISE.
          </p>
        </div>

        {/* Login Card */}
        <QLPanel variant="surface" padding="lg" className="border-border">
          <form onSubmit={handleSubmit} className="space-y-5">
            {error && (
              <div className="p-3.5 rounded-lg border border-rose-500/30 bg-rose-500/10 text-rose-300 text-xs font-mono flex items-start gap-2.5">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            <div className="space-y-1.5">
              <label className="text-xs font-mono font-bold text-text-secondary uppercase tracking-wider block">
                Email Address
              </label>
              <div className="relative">
                <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="trader@quantlab.io"
                  className="w-full bg-surface-elevated border border-border rounded-lg pl-10 pr-3.5 py-2.5 text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent font-sans transition-colors"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label className="text-xs font-mono font-bold text-text-secondary uppercase tracking-wider block">
                  Password
                </label>
                <span className="text-[11px] font-mono text-text-muted hover:text-text-primary cursor-pointer">
                  Forgot password?
                </span>
              </div>
              <div className="relative">
                <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••••••"
                  className="w-full bg-surface-elevated border border-border rounded-lg pl-10 pr-3.5 py-2.5 text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent font-sans transition-colors"
                />
              </div>
            </div>

            <div className="flex items-center justify-between pt-1 font-mono text-xs text-text-muted">
              <label className="flex items-center gap-2 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="rounded border-border bg-surface-elevated text-accent focus:ring-0 cursor-pointer"
                />
                <span>Remember session</span>
              </label>
              <span className="text-[10px] text-text-muted">PBKDF2 SHA-256</span>
            </div>

            <QLButton
              type="submit"
              variant="primary"
              size="lg"
              className="w-full justify-center font-mono font-bold"
              disabled={loading}
            >
              <span>{loading ? 'AUTHENTICATING...' : 'SIGN IN'}</span>
              {!loading && <ArrowRight className="w-4 h-4" />}
            </QLButton>
          </form>

          <div className="mt-6 pt-6 border-t border-border/60 text-center font-mono text-xs text-text-muted">
            <span>New to QuantLab? </span>
            <Link to="/signup" className="text-accent hover:underline font-bold">
              Create account
            </Link>
          </div>
        </QLPanel>

        {/* Security Notice */}
        <p className="text-[11px] font-mono text-center text-text-muted max-w-sm mx-auto leading-relaxed">
          QuantLab never requests or stores broker passwords or trading PINs. Authorized broker connections use official OAuth APIs.
        </p>
      </div>
    </div>
  );
}
