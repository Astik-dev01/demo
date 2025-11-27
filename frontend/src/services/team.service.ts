import api from './api';
import {
  Team,
  TeamMember,
  CreateTeamRequest,
  UpdateTeamRequest,
} from '@/types/team.types';
import { Page } from '@/types/project.types';

export const teamService = {
  async create(data: CreateTeamRequest): Promise<Team> {
    const response = await api.post<Team>('/teams', data);
    return response.data;
  },

  async getById(id: string): Promise<Team> {
    const response = await api.get<Team>(`/teams/${id}`);
    return response.data;
  },

  async getMyTeams(page = 0, size = 20): Promise<Page<Team>> {
    const response = await api.get<Page<Team>>('/teams/my', {
      params: { page, size },
    });
    return response.data;
  },

  async getPublicTeams(page = 0, size = 20): Promise<Page<Team>> {
    const response = await api.get<Page<Team>>('/teams/public', {
      params: { page, size },
    });
    return response.data;
  },

  async update(id: string, data: UpdateTeamRequest): Promise<Team> {
    const response = await api.put<Team>(`/teams/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/teams/${id}`);
  },

  // Member management
  async getMembers(teamId: string): Promise<TeamMember[]> {
    const response = await api.get<TeamMember[]>(`/teams/${teamId}/members`);
    return response.data;
  },

  async addMember(teamId: string, userId: string, role = 'MEMBER'): Promise<TeamMember> {
    const response = await api.post<TeamMember>(`/teams/${teamId}/members/${userId}`, null, {
      params: { role },
    });
    return response.data;
  },

  async removeMember(teamId: string, userId: string): Promise<void> {
    await api.delete(`/teams/${teamId}/members/${userId}`);
  },

  async updateMemberRole(teamId: string, userId: string, role: string): Promise<void> {
    await api.put(`/teams/${teamId}/members/${userId}/role`, null, {
      params: { role },
    });
  },
};
