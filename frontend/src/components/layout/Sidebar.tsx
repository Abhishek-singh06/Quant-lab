import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  TrendingUp,
  BarChart3,
  Activity,
  Terminal,
  Building2,
  LineChart,
  Brain,
  GitFork,
  FlaskConical,
  Cpu,
  Shield,
  Briefcase,
  AlertTriangle,
  Zap,
  Sliders,
  Settings,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { QLBadge } from '@/design-system/QLBadge';

interface NavItem {
  icon: React.ElementType;
  label: string;
  href: string;
  badge?: string;
  badgeVariant?: 'positive' | 'negative' | 'warning' | 'info' | 'accent' | 'neutral';
  group: 'MARKETS' | 'INTELLIGENCE' | 'QUANT' | 'PORTFOLIO' | 'SYSTEM';
}

const navItems: NavItem[] = [
  // MARKETS
  { icon: LayoutDashboard, label: 'Overview', href: '/', group: 'MARKETS' },
  { icon: TrendingUp, label: 'Marketplace', href: '/markets', group: 'MARKETS' },
  { icon: BarChart3, label: 'Stock Analysis', href: '/stock/RELIANCE', group: 'MARKETS' },
  { icon: Activity, label: 'Market Pulse', href: '/market-pulse', group: 'MARKETS' },
  { icon: Terminal, label: 'Market Terminal', href: '/terminal', group: 'MARKETS', badge: 'PRO', badgeVariant: 'accent' },

  // PORTFOLIO
  { icon: Briefcase, label: 'Your Portfolio', href: '/portfolio', group: 'PORTFOLIO' },
  { icon: Zap, label: 'Brokers & APIs', href: '/connect-broker', group: 'PORTFOLIO', badge: 'GROWW', badgeVariant: 'positive' },
  { icon: Sliders, label: 'Risk Profile', href: '/settings/investment-profile', group: 'PORTFOLIO' },
  { icon: Terminal, label: 'Paper Simulator', href: '/paper-trading', group: 'PORTFOLIO', badge: 'VIRTUAL', badgeVariant: 'positive' },

  // INTELLIGENCE
  { icon: Building2, label: 'Institutional', href: '/institutional', group: 'INTELLIGENCE' },
  { icon: LineChart, label: 'Mutual Funds', href: '/mutual-funds', group: 'INTELLIGENCE' },
  { icon: Brain, label: 'AI Research', href: '/ai-research', group: 'INTELLIGENCE' },

  // QUANT
  { icon: GitFork, label: 'Strategies', href: '/strategies', group: 'QUANT' },
  { icon: Cpu, label: 'Models', href: '/models', group: 'QUANT' },
  { icon: FlaskConical, label: 'Backtests', href: '/backtests', group: 'QUANT' },
  { icon: Shield, label: 'Risk Center', href: '/risk', group: 'QUANT' },

  // SYSTEM
  { icon: AlertTriangle, label: 'Monitoring', href: '/monitoring', group: 'SYSTEM', badge: 'ACTIVE', badgeVariant: 'positive' },
  { icon: Zap, label: 'Live Trading', href: '/live-trading', group: 'SYSTEM', badge: 'DISABLED', badgeVariant: 'negative' },
  { icon: Settings, label: 'Settings', href: '/settings', group: 'SYSTEM' },
];

const groups = ['MARKETS', 'INTELLIGENCE', 'QUANT', 'PORTFOLIO', 'SYSTEM'] as const;

export function Sidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  const isActive = (href: string) => {
    if (href === '/') return location.pathname === '/';
    if (href.startsWith('/stock/')) return location.pathname.startsWith('/stock');
    return location.pathname.startsWith(href);
  };

  return (
    <motion.aside
      initial={false}
      animate={{ width: collapsed ? 64 : 224 }}
      transition={{ duration: 0.15, ease: 'easeInOut' }}
      className="h-screen flex flex-col border-r border-border bg-[#090a0f] overflow-hidden shrink-0 select-none z-20"
    >
      {/* Navigation Groups */}
      <nav className="flex-1 overflow-y-auto px-2.5 py-4 space-y-5 no-scrollbar">
        {groups.map((group) => {
          const items = navItems.filter((item) => item.group === group);
          return (
            <div key={group} className="space-y-1">
              <AnimatePresence mode="wait">
                {!collapsed && (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="px-2 pb-1 text-[10px] font-bold uppercase tracking-widest text-text-muted font-mono"
                  >
                    {group}
                  </motion.div>
                )}
              </AnimatePresence>

              <div className="space-y-0.5">
                {items.map((item) => {
                  const active = isActive(item.href);
                  const Icon = item.icon;
                  return (
                    <button
                      key={item.href}
                      onClick={() => navigate(item.href)}
                      title={collapsed ? item.label : undefined}
                      className={cn(
                        'flex w-full items-center gap-2.5 rounded-md px-2.5 py-1.5 text-xs font-medium transition-colors cursor-pointer group text-left',
                        active
                          ? 'bg-surface-elevated text-text-primary font-semibold border border-border/80'
                          : 'text-text-secondary hover:bg-surface-elevated/50 hover:text-text-primary'
                      )}
                    >
                      <Icon
                        className={cn(
                          'w-4 h-4 shrink-0 transition-colors',
                          active ? 'text-accent' : 'text-text-muted group-hover:text-text-secondary'
                        )}
                      />
                      <AnimatePresence mode="wait">
                        {!collapsed && (
                          <motion.div
                            initial={{ opacity: 0, width: 0 }}
                            animate={{ opacity: 1, width: 'auto' }}
                            exit={{ opacity: 0, width: 0 }}
                            className="flex flex-1 items-center justify-between overflow-hidden"
                          >
                            <span className="truncate">{item.label}</span>
                            {item.badge && (
                              <QLBadge
                                variant={item.badgeVariant || 'neutral'}
                                size="xs"
                                className="ml-1.5"
                              >
                                {item.badge}
                              </QLBadge>
                            )}
                          </motion.div>
                        )}
                      </AnimatePresence>
                    </button>
                  );
                })}
              </div>
            </div>
          );
        })}
      </nav>

      {/* Collapse Rail Trigger */}
      <div className="border-t border-border p-2.5 bg-surface/50">
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="flex w-full items-center justify-center gap-2 rounded-md p-1.5 text-xs font-mono text-text-muted hover:bg-surface-elevated hover:text-text-primary transition-colors cursor-pointer"
          title={collapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
        >
          {collapsed ? (
            <ChevronRight className="w-4 h-4" />
          ) : (
            <>
              <ChevronLeft className="w-4 h-4" />
              <span className="text-[11px] uppercase tracking-wider">Collapse Rail</span>
            </>
          )}
        </button>
      </div>
    </motion.aside>
  );
}
