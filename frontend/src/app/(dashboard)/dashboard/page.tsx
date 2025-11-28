'use client';

import { useEffect, useState } from 'react';
import { analyticsService } from '@/services/analytics.service';
import { DashboardOverview, AdminDashboard } from '@/types/analytics.types';
import { useAuthStore } from '@/stores/auth-store';
import {
  StatsCard,
  TasksByStatusChart,
  TasksByPriorityChart,
  UpcomingDeadlines,
  RecentActivityList,
} from '@/components/analytics';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Progress } from '@/components/ui/progress';
import {
  FolderKanban,
  CheckCircle2,
  AlertTriangle,
  Timer,
  Users,
  Layers,
  Clock,
  TrendingUp,
  Archive,
} from 'lucide-react';

export default function DashboardPage() {
  const { user } = useAuthStore();
  const isAdmin = user?.roles?.includes('ADMIN');

  const [userDashboard, setUserDashboard] = useState<DashboardOverview | null>(null);
  const [adminDashboard, setAdminDashboard] = useState<AdminDashboard | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        if (isAdmin) {
          const data = await analyticsService.getAdminDashboard();
          setAdminDashboard(data);
        } else {
          const data = await analyticsService.getDashboardOverview();
          setUserDashboard(data);
        }
      } catch (err) {
        console.error('Failed to fetch dashboard data:', err);
        setError('Failed to load dashboard data');
      } finally {
        setIsLoading(false);
      }
    };

    if (user) {
      fetchData();
    }
  }, [user, isAdmin]);

  const formatTime = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours}h ${mins}m`;
    }
    return `${mins}m`;
  };

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

  if (error) {
    return (
      <div className="flex h-[calc(100vh-200px)] items-center justify-center">
        <div className="text-center">
          <AlertTriangle className="mx-auto h-12 w-12 text-destructive" />
          <p className="mt-4 text-muted-foreground">{error}</p>
        </div>
      </div>
    );
  }

  // Admin Dashboard
  if (isAdmin && adminDashboard) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold">Admin Dashboard</h1>
          <p className="text-muted-foreground">System-wide overview and statistics</p>
        </div>

        {/* Main Stats */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <StatsCard
            title="Total Users"
            value={adminDashboard.totalUsers}
            icon={Users}
            description={`${adminDashboard.activeUsers} active, +${adminDashboard.newUsersThisMonth} this month`}
          />
          <StatsCard
            title="Projects"
            value={adminDashboard.totalProjects}
            icon={FolderKanban}
            description={`${adminDashboard.activeProjects} active, ${adminDashboard.archivedProjects} archived`}
          />
          <StatsCard
            title="Tasks"
            value={adminDashboard.totalTasks}
            icon={CheckCircle2}
            description={`${adminDashboard.completedTasks} completed, ${adminDashboard.overdueTasks} overdue`}
          />
          <StatsCard
            title="Teams"
            value={adminDashboard.totalTeams}
            icon={Layers}
            description="Active teams"
          />
        </div>

        {/* Secondary Stats */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <StatsCard
            title="Time Tracked (Total)"
            value={formatTime(adminDashboard.totalTimeTrackedMinutes)}
            icon={Timer}
            description="All time"
          />
          <StatsCard
            title="Time This Month"
            value={formatTime(adminDashboard.timeTrackedThisMonthMinutes)}
            icon={Clock}
            description="Current month"
          />
          <StatsCard
            title="Tasks Created"
            value={adminDashboard.tasksCreatedThisMonth}
            icon={TrendingUp}
            description="This month"
          />
        </div>

        {/* Top Projects and Users */}
        <div className="grid gap-6 md:grid-cols-2">
          {/* Top Projects */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FolderKanban className="h-5 w-5" />
                Top Projects
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {adminDashboard.topProjects.length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-4">
                    No projects yet
                  </p>
                ) : (
                  adminDashboard.topProjects.map((project) => {
                    const completionRate = project.taskCount > 0
                      ? Math.round((project.completedTaskCount / project.taskCount) * 100)
                      : 0;
                    return (
                      <div key={project.projectId} className="space-y-2">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="font-medium">{project.projectName}</p>
                            <p className="text-sm text-muted-foreground">
                              {project.projectKey} • {project.memberCount} members
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="font-medium">{project.taskCount} tasks</p>
                            <p className="text-sm text-muted-foreground">
                              {project.completedTaskCount} completed
                            </p>
                          </div>
                        </div>
                        <Progress value={completionRate} className="h-2" />
                      </div>
                    );
                  })
                )}
              </div>
            </CardContent>
          </Card>

          {/* Top Users */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Users className="h-5 w-5" />
                Top Contributors
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {adminDashboard.topUsers.length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-4">
                    No user activity yet
                  </p>
                ) : (
                  adminDashboard.topUsers.map((userStats) => {
                    const completionRate = userStats.totalTasks > 0
                      ? Math.round((userStats.completedTasks / userStats.totalTasks) * 100)
                      : 0;
                    return (
                      <div key={userStats.userId} className="flex items-center gap-3">
                        <Avatar className="h-10 w-10">
                          <AvatarImage src={userStats.avatarUrl || undefined} />
                          <AvatarFallback>
                            {userStats.userName.split(' ').map(n => n[0]).join('').toUpperCase()}
                          </AvatarFallback>
                        </Avatar>
                        <div className="flex-1 min-w-0">
                          <p className="font-medium truncate">{userStats.userName}</p>
                          <p className="text-sm text-muted-foreground">
                            {userStats.completedTasks}/{userStats.totalTasks} tasks • {formatTime(userStats.timeTrackedMinutes)}
                          </p>
                        </div>
                        <div className="text-right">
                          <p className="font-medium text-primary">{completionRate}%</p>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Recent Activity */}
        <RecentActivityList activities={adminDashboard.recentActivity} />
      </div>
    );
  }

  // User Dashboard
  if (!userDashboard) {
    return (
      <div className="flex h-[calc(100vh-200px)] items-center justify-center">
        <div className="text-center">
          <AlertTriangle className="mx-auto h-12 w-12 text-destructive" />
          <p className="mt-4 text-muted-foreground">Something went wrong</p>
        </div>
      </div>
    );
  }

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
          value={userDashboard.totalProjects}
          icon={FolderKanban}
          description={`${userDashboard.activeProjects} active`}
        />
        <StatsCard
          title="Total Tasks"
          value={userDashboard.totalTasks}
          icon={CheckCircle2}
          description={`${userDashboard.completedTasks} completed`}
        />
        <StatsCard
          title="Overdue"
          value={userDashboard.overdueTasks}
          icon={AlertTriangle}
          description="Tasks past due date"
        />
        <StatsCard
          title="Time This Month"
          value={formatTime(userDashboard.totalTimeSpentMinutes)}
          icon={Timer}
          description={`${userDashboard.completionRate}% completion rate`}
        />
      </div>

      {/* Charts Row */}
      <div className="grid gap-6 md:grid-cols-2">
        <TasksByStatusChart data={userDashboard.tasksByStatus} />
        <TasksByPriorityChart data={userDashboard.tasksByPriority} />
      </div>

      {/* Activity & Deadlines Row */}
      <div className="grid gap-6 md:grid-cols-2">
        <RecentActivityList activities={userDashboard.recentActivity} />
        <UpcomingDeadlines deadlines={userDashboard.upcomingDeadlines} />
      </div>
    </div>
  );
}
