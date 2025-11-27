'use client';

import { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { StatsCard } from './stats-card';
import { TasksByStatusChart } from './tasks-by-status-chart';
import { TasksByPriorityChart } from './tasks-by-priority-chart';
import { BurndownChart } from './burndown-chart';
import { VelocityChart } from './velocity-chart';
import { analyticsService } from '@/services/analytics.service';
import {
  ProjectAnalytics,
  BurndownChart as BurndownData,
  VelocityChart as VelocityData,
} from '@/types/analytics.types';
import {
  CheckCircle2,
  Clock,
  AlertTriangle,
  ListTodo,
  Users,
  Timer,
} from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Progress } from '@/components/ui/progress';

interface ProjectAnalyticsViewProps {
  projectId: string;
}

export function ProjectAnalyticsView({ projectId }: ProjectAnalyticsViewProps) {
  const [analytics, setAnalytics] = useState<ProjectAnalytics | null>(null);
  const [burndown, setBurndown] = useState<BurndownData | null>(null);
  const [velocity, setVelocity] = useState<VelocityData | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const [analyticsData, burndownData, velocityData] = await Promise.all([
          analyticsService.getProjectAnalytics(projectId),
          analyticsService.getBurndownChart(projectId),
          analyticsService.getVelocityChart(projectId),
        ]);
        setAnalytics(analyticsData);
        setBurndown(burndownData);
        setVelocity(velocityData);
      } catch (error) {
        console.error('Failed to fetch analytics:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [projectId]);

  if (isLoading || !analytics) {
    return (
      <div className="flex h-[400px] items-center justify-center">
        <p className="text-muted-foreground">Loading analytics...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Stats Overview */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          title="Total Tasks"
          value={analytics.totalTasks}
          icon={ListTodo}
          description={`${analytics.completedTasks} completed`}
        />
        <StatsCard
          title="Completion Rate"
          value={`${analytics.completionRate}%`}
          icon={CheckCircle2}
          description={`${analytics.inProgressTasks} in progress`}
        />
        <StatsCard
          title="Overdue Tasks"
          value={analytics.overdueTasks}
          icon={AlertTriangle}
          description="Tasks past due date"
        />
        <StatsCard
          title="Hours Tracked"
          value={`${Number(analytics.spentHours).toFixed(1)}h`}
          icon={Timer}
          description={`${Number(analytics.estimatedHours).toFixed(1)}h estimated`}
        />
      </div>

      {/* Charts Row */}
      <div className="grid gap-6 md:grid-cols-2">
        <TasksByStatusChart data={analytics.tasksByStatus} />
        <TasksByPriorityChart data={analytics.tasksByPriority} />
      </div>

      {/* Burndown & Velocity */}
      <div className="grid gap-6 md:grid-cols-2">
        <BurndownChart data={burndown} />
        <VelocityChart data={velocity} />
      </div>

      {/* Team Performance */}
      {analytics.tasksByAssignee && analytics.tasksByAssignee.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Users className="h-4 w-4" />
              Team Performance
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {analytics.tasksByAssignee.map((member) => {
                const progress = member.totalTasks > 0
                  ? Math.round((member.completedTasks / member.totalTasks) * 100)
                  : 0;
                return (
                  <div key={member.userId} className="space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <Avatar className="h-8 w-8">
                          <AvatarImage src={member.avatarUrl || undefined} />
                          <AvatarFallback>
                            {member.userName
                              .split(' ')
                              .map((n) => n[0])
                              .join('')}
                          </AvatarFallback>
                        </Avatar>
                        <div>
                          <p className="text-sm font-medium">{member.userName}</p>
                          <p className="text-xs text-muted-foreground">
                            {member.completedTasks}/{member.totalTasks} tasks completed
                          </p>
                        </div>
                      </div>
                      <span className="text-sm font-medium">{progress}%</span>
                    </div>
                    <Progress value={progress} className="h-2" />
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
