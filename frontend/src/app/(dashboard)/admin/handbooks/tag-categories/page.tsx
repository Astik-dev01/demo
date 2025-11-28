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
import { tagCategoryService } from '@/services/handbook.service';
import { TagCategory, TagCategoryRequest } from '@/types/handbook.types';

const tagCategorySchema = z.object({
  alias: z.string().min(1, 'Alias is required').regex(/^[A-Z][A-Z0-9_]*$/, 'Alias must be uppercase'),
  nameRu: z.string().min(1, 'Russian name is required'),
  nameKy: z.string().min(1, 'Kyrgyz name is required'),
  nameEn: z.string().optional(),
  color: z.string().regex(/^#[0-9A-Fa-f]{6}$/, 'Invalid color format'),
});

type TagCategoryForm = z.infer<typeof tagCategorySchema>;

export default function TagCategoriesPage() {
  const [categories, setCategories] = useState<TagCategory[]>([]);
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
  } = useForm<TagCategoryForm>({
    resolver: zodResolver(tagCategorySchema),
    defaultValues: {
      color: '#6B7280',
    },
  });

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const data = await tagCategoryService.findAll();
      setCategories(data);
    } catch (error) {
      toast.error('Failed to load tag categories');
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = async (data: TagCategoryForm) => {
    try {
      if (editingId) {
        await tagCategoryService.update(editingId, data);
        toast.success('Tag category updated');
      } else {
        await tagCategoryService.create(data);
        toast.success('Tag category created');
      }
      setDialogOpen(false);
      reset();
      setEditingId(null);
      loadCategories();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleEdit = (category: TagCategory) => {
    setEditingId(category.id);
    setValue('alias', category.alias);
    setValue('nameRu', category.nameRu);
    setValue('nameKy', category.nameKy);
    setValue('nameEn', category.nameEn || '');
    setValue('color', category.color);
    setDialogOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this tag category?')) return;
    try {
      await tagCategoryService.delete(id);
      toast.success('Tag category deleted');
      loadCategories();
    } catch (error) {
      toast.error('Failed to delete tag category');
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
          <h1 className="text-2xl font-bold">Tag Categories</h1>
          <p className="text-muted-foreground">Manage tag categories for task labeling</p>
        </div>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={handleNew}>Add Category</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>{editingId ? 'Edit Category' : 'Add Category'}</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="alias">Alias</Label>
                <Input id="alias" {...register('alias')} placeholder="FEATURE" />
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
              <div className="space-y-2">
                <Label htmlFor="color">Color</Label>
                <div className="flex gap-2">
                  <Input
                    id="color"
                    type="color"
                    value={watch('color') || '#6B7280'}
                    onChange={(e) => setValue('color', e.target.value, { shouldDirty: true })}
                    className="h-10 w-14 p-1"
                  />
                  <Input
                    value={watch('color') || ''}
                    onChange={(e) => setValue('color', e.target.value, { shouldDirty: true })}
                    placeholder="#6B7280"
                  />
                </div>
                {errors.color && <p className="text-sm text-destructive">{errors.color.message}</p>}
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
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {categories.map((category) => (
                <TableRow key={category.id}>
                  <TableCell>
                    <div
                      className="h-6 w-6 rounded"
                      style={{ backgroundColor: category.color }}
                    />
                  </TableCell>
                  <TableCell className="font-mono">{category.alias}</TableCell>
                  <TableCell>{category.nameRu}</TableCell>
                  <TableCell>{category.nameKy}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => handleEdit(category)}>
                      Edit
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-destructive"
                      onClick={() => handleDelete(category.id)}
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
