import { useEffect, useState } from 'react';
import { CheckCircle2, XCircle, Loader2 } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';

interface ServiceStatus {
  name: string;
  status: 'healthy' | 'unhealthy' | 'checking';
  latency?: number;
}

export function SystemStatusCard() {
  const [services, setServices] = useState<ServiceStatus[]>([
    { name: 'API Gateway', status: 'checking' },
    { name: 'Quant Engine', status: 'checking' },
    { name: 'Database', status: 'checking' },
    { name: 'Cache', status: 'checking' },
  ]);

  useEffect(() => {
    // Check health endpoints
    const checkHealth = async () => {
      const results: ServiceStatus[] = [];

      // Backend health
      try {
        const start = Date.now();
        const res = await fetch('/api/health');
        const latency = Date.now() - start;
        results.push({
          name: 'API Gateway',
          status: res.ok ? 'healthy' : 'unhealthy',
          latency,
        });
      } catch {
        results.push({ name: 'API Gateway', status: 'unhealthy' });
      }

      // Quant service health
      try {
        const start = Date.now();
        const res = await fetch('/api/quant/health');
        const latency = Date.now() - start;
        results.push({
          name: 'Quant Engine',
          status: res.ok ? 'healthy' : 'unhealthy',
          latency,
        });
      } catch {
        results.push({ name: 'Quant Engine', status: 'unhealthy' });
      }

      // For DB and Cache, infer from backend health
      results.push(
        { name: 'Database', status: results[0]?.status === 'healthy' ? 'healthy' : 'unhealthy' },
        { name: 'Cache', status: results[0]?.status === 'healthy' ? 'healthy' : 'unhealthy' }
      );

      setServices(results);
    };

    checkHealth();
    const interval = setInterval(checkHealth, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <Card>
      <CardHeader>
        <CardTitle>System Status</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {services.map((service) => (
            <div
              key={service.name}
              className="flex items-center justify-between rounded-lg border border-border-subtle bg-background px-4 py-3"
            >
              <div className="flex items-center gap-3">
                {service.status === 'checking' && (
                  <Loader2 className="h-4 w-4 animate-spin text-text-muted" />
                )}
                {service.status === 'healthy' && (
                  <CheckCircle2 className="h-4 w-4 text-success" />
                )}
                {service.status === 'unhealthy' && (
                  <XCircle className="h-4 w-4 text-danger" />
                )}
                <span className="text-sm font-medium">{service.name}</span>
              </div>
              {service.latency !== undefined && (
                <span className="text-xs text-text-muted font-mono">
                  {service.latency}ms
                </span>
              )}
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
