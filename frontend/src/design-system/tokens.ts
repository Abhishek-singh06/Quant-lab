/**
 * QuantLab Institutional Design System Tokens
 * Recreating the visual rhythm, dark editorial hierarchy, and precision of high-end intelligence terminals.
 */

export const QL_COLORS = {
  bg: {
    base: '#07070a',
    alt: '#0b0c12',
    surface: '#0f1118',
    surfaceElevated: '#151822',
    surfaceHover: '#1b1e2b',
  },
  border: {
    hairline: 'rgba(255, 255, 255, 0.07)',
    subtle: '#181a26',
    default: '#232636',
    strong: '#32364c',
  },
  text: {
    primary: '#f3f4f6',
    secondary: '#9ca3af',
    muted: '#606575',
    subdued: '#3e4252',
  },
  signal: {
    positive: '#10b981', // emerald-500
    negative: '#f43f5e', // rose-500
    warning: '#f59e0b',  // amber-500
    info: '#38bdf8',     // sky-400
    accent: '#6366f1',   // indigo-500
  },
} as const;

export type SignalType = 'positive' | 'negative' | 'warning' | 'info' | 'neutral' | 'accent';
export type MarketState = 'OPEN' | 'CLOSED' | 'PRE_MARKET' | 'POST_MARKET';
export type DataState = 'LIVE' | 'DELAYED' | 'EOD' | 'UNAVAILABLE' | 'TEST_FIXTURE';
export type PaperModeState = 'ACTIVE' | 'PAUSED' | 'EMERGENCY_STOP';
