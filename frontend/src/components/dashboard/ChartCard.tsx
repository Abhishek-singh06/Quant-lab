import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

interface ChartPoint {
  date: string;
  value: number;
}

interface ChartCardProps {
  data?: ChartPoint[];
  loading?: boolean;
}

const HISTORICAL_NIFTY_YTD: ChartPoint[] = [
  { date: 'Jan', value: 21731 },
  { date: 'Feb', value: 21982 },
  { date: 'Mar', value: 22326 },
  { date: 'Apr', value: 22604 },
  { date: 'May', value: 22530 },
  { date: 'Jun', value: 24010 },
  { date: 'Jul', value: 24951 },
  { date: 'Aug', value: 25235 },
  { date: 'Sep', value: 24850 },
];

export function ChartCard({ data, loading }: ChartCardProps) {
  const chartData = data && data.length > 0 ? data : HISTORICAL_NIFTY_YTD;

  if (loading) {
    return (
      <Card className="animate-pulse">
        <CardHeader>
          <CardTitle>NIFTY 50 — Historical Benchmark Curve</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-[300px] bg-surface-elevated rounded-lg" />
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>NIFTY 50 — Historical Benchmark Curve</CardTitle>
          <div className="flex items-center gap-2">
            <Badge variant="outline" className="text-[10px] font-mono border-border text-text-secondary">
              EOD · YAHOO FINANCE
            </Badge>
            <Badge variant="outline" className="text-[10px] font-mono text-text-muted">
              BENCHMARK
            </Badge>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="h-[300px] w-full">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={chartData}>
              <defs>
                <linearGradient id="chartGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366f1" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#2a2a3e" />
              <XAxis
                dataKey="date"
                stroke="#55556a"
                fontSize={12}
                tickLine={false}
              />
              <YAxis
                stroke="#55556a"
                fontSize={12}
                tickLine={false}
                domain={['dataMin - 500', 'dataMax + 500']}
                tickFormatter={(value: number) => value.toLocaleString()}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#1a1a2e',
                  border: '1px solid #2a2a3e',
                  borderRadius: '8px',
                  color: '#f0f0f5',
                }}
                formatter={(value: any) => [Number(value).toLocaleString('en-IN', { maximumFractionDigits: 2 }), 'Close']}
              />
              <Area
                type="monotone"
                dataKey="value"
                stroke="#6366f1"
                strokeWidth={2}
                fillOpacity={1}
                fill="url(#chartGradient)"
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
