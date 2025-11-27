'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
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
import { Badge } from '@/components/ui/badge';
import { taskStatusService } from '@/services/handbook.service';
import { TaskStatus, TaskStatusRequest } from '@/types/handbook.types';

const statusSchema = z.object({
  alias: z.string().min(1, 'Alias is required').regex(/^[A-Z][A-Z0-9_]*$/, 'Alias must be uppercase'),
  nameRu: z.string().min(1, 'Russian name is required'),
  nameKy: z.string().min(1, 'Kyrgyz name is required'),
  nameEn: z.string().optional(),
  color: z.string().regex(/^#[0-9A-Fa-f]{6}$/, 'Invalid color format'),
  icon: z.string().optional(),
  isFinal: z.boolean(),
  isDefault: z.boolean(),
});

type StatusForm = z.infer<typeof statusSchema>;

export default function StatusesPage() {
  const [statuses, setStatuses] = useState<TaskStatus[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<StatusForm>({
    resolver: zodResolver(statusSchema),
    defaultValues: {
      color: '#6B7280',
      isFinal: false,
      isDefault: false,
    },
  });

  useEffect(() => {
    loadStatuses();
  }, []);

  const loadStatuses = async () => {
    try {
      const data = await taskStatusService.findAll();
      setStatuses(data);
    } catch (error) {
      toast.error('Failed to load statuses');
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = async (data: StatusForm) => {
    try {
      if (editingId) {
        await taskStatusService.update(editingId, data);
        toast.success('Status updated');
      } else {
        await taskStatusService.create(data);
        toast.success('Status created');
      }
      setDialogOpen(false);
      reset();
      setEditingId(null);
      loadStatuses();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleEdit = (status: TaskStatus) => {
    setEditingId(status.id);
    setValue('alias', status.alias);
    setValue('nameRu', status.nameRu);
    setValue('nameKy', status.nameKy);
    setValue('nameEn', status.nameEn || '');
    setValue('color', status.color);
    setValue('icon', status.icon || '');
    setValue('isFinal', status.isFinal);
    setValue('isDefault', status.isDefault);
    setDialogOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this status?')) return;
    try {
      await taskStatusService.delete(id);
      toast.success('Status deleted');
      loadStatuses();
    } catch (error) {
      toast.error('Failed to delete status');
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
          <h1 className="text-2xl font-bold">Task Statuses</h1>
          <p className="text-muted-foreground">Manage task workflow statuses</p>
        </div>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={handleNew}>Add Status</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>{editingId ? 'Edit Status' : 'Add Status'}</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="alias">Alias</Label>
                <Input id="alias" {...register('alias')} placeholder="IN_PROGRESS" />
                {errors.alias && <p className="text-sm text-destructive">{errors.alias.message}</p>}
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
                </div>
                <div className="space-y-2">
                  <Label htmlFor="icon">Icon</Label>
                  <Input id="icon" {...register('icon')} placeholder="check-circle" />
                </div>
              </div>
              <div className="flex gap-6">
                <div className="flex items-center gap-2">
                  <Checkbox
                    id="isFinal"
                    checked={watch('isFinal')}
                    onCheckedChange={(checked) => setValue('isFinal', !!checked)}
                  />
                  <Label htmlFor="isFinal">Final Status</Label>
                </div>
                <div className="flex items-center gap-2">
                  <Checkbox
                    id="isDefault"
                    checked={watch('isDefault')}
                    onCheckedChange={(checked) => setValue('isDefault', !!checked)}
                  />
                  <Label htmlFor="isDefault">Default Status</Label>
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
                <TableHead>Flags</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {statuses.map((status) => (
                <TableRow key={status.id}>
                  <TableCell>
                    <div
                      className="h-6 w-6 rounded"
                      style={{ backgroundColor: status.color }}
                    />
                  </TableCell>
                  <TableCell className="font-mono">{status.alias}</TableCell>
                  <TableCell>{status.nameRu}</TableCell>
                  <TableCell>
                    <div className="flex gap-1">
                      {status.isDefault && <Badge variant="secondary">Default</Badge>}
                      {status.isFinal && <Badge variant="outline">Final</Badge>}
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => handleEdit(status)}>
                      Edit
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-destructive"
                      onClick={() => handleDelete(status.id)}
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
