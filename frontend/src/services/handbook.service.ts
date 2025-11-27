import api from './api';
import {
  TaskPriority,
  TaskPriorityRequest,
  TaskStatus,
  TaskStatusRequest,
  ProjectType,
  ProjectTypeRequest,
  TagCategory,
  TagCategoryRequest,
  RoleInProject,
  RoleInProjectRequest,
} from '@/types/handbook.types';

// Task Priorities
export const taskPriorityService = {
  async findAll(): Promise<TaskPriority[]> {
    const response = await api.get<TaskPriority[]>('/handbooks/task-priorities');
    return response.data;
  },

  async findById(id: string): Promise<TaskPriority> {
    const response = await api.get<TaskPriority>(`/handbooks/task-priorities/${id}`);
    return response.data;
  },

  async create(data: TaskPriorityRequest): Promise<TaskPriority> {
    const response = await api.post<TaskPriority>('/handbooks/task-priorities', data);
    return response.data;
  },

  async update(id: string, data: TaskPriorityRequest): Promise<TaskPriority> {
    const response = await api.put<TaskPriority>(`/handbooks/task-priorities/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/handbooks/task-priorities/${id}`);
  },

  async restore(id: string): Promise<void> {
    await api.post(`/handbooks/task-priorities/${id}/restore`);
  },
};

// Task Statuses
export const taskStatusService = {
  async findAll(): Promise<TaskStatus[]> {
    const response = await api.get<TaskStatus[]>('/handbooks/task-statuses');
    return response.data;
  },

  async findById(id: string): Promise<TaskStatus> {
    const response = await api.get<TaskStatus>(`/handbooks/task-statuses/${id}`);
    return response.data;
  },

  async findDefault(): Promise<TaskStatus> {
    const response = await api.get<TaskStatus>('/handbooks/task-statuses/default');
    return response.data;
  },

  async create(data: TaskStatusRequest): Promise<TaskStatus> {
    const response = await api.post<TaskStatus>('/handbooks/task-statuses', data);
    return response.data;
  },

  async update(id: string, data: TaskStatusRequest): Promise<TaskStatus> {
    const response = await api.put<TaskStatus>(`/handbooks/task-statuses/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/handbooks/task-statuses/${id}`);
  },

  async restore(id: string): Promise<void> {
    await api.post(`/handbooks/task-statuses/${id}/restore`);
  },
};

// Project Types
export const projectTypeService = {
  async findAll(): Promise<ProjectType[]> {
    const response = await api.get<ProjectType[]>('/handbooks/project-types');
    return response.data;
  },

  async findById(id: string): Promise<ProjectType> {
    const response = await api.get<ProjectType>(`/handbooks/project-types/${id}`);
    return response.data;
  },

  async create(data: ProjectTypeRequest): Promise<ProjectType> {
    const response = await api.post<ProjectType>('/handbooks/project-types', data);
    return response.data;
  },

  async update(id: string, data: ProjectTypeRequest): Promise<ProjectType> {
    const response = await api.put<ProjectType>(`/handbooks/project-types/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/handbooks/project-types/${id}`);
  },

  async restore(id: string): Promise<void> {
    await api.post(`/handbooks/project-types/${id}/restore`);
  },
};

// Tag Categories
export const tagCategoryService = {
  async findAll(): Promise<TagCategory[]> {
    const response = await api.get<TagCategory[]>('/handbooks/tag-categories');
    return response.data;
  },

  async findById(id: string): Promise<TagCategory> {
    const response = await api.get<TagCategory>(`/handbooks/tag-categories/${id}`);
    return response.data;
  },

  async create(data: TagCategoryRequest): Promise<TagCategory> {
    const response = await api.post<TagCategory>('/handbooks/tag-categories', data);
    return response.data;
  },

  async update(id: string, data: TagCategoryRequest): Promise<TagCategory> {
    const response = await api.put<TagCategory>(`/handbooks/tag-categories/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/handbooks/tag-categories/${id}`);
  },

  async restore(id: string): Promise<void> {
    await api.post(`/handbooks/tag-categories/${id}/restore`);
  },
};

// Project Roles
export const projectRoleService = {
  async findAll(): Promise<RoleInProject[]> {
    const response = await api.get<RoleInProject[]>('/handbooks/project-roles');
    return response.data;
  },

  async findById(id: string): Promise<RoleInProject> {
    const response = await api.get<RoleInProject>(`/handbooks/project-roles/${id}`);
    return response.data;
  },

  async create(data: RoleInProjectRequest): Promise<RoleInProject> {
    const response = await api.post<RoleInProject>('/handbooks/project-roles', data);
    return response.data;
  },

  async update(id: string, data: RoleInProjectRequest): Promise<RoleInProject> {
    const response = await api.put<RoleInProject>(`/handbooks/project-roles/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/handbooks/project-roles/${id}`);
  },

  async restore(id: string): Promise<void> {
    await api.post(`/handbooks/project-roles/${id}/restore`);
  },
};
