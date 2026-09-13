import { api } from '@/lib/api';

export interface HealthStatus {
  status: string;
  service: string;
  timestamp: string;
  version: string;
}

export interface SystemHealth {
  backend: HealthStatus | null;
  quantService: HealthStatus | null;
  overall: 'healthy' | 'degraded' | 'unhealthy';
}

export async function checkBackendHealth(): Promise<HealthStatus> {
  return api.get<HealthStatus>('/health');
}

export async function checkQuantServiceHealth(): Promise<HealthStatus> {
  return api.get<HealthStatus>('/quant/health');
}
