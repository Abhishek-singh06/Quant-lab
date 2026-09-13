import { ShieldCheck, Radio } from 'lucide-react';

export function MockBanner() {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-amber-500/30 bg-amber-500/10 px-4 py-2.5 text-xs text-amber-300">
      <div className="flex items-center gap-2">
        <ShieldCheck className="h-4 w-4 shrink-0 text-amber-400" />
        <span>
          <strong className="text-amber-200">PAPER TRADING SIMULATION:</strong> Virtual Capital &bull; Real Money at Risk: <strong>₹0.00</strong> &bull; Live Broker Orders: <strong>DISABLED</strong>
        </span>
      </div>
      <div className="flex items-center gap-3 font-mono text-[11px] text-amber-400/90">
        <span className="flex items-center gap-1">
          <Radio className="h-3 w-3 text-amber-400 animate-pulse" />
          Feed: <strong>DELAYED MARKET DATA (~15m Delay via Yahoo Polling)</strong>
        </span>
        <span className="hidden sm:inline-block border-l border-amber-500/30 pl-3">
          Model: <strong>PHASE_16_FROZEN_RIDGE_TOP8_V1</strong>
        </span>
      </div>
    </div>
  );
}

