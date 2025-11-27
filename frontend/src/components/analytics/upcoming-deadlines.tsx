'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { UpcomingDeadline } from '@/types/analytics.types';
import { Calendar, AlertTriangle } from 'lucide-react';
import { cn } from '@/lib/utils';

interface UpcomingDeadlinesProps {
  deadlines: UpcomingDeadline[];
}

export function UpcomingDeadlines({ deadlines }: UpcomingDeadlinesProps) {
  if (!deadlines || deadlines.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Upcoming Deadlines</CardTitle>
        </CardHeader>
        <CardContent className="flex h-[200px] items-center justify-center">
          <p className="text-muted-foreground">No upcoming deadlines</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Upcoming Deadlines</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {deadlines.slice(0, 5).map((deadline) => (
            <div
              key={deadline.taskId}
              className="flex items-start justify-between gap-4 rounded-lg border p-3"
            >
              <div className="flex-1 space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-medium text-muted-foreground">
                    {deadline.taskKey}
                  </span>
                  {deadline.priority && (
                    <Badge
                      variant="outline"
                      style={{
                        borderColor: deadline.priorityColor || undefined,
                        color: deadline.priorityColor || undefined,
                      }}
                    >
                      {deadline.priority}
                    </Badge>
                  )}
                </div>
                <p className="text-sm font-medium">{deadline.taskTitle}</p>
                <p className="text-xs text-muted-foreground">{deadline.projectName}</p>
              </div>
              <div className="flex flex-col items-end gap-1">
                <div className="flex items-center gap-1 text-xs text-muted-foreground">
                  <Calendar className="h-3 w-3" />
                  {new Date(deadline.dueDate).toLocaleDateString()}
                </div>
                <span
                  className={cn(
                    'flex items-center gap-1 text-xs font-medium',
                    deadline.daysRemaining <= 1
                      ? 'text-red-600'
                      : deadline.daysRemaining <= 3
                        ? 'text-yellow-600'
                        : 'text-muted-foreground'
                  )}
                >
                  {deadline.daysRemaining <= 1 && <AlertTriangle className="h-3 w-3" />}
                  {deadline.daysRemaining === 0
                    ? 'Today'
                    : deadline.daysRemaining === 1
                      ? 'Tomorrow'
                      : `${deadline.daysRemaining} days left`}
                </span>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
