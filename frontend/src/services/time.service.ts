import api from './api';
import {
  TimeEntry,
  CreateTimeEntryRequest,
  UpdateTimeEntryRequest,
  TimeReport,
} from '@/types/time.types';
import { Page } from '@/types/project.types';

export const timeService = {
  // Timer operations
  async startTimer(taskId: string, description?: string): Promise<TimeEntry> {
    const response = await api.post<TimeEntry>(`/time/timer/start/${taskId}`, null, {
      params: { description },
    });
    return response.data;
  },

  async stopTimer(): Promise<TimeEntry> {
    const response = await api.post<TimeEntry>('/time/timer/stop');
    return response.data;
  },

  async getRunningTimer(): Promise<TimeEntry | null> {
    try {
      const response = await api.get<TimeEntry>('/time/timer/running');
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 204) {
        return null;
      }
      throw error;
    }
  },

  // CRUD operations
  async create(data: CreateTimeEntryRequest): Promise<TimeEntry> {
    const response = await api.post<TimeEntry>('/time', data);
    return response.data;
  },

  async getById(id: string): Promise<TimeEntry> {
    const response = await api.get<TimeEntry>(`/time/${id}`);
    return response.data;
  },

  async update(id: string, data: UpdateTimeEntryRequest): Promise<TimeEntry> {
    const response = await api.put<TimeEntry>(`/time/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/time/${id}`);
  },

  // Queries
  async getByTask(taskId: string): Promise<TimeEntry[]> {
    const response = await api.get<TimeEntry[]>(`/time/task/${taskId}`);
    return response.data;
  },

  async getMyEntries(page = 0, size = 20): Promise<Page<TimeEntry>> {
    const response = await api.get<Page<TimeEntry>>('/time/my', {
      params: { page, size },
    });
    return response.data;
  },

  async getMyEntriesForDateRange(startDate: string, endDate: string): Promise<TimeEntry[]> {
    const response = await api.get<TimeEntry[]>('/time/my/range', {
      params: { startDate, endDate },
    });
    return response.data;
  },

  async getByProject(projectId: string, page = 0, size = 20): Promise<Page<TimeEntry>> {
    const response = await api.get<Page<TimeEntry>>(`/time/project/${projectId}`, {
      params: { page, size },
    });
    return response.data;
  },

  // Reports
  async getTotalMinutesByTask(taskId: string): Promise<number> {
    const response = await api.get<number>(`/time/task/${taskId}/total`);
    return response.data;
  },

  async getMyWeeklyReport(): Promise<TimeReport> {
    const response = await api.get<TimeReport>('/time/report/weekly');
    return response.data;
  },

  async getMyReport(startDate: string, endDate: string): Promise<TimeReport> {
    const response = await api.get<TimeReport>('/time/report', {
      params: { startDate, endDate },
    });
    return response.data;
  },
};
