import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldCheck, ArrowRight, AlertCircle, Lock, Mail, User } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { QLButton } from '@/design-system/QLButton';
import { QLPanel } from '@/design-system/QLPanel';

export function SignupPage() {
  const navigate = useNavigate();
  const { signup } = useAuth();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [agreeTerms, setAgreeTerms] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    if (!agreeTerms) {
      setError('Please agree to the terms and privacy policy.');
      return;
    }

    setLoading(true);

    try {
      const res = await fetch('/api/v1/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, password }),
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.detail || 'Registration failed. Please try again.');
      }

      signup(data.token, data.user);
      navigate('/onboarding');
    } catch (err: any) {
      setError(err.message || 'An error occurred during registration.');
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
            <span>INSTITUTIONAL QUANTITATIVE TERMINAL</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-black font-mono tracking-tight text-text-primary uppercase">
            CREATE ACCOUNT
          </h1>
          <p className="text-xs sm:text-sm font-mono text-text-muted tracking-wide">
            ENTER THE QUANTLAB RESEARCH ECOSYSTEM.
          </p>
        </div>

        {/* Signup Card */}
        <QLPanel variant="surface" padding="lg" className="border-border">
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="p-3.5 rounded-lg border border-rose-500/30 bg-rose-500/10 text-rose-300 text-xs font-mono flex items-start gap-2.5">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            <div className="space-y-1.5">
              <label className="text-xs font-mono font-bold text-text-secondary uppercase tracking-wider block">
                Full Name
              </label>
              <div className="relative">
                <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Arjun Sharma"
                  className="w-full bg-surface-elevated border border-border rounded-lg pl-10 pr-3.5 py-2 text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent font-sans transition-colors"
                />
              </div>
            </div>

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
                  className="w-full bg-surface-elevated border border-border rounded-lg pl-10 pr-3.5 py-2 text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent font-sans transition-colors"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-mono font-bold text-text-secondary uppercase tracking-wider block">
                Password
              </label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Minimum 6 characters"
                  className="w-full bg-surface-elevated border border-border rounded-lg pl-10 pr-3.5 py-2 text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent font-sans transition-colors"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-mono font-bold text-text-secondary uppercase tracking-wider block">
                Confirm Password
              </label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
                <input
                  type="password"
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Re-enter password"
                  className="w-full bg-surface-elevated border border-border rounded-lg pl-10 pr-3.5 py-2 text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent font-sans transition-colors"
                />
              </div>
            </div>

            <div className="pt-1 font-mono text-xs text-text-muted">
              <label className="flex items-start gap-2 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={agreeTerms}
                  onChange={(e) => setAgreeTerms(e.target.checked)}
                  className="rounded border-border bg-surface-elevated text-accent focus:ring-0 cursor-pointer mt-0.5"
                />
                <span className="text-[11px] leading-relaxed">
                  I agree to the QuantLab terms of service and acknowledge that model recommendations are decision-support tools with ₹0 capital risk in simulation.
                </span>
              </label>
            </div>

            <QLButton
              type="submit"
              variant="primary"
              size="lg"
              className="w-full justify-center font-mono font-bold mt-2"
              disabled={loading}
            >
              <span>{loading ? 'CREATING ACCOUNT...' : 'CREATE QUANTLAB ACCOUNT'}</span>
              {!loading && <ArrowRight className="w-4 h-4" />}
            </QLButton>
          </form>

          <div className="mt-6 pt-6 border-t border-border/60 text-center font-mono text-xs text-text-muted">
            <span>Already registered? </span>
            <Link to="/login" className="text-accent hover:underline font-bold">
              Sign in
            </Link>
          </div>
        </QLPanel>
      </div>
    </div>
  );
}
