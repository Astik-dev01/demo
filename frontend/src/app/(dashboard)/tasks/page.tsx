'use client';

import { useState, useEffect } from 'react';
import { Search, Filter, Loader2, CheckCircle2, Clock, Calendar, AlertCircle } from 'lucide-react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { taskService } from '@/services/task.service';
import { TaskListItem } from '@/types/task.types';
import { Page } from '@/types/project.types';
import { format, parseISO, isPast, isToday } from 'date-fns';

type FilterStatus = 'all' | 'todo' | 'in_progress' | 'done' | 'overdue';

export default function TasksPage() {
  const [tasks, setTasks] = useState<TaskListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<FilterStatus>('all');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadTasks();
  }, [page]);

  const loadTasks = async () => {
    try {
      setLoading(true);
      const response = await taskService.getMyTasks(page, 20);
      setTasks(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      console.error('Failed to load tasks:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredTasks = tasks.filter((task) => {
    const matchesSearch =
      task.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      task.key.toLowerCase().includes(searchQuery.toLowerCase());

    if (!matchesSearch) return false;

    switch (statusFilter) {
      case 'overdue':
        return task.isOverdue;
      case 'todo':
        return task.statusName?.toLowerCase().includes('todo') || task.statusName?.toLowerCase().includes('к выполнению');
      case 'in_progress':
        return task.statusName?.toLowerCase().includes('progress') || task.statusName?.toLowerCase().includes('работе');
      case 'done':
        return task.statusName?.toLowerCase().includes('done') || task.statusName?.toLowerCase().includes('выполнено');
      default:
        return true;
    }
  });

  const getStatusIcon = (statusName: string | null) => {
    if (!statusName) return <Clock className="h-4 w-4" />;
    const lower = statusName.toLowerCase();
    if (lower.includes('done') || lower.includes('выполнено')) {
      return <CheckCircle2 className="h-4 w-4 text-green-500" />;
    }
    if (lower.includes('progress') || lower.includes('работе')) {
      return <Loader2 className="h-4 w-4 text-blue-500" />;
    }
    return <Clock className="h-4 w-4 text-gray-500" />;
  };

  const formatDueDate = (dueDate: string | null, isOverdue: boolean) => {
    if (!dueDate) return null;
    const date = parseISO(dueDate);
    const formatted = format(date, 'MMM d, yyyy');

    if (isOverdue) {
      return <span className="text-red-500 flex items-center gap-1"><AlertCircle className="h-3 w-3" />{formatted}</span>;
    }
    if (isToday(date)) {
      return <span className="text-orange-500">Today</span>;
    }
    return <span className="text-muted-foreground">{formatted}</span>;
  };

  if (loading && tasks.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">My Tasks</h1>
          <p className="text-muted-foreground">View and manage all tasks assigned to you</p>
        </div>
      </div>

      <div className="flex items-center gap-4">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search tasks..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>
        <Select value={statusFilter} onValueChange={(v) => setStatusFilter(v as FilterStatus)}>
          <SelectTrigger className="w-[180px]">
            <Filter className="h-4 w-4 mr-2" />
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Tasks</SelectItem>
            <SelectItem value="todo">To Do</SelectItem>
            <SelectItem value="in_progress">In Progress</SelectItem>
            <SelectItem value="done">Done</SelectItem>
            <SelectItem value="overdue">Overdue</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {filteredTasks.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <CheckCircle2 className="h-16 w-16 text-muted-foreground mb-4" />
          <h2 className="text-xl font-semibold mb-2">No tasks found</h2>
          <p className="text-muted-foreground">
            {searchQuery || statusFilter !== 'all'
              ? 'Try adjusting your filters'
              : 'Tasks assigned to you will appear here'}
          </p>
        </div>
      ) : (
        <>
          <Card>
            <CardContent className="p-0">
              <div className="divide-y">
                {filteredTasks.map((task) => (
                  <Link
                    key={task.id}
                    href={`/tasks/${task.id}`}
                    className="flex items-center gap-4 p-4 hover:bg-muted/50 transition-colors"
                  >
                    <div className="flex-shrink-0">
                      {getStatusIcon(task.statusName)}
                    </div>

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <Badge variant="outline" className="font-mono text-xs">
                          {task.key}
                        </Badge>
                        <span className="font-medium truncate">{task.title}</span>
                      </div>
                      <div className="flex items-center gap-3 text-sm">
                        {task.statusName && (
                          <Badge
                            variant="secondary"
                            style={{
                              backgroundColor: task.statusColor ? `${task.statusColor}20` : undefined,
                              color: task.statusColor || undefined,
                            }}
                          >
                            {task.statusName}
                          </Badge>
                        )}
                        {task.priorityName && (
                          <Badge
                            variant="outline"
                            style={{
                              borderColor: task.priorityColor || undefined,
                              color: task.priorityColor || undefined,
                            }}
                          >
                            {task.priorityName}
                          </Badge>
                        )}
                        {task.tagNames?.slice(0, 2).map((tag, i) => (
                          <Badge
                            key={tag}
                            variant="secondary"
                            className="text-xs"
                            style={{
                              backgroundColor: task.tagColors?.[i] ? `${task.tagColors[i]}20` : undefined,
                              color: task.tagColors?.[i] || undefined,
                            }}
                          >
                            {tag}
                          </Badge>
                        ))}
                      </div>
                    </div>

                    <div className="flex items-center gap-4 text-sm">
                      {task.dueDate && (
                        <div className="flex items-center gap-1">
                          <Calendar className="h-4 w-4 text-muted-foreground" />
                          {formatDueDate(task.dueDate, task.isOverdue)}
                        </div>
                      )}
                      {task.commentCount > 0 && (
                        <span className="text-muted-foreground">{task.commentCount} comments</span>
                      )}
                    </div>
                  </Link>
                ))}
              </div>
            </CardContent>
          </Card>

          {totalPages > 1 && (
            <div className="flex justify-center gap-2 pt-4">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Previous
              </Button>
              <span className="flex items-center px-4 text-sm text-muted-foreground">
                Page {page + 1} of {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
              >
                Next
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
