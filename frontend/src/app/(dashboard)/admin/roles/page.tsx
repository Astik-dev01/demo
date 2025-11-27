'use client';

import { useState, useEffect, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { Plus, MoreHorizontal, Shield, ShieldCheck, ShieldX, Trash2 } from 'lucide-react';
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
} from '@/components/ui/dialog';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Badge } from '@/components/ui/badge';
import { adminRoleService, CreateRoleRequest, UpdateRoleRequest } from '@/services/admin.service';
import { SystemRole } from '@/types/admin.types';
import { useLanguage } from '@/contexts/language-context';

const roleSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  code: z.string().optional(),
  nameRu: z.string().optional(),
  nameEn: z.string().optional(),
  description: z.string().optional(),
  priority: z.number().optional(),
});

type RoleForm = z.infer<typeof roleSchema>;

export default function AdminRolesPage() {
  const { t } = useLanguage();
  const [roles, setRoles] = useState<SystemRole[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<SystemRole | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<RoleForm>({
    resolver: zodResolver(roleSchema),
    defaultValues: {
      priority: 100,
    },
  });

  const loadRoles = useCallback(async () => {
    try {
      setLoading(true);
      const data = await adminRoleService.findAll();
      setRoles(data);
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to load roles');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadRoles();
  }, [loadRoles]);

  const onSubmit = async (data: RoleForm) => {
    try {
      if (editingRole) {
        const updateData: UpdateRoleRequest = {
          name: data.name,
          code: data.code,
          nameRu: data.nameRu,
          nameEn: data.nameEn,
          description: data.description,
          priority: data.priority,
        };
        await adminRoleService.update(editingRole.id, updateData);
        toast.success('Role updated');
      } else {
        const createData: CreateRoleRequest = {
          name: data.name,
          code: data.code,
          nameRu: data.nameRu,
          nameEn: data.nameEn,
          description: data.description,
          priority: data.priority,
        };
        await adminRoleService.create(createData);
        toast.success('Role created');
      }
      setDialogOpen(false);
      reset();
      setEditingRole(null);
      loadRoles();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleEdit = (role: SystemRole) => {
    setEditingRole(role);
    setValue('name', role.name);
    setValue('code', role.code || '');
    setValue('nameRu', role.nameRu || '');
    setValue('nameEn', role.nameEn || '');
    setValue('description', role.description || '');
    setValue('priority', role.priority || 100);
    setDialogOpen(true);
  };

  const handleNew = () => {
    setEditingRole(null);
    reset({
      name: '',
      code: '',
      nameRu: '',
      nameEn: '',
      description: '',
      priority: 100,
    });
    setDialogOpen(true);
  };

  const handleToggleActive = async (role: SystemRole) => {
    if (role.isSystem) {
      toast.error('Cannot modify system role');
      return;
    }
    try {
      await adminRoleService.toggleActive(role.id);
      toast.success(role.active ? 'Role deactivated' : 'Role activated');
      loadRoles();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (role: SystemRole) => {
    if (role.isSystem) {
      toast.error('Cannot delete system role');
      return;
    }
    if (!confirm(`Are you sure you want to delete "${role.name}"?`)) return;
    try {
      await adminRoleService.delete(role.id);
      toast.success('Role deleted');
      loadRoles();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to delete role');
    }
  };

  const formatDate = (dateString: string | null) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">{t('nav.admin.roles')}</h1>
          <p className="text-muted-foreground">{t('admin.roles.desc')}</p>
        </div>
        <Button onClick={handleNew}>
          <Plus className="mr-2 h-4 w-4" />
          Add Role
        </Button>
      </div>

      {/* Roles Table */}
      <Card>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center p-8">Loading...</div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Role</TableHead>
                  <TableHead>Code</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead>Priority</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {roles.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                      No roles found
                    </TableCell>
                  </TableRow>
                ) : (
                  roles.map((role) => (
                    <TableRow key={role.id}>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <Shield className="h-4 w-4 text-muted-foreground" />
                          <div>
                            <div className="font-medium">{role.name}</div>
                            {role.nameRu && (
                              <div className="text-xs text-muted-foreground">{role.nameRu}</div>
                            )}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <code className="text-sm bg-muted px-2 py-1 rounded">
                          {role.code || role.name}
                        </code>
                      </TableCell>
                      <TableCell className="max-w-[200px] truncate">
                        {role.description || '-'}
                      </TableCell>
                      <TableCell>{role.priority || 100}</TableCell>
                      <TableCell>
                        <div className="flex flex-col gap-1">
                          <Badge variant={role.active ? 'default' : 'secondary'}>
                            {role.active ? 'Active' : 'Inactive'}
                          </Badge>
                          {role.isSystem && (
                            <Badge variant="outline">System</Badge>
                          )}
                        </div>
                      </TableCell>
                      <TableCell>{formatDate(role.createdAt)}</TableCell>
                      <TableCell className="text-right">
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem
                              onClick={() => handleEdit(role)}
                              disabled={role.isSystem}
                            >
                              Edit
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => handleToggleActive(role)}
                              disabled={role.isSystem}
                            >
                              {role.active ? (
                                <>
                                  <ShieldX className="mr-2 h-4 w-4" />
                                  Deactivate
                                </>
                              ) : (
                                <>
                                  <ShieldCheck className="mr-2 h-4 w-4" />
                                  Activate
                                </>
                              )}
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              className="text-destructive"
                              onClick={() => handleDelete(role)}
                              disabled={role.isSystem}
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              Delete
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Create/Edit Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>{editingRole ? 'Edit Role' : 'Create Role'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Name *</Label>
              <Input id="name" {...register('name')} placeholder="MODERATOR" />
              {errors.name && (
                <p className="text-sm text-destructive">{errors.name.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="code">Code</Label>
              <Input id="code" {...register('code')} placeholder="ROLE_MODERATOR" />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="nameRu">Name (RU)</Label>
                <Input id="nameRu" {...register('nameRu')} placeholder="Moderator" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="nameEn">Name (EN)</Label>
                <Input id="nameEn" {...register('nameEn')} placeholder="Moderator" />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                {...register('description')}
                placeholder="Role description..."
                rows={3}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="priority">Priority</Label>
              <Input
                id="priority"
                type="number"
                {...register('priority', { valueAsNumber: true })}
                placeholder="100"
              />
              <p className="text-xs text-muted-foreground">
                Lower priority = higher importance. Admin = 0, User = 100
              </p>
            </div>

            <div className="flex justify-end gap-2 pt-4">
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
  );
}
