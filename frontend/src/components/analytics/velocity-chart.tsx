'use client';

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { VelocityChart as VelocityChartData } from '@/types/analytics.types';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine,
} from 'recharts';

interface VelocityChartProps {
  data: VelocityChartData | null;
  isLoading?: boolean;
}

export function VelocityChart({ data, isLoading }: VelocityChartProps) {
  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Velocity Chart</CardTitle>
          <CardDescription>Tasks completed per week</CardDescription>
        </CardHeader>
        <CardContent className="flex h-[300px] items-center justify-center">
          <p className="text-muted-foreground">Loading...</p>
        </CardContent>
      </Card>
    );
  }

  if (!data || !data.dataPoints || data.dataPoints.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Velocity Chart</CardTitle>
          <CardDescription>Tasks completed per week</CardDescription>
        </CardHeader>
        <CardContent className="flex h-[300px] items-center justify-center">
          <p className="text-muted-foreground">No data available</p>
        </CardContent>
      </Card>
    );
  }

  const chartData = data.dataPoints.map((point) => ({
    period: point.period,
    tasks: point.tasksCompleted,
    hours: Number(point.hoursSpent),
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle>Velocity Chart</CardTitle>
        <CardDescription>
          Average velocity: {data.averageVelocity} tasks/week | Total completed: {data.totalTasksCompleted} tasks
        </CardDescription>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="period" tick={{ fontSize: 12 }} />
            <YAxis tick={{ fontSize: 12 }} />
            <Tooltip
              content={({ active, payload, label }) => {
                if (active && payload && payload.length) {
                  return (
                    <div className="rounded-lg border bg-background p-3 shadow-md">
                      <p className="font-medium">{label}</p>
                      <p className="text-sm text-muted-foreground">
                        Tasks: {payload[0].value}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        Hours: {payload[0].payload.hours.toFixed(1)}h
                      </p>
                    </div>
                  );
                }
                return null;
              }}
            />
            <ReferenceLine
              y={Number(data.averageVelocity)}
              stroke="#f59e0b"
              strokeDasharray="5 5"
              label={{ value: 'Avg', position: 'right', fill: '#f59e0b', fontSize: 12 }}
            />
            <Bar dataKey="tasks" fill="#3b82f6" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}
