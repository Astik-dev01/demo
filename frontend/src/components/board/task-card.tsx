'use client';

import { TaskListItem } from '@/types/task.types';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { MessageSquare, Calendar, AlertCircle } from 'lucide-react';
import { format, parseISO } from 'date-fns';

interface TaskCardProps {
  task: TaskListItem;
  onClick?: () => void;
}

export function TaskCard({ task, onClick }: TaskCardProps) {
  return (
    <Card
      className="cursor-pointer hover:shadow-md transition-shadow mb-2"
      onClick={onClick}
    >
      <CardContent className="p-3">
        <div className="flex items-start justify-between gap-2 mb-2">
          <span className="text-xs text-muted-foreground font-mono">{task.key}</span>
          {task.priorityColor && (
            <div
              className="w-2 h-2 rounded-full"
              style={{ backgroundColor: task.priorityColor }}
              title={task.priorityName || undefined}
            />
          )}
        </div>

        <h4 className="text-sm font-medium line-clamp-2 mb-2">{task.title}</h4>

        {task.tagNames && task.tagNames.length > 0 && (
          <div className="flex flex-wrap gap-1 mb-2">
            {task.tagNames.slice(0, 3).map((tag, idx) => (
              <Badge
                key={tag}
                variant="outline"
                className="text-xs px-1 py-0"
                style={{
                  borderColor: task.tagColors?.[idx],
                  color: task.tagColors?.[idx],
                }}
              >
                {tag}
              </Badge>
            ))}
            {task.tagNames.length > 3 && (
              <Badge variant="outline" className="text-xs px-1 py-0">
                +{task.tagNames.length - 3}
              </Badge>
            )}
          </div>
        )}

        <div className="flex items-center justify-between text-xs text-muted-foreground">
          <div className="flex items-center gap-2">
            {task.dueDate && (
              <span className={`flex items-center gap-1 ${task.isOverdue ? 'text-red-500' : ''}`}>
                {task.isOverdue && <AlertCircle className="h-3 w-3" />}
                <Calendar className="h-3 w-3" />
                {format(parseISO(task.dueDate), 'MMM d')}
              </span>
            )}
            {task.commentCount > 0 && (
              <span className="flex items-center gap-1">
                <MessageSquare className="h-3 w-3" />
                {task.commentCount}
              </span>
            )}
          </div>

          {task.assigneeName && (
            <div
              className="w-6 h-6 rounded-full bg-primary flex items-center justify-center text-primary-foreground text-xs font-medium"
              title={task.assigneeName}
            >
              {task.assigneeName.split(' ').map(n => n[0]).join('').substring(0, 2)}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
