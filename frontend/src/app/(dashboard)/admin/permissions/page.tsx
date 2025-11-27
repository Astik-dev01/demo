'use client';

import { useState, useEffect, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { Plus, MoreHorizontal, Trash2, RotateCcw, Route, Check, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
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
} from '@/components/ui/dialog';
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
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  adminRouteService,
  adminRoleService,
  adminPermissionService,
  AvailableRoute,
  Permission,
  CreateRouteRequest,
} from '@/services/admin.service';
import { SystemRole } from '@/types/admin.types';
import { useLanguage } from '@/contexts/language-context';

const routeSchema = z.object({
  code: z.string().min(1, 'Code is required'),
  descriptionRu: z.string().optional(),
  descriptionEn: z.string().optional(),
});

type RouteForm = z.infer<typeof routeSchema>;

export default function AdminPermissionsPage() {
  const { t, language } = useLanguage();
  const [routes, setRoutes] = useState<AvailableRoute[]>([]);
  const [roles, setRoles] = useState<SystemRole[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(true);
  const [routeDialogOpen, setRouteDialogOpen] = useState(false);
  const [editingRoute, setEditingRoute] = useState<AvailableRoute | null>(null);
  const [selectedRoleId, setSelectedRoleId] = useState<string>('');

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<RouteForm>({
    resolver: zodResolver(routeSchema),
  });

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const [routesData, rolesData, permissionsData] = await Promise.all([
        adminRouteService.findAll(),
        adminRoleService.findAll(),
        adminPermissionService.findAll(),
      ]);
      setRoutes(routesData);
      setRoles(rolesData);
      setPermissions(permissionsData);

      if (rolesData.length > 0 && !selectedRoleId) {
        setSelectedRoleId(rolesData[0].id);
      }
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  }, [selectedRoleId]);

  useEffect(() => {
    loadData();
  }, []);

  const onSubmitRoute = async (data: RouteForm) => {
    try {
      if (editingRoute) {
        await adminRouteService.update(editingRoute.id, data);
        toast.success('Route updated');
      } else {
        await adminRouteService.create(data as CreateRouteRequest);
        toast.success('Route created');
      }
      setRouteDialogOpen(false);
      reset();
      setEditingRoute(null);
      loadData();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleEditRoute = (route: AvailableRoute) => {
    setEditingRoute(route);
    setValue('code', route.code);
    setValue('descriptionRu', route.descriptionRu || '');
    setValue('descriptionEn', route.descriptionEn || '');
    setRouteDialogOpen(true);
  };

  const handleNewRoute = () => {
    setEditingRoute(null);
    reset({ code: '', descriptionRu: '', descriptionEn: '' });
    setRouteDialogOpen(true);
  };

  const handleDeleteRoute = async (route: AvailableRoute) => {
    if (!confirm(`Are you sure you want to delete "${route.code}"?`)) return;
    try {
      await adminRouteService.delete(route.id);
      toast.success('Route deleted');
      loadData();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to delete');
    }
  };

  const handleRestoreRoute = async (route: AvailableRoute) => {
    try {
      await adminRouteService.restore(route.id);
      toast.success('Route restored');
      loadData();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to restore');
    }
  };

  const getPermission = (roleId: string, routeId: string): Permission | undefined => {
    return permissions.find(p => p.roleId === roleId && p.routeId === routeId);
  };

  const handlePermissionChange = async (
    roleId: string,
    routeId: string,
    method: 'methodGet' | 'methodPost' | 'methodPut' | 'methodDelete',
    value: boolean
  ) => {
    try {
      const existingPermission = getPermission(roleId, routeId);

      if (existingPermission) {
        await adminPermissionService.update(existingPermission.id, {
          [method]: value,
        });
      } else {
        await adminPermissionService.createOrUpdate({
          roleId,
          routeId,
          [method]: value,
        });
      }

      // Reload permissions
      const updatedPermissions = await adminPermissionService.findAll();
      setPermissions(updatedPermissions);
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to update permission');
    }
  };

  const getRouteDescription = (route: AvailableRoute) => {
    if (language === 'ru' && route.descriptionRu) return route.descriptionRu;
    if (language === 'en' && route.descriptionEn) return route.descriptionEn;
    return route.descriptionRu || route.descriptionEn || route.code;
  };

  const formatDate = (dateString: string | null) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const activeRoutes = routes.filter(r => !r.isDeleted);
  const selectedRole = roles.find(r => r.id === selectedRoleId);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">{t('nav.admin.permissions')}</h1>
          <p className="text-muted-foreground">{t('admin.permissions.desc')}</p>
        </div>
      </div>

      <Tabs defaultValue="matrix" className="space-y-4">
        <TabsList>
          <TabsTrigger value="matrix">Permission Matrix</TabsTrigger>
          <TabsTrigger value="routes">Routes</TabsTrigger>
        </TabsList>

        <TabsContent value="matrix" className="space-y-4">
          {/* Role Selector */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base">Select Role</CardTitle>
            </CardHeader>
            <CardContent>
              <Select value={selectedRoleId} onValueChange={setSelectedRoleId}>
                <SelectTrigger className="w-[300px]">
                  <SelectValue placeholder="Select role" />
                </SelectTrigger>
                <SelectContent>
                  {roles.map((role) => (
                    <SelectItem key={role.id} value={role.id}>
                      {role.name} {role.isSystem && '(System)'}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </CardContent>
          </Card>

          {/* Permission Matrix */}
          {selectedRole && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">
                  Permissions for: {selectedRole.name}
                </CardTitle>
              </CardHeader>
              <CardContent className="p-0">
                {loading ? (
                  <div className="flex items-center justify-center p-8">Loading...</div>
                ) : (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Route</TableHead>
                        <TableHead className="text-center w-20">GET</TableHead>
                        <TableHead className="text-center w-20">POST</TableHead>
                        <TableHead className="text-center w-20">PUT</TableHead>
                        <TableHead className="text-center w-20">DELETE</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {activeRoutes.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={5} className="text-center py-8 text-muted-foreground">
                            No routes found
                          </TableCell>
                        </TableRow>
                      ) : (
                        activeRoutes.map((route) => {
                          const perm = getPermission(selectedRoleId, route.id);
                          return (
                            <TableRow key={route.id}>
                              <TableCell>
                                <div>
                                  <code className="text-sm">{route.code}</code>
                                  <div className="text-xs text-muted-foreground">
                                    {getRouteDescription(route)}
                                  </div>
                                </div>
                              </TableCell>
                              <TableCell className="text-center">
                                <Checkbox
                                  checked={perm?.methodGet || false}
                                  onCheckedChange={(checked) =>
                                    handlePermissionChange(selectedRoleId, route.id, 'methodGet', !!checked)
                                  }
                                />
                              </TableCell>
                              <TableCell className="text-center">
                                <Checkbox
                                  checked={perm?.methodPost || false}
                                  onCheckedChange={(checked) =>
                                    handlePermissionChange(selectedRoleId, route.id, 'methodPost', !!checked)
                                  }
                                />
                              </TableCell>
                              <TableCell className="text-center">
                                <Checkbox
                                  checked={perm?.methodPut || false}
                                  onCheckedChange={(checked) =>
                                    handlePermissionChange(selectedRoleId, route.id, 'methodPut', !!checked)
                                  }
                                />
                              </TableCell>
                              <TableCell className="text-center">
                                <Checkbox
                                  checked={perm?.methodDelete || false}
                                  onCheckedChange={(checked) =>
                                    handlePermissionChange(selectedRoleId, route.id, 'methodDelete', !!checked)
                                  }
                                />
                              </TableCell>
                            </TableRow>
                          );
                        })
                      )}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="routes" className="space-y-4">
          <div className="flex justify-end">
            <Button onClick={handleNewRoute}>
              <Plus className="mr-2 h-4 w-4" />
              Add Route
            </Button>
          </div>

          {/* Routes Table */}
          <Card>
            <CardContent className="p-0">
              {loading ? (
                <div className="flex items-center justify-center p-8">Loading...</div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Route Code</TableHead>
                      <TableHead>Description (RU)</TableHead>
                      <TableHead>Description (EN)</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Created</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {routes.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                          No routes found
                        </TableCell>
                      </TableRow>
                    ) : (
                      routes.map((route) => (
                        <TableRow key={route.id} className={route.isDeleted ? 'opacity-50' : ''}>
                          <TableCell>
                            <div className="flex items-center gap-2">
                              <Route className="h-4 w-4 text-muted-foreground" />
                              <code className="text-sm">{route.code}</code>
                            </div>
                          </TableCell>
                          <TableCell className="max-w-[200px] truncate">
                            {route.descriptionRu || '-'}
                          </TableCell>
                          <TableCell className="max-w-[200px] truncate">
                            {route.descriptionEn || '-'}
                          </TableCell>
                          <TableCell>
                            <Badge variant={route.isDeleted ? 'destructive' : 'default'}>
                              {route.isDeleted ? 'Deleted' : 'Active'}
                            </Badge>
                          </TableCell>
                          <TableCell>{formatDate(route.createdAt)}</TableCell>
                          <TableCell className="text-right">
                            <DropdownMenu>
                              <DropdownMenuTrigger asChild>
                                <Button variant="ghost" size="icon">
                                  <MoreHorizontal className="h-4 w-4" />
                                </Button>
                              </DropdownMenuTrigger>
                              <DropdownMenuContent align="end">
                                <DropdownMenuItem onClick={() => handleEditRoute(route)}>
                                  Edit
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                                {route.isDeleted ? (
                                  <DropdownMenuItem onClick={() => handleRestoreRoute(route)}>
                                    <RotateCcw className="mr-2 h-4 w-4" />
                                    Restore
                                  </DropdownMenuItem>
                                ) : (
                                  <DropdownMenuItem
                                    className="text-destructive"
                                    onClick={() => handleDeleteRoute(route)}
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
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Create/Edit Route Dialog */}
      <Dialog open={routeDialogOpen} onOpenChange={setRouteDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>{editingRoute ? 'Edit Route' : 'Create Route'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmitRoute)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="code">Route Code *</Label>
              <Input
                id="code"
                {...register('code')}
                placeholder="/api/users"
              />
              {errors.code && (
                <p className="text-sm text-destructive">{errors.code.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="descriptionRu">Description (RU)</Label>
              <Input
                id="descriptionRu"
                {...register('descriptionRu')}
                placeholder="Description in Russian"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="descriptionEn">Description (EN)</Label>
              <Input
                id="descriptionEn"
                {...register('descriptionEn')}
                placeholder="Description in English"
              />
            </div>

            <div className="flex justify-end gap-2 pt-4">
              <Button type="button" variant="outline" onClick={() => setRouteDialogOpen(false)}>
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
