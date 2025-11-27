'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
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
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { projectTypeService } from '@/services/handbook.service';
import { ProjectType, ProjectTypeRequest } from '@/types/handbook.types';

const projectTypeSchema = z.object({
  alias: z.string().min(1, 'Alias is required').regex(/^[A-Z][A-Z0-9_]*$/, 'Alias must be uppercase'),
  nameRu: z.string().min(1, 'Russian name is required'),
  nameKy: z.string().min(1, 'Kyrgyz name is required'),
  nameEn: z.string().optional(),
  icon: z.string().optional(),
  descriptionRu: z.string().optional(),
  descriptionKy: z.string().optional(),
});

type ProjectTypeForm = z.infer<typeof projectTypeSchema>;

export default function ProjectTypesPage() {
  const [projectTypes, setProjectTypes] = useState<ProjectType[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<ProjectTypeForm>({
    resolver: zodResolver(projectTypeSchema),
  });

  useEffect(() => {
    loadProjectTypes();
  }, []);

  const loadProjectTypes = async () => {
    try {
      const data = await projectTypeService.findAll();
      setProjectTypes(data);
    } catch (error) {
      toast.error('Failed to load project types');
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = async (data: ProjectTypeForm) => {
    try {
      if (editingId) {
        await projectTypeService.update(editingId, data);
        toast.success('Project type updated');
      } else {
        await projectTypeService.create(data);
        toast.success('Project type created');
      }
      setDialogOpen(false);
      reset();
      setEditingId(null);
      loadProjectTypes();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleEdit = (projectType: ProjectType) => {
    setEditingId(projectType.id);
    setValue('alias', projectType.alias);
    setValue('nameRu', projectType.nameRu);
    setValue('nameKy', projectType.nameKy);
    setValue('nameEn', projectType.nameEn || '');
    setValue('icon', projectType.icon || '');
    setValue('descriptionRu', projectType.descriptionRu || '');
    setValue('descriptionKy', projectType.descriptionKy || '');
    setDialogOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this project type?')) return;
    try {
      await projectTypeService.delete(id);
      toast.success('Project type deleted');
      loadProjectTypes();
    } catch (error) {
      toast.error('Failed to delete project type');
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
          <h1 className="text-2xl font-bold">Project Types</h1>
          <p className="text-muted-foreground">Manage project type categories</p>
        </div>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={handleNew}>Add Project Type</Button>
          </DialogTrigger>
          <DialogContent className="max-w-lg">
            <DialogHeader>
              <DialogTitle>{editingId ? 'Edit Project Type' : 'Add Project Type'}</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="alias">Alias</Label>
                  <Input id="alias" {...register('alias')} placeholder="SOFTWARE" />
                  {errors.alias && <p className="text-sm text-destructive">{errors.alias.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="icon">Icon</Label>
                  <Input id="icon" {...register('icon')} placeholder="code" />
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
              <div className="space-y-2">
                <Label htmlFor="descriptionRu">Description (Russian)</Label>
                <Textarea id="descriptionRu" {...register('descriptionRu')} rows={2} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="descriptionKy">Description (Kyrgyz)</Label>
                <Textarea id="descriptionKy" {...register('descriptionKy')} rows={2} />
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
                <TableHead>Icon</TableHead>
                <TableHead>Alias</TableHead>
                <TableHead>Name (RU)</TableHead>
                <TableHead>Name (KY)</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {projectTypes.map((type) => (
                <TableRow key={type.id}>
                  <TableCell>{type.icon || '-'}</TableCell>
                  <TableCell className="font-mono">{type.alias}</TableCell>
                  <TableCell>{type.nameRu}</TableCell>
                  <TableCell>{type.nameKy}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => handleEdit(type)}>
                      Edit
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-destructive"
                      onClick={() => handleDelete(type.id)}
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
