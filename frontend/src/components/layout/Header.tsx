import { Bell, Search, User } from 'lucide-react';
import { Badge } from '@/components/ui/badge';

export function Header() {
  return (
    <header className="flex h-16 items-center justify-between border-b border-border bg-surface px-6">
      {/* Search */}
      <div className="flex items-center gap-3 flex-1 max-w-md">
        <div className="flex items-center gap-2 rounded-lg border border-border bg-background px-3 py-2 w-full">
          <Search className="h-4 w-4 text-text-muted" />
          <input
            type="text"
            placeholder="Search stocks, indices, or news..."
            className="bg-transparent text-sm text-text-primary placeholder:text-text-muted outline-none w-full"
          />
          <kbd className="hidden sm:inline-flex h-5 items-center gap-1 rounded border border-border bg-surface-elevated px-1.5 text-[10px] font-medium text-text-muted">
            ⌘K
          </kbd>
        </div>
      </div>

      {/* Right section */}
      <div className="flex items-center gap-3">
        <div className="hidden sm:flex items-center gap-2">
          <Badge variant="outline" className="text-[10px] font-mono border-amber-500/40 text-amber-400 bg-amber-500/10 py-1">
            DELAYED FEED (~15m)
          </Badge>
          <Badge variant="outline" className="text-[10px] font-mono border-emerald-500/40 text-emerald-400 bg-emerald-500/10 py-1">
            PAPER MODE (₹0 RISK)
          </Badge>
        </div>
        <button className="relative p-2 text-text-secondary hover:text-text-primary transition-colors" title="Notifications">
          <Bell className="h-5 w-5" />
          <span className="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-accent" />
        </button>
        <button className="flex items-center gap-2 rounded-lg border border-border px-3 py-1.5 text-sm text-text-secondary hover:bg-surface-elevated transition-colors">
          <User className="h-4 w-4" />
          <span className="hidden md:inline">Account</span>
        </button>
      </div>
    </header>
  );
}
