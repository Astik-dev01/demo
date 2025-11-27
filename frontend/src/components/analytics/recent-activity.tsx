'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { RecentActivity } from '@/types/analytics.types';
import { Activity, FileText, CheckCircle, Edit } from 'lucide-react';

interface RecentActivityListProps {
  activities: RecentActivity[];
}

const getActivityIcon = (type: string) => {
  switch (type) {
    case 'created':
      return FileText;
    case 'completed':
      return CheckCircle;
    case 'updated':
    default:
      return Edit;
  }
};

export function RecentActivityList({ activities }: RecentActivityListProps) {
  if (!activities || activities.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Activity className="h-4 w-4" />
            Recent Activity
          </CardTitle>
        </CardHeader>
        <CardContent className="flex h-[200px] items-center justify-center">
          <p className="text-muted-foreground">No recent activity</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-base">
          <Activity className="h-4 w-4" />
          Recent Activity
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {activities.slice(0, 5).map((activity, index) => {
            const Icon = getActivityIcon(activity.activityType);
            return (
              <div key={`${activity.taskId}-${index}`} className="flex items-start gap-3">
                <div className="rounded-full bg-muted p-2">
                  <Icon className="h-4 w-4 text-muted-foreground" />
                </div>
                <div className="flex-1 space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-medium text-primary">{activity.taskKey}</span>
                    <span className="text-sm font-medium">{activity.taskTitle}</span>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {activity.description} by {activity.userName}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    {new Date(activity.timestamp).toLocaleString()}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}
