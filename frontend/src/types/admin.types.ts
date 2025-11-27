export interface AdminRole {
  id: string;
  name: string;
  code: string;
  nameRu: string | null;
  nameEn: string | null;
}

export interface AdminUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  avatarUrl: string | null;
  phone: string | null;
  isActive: boolean;
  isEmailVerified: boolean;
  isDeleted: boolean;
  roles: AdminRole[];
  createdAt: string;
  updatedAt: string | null;
  deletedAt: string | null;
}

export interface AdminUserFilterRequest {
  search?: string;
  roleId?: string;
  isActive?: boolean;
  isDeleted?: boolean;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  isActive?: boolean;
  isEmailVerified?: boolean;
  roleIds?: string[];
}

export interface UpdateUserRequest {
  email?: string;
  password?: string;
  firstName: string;
  lastName: string;
  phone?: string;
  isActive?: boolean;
  isEmailVerified?: boolean;
  roleIds?: string[];
}

export interface AdminUserPage {
  content: AdminUser[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
  };
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  size: number;
  number: number;
  empty: boolean;
}

export interface SystemRole {
  id: string;
  name: string;
  code: string;
  nameRu: string | null;
  nameEn: string | null;
  description: string | null;
  priority: number;
  active: boolean;
  isSystem: boolean;
  createdAt: string;
}
