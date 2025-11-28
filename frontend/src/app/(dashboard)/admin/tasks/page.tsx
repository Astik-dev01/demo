'use client';

import { useState, useEffect, useCallback } from 'react';
import toast from 'react-hot-toast';
import { Search, MoreHorizontal, Trash2, RotateCcw, ExternalLink } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { adminTaskService } from '@/services/admin.service';
import { useLanguage } from '@/contexts/language-context';
import { useDebounce } from '@/hooks/use-debounce';
import Link from 'next/link';

interface AdminTask {
  id: string;
  key: string;
  number: number;
  title: string;
  projectId: string;
  projectName: string;
  projectKey: string;
  statusName: string;
  statusColor: string;
  priorityName: string;
  priorityColor: string;
  assigneeName: string | null;
  assigneeAvatar: string | null;
  dueDate: string | null;
  isDeleted: boolean;
  createdAt: string;
}

export default function AdminTasksPage() {
  const { t } = useLanguage();
  const [tasks, setTasks] = useState<AdminTask[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterDeleted, setFilterDeleted] = useState<string>('false');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const debouncedSearch = useDebounce(searchQuery, 300);

  const loadTasks = useCallback(async () => {
    try {
      setLoading(true);
      const filters: any = {};

      if (debouncedSearch) {
        filters.search = debouncedSearch;
      }

      if (filterDeleted !== 'all') {
        filters.isDeleted = filterDeleted === 'true';
      }

      const response = await adminTaskService.findAll(page, 20, filters);
      setTasks(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to load tasks');
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch, filterDeleted, page]);

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  const handleDelete = async (task: AdminTask) => {
    if (!confirm(`Are you sure you want to delete "${task.title}"?`)) return;
    try {
      await adminTaskService.delete(task.id);
      toast.success('Task deleted');
      loadTasks();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to delete task');
    }
  };

  const handleRestore = async (task: AdminTask) => {
    try {
      await adminTaskService.restore(task.id);
      toast.success('Task restored');
      loadTasks();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to restore task');
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  const isOverdue = (dueDate: string | null) => {
    if (!dueDate) return false;
    return new Date(dueDate) < new Date();
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">{t('nav.admin.tasks')}</h1>
          <p className="text-muted-foreground">{t('admin.tasks.desc')}</p>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-wrap gap-4">
            <div className="flex-1 min-w-[200px]">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search tasks..."
                  value={searchQuery}
                  onChange={(e) => {
                    setSearchQuery(e.target.value);
                    setPage(0);
                  }}
                  className="pl-10"
                />
              </div>
            </div>
            <Select value={filterDeleted} onValueChange={(v) => { setFilterDeleted(v); setPage(0); }}>
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder="Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="false">Active</SelectItem>
                <SelectItem value="true">Deleted</SelectItem>
                <SelectItem value="all">All</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Tasks Table */}
      <Card>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center p-8">Loading...</div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Task</TableHead>
                    <TableHead>Project</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Priority</TableHead>
                    <TableHead>Assignee</TableHead>
                    <TableHead>Due Date</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {tasks.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                        No tasks found
                      </TableCell>
                    </TableRow>
                  ) : (
                    tasks.map((task) => (
                      <TableRow key={task.id} className={task.isDeleted ? 'opacity-50' : ''}>
                        <TableCell>
                          <div>
                            <div className="flex items-center gap-2">
                              <Badge variant="outline" className="text-xs font-mono">
                                {task.key || `${task.projectKey}-${task.number}`}
                              </Badge>
                              <span className="font-medium truncate max-w-[200px]">{task.title}</span>
                            </div>
                            {task.isDeleted && (
                              <Badge variant="destructive" className="mt-1">Deleted</Badge>
                            )}
                          </div>
                        </TableCell>
                        <TableCell>
                          <span className="text-sm">{task.projectName || '-'}</span>
                        </TableCell>
                        <TableCell>
                          {task.statusName ? (
                            <Badge
                              style={{
                                backgroundColor: task.statusColor || undefined,
                                color: task.statusColor ? 'white' : undefined
                              }}
                            >
                              {task.statusName}
                            </Badge>
                          ) : '-'}
                        </TableCell>
                        <TableCell>
                          {task.priorityName ? (
                            <Badge
                              variant="outline"
                              style={{
                                borderColor: task.priorityColor || undefined,
                                color: task.priorityColor || undefined
                              }}
                            >
                              {task.priorityName}
                            </Badge>
                          ) : '-'}
                        </TableCell>
                        <TableCell>
                          {task.assigneeName ? (
                            <div className="flex items-center gap-2">
                              <Avatar className="h-6 w-6">
                                <AvatarImage src={task.assigneeAvatar || undefined} />
                                <AvatarFallback className="text-xs">
                                  {getInitials(task.assigneeName)}
                                </AvatarFallback>
                              </Avatar>
                              <span className="text-sm">{task.assigneeName}</span>
                            </div>
                          ) : (
                            <span className="text-muted-foreground">-</span>
                          )}
                        </TableCell>
                        <TableCell>
                          {task.dueDate ? (
                            <span className={isOverdue(task.dueDate) ? 'text-destructive' : ''}>
                              {formatDate(task.dueDate)}
                            </span>
                          ) : '-'}
                        </TableCell>
                        <TableCell>{formatDate(task.createdAt)}</TableCell>
                        <TableCell className="text-right">
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button variant="ghost" size="icon">
                                <MoreHorizontal className="h-4 w-4" />
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                              <DropdownMenuItem asChild>
                                <Link href={`/tasks/${task.id}`}>
                                  <ExternalLink className="mr-2 h-4 w-4" />
                                  View Task
                                </Link>
                              </DropdownMenuItem>
                              <DropdownMenuSeparator />
                              {task.isDeleted ? (
                                <DropdownMenuItem onClick={() => handleRestore(task)}>
                                  <RotateCcw className="mr-2 h-4 w-4" />
                                  Restore
                                </DropdownMenuItem>
                              ) : (
                                <DropdownMenuItem
                                  className="text-destructive"
                                  onClick={() => handleDelete(task)}
                                >
                                  <Trash2 className="mr-2 h-4 w-4" />
                                  Delete
                                </DropdownMenuItem>
                              )}
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-between px-4 py-3 border-t">
                  <div className="text-sm text-muted-foreground">
                    Showing {page * 20 + 1} to {Math.min((page + 1) * 20, totalElements)} of {totalElements} tasks
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={page === 0}
                      onClick={() => setPage(p => p - 1)}
                    >
                      Previous
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={page >= totalPages - 1}
                      onClick={() => setPage(p => p + 1)}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
