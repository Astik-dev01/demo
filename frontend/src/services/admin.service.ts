import api from './api';
import {
  AdminUser,
  AdminUserPage,
  AdminUserFilterRequest,
  CreateUserRequest,
  UpdateUserRequest,
  SystemRole,
} from '@/types/admin.types';

export const adminUserService = {
  async findAll(
    filter: AdminUserFilterRequest = {},
    page: number = 0,
    size: number = 20,
    sort: string = 'createdAt,desc'
  ): Promise<AdminUserPage> {
    const params: Record<string, any> = {
      page,
      size,
      sort,
    };
    if (filter.search) params.search = filter.search;
    if (filter.roleId) params.roleId = filter.roleId;
    if (filter.isActive !== undefined) params.isActive = filter.isActive;
    if (filter.isDeleted !== undefined) params.isDeleted = filter.isDeleted;

    const response = await api.get<AdminUserPage>('/admin/users', { params });
    return response.data;
  },

  async findById(id: string): Promise<AdminUser> {
    const response = await api.get<AdminUser>(`/admin/users/${id}`);
    return response.data;
  },

  async create(data: CreateUserRequest): Promise<AdminUser> {
    const response = await api.post<AdminUser>('/admin/users', data);
    return response.data;
  },

  async update(id: string, data: UpdateUserRequest): Promise<AdminUser> {
    const response = await api.put<AdminUser>(`/admin/users/${id}`, data);
    return response.data;
  },

  async toggleActive(id: string): Promise<void> {
    await api.put(`/admin/users/${id}/toggle-active`);
  },

  async assignRoles(id: string, roleIds: string[]): Promise<void> {
    await api.put(`/admin/users/${id}/roles`, roleIds);
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/admin/users/${id}`);
  },

  async restore(id: string): Promise<void> {
    await api.post(`/admin/users/${id}/restore`);
  },
};

// Admin Role Service (extended)
export interface RoleWithPermissions {
  id: string;
  name: string;
  code: string;
  nameRu: string | null;
  nameEn: string | null;
  description: string | null;
  priority: number;
  active: boolean;
  isSystem: boolean;
  createdAt: string | null;
  permissions: RolePermission[];
}

export interface RolePermission {
  id: string;
  routeId: string;
  routeCode: string;
  routeDescriptionRu: string | null;
  routeDescriptionEn: string | null;
  methodGet: boolean;
  methodPost: boolean;
  methodPut: boolean;
  methodDelete: boolean;
}

export interface CreateRoleRequest {
  name: string;
  code?: string;
  nameRu?: string;
  nameEn?: string;
  description?: string;
  priority?: number;
}

export interface UpdateRoleRequest {
  name?: string;
  code?: string;
  nameRu?: string;
  nameEn?: string;
  description?: string;
  priority?: number;
  active?: boolean;
}

export const adminRoleService = {
  async findAll(): Promise<SystemRole[]> {
    try {
      const response = await api.get<SystemRole[]>('/admin/roles');
      return response.data;
    } catch (error) {
      return [];
    }
  },

  async findAllActive(): Promise<SystemRole[]> {
    const response = await api.get<SystemRole[]>('/admin/roles/active');
    return response.data;
  },

  async findById(id: string): Promise<RoleWithPermissions> {
    const response = await api.get<RoleWithPermissions>(`/admin/roles/${id}`);
    return response.data;
  },

  async create(data: CreateRoleRequest): Promise<SystemRole> {
    const response = await api.post<SystemRole>('/admin/roles', data);
    return response.data;
  },

  async update(id: string, data: UpdateRoleRequest): Promise<SystemRole> {
    const response = await api.put<SystemRole>(`/admin/roles/${id}`, data);
    return response.data;
  },

  async toggleActive(id: string): Promise<void> {
    await api.put(`/admin/roles/${id}/toggle-active`);
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/admin/roles/${id}`);
  },
};

// Admin Route Service
export interface AvailableRoute {
  id: string;
  code: string;
  descriptionRu: string | null;
  descriptionEn: string | null;
  createdAt: string;
  updatedAt: string | null;
  isDeleted: boolean;
}

export interface CreateRouteRequest {
  code: string;
  descriptionRu?: string;
  descriptionEn?: string;
}

export interface UpdateRouteRequest {
  code?: string;
  descriptionRu?: string;
  descriptionEn?: string;
}

export const adminRouteService = {
  async findAll(): Promise<AvailableRoute[]> {
    const response = await api.get<AvailableRoute[]>('/admin/routes');
    return response.data;
  },

  async findAllActive(): Promise<AvailableRoute[]> {
    const response = await api.get<AvailableRoute[]>('/admin/routes/active');
    return response.data;
  },

  async findById(id: string): Promise<AvailableRoute> {
    const response = await api.get<AvailableRoute>(`/admin/routes/${id}`);
    return response.data;
  },

  async create(data: CreateRouteRequest): Promise<AvailableRoute> {
    const response = await api.post<AvailableRoute>('/admin/routes', data);
    return response.data;
  },

  async update(id: string, data: UpdateRouteRequest): Promise<AvailableRoute> {
    const response = await api.put<AvailableRoute>(`/admin/routes/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/admin/routes/${id}`);
  },

  async restore(id: string): Promise<void> {
    await api.post(`/admin/routes/${id}/restore`);
  },
};

// Admin Permission Service
export interface Permission {
  id: string;
  roleId: string;
  roleName: string;
  roleCode: string;
  routeId: string;
  routeCode: string;
  routeDescriptionRu: string | null;
  routeDescriptionEn: string | null;
  methodGet: boolean;
  methodPost: boolean;
  methodPut: boolean;
  methodDelete: boolean;
  createdAt: string | null;
}

export interface CreatePermissionRequest {
  roleId: string;
  routeId: string;
  methodGet?: boolean;
  methodPost?: boolean;
  methodPut?: boolean;
  methodDelete?: boolean;
}

export interface UpdatePermissionRequest {
  methodGet?: boolean;
  methodPost?: boolean;
  methodPut?: boolean;
  methodDelete?: boolean;
}

export interface BulkPermissionRequest {
  roleId: string;
  permissions: {
    routeId: string;
    methodGet?: boolean;
    methodPost?: boolean;
    methodPut?: boolean;
    methodDelete?: boolean;
  }[];
}

export const adminPermissionService = {
  async findAll(): Promise<Permission[]> {
    const response = await api.get<Permission[]>('/admin/permissions');
    return response.data;
  },

  async findByRole(roleId: string): Promise<Permission[]> {
    const response = await api.get<Permission[]>(`/admin/permissions/by-role/${roleId}`);
    return response.data;
  },

  async findByRoute(routeId: string): Promise<Permission[]> {
    const response = await api.get<Permission[]>(`/admin/permissions/by-route/${routeId}`);
    return response.data;
  },

  async createOrUpdate(data: CreatePermissionRequest): Promise<Permission> {
    const response = await api.post<Permission>('/admin/permissions', data);
    return response.data;
  },

  async update(id: string, data: UpdatePermissionRequest): Promise<Permission> {
    const response = await api.put<Permission>(`/admin/permissions/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/admin/permissions/${id}`);
  },

  async bulkUpdate(data: BulkPermissionRequest): Promise<Permission[]> {
    const response = await api.post<Permission[]>('/admin/permissions/bulk', data);
    return response.data;
  },
};

// Admin Project Service
export interface AdminProjectPage {
  content: any[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const adminProjectService = {
  async findAll(
    page: number = 0,
    size: number = 20,
    isArchived?: boolean,
    isDeleted?: boolean
  ): Promise<AdminProjectPage> {
    const params: Record<string, any> = { page, size };
    if (isArchived !== undefined) params.isArchived = isArchived;
    if (isDeleted !== undefined) params.isDeleted = isDeleted;

    const response = await api.get<AdminProjectPage>('/admin/projects', { params });
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/admin/projects/${id}`);
  },

  async restore(id: string): Promise<void> {
    await api.post(`/admin/projects/${id}/restore`);
  },

  async archive(id: string): Promise<void> {
    await api.post(`/admin/projects/${id}/archive`);
  },

  async unarchive(id: string): Promise<void> {
    await api.post(`/admin/projects/${id}/unarchive`);
  },
};

// Admin Task Service
export interface AdminTaskPage {
  content: any[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const adminTaskService = {
  async findAll(
    page: number = 0,
    size: number = 20,
    filters: {
      search?: string;
      projectId?: string;
      statusId?: string;
      priorityId?: string;
      assigneeId?: string;
      isDeleted?: boolean;
    } = {}
  ): Promise<AdminTaskPage> {
    const params: Record<string, any> = { page, size, ...filters };
    const response = await api.get<AdminTaskPage>('/admin/tasks', { params });
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/admin/tasks/${id}`);
  },

  async restore(id: string): Promise<void> {
    await api.post(`/admin/tasks/${id}/restore`);
  },
};
