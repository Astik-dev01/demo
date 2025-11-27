'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { format } from 'date-fns';
import {
  ArrowLeft,
  Loader2,
  Calendar as CalendarIcon,
  User,
  Flag,
  Clock,
  Tag,
} from 'lucide-react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import { Calendar } from '@/components/ui/calendar';
import { useToast } from '@/components/ui/use-toast';
import { projectService } from '@/services/project.service';
import { boardService } from '@/services/board.service';
import { taskService } from '@/services/task.service';
import { taskPriorityService } from '@/services/handbook.service';
import { Project, ProjectMember } from '@/types/project.types';
import { Board, BoardColumn } from '@/types/board.types';
import { TaskPriority } from '@/types/handbook.types';
import { cn } from '@/lib/utils';

const taskSchema = z.object({
  title: z.string().min(1, 'Title is required').max(500),
  description: z.string().optional(),
  assigneeId: z.string().optional(),
  priorityId: z.string().optional(),
  dueDate: z.date().optional(),
  estimatedHours: z.number().min(0).optional(),
});

type TaskForm = z.infer<typeof taskSchema>;

export default function NewTaskPage() {
  const params = useParams();
  const router = useRouter();
  const searchParams = useSearchParams();
  const { toast } = useToast();
  const projectKey = params.key as string;
  const columnIdParam = searchParams.get('columnId');

  const [project, setProject] = useState<Project | null>(null);
  const [board, setBoard] = useState<Board | null>(null);
  const [members, setMembers] = useState<ProjectMember[]>([]);
  const [priorities, setPriorities] = useState<TaskPriority[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedColumn, setSelectedColumn] = useState<string>('');

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<TaskForm>({
    resolver: zodResolver(taskSchema),
  });

  const watchedAssignee = watch('assigneeId');
  const watchedPriority = watch('priorityId');
  const watchedDueDate = watch('dueDate');

  useEffect(() => {
    loadData();
  }, [projectKey]);

  const loadData = async () => {
    try {
      setLoading(true);

      // Load project
      const projectData = await projectService.getByKey(projectKey);
      setProject(projectData);

      // Load board
      const boards = await boardService.getByProject(projectData.id);
      if (boards.length > 0) {
        const defaultBoard = boards.find(b => b.isDefault) || boards[0];
        setBoard(defaultBoard);

        // Set column from URL param or first column
        if (columnIdParam && defaultBoard.columns.some(c => c.id === columnIdParam)) {
          setSelectedColumn(columnIdParam);
        } else if (defaultBoard.columns.length > 0) {
          setSelectedColumn(defaultBoard.columns[0].id);
        }
      }

      // Load members
      const membersData = await projectService.getMembers(projectData.id);
      setMembers(membersData);

      // Load priorities
      const prioritiesData = await taskPriorityService.findAll();
      setPriorities(prioritiesData);
    } catch (error: any) {
      console.error('Failed to load data:', error);
      toast({
        title: 'Error',
        description: 'Failed to load project data',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = async (data: TaskForm) => {
    if (!project || !board || !selectedColumn) {
      toast({
        title: 'Error',
        description: 'Missing project or board data',
        variant: 'destructive',
      });
      return;
    }

    try {
      await taskService.create({
        projectId: project.id,
        boardId: board.id,
        columnId: selectedColumn,
        title: data.title,
        description: data.description,
        assigneeId: data.assigneeId,
        priorityId: data.priorityId,
        dueDate: data.dueDate ? format(data.dueDate, 'yyyy-MM-dd') : undefined,
        estimatedHours: data.estimatedHours,
      });

      toast({
        title: 'Task created',
        description: 'Your task has been created successfully',
      });

      router.push(`/projects/${projectKey}/board`);
    } catch (error: any) {
      toast({
        title: 'Error',
        description: error.response?.data?.message || 'Failed to create task',
        variant: 'destructive',
      });
    }
  };

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  // Create combined list of assignable users (owner + members)
  const assignableUsers = project ? [
    { id: project.owner.id, user: project.owner, isOwner: true },
    ...members.map(m => ({ id: m.user.id, user: m.user, isOwner: false }))
  ] : [];

  const selectedAssignee = assignableUsers.find(u => u.id === watchedAssignee);
  const selectedPriorityObj = priorities.find(p => p.id === watchedPriority);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!project || !board) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <p className="text-lg text-muted-foreground">Project or board not found</p>
        <Link href="/projects">
          <Button>Back to Projects</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Link href={`/projects/${projectKey}/board`}>
          <Button variant="ghost" size="icon">
            <ArrowLeft className="h-5 w-5" />
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold">Create New Task</h1>
          <p className="text-muted-foreground">
            Add a new task to {project.name}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Main Info */}
        <Card>
          <CardHeader>
            <CardTitle>Task Details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="title">Title *</Label>
              <Input
                id="title"
                {...register('title')}
                placeholder="Enter task title"
                className="text-lg"
              />
              {errors.title && (
                <p className="text-sm text-destructive">{errors.title.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                {...register('description')}
                placeholder="Describe the task..."
                rows={5}
              />
            </div>

            <div className="space-y-2">
              <Label>Column</Label>
              <Select value={selectedColumn} onValueChange={setSelectedColumn}>
                <SelectTrigger>
                  <SelectValue placeholder="Select column" />
                </SelectTrigger>
                <SelectContent>
                  {board.columns.map((column) => (
                    <SelectItem key={column.id} value={column.id}>
                      <div className="flex items-center gap-2">
                        <div
                          className="h-3 w-3 rounded-full"
                          style={{ backgroundColor: column.color }}
                        />
                        {column.name}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </CardContent>
        </Card>

        {/* Assignment & Priority */}
        <Card>
          <CardHeader>
            <CardTitle>Assignment</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Assignee */}
              <div className="space-y-2">
                <Label className="flex items-center gap-2">
                  <User className="h-4 w-4" />
                  Assignee
                </Label>
                <Select
                  value={watchedAssignee || ''}
                  onValueChange={(v) => setValue('assigneeId', v || undefined)}
                >
                  <SelectTrigger>
                    {selectedAssignee ? (
                      <div className="flex items-center gap-2">
                        <Avatar className="h-6 w-6">
                          <AvatarImage src={selectedAssignee.user.avatarUrl || undefined} />
                          <AvatarFallback className="text-xs">
                            {getInitials(selectedAssignee.user.fullName)}
                          </AvatarFallback>
                        </Avatar>
                        <span>{selectedAssignee.user.fullName}</span>
                        {selectedAssignee.isOwner && (
                          <span className="text-xs text-muted-foreground">(Owner)</span>
                        )}
                      </div>
                    ) : (
                      <SelectValue placeholder="Select assignee" />
                    )}
                  </SelectTrigger>
                  <SelectContent>
                    {assignableUsers.map((assignee) => (
                      <SelectItem key={assignee.id} value={assignee.id}>
                        <div className="flex items-center gap-2">
                          <Avatar className="h-6 w-6">
                            <AvatarImage src={assignee.user.avatarUrl || undefined} />
                            <AvatarFallback className="text-xs">
                              {getInitials(assignee.user.fullName)}
                            </AvatarFallback>
                          </Avatar>
                          <span>{assignee.user.fullName}</span>
                          {assignee.isOwner && (
                            <span className="text-xs text-muted-foreground ml-1">(Owner)</span>
                          )}
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {/* Priority */}
              <div className="space-y-2">
                <Label className="flex items-center gap-2">
                  <Flag className="h-4 w-4" />
                  Priority
                </Label>
                <Select
                  value={watchedPriority || ''}
                  onValueChange={(v) => setValue('priorityId', v || undefined)}
                >
                  <SelectTrigger>
                    {selectedPriorityObj ? (
                      <div className="flex items-center gap-2">
                        <div
                          className="h-3 w-3 rounded-full"
                          style={{ backgroundColor: selectedPriorityObj.color || '#888' }}
                        />
                        <span>{selectedPriorityObj.name}</span>
                      </div>
                    ) : (
                      <SelectValue placeholder="Select priority" />
                    )}
                  </SelectTrigger>
                  <SelectContent>
                    {priorities.map((priority) => (
                      <SelectItem key={priority.id} value={priority.id}>
                        <div className="flex items-center gap-2">
                          <div
                            className="h-3 w-3 rounded-full"
                            style={{ backgroundColor: priority.color || '#888' }}
                          />
                          {priority.name}
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Schedule */}
        <Card>
          <CardHeader>
            <CardTitle>Schedule</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Due Date */}
              <div className="space-y-2">
                <Label className="flex items-center gap-2">
                  <CalendarIcon className="h-4 w-4" />
                  Due Date
                </Label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button
                      variant="outline"
                      className={cn(
                        'w-full justify-start text-left font-normal',
                        !watchedDueDate && 'text-muted-foreground'
                      )}
                    >
                      <CalendarIcon className="mr-2 h-4 w-4" />
                      {watchedDueDate ? format(watchedDueDate, 'PPP') : 'Pick a date'}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      mode="single"
                      selected={watchedDueDate}
                      onSelect={(date) => setValue('dueDate', date)}
                      initialFocus
                    />
                  </PopoverContent>
                </Popover>
              </div>

              {/* Estimated Hours */}
              <div className="space-y-2">
                <Label className="flex items-center gap-2">
                  <Clock className="h-4 w-4" />
                  Estimated Hours
                </Label>
                <Input
                  type="number"
                  min="0"
                  step="0.5"
                  {...register('estimatedHours', { valueAsNumber: true })}
                  placeholder="e.g., 4"
                />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Actions */}
        <div className="flex justify-end gap-3">
          <Link href={`/projects/${projectKey}/board`}>
            <Button type="button" variant="outline">
              Cancel
            </Button>
          </Link>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Creating...
              </>
            ) : (
              'Create Task'
            )}
          </Button>
        </div>
      </form>
    </div>
  );
}
