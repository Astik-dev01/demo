import api from './api';
import {
  Project,
  ProjectListItem,
  ProjectMember,
  CreateProjectRequest,
  UpdateProjectRequest,
  AddMemberRequest,
  Page,
  BasePageResponse,
  BaseResponse,
  ProjectFilter,
} from '@/types/project.types';

export const projectService = {
  async create(data: CreateProjectRequest): Promise<Project> {
    const response = await api.post<Project>('/projects', data);
    return response.data;
  },

  async getById(id: string): Promise<Project> {
    const response = await api.get<Project>(`/projects/${id}`);
    return response.data;
  },

  async getByKey(key: string): Promise<Project> {
    const response = await api.get<Project>(`/projects/key/${key}`);
    return response.data;
  },

  // New filter method with proper pagination format
  async filterMyProjects(filter: ProjectFilter = {}): Promise<BasePageResponse<ProjectListItem>> {
    const response = await api.post<BaseResponse<BasePageResponse<ProjectListItem>>>('/projects/my/filter', {
      page: filter.page ?? 1,
      size: filter.size ?? 15,
      sortBy: filter.sortBy,
      sortDirection: filter.sortDirection,
      ...filter,
    });
    return response.data.result;
  },

  async filterPublicProjects(filter: ProjectFilter = {}): Promise<BasePageResponse<ProjectListItem>> {
    const response = await api.post<BaseResponse<BasePageResponse<ProjectListItem>>>('/projects/public/filter', {
      page: filter.page ?? 1,
      size: filter.size ?? 15,
      sortBy: filter.sortBy,
      sortDirection: filter.sortDirection,
      ...filter,
    });
    return response.data.result;
  },

  // Deprecated - use filterMyProjects instead
  async getMyProjects(page = 0, size = 20): Promise<Page<ProjectListItem>> {
    const response = await api.get<Page<ProjectListItem>>('/projects/my', {
      params: { page, size },
    });
    return response.data;
  },

  // Deprecated - use filterPublicProjects instead
  async getPublicProjects(page = 0, size = 20): Promise<Page<ProjectListItem>> {
    const response = await api.get<Page<ProjectListItem>>('/projects/public', {
      params: { page, size },
    });
    return response.data;
  },

  async update(id: string, data: UpdateProjectRequest): Promise<Project> {
    const response = await api.put<Project>(`/projects/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/projects/${id}`);
  },

  async archive(id: string): Promise<void> {
    await api.post(`/projects/${id}/archive`);
  },

  async unarchive(id: string): Promise<void> {
    await api.post(`/projects/${id}/unarchive`);
  },

  // Member management
  async getMembers(projectId: string): Promise<ProjectMember[]> {
    const response = await api.get<ProjectMember[]>(`/projects/${projectId}/members`);
    return response.data;
  },

  async addMember(projectId: string, data: AddMemberRequest): Promise<ProjectMember> {
    const response = await api.post<ProjectMember>(`/projects/${projectId}/members`, data);
    return response.data;
  },

  async removeMember(projectId: string, memberId: string): Promise<void> {
    await api.delete(`/projects/${projectId}/members/${memberId}`);
  },

  async updateMemberRole(projectId: string, memberId: string, roleId: string): Promise<void> {
    await api.put(`/projects/${projectId}/members/${memberId}/role`, null, {
      params: { roleId },
    });
  },
};
