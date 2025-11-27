'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { taskPriorityService } from '@/services/handbook.service';
import { TaskPriority, TaskPriorityRequest } from '@/types/handbook.types';

const prioritySchema = z.object({
  alias: z.string().min(1, 'Alias is required').regex(/^[A-Z][A-Z0-9_]*$/, 'Alias must be uppercase'),
  nameRu: z.string().min(1, 'Russian name is required'),
  nameKy: z.string().min(1, 'Kyrgyz name is required'),
  nameEn: z.string().optional(),
  color: z.string().regex(/^#[0-9A-Fa-f]{6}$/, 'Invalid color format'),
  icon: z.string().optional(),
  level: z.number().min(0).max(100),
});

type PriorityForm = z.infer<typeof prioritySchema>;

export default function PrioritiesPage() {
  const [priorities, setPriorities] = useState<TaskPriority[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<PriorityForm>({
    resolver: zodResolver(prioritySchema),
    defaultValues: {
      color: '#6B7280',
      level: 0,
    },
  });

  useEffect(() => {
    loadPriorities();
  }, []);

  const loadPriorities = async () => {
    try {
      const data = await taskPriorityService.findAll();
      setPriorities(data);
    } catch (error) {
      toast.error('Failed to load priorities');
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = async (data: PriorityForm) => {
    try {
      if (editingId) {
        await taskPriorityService.update(editingId, data);
        toast.success('Priority updated');
      } else {
        await taskPriorityService.create(data);
        toast.success('Priority created');
      }
      setDialogOpen(false);
      reset();
      setEditingId(null);
      loadPriorities();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleEdit = (priority: TaskPriority) => {
    setEditingId(priority.id);
    setValue('alias', priority.alias);
    setValue('nameRu', priority.nameRu);
    setValue('nameKy', priority.nameKy);
    setValue('nameEn', priority.nameEn || '');
    setValue('color', priority.color);
    setValue('icon', priority.icon || '');
    setValue('level', priority.level);
    setDialogOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this priority?')) return;
    try {
      await taskPriorityService.delete(id);
      toast.success('Priority deleted');
      loadPriorities();
    } catch (error) {
      toast.error('Failed to delete priority');
    }
  };

  const handleNew = () => {
    setEditingId(null);
    reset();
    setDialogOpen(true);
  };

  if (loading) {
    return <div className="flex items-center justify-center p-8">Loading...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Task Priorities</h1>
          <p className="text-muted-foreground">Manage task priority levels</p>
        </div>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={handleNew}>Add Priority</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>{editingId ? 'Edit Priority' : 'Add Priority'}</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="alias">Alias</Label>
                  <Input id="alias" {...register('alias')} placeholder="HIGH" />
                  {errors.alias && <p className="text-sm text-destructive">{errors.alias.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="level">Level</Label>
                  <Input id="level" type="number" {...register('level', { valueAsNumber: true })} />
                  {errors.level && <p className="text-sm text-destructive">{errors.level.message}</p>}
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="nameRu">Name (Russian)</Label>
                <Input id="nameRu" {...register('nameRu')} />
                {errors.nameRu && <p className="text-sm text-destructive">{errors.nameRu.message}</p>}
              </div>
              <div className="space-y-2">
                <Label htmlFor="nameKy">Name (Kyrgyz)</Label>
                <Input id="nameKy" {...register('nameKy')} />
                {errors.nameKy && <p className="text-sm text-destructive">{errors.nameKy.message}</p>}
              </div>
              <div className="space-y-2">
                <Label htmlFor="nameEn">Name (English)</Label>
                <Input id="nameEn" {...register('nameEn')} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="color">Color</Label>
                  <div className="flex gap-2">
                    <Input id="color" type="color" {...register('color')} className="h-10 w-14 p-1" />
                    <Input {...register('color')} placeholder="#6B7280" />
                  </div>
                  {errors.color && <p className="text-sm text-destructive">{errors.color.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="icon">Icon</Label>
                  <Input id="icon" {...register('icon')} placeholder="arrow-up" />
                </div>
              </div>
              <div className="flex justify-end gap-2">
                <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? 'Saving...' : 'Save'}
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <Card>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Color</TableHead>
                <TableHead>Alias</TableHead>
                <TableHead>Name (RU)</TableHead>
                <TableHead>Name (KY)</TableHead>
                <TableHead>Level</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {priorities.map((priority) => (
                <TableRow key={priority.id}>
                  <TableCell>
                    <div
                      className="h-6 w-6 rounded"
                      style={{ backgroundColor: priority.color }}
                    />
                  </TableCell>
                  <TableCell className="font-mono">{priority.alias}</TableCell>
                  <TableCell>{priority.nameRu}</TableCell>
                  <TableCell>{priority.nameKy}</TableCell>
                  <TableCell>{priority.level}</TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleEdit(priority)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-destructive"
                      onClick={() => handleDelete(priority.id)}
                    >
                      Delete
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
