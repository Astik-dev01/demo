'use client';

import { useEffect, useState } from 'react';
import { analyticsService } from '@/services/analytics.service';
import { DashboardOverview } from '@/types/analytics.types';
import {
  StatsCard,
  TasksByStatusChart,
  TasksByPriorityChart,
  UpcomingDeadlines,
  RecentActivityList,
} from '@/components/analytics';
import {
  FolderKanban,
  CheckCircle2,
  AlertTriangle,
  Clock,
  Timer,
} from 'lucide-react';

export default function DashboardPage() {
  const [data, setData] = useState<DashboardOverview | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const overview = await analyticsService.getDashboardOverview();
        setData(overview);
      } catch (err) {
        console.error('Failed to fetch dashboard data:', err);
        setError('Failed to load dashboard data');
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  if (isLoading) {
    return (
      <div className="flex h-[calc(100vh-200px)] items-center justify-center">
        <div className="text-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent mx-auto" />
          <p className="mt-4 text-muted-foreground">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="flex h-[calc(100vh-200px)] items-center justify-center">
        <div className="text-center">
          <AlertTriangle className="mx-auto h-12 w-12 text-destructive" />
          <p className="mt-4 text-muted-foreground">{error || 'Something went wrong'}</p>
        </div>
      </div>
    );
  }

  const formatTime = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-muted-foreground">Overview of your work and progress</p>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          title="Projects"
          value={data.totalProjects}
          icon={FolderKanban}
          description={`${data.activeProjects} active`}
        />
        <StatsCard
          title="Total Tasks"
          value={data.totalTasks}
          icon={CheckCircle2}
          description={`${data.completedTasks} completed`}
        />
        <StatsCard
          title="Overdue"
          value={data.overdueTasks}
          icon={AlertTriangle}
          description="Tasks past due date"
        />
        <StatsCard
          title="Time This Month"
          value={formatTime(data.totalTimeSpentMinutes)}
          icon={Timer}
          description={`${data.completionRate}% completion rate`}
        />
      </div>

      {/* Charts Row */}
      <div className="grid gap-6 md:grid-cols-2">
        <TasksByStatusChart data={data.tasksByStatus} />
        <TasksByPriorityChart data={data.tasksByPriority} />
      </div>

      {/* Activity & Deadlines Row */}
      <div className="grid gap-6 md:grid-cols-2">
        <RecentActivityList activities={data.recentActivity} />
        <UpcomingDeadlines deadlines={data.upcomingDeadlines} />
      </div>
    </div>
  );
}
