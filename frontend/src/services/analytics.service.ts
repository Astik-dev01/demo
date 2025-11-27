import api from './api';
import {
  DashboardOverview,
  ProjectAnalytics,
  BurndownChart,
  VelocityChart,
} from '@/types/analytics.types';

export const analyticsService = {
  async getDashboardOverview(): Promise<DashboardOverview> {
    const response = await api.get<DashboardOverview>('/analytics/dashboard');
    return response.data;
  },

  async getProjectAnalytics(projectId: string): Promise<ProjectAnalytics> {
    const response = await api.get<ProjectAnalytics>(`/analytics/projects/${projectId}`);
    return response.data;
  },

  async getBurndownChart(
    projectId: string,
    startDate?: string,
    endDate?: string
  ): Promise<BurndownChart> {
    const response = await api.get<BurndownChart>(`/analytics/projects/${projectId}/burndown`, {
      params: { startDate, endDate },
    });
    return response.data;
  },

  async getVelocityChart(projectId: string, weeks = 8): Promise<VelocityChart> {
    const response = await api.get<VelocityChart>(`/analytics/projects/${projectId}/velocity`, {
      params: { weeks },
    });
    return response.data;
  },
};
