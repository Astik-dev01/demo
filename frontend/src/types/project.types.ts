import { User } from './auth.types';
import { ProjectType, RoleInProject } from './handbook.types';

export interface Project {
  id: string;
  name: string;
  projectKey: string;
  description: string | null;
  owner: User;
  type: ProjectType | null;
  color: string;
  icon: string | null;
  isPublic: boolean;
  isArchived: boolean;
  archivedAt: string | null;
  memberCount: number;
  taskCount: number;
  createdAt: string;
  updatedAt: string | null;
}

export interface ProjectListItem {
  id: string;
  name: string;
  projectKey: string;
  description: string | null;
  ownerId: string;
  ownerName: string;
  typeName: string | null;
  color: string;
  icon: string | null;
  isPublic: boolean;
  isArchived: boolean;
  memberCount: number;
  createdAt: string;
}

export interface ProjectMember {
  id: string;
  user: User;
  role: RoleInProject;
  joinedAt: string;
  invitedBy: User | null;
  isActive: boolean;
}

export interface CreateProjectRequest {
  name: string;
  projectKey: string;
  description?: string;
  typeId?: string;
  color?: string;
  icon?: string;
  isPublic?: boolean;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
  typeId?: string;
  color?: string;
  icon?: string;
  isPublic?: boolean;
}

export interface AddMemberRequest {
  userId: string;
  roleId: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// New pagination format according to page.md
export interface BasePageResponse<T> {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  content: T[];
}

export interface BaseResponse<T> {
  success: boolean;
  message?: string;
  result: T;
  time: string;
  ver: string;
}

export interface ProjectFilter {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
  name?: string;
  projectKey?: string;
  typeId?: string;
  ownerId?: string;
  isArchived?: boolean;
  isPublic?: boolean;
  searchText?: string;
}
