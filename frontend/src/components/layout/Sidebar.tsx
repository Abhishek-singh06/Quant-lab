import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  TrendingUp,
  BarChart3,
  Activity,
  Briefcase,
  Shield,
  FlaskConical,
  GitFork,
  Brain,
  Newspaper,
  Building2,
  LineChart,
  Terminal,
  Zap,
  Settings,
  ChevronLeft,
  ChevronRight,
  AlertTriangle,
} from 'lucide-react';
import { cn } from '@/lib/utils';

interface NavItem {
  icon: React.ElementType;
  label: string;
  href: string;
  badge?: string;
  badgeColor?: string;
  section?: string;
}

const navItems: NavItem[] = [
  // Overview
  { icon: LayoutDashboard, label: 'Overview', href: '/', section: 'Main' },
  { icon: Terminal, label: 'Market Terminal', href: '/terminal', section: 'Main', badge: 'NEW', badgeColor: 'text-accent' },
  { icon: TrendingUp, label: 'Markets', href: '/markets', section: 'Main' },
  { icon: BarChart3, label: 'Stock Analysis', href: '/stock/NIFTY', section: 'Main' },
  { icon: Activity, label: 'Market Pulse', href: '/market-pulse', section: 'Main' },
  // Intelligence
  { icon: Building2, label: 'Institutional', href: '/institutional', section: 'Intelligence' },
  { icon: LineChart, label: 'Mutual Funds', href: '/mutual-funds', section: 'Intelligence' },
  { icon: Brain, label: 'AI Research', href: '/ai-research', section: 'Intelligence' },
  // Quant Engine
  { icon: GitFork, label: 'Strategies', href: '/strategies', section: 'Quant Engine' },
  { icon: FlaskConical, label: 'Backtests', href: '/backtests', section: 'Quant Engine' },
  { icon: Newspaper, label: 'Models', href: '/models', section: 'Quant Engine' },
  // Execution
  { icon: Terminal, label: 'Paper Trading', href: '/paper-trading', section: 'Execution', badge: 'VIRTUAL', badgeColor: 'text-emerald-400' },
  { icon: Briefcase, label: 'Portfolio', href: '/portfolio', section: 'Execution' },
  { icon: Shield, label: 'Risk', href: '/risk', section: 'Execution' },
  // Production
  { icon: AlertTriangle, label: 'Monitoring', href: '/monitoring', section: 'Production', badge: 'ACTIVE', badgeColor: 'text-emerald-400' },
  { icon: Zap, label: 'Live Trading', href: '/live-trading', section: 'Production', badge: 'DISABLED', badgeColor: 'text-rose-400' },
];

const sections = ['Main', 'Intelligence', 'Quant Engine', 'Execution', 'Production'];

export function Sidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  const isActive = (href: string) => {
    if (href === '/') return location.pathname === '/';
    return location.pathname.startsWith(href);
  };

  return (
    <motion.aside
      initial={false}
      animate={{ width: collapsed ? 72 : 240 }}
      transition={{ duration: 0.2, ease: 'easeInOut' }}
      className="flex h-screen flex-col border-r border-border bg-surface overflow-hidden shrink-0"
    >
      {/* Logo */}
      <div className="flex h-16 items-center justify-between px-4 border-b border-border">
        <AnimatePresence mode="wait">
          {!collapsed && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="flex items-center gap-2"
            >
              <div className="flex h-8 w-8 items-center justify-center rounded-lg gradient-accent">
                <Activity className="h-4 w-4 text-white" />
              </div>
              <span className="text-lg font-bold gradient-text">QuantLab</span>
            </motion.div>
          )}
        </AnimatePresence>
        {collapsed && (
          <div className="flex h-8 w-8 items-center justify-center rounded-lg gradient-accent mx-auto">
            <Activity className="h-4 w-4 text-white" />
          </div>
        )}
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-4">
        {sections.map((section) => {
          const items = navItems.filter((item) => item.section === section);
          return (
            <div key={section}>
              <AnimatePresence mode="wait">
                {!collapsed && (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="px-3 pb-1 text-[10px] font-semibold uppercase tracking-widest text-text-muted"
                  >
                    {section}
                  </motion.div>
                )}
              </AnimatePresence>
              <div className="space-y-0.5">
                {items.map((item) => (
                  <button
                    key={item.href}
                    onClick={() => navigate(item.href)}
                    title={collapsed ? item.label : undefined}
                    className={cn(
                      'flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all',
                      isActive(item.href)
                        ? 'bg-accent-muted text-accent'
                        : 'text-text-secondary hover:bg-surface-elevated hover:text-text-primary'
                    )}
                  >
                    <item.icon className="h-4 w-4 shrink-0" />
                    <AnimatePresence mode="wait">
                      {!collapsed && (
                        <motion.div
                          initial={{ opacity: 0, width: 0 }}
                          animate={{ opacity: 1, width: 'auto' }}
                          exit={{ opacity: 0, width: 0 }}
                          className="flex flex-1 items-center justify-between overflow-hidden"
                        >
                          <span className="whitespace-nowrap">{item.label}</span>
                          {item.badge && (
                            <span className={cn('text-[9px] font-bold', item.badgeColor ?? 'text-accent')}>
                              {item.badge}
                            </span>
                          )}
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </button>
                ))}
              </div>
            </div>
          );
        })}
      </nav>

      {/* Bottom */}
      <div className="border-t border-border px-3 py-4 space-y-1">
        <button
          onClick={() => navigate('/settings')}
          className={cn(
            'flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all',
            isActive('/settings')
              ? 'bg-accent-muted text-accent'
              : 'text-text-secondary hover:bg-surface-elevated hover:text-text-primary'
          )}
        >
          <Settings className="h-4 w-4 shrink-0" />
          {!collapsed && <span>Settings</span>}
        </button>
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-text-muted hover:bg-surface-elevated hover:text-text-secondary transition-all"
        >
          {collapsed ? (
            <ChevronRight className="h-4 w-4 shrink-0" />
          ) : (
            <>
              <ChevronLeft className="h-4 w-4 shrink-0" />
              <span>Collapse</span>
            </>
          )}
        </button>
      </div>
    </motion.aside>
  );
}
