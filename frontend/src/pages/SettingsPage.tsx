import { Settings, Database, Cpu, Bell, Shield, Info } from 'lucide-react';
import { MockBanner } from '@/components/dashboard/MockBanner';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';

function SettingsSection({ icon: Icon, title, children }: { icon: React.ElementType; title: string; children: React.ReactNode }) {
  return (
    <Card className="border-border">
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-sm font-semibold text-text-primary">
          <Icon className="h-4 w-4 text-accent" />
          {title}
        </CardTitle>
      </CardHeader>
      <CardContent>{children}</CardContent>
    </Card>
  );
}

function SettingRow({ label, value, description }: { label: string; value: string; description?: string }) {
  return (
    <div className="flex items-start justify-between gap-4 py-3 border-b border-border last:border-0">
      <div>
        <p className="text-sm font-medium text-text-primary">{label}</p>
        {description && <p className="text-xs text-text-muted mt-0.5">{description}</p>}
      </div>
      <span className="text-xs font-mono text-accent bg-accent-muted rounded px-2 py-1 shrink-0">{value}</span>
    </div>
  );
}

export function SettingsPage() {
  return (
    <div className="space-y-6">
      <MockBanner />
      <div className="flex items-center gap-3">
        <Settings className="h-6 w-6 text-accent" />
        <div>
          <h1 className="text-2xl font-bold gradient-text">Settings</h1>
          <p className="text-sm text-text-muted mt-0.5">System configuration and environment information</p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <SettingsSection icon={Database} title="Data Configuration">
          <SettingRow
            label="Market Data Provider"
            value="Mock / NSE"
            description="Live NSE feed requires authorized credentials"
          />
          <SettingRow
            label="Data Freshness Threshold"
            value="5 min"
            description="Staleness limit before STALE status is raised"
          />
          <SettingRow
            label="Historical Warehouse"
            value="PostgreSQL"
            description="Adjusted OHLCV with corporate action corrections"
          />
          <SettingRow
            label="Execution Mode"
            value="PAPER_TRADING"
            description="Zero real-money order routing"
          />
        </SettingsSection>

        <SettingsSection icon={Cpu} title="Quant Engine">
          <SettingRow
            label="Python Quant Service"
            value="FastAPI / Port 8001"
            description="ML inference, signal engine, risk engine"
          />
          <SettingRow
            label="Java Backend"
            value="Spring Boot / Port 8080"
            description="Data persistence, REST API, JPA"
          />
          <SettingRow
            label="Walk-Forward Retraining"
            value="Weekly"
            description="Expanding window with purged CV"
          />
          <SettingRow
            label="Model Horizon Coverage"
            value="1D / 1W / 1M / 3M / 1Y"
            description="Short, medium, and long-term predictions"
          />
        </SettingsSection>

        <SettingsSection icon={Bell} title="Alerts & Monitoring">
          <SettingRow
            label="Alert Retention"
            value="30 days"
            description="Resolved alerts purged after 30 days"
          />
          <SettingRow
            label="Health Check Interval"
            value="60 sec"
            description="System health polling frequency"
          />
          <SettingRow
            label="Incident Auto-Close"
            value="Disabled"
            description="Incidents require manual resolution"
          />
        </SettingsSection>

        <SettingsSection icon={Shield} title="Risk Controls">
          <SettingRow
            label="Max Portfolio Drawdown"
            value="10%"
            description="Trigger circuit breaker and alert"
          />
          <SettingRow
            label="Max Single Position Size"
            value="5% NAV"
            description="Per-symbol exposure cap"
          />
          <SettingRow
            label="Sector Concentration Limit"
            value="25% NAV"
            description="Per-sector exposure cap"
          />
          <SettingRow
            label="Kill Switch"
            value="Manual"
            description="Live trading emergency halt — manual trigger only"
          />
        </SettingsSection>
      </div>

      <SettingsSection icon={Info} title="System Information">
        <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
          {[
            { label: 'Version', value: 'QuantLab v20' },
            { label: 'Build', value: 'Part 20 — Final' },
            { label: 'Environment', value: 'Development' },
            { label: 'Database', value: 'PostgreSQL 15' },
          ].map(({ label, value }) => (
            <div key={label} className="rounded-lg border border-border bg-surface-elevated p-4 text-center">
              <p className="text-xs text-text-muted">{label}</p>
              <p className="mt-1 text-sm font-semibold text-text-primary">{value}</p>
            </div>
          ))}
        </div>
      </SettingsSection>
    </div>
  );
}
