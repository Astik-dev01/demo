'use client';

import { useEffect, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { Search, Plus, MoreHorizontal, UserCheck, UserX, Trash2, RotateCcw, Shield } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
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
import { adminUserService, adminRoleService } from '@/services/admin.service';
import { AdminUser, CreateUserRequest, SystemRole } from '@/types/admin.types';
import { useLanguage } from '@/contexts/language-context';
import { useDebounce } from '@/hooks/use-debounce';
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

// ==================== ZUSTAND STORE ====================
interface AdminUsersPageState {
  users: AdminUser[];
  roles: SystemRole[];
  loading: boolean;
  dialogOpen: boolean;
  editingUser: AdminUser | null;
  searchQuery: string;
  filterActive: string;
  filterDeleted: string;
  page: number;
  totalPages: number;
  totalElements: number;

  // Actions
  setUsers: (users: AdminUser[]) => void;
  setRoles: (roles: SystemRole[]) => void;
  setLoading: (loading: boolean) => void;
  setDialogOpen: (open: boolean) => void;
  setEditingUser: (user: AdminUser | null) => void;
  setSearchQuery: (query: string) => void;
  setFilterActive: (filter: string) => void;
  setFilterDeleted: (filter: string) => void;
  setPage: (page: number) => void;
  setPagination: (totalPages: number, totalElements: number) => void;
  updateUser: (user: AdminUser) => void;
  removeUser: (id: string) => void;
  reset: () => void;
}

const initialState = {
  users: [],
  roles: [],
  loading: true,
  dialogOpen: false,
  editingUser: null,
  searchQuery: '',
  filterActive: 'all',
  filterDeleted: 'false',
  page: 0,
  totalPages: 0,
  totalElements: 0,
};

const useAdminUsersPageStore = create<AdminUsersPageState>()(
  devtools(
    (set) => ({
      ...initialState,
      setUsers: (users) => set({ users }),
      setRoles: (roles) => set({ roles }),
      setLoading: (loading) => set({ loading }),
      setDialogOpen: (dialogOpen) => set({ dialogOpen }),
      setEditingUser: (editingUser) => set({ editingUser }),
      setSearchQuery: (searchQuery) => set({ searchQuery, page: 0 }),
      setFilterActive: (filterActive) => set({ filterActive, page: 0 }),
      setFilterDeleted: (filterDeleted) => set({ filterDeleted, page: 0 }),
      setPage: (page) => set({ page }),
      setPagination: (totalPages, totalElements) => set({ totalPages, totalElements }),
      updateUser: (updatedUser) =>
        set((state) => ({
          users: state.users.map((u) => (u.id === updatedUser.id ? updatedUser : u)),
        })),
      removeUser: (id) =>
        set((state) => ({
          users: state.users.filter((u) => u.id !== id),
          totalElements: state.totalElements - 1,
        })),
      reset: () => set(initialState),
    }),
    { name: 'admin-users-page' }
  )
);

// ==================== SCHEMAS ====================
const createUserSchema = z.object({
  email: z.string().email('Invalid email'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  phone: z.string().regex(/^\+996\d{9}$/, 'Phone must be +996XXXXXXXXX').optional().or(z.literal('')),
  isActive: z.boolean(),
  isEmailVerified: z.boolean(),
  roleIds: z.array(z.string()),
});

const updateUserSchema = z.object({
  email: z.string().email('Invalid email').optional(),
  password: z.string().min(6, 'Password must be at least 6 characters').optional().or(z.literal('')),
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  phone: z.string().regex(/^\+996\d{9}$/, 'Phone must be +996XXXXXXXXX').optional().or(z.literal('')),
  isActive: z.boolean(),
  isEmailVerified: z.boolean(),
  roleIds: z.array(z.string()),
});

type CreateUserForm = z.infer<typeof createUserSchema>;
type UpdateUserForm = z.infer<typeof updateUserSchema>;

// ==================== COMPONENT ====================
export default function AdminUsersPage() {
  const { t } = useLanguage();

  // Zustand store
  const {
    users,
    roles,
    loading,
    dialogOpen,
    editingUser,
    searchQuery,
    filterActive,
    filterDeleted,
    page,
    totalPages,
    totalElements,
    setUsers,
    setRoles,
    setLoading,
    setDialogOpen,
    setEditingUser,
    setSearchQuery,
    setFilterActive,
    setFilterDeleted,
    setPage,
    setPagination,
    updateUser,
  } = useAdminUsersPageStore();

  const debouncedSearch = useDebounce(searchQuery, 300);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<CreateUserForm | UpdateUserForm>({
    resolver: zodResolver(editingUser ? updateUserSchema : createUserSchema),
    defaultValues: {
      isActive: true,
      isEmailVerified: false,
      roleIds: [],
    },
  });

  const loadRoles = async () => {
    try {
      const data = await adminRoleService.findAll();
      if (data.length > 0) {
        setRoles(data);
      }
    } catch (error) {
      // Roles endpoint not ready
    }
  };

  const loadUsers = useCallback(async () => {
    try {
      setLoading(true);
      const filter: any = {};

      if (debouncedSearch) {
        filter.search = debouncedSearch;
      }

      if (filterActive !== 'all') {
        filter.isActive = filterActive === 'true';
      }

      if (filterDeleted !== 'all') {
        filter.isDeleted = filterDeleted === 'true';
      }

      const response = await adminUserService.findAll(filter, page, 20);
      setUsers(response.content);
      setPagination(response.totalPages, response.totalElements);
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch, filterActive, filterDeleted, page, setUsers, setLoading, setPagination]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  useEffect(() => {
    loadRoles();
  }, []);

  const onSubmit = async (data: CreateUserForm | UpdateUserForm) => {
    try {
      const payload: any = {
        ...data,
        phone: data.phone || null,
      };

      if (editingUser) {
        if (!payload.password) {
          delete payload.password;
        }
        await adminUserService.update(editingUser.id, payload);
        toast.success('User updated');
      } else {
        await adminUserService.create(payload as CreateUserRequest);
        toast.success('User created');
      }
      setDialogOpen(false);
      reset();
      setEditingUser(null);
      loadUsers();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleEdit = (user: AdminUser) => {
    setEditingUser(user);
    setValue('email', user.email);
    setValue('firstName', user.firstName);
    setValue('lastName', user.lastName);
    setValue('phone', user.phone || '');
    setValue('isActive', user.isActive);
    setValue('isEmailVerified', user.isEmailVerified);
    setValue('roleIds', user.roles.map(r => r.id));
    setValue('password', '');
    setDialogOpen(true);
  };

  const handleNew = () => {
    setEditingUser(null);
    reset({
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      phone: '',
      isActive: true,
      isEmailVerified: false,
      roleIds: [],
    });
    setDialogOpen(true);
  };

  const handleToggleActive = async (user: AdminUser) => {
    try {
      await adminUserService.toggleActive(user.id);
      toast.success(user.isActive ? 'User blocked' : 'User unblocked');
      loadUsers();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (user: AdminUser) => {
    if (!confirm(`Are you sure you want to delete ${user.fullName}?`)) return;
    try {
      await adminUserService.delete(user.id);
      toast.success('User deleted');
      loadUsers();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to delete user');
    }
  };

  const handleRestore = async (user: AdminUser) => {
    try {
      await adminUserService.restore(user.id);
      toast.success('User restored');
      loadUsers();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to restore user');
    }
  };

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const watchedRoleIds = watch('roleIds') || [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">{t('nav.admin.users')}</h1>
          <p className="text-muted-foreground">{t('admin.users.desc')}</p>
        </div>
        <Button onClick={handleNew}>
          <Plus className="mr-2 h-4 w-4" />
          Add User
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-wrap gap-4">
            <div className="flex-1 min-w-[200px]">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search users..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <Select value={filterActive} onValueChange={setFilterActive}>
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder="Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="true">Active</SelectItem>
                <SelectItem value="false">Blocked</SelectItem>
              </SelectContent>
            </Select>
            <Select value={filterDeleted} onValueChange={setFilterDeleted}>
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder="Deleted" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="false">Active Users</SelectItem>
                <SelectItem value="true">Deleted Users</SelectItem>
                <SelectItem value="all">All Users</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Users Table */}
      <Card>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center p-8">Loading...</div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>User</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Roles</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {users.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                        No users found
                      </TableCell>
                    </TableRow>
                  ) : (
                    users.map((user) => (
                      <TableRow key={user.id} className={user.isDeleted ? 'opacity-50' : ''}>
                        <TableCell>
                          <div className="flex items-center gap-3">
                            <Avatar className="h-8 w-8">
                              <AvatarImage src={user.avatarUrl || undefined} />
                              <AvatarFallback>{getInitials(user.fullName)}</AvatarFallback>
                            </Avatar>
                            <div>
                              <div className="font-medium">{user.fullName}</div>
                              {user.phone && (
                                <div className="text-xs text-muted-foreground">{user.phone}</div>
                              )}
                            </div>
                          </div>
                        </TableCell>
                        <TableCell>{user.email}</TableCell>
                        <TableCell>
                          <div className="flex flex-wrap gap-1">
                            {user.roles.map((role) => (
                              <Badge key={role.id} variant={role.name === 'ADMIN' ? 'destructive' : 'secondary'}>
                                {role.name}
                              </Badge>
                            ))}
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="flex flex-col gap-1">
                            <Badge variant={user.isActive ? 'default' : 'outline'}>
                              {user.isActive ? 'Active' : 'Blocked'}
                            </Badge>
                            {user.isDeleted && (
                              <Badge variant="destructive">Deleted</Badge>
                            )}
                          </div>
                        </TableCell>
                        <TableCell>{formatDate(user.createdAt)}</TableCell>
                        <TableCell className="text-right">
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button variant="ghost" size="icon">
                                <MoreHorizontal className="h-4 w-4" />
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                              <DropdownMenuItem onClick={() => handleEdit(user)}>
                                Edit
                              </DropdownMenuItem>
                              <DropdownMenuItem onClick={() => handleToggleActive(user)}>
                                {user.isActive ? (
                                  <>
                                    <UserX className="mr-2 h-4 w-4" />
                                    Block
                                  </>
                                ) : (
                                  <>
                                    <UserCheck className="mr-2 h-4 w-4" />
                                    Unblock
                                  </>
                                )}
                              </DropdownMenuItem>
                              <DropdownMenuSeparator />
                              {user.isDeleted ? (
                                <DropdownMenuItem onClick={() => handleRestore(user)}>
                                  <RotateCcw className="mr-2 h-4 w-4" />
                                  Restore
                                </DropdownMenuItem>
                              ) : (
                                <DropdownMenuItem
                                  className="text-destructive"
                                  onClick={() => handleDelete(user)}
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
                    Showing {page * 20 + 1} to {Math.min((page + 1) * 20, totalElements)} of {totalElements} users
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={page === 0}
                      onClick={() => setPage(page - 1)}
                    >
                      Previous
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={page >= totalPages - 1}
                      onClick={() => setPage(page + 1)}
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

      {/* Create/Edit Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>{editingUser ? 'Edit User' : 'Create User'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName">First Name</Label>
                <Input id="firstName" {...register('firstName')} />
                {errors.firstName && (
                  <p className="text-sm text-destructive">{errors.firstName.message}</p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName">Last Name</Label>
                <Input id="lastName" {...register('lastName')} />
                {errors.lastName && (
                  <p className="text-sm text-destructive">{errors.lastName.message}</p>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" {...register('email')} />
              {errors.email && (
                <p className="text-sm text-destructive">{errors.email.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">
                Password {editingUser && '(leave empty to keep current)'}
              </Label>
              <Input id="password" type="password" {...register('password')} />
              {errors.password && (
                <p className="text-sm text-destructive">{errors.password.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="phone">Phone</Label>
              <Input id="phone" {...register('phone')} placeholder="+996XXXXXXXXX" />
              {errors.phone && (
                <p className="text-sm text-destructive">{errors.phone.message}</p>
              )}
            </div>

            <div className="flex gap-6">
              <div className="flex items-center gap-2">
                <Checkbox
                  id="isActive"
                  checked={watch('isActive')}
                  onCheckedChange={(checked) => setValue('isActive', !!checked)}
                />
                <Label htmlFor="isActive">Active</Label>
              </div>
              <div className="flex items-center gap-2">
                <Checkbox
                  id="isEmailVerified"
                  checked={watch('isEmailVerified')}
                  onCheckedChange={(checked) => setValue('isEmailVerified', !!checked)}
                />
                <Label htmlFor="isEmailVerified">Email Verified</Label>
              </div>
            </div>

            <div className="space-y-2">
              <Label>Roles</Label>
              <div className="flex flex-wrap gap-2 p-3 border rounded-md min-h-[60px]">
                {roles.length > 0 ? (
                  roles.map((role) => (
                    <Badge
                      key={role.id}
                      variant={watchedRoleIds.includes(role.id) ? 'default' : 'outline'}
                      className="cursor-pointer"
                      onClick={() => {
                        const current = watchedRoleIds;
                        if (current.includes(role.id)) {
                          setValue('roleIds', current.filter(r => r !== role.id));
                        } else {
                          setValue('roleIds', [...current, role.id]);
                        }
                      }}
                    >
                      <Shield className="mr-1 h-3 w-3" />
                      {role.name}
                    </Badge>
                  ))
                ) : (
                  <span className="text-sm text-muted-foreground">Loading roles...</span>
                )}
              </div>
              <p className="text-xs text-muted-foreground">Click to toggle role selection</p>
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
