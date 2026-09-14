import { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { Search, Bell, User, LogOut } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { InstrumentSearchModal } from '@/components/terminal/InstrumentSearchModal';
import { QLBadge } from '@/design-system/QLBadge';
import { QLButton } from '@/design-system/QLButton';
import type { InstrumentSearchResult } from '@/types/terminal';
import { api } from '@/lib/api';

export function Header() {
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [paperSession, setPaperSession] = useState<any>(null);
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setIsSearchOpen(true);
      } else if (
        e.key === '/' &&
        document.activeElement?.tagName !== 'INPUT' &&
        document.activeElement?.tagName !== 'TEXTAREA'
      ) {
        e.preventDefault();
        setIsSearchOpen(true);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  useEffect(() => {
    api.get<any>('/v1/paper/session')
      .then((data) => setPaperSession(data))
      .catch(() => setPaperSession(null));
  }, [location.pathname]);

  const handleSelectInstrument = (instrument: InstrumentSearchResult) => {
    navigate(`/stock/${instrument.symbol}`);
  };

  const realMoneyAtRisk = paperSession?.real_money_at_risk ?? 0;

  return (
    <>
      <header className="h-14 border-b border-border bg-[#090a0f] px-4 sm:px-6 flex items-center justify-between gap-4 select-none shrink-0 sticky top-0 z-30">
        {/* Left: Brand Identity & Terminal Workspace Indicator */}
        <div className="flex items-center gap-3 shrink-0">
          <Link to="/" className="flex items-center gap-2 group cursor-pointer">
            <div className="w-7 h-7 rounded-md bg-surface-elevated border border-border flex items-center justify-center font-black text-xs text-text-primary group-hover:border-accent transition-colors">
              <span className="text-accent font-mono">Q</span>L
            </div>
            <div className="flex flex-col">
              <span className="font-extrabold text-sm tracking-wider uppercase text-text-primary font-mono group-hover:text-accent transition-colors">
                QUANTLAB
              </span>
              <span className="text-[9px] text-text-muted uppercase tracking-widest font-mono hidden sm:inline">
                TERMINAL v2.0
              </span>
            </div>
          </Link>
        </div>

        {/* Center: Command Palette Trigger */}
        <div className="flex-1 max-w-xl mx-auto">
          <button
            type="button"
            onClick={() => setIsSearchOpen(true)}
            className="flex items-center justify-between w-full h-9 rounded-md bg-surface-elevated/80 border border-border/80 px-3 text-xs text-text-muted hover:border-border-strong hover:text-text-secondary transition-all cursor-pointer group"
          >
            <div className="flex items-center gap-2 truncate">
              <Search className="w-3.5 h-3.5 text-text-muted group-hover:text-accent transition-colors shrink-0" />
              <span className="truncate">Search stocks, ETFs, mutual funds, commodities (⌘K)...</span>
            </div>
            <div className="flex items-center gap-1 shrink-0">
              <kbd className="hidden sm:inline-flex h-5 items-center px-1.5 rounded bg-surface border border-border font-mono text-[10px] text-text-muted">
                ⌘K
              </kbd>
            </div>
          </button>
        </div>

        {/* Right: Telemetry & User Controls */}
        <div className="flex items-center gap-2 shrink-0">
          {/* Status strip */}
          <div className="hidden lg:flex items-center gap-2 font-mono text-[11px]">
            <div className="flex items-center gap-1.5 px-2 py-1 rounded bg-surface-elevated/60 border border-border/60">
              <span className="text-text-muted text-[10px]">NSE</span>
              <QLBadge variant="warning" size="xs" dot>
                CLOSED
              </QLBadge>
            </div>

            <div className="flex items-center gap-1.5 px-2 py-1 rounded bg-surface-elevated/60 border border-border/60">
              <span className="text-text-muted text-[10px]">DATA</span>
              <QLBadge variant="warning" size="xs">
                DELAYED
              </QLBadge>
            </div>

            <div className="flex items-center gap-1.5 px-2 py-1 rounded bg-surface-elevated/60 border border-border/60">
              <span className="text-text-muted text-[10px]">RISK</span>
              <span className="text-emerald-400 font-bold">
                ₹{realMoneyAtRisk.toFixed(2)}
              </span>
            </div>
          </div>

          {/* User Account Controls */}
          {isAuthenticated && user ? (
            <div className="flex items-center gap-2 pl-2 border-l border-border/60">
              <Link
                to="/settings/investment-profile"
                className="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-surface-elevated hover:bg-surface border border-border text-xs font-mono text-text-primary transition-colors"
              >
                <User className="w-3.5 h-3.5 text-accent" />
                <span className="font-bold truncate max-w-[100px]">{user.name.split(' ')[0]}</span>
              </Link>
              <button
                onClick={logout}
                className="p-1.5 rounded-md text-text-muted hover:text-rose-400 hover:bg-rose-500/10 transition-colors cursor-pointer"
                title="Sign out"
              >
                <LogOut className="w-3.5 h-3.5" />
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link to="/login">
                <QLButton variant="outline" size="sm" className="font-mono text-xs">
                  Sign In
                </QLButton>
              </Link>
              <Link to="/signup" className="hidden sm:inline-flex">
                <QLButton variant="primary" size="sm" className="font-mono text-xs font-bold">
                  Sign Up
                </QLButton>
              </Link>
            </div>
          )}

          {/* Notifications */}
          <button
            onClick={() => navigate('/monitoring')}
            className="p-2 rounded-md text-text-muted hover:text-text-primary hover:bg-surface-elevated transition-colors relative cursor-pointer"
            title="System Observability"
          >
            <Bell className="w-4 h-4" />
            <span className="w-1.5 h-1.5 rounded-full bg-accent absolute top-1.5 right-1.5" />
          </button>
        </div>
      </header>

      <InstrumentSearchModal
        isOpen={isSearchOpen}
        onClose={() => setIsSearchOpen(false)}
        onSelect={handleSelectInstrument}
      />
    </>
  );
}
