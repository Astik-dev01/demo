'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ArrowLeft, Loader2 } from 'lucide-react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { projectService } from '@/services/project.service';
import { projectTypeService } from '@/services/handbook.service';
import { ProjectType } from '@/types/handbook.types';
import { CreateProjectRequest } from '@/types/project.types';

const projectSchema = z.object({
  name: z
    .string()
    .min(1, 'Project name is required')
    .max(200, 'Name must be at most 200 characters'),
  projectKey: z
    .string()
    .min(2, 'Key must be at least 2 characters')
    .max(10, 'Key must be at most 10 characters')
    .regex(/^[A-Z][A-Z0-9]*$/, 'Key must be uppercase alphanumeric and start with a letter'),
  description: z.string().max(2000, 'Description must be at most 2000 characters').optional(),
  typeId: z.string().optional(),
  color: z
    .string()
    .regex(/^#[0-9A-Fa-f]{6}$/, 'Invalid color format')
    .optional(),
  isPublic: z.boolean().optional(),
});

type ProjectFormData = z.infer<typeof projectSchema>;

const DEFAULT_COLORS = [
  '#3B82F6', // Blue
  '#10B981', // Green
  '#F59E0B', // Amber
  '#EF4444', // Red
  '#8B5CF6', // Purple
  '#EC4899', // Pink
  '#06B6D4', // Cyan
  '#84CC16', // Lime
];

export default function NewProjectPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [projectTypes, setProjectTypes] = useState<ProjectType[]>([]);
  const [selectedColor, setSelectedColor] = useState(DEFAULT_COLORS[0]);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<ProjectFormData>({
    resolver: zodResolver(projectSchema),
    defaultValues: {
      color: DEFAULT_COLORS[0],
      isPublic: false,
    },
  });

  const nameValue = watch('name');

  useEffect(() => {
    loadProjectTypes();
  }, []);

  useEffect(() => {
    // Auto-generate project key from name
    if (nameValue) {
      const key = nameValue
        .toUpperCase()
        .replace(/[^A-Z0-9]/g, '')
        .substring(0, 10);
      if (key) {
        setValue('projectKey', key.substring(0, 1) + key.substring(1));
      }
    }
  }, [nameValue, setValue]);

  const loadProjectTypes = async () => {
    try {
      const types = await projectTypeService.findAll();
      setProjectTypes(types);
    } catch (error) {
      console.error('Failed to load project types:', error);
    }
  };

  const onSubmit = async (data: ProjectFormData) => {
    try {
      setLoading(true);
      const request: CreateProjectRequest = {
        name: data.name,
        projectKey: data.projectKey,
        description: data.description,
        typeId: data.typeId,
        color: selectedColor,
        isPublic: data.isPublic,
      };
      const project = await projectService.create(request);
      router.push(`/projects/${project.projectKey}`);
    } catch (error: any) {
      console.error('Failed to create project:', error);
      alert(error.response?.data?.message || 'Failed to create project');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/projects">
          <Button variant="ghost" size="icon">
            <ArrowLeft className="h-5 w-5" />
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold">Create New Project</h1>
          <p className="text-muted-foreground">Set up your project details</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Project Details</CardTitle>
          <CardDescription>Fill in the basic information about your project</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="name">Project Name *</Label>
              <Input
                id="name"
                placeholder="My Awesome Project"
                {...register('name')}
                className={errors.name ? 'border-red-500' : ''}
              />
              {errors.name && <p className="text-sm text-red-500">{errors.name.message}</p>}
            </div>

            <div className="space-y-2">
              <Label htmlFor="projectKey">Project Key *</Label>
              <Input
                id="projectKey"
                placeholder="MAP"
                {...register('projectKey')}
                className={errors.projectKey ? 'border-red-500' : ''}
              />
              <p className="text-sm text-muted-foreground">
                Used as a prefix for task IDs (e.g., MAP-1, MAP-2)
              </p>
              {errors.projectKey && (
                <p className="text-sm text-red-500">{errors.projectKey.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                placeholder="Describe your project..."
                rows={4}
                {...register('description')}
                className={errors.description ? 'border-red-500' : ''}
              />
              {errors.description && (
                <p className="text-sm text-red-500">{errors.description.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label>Project Type</Label>
              <Select onValueChange={(value) => setValue('typeId', value)}>
                <SelectTrigger>
                  <SelectValue placeholder="Select project type" />
                </SelectTrigger>
                <SelectContent>
                  {projectTypes.map((type) => (
                    <SelectItem key={type.id} value={type.id}>
                      {type.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Project Color</Label>
              <div className="flex gap-2 flex-wrap">
                {DEFAULT_COLORS.map((color) => (
                  <button
                    key={color}
                    type="button"
                    onClick={() => {
                      setSelectedColor(color);
                      setValue('color', color);
                    }}
                    className={`w-8 h-8 rounded-full transition-all ${
                      selectedColor === color ? 'ring-2 ring-offset-2 ring-primary' : ''
                    }`}
                    style={{ backgroundColor: color }}
                  />
                ))}
              </div>
            </div>

            <div className="flex items-center space-x-2">
              <Checkbox
                id="isPublic"
                onCheckedChange={(checked) => setValue('isPublic', checked as boolean)}
              />
              <Label htmlFor="isPublic" className="text-sm font-normal cursor-pointer">
                Make this project public (visible to all users)
              </Label>
            </div>

            <div className="flex gap-4 pt-4">
              <Button type="submit" disabled={loading}>
                {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Create Project
              </Button>
              <Link href="/projects">
                <Button type="button" variant="outline">
                  Cancel
                </Button>
              </Link>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
