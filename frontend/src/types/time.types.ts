import { User } from './auth.types';

export interface TimeEntry {
  id: string;
  taskId: string;
  taskKey: string;
  taskTitle: string;
  projectId: string;
  projectName: string;
  user: User;
  description: string | null;
  startedAt: string;
  endedAt: string | null;
  durationMinutes: number | null;
  isBillable: boolean;
  isRunning: boolean;
  createdAt: string;
}

export interface CreateTimeEntryRequest {
  taskId: string;
  description?: string;
  startedAt?: string;
  endedAt?: string;
  durationMinutes?: number;
  isBillable?: boolean;
}

export interface UpdateTimeEntryRequest {
  description?: string;
  startedAt?: string;
  endedAt?: string;
  durationMinutes?: number;
  isBillable?: boolean;
}

export interface TimeReport {
  userId: string;
  userName: string;
  startDate: string;
  endDate: string;
  totalMinutes: number;
  billableMinutes: number;
  dailyBreakdown?: DailyTime[];
  projectBreakdown?: ProjectTime[];
}

export interface DailyTime {
  date: string;
  totalMinutes: number;
  billableMinutes: number;
}

export interface ProjectTime {
  projectId: string;
  projectName: string;
  totalMinutes: number;
  billableMinutes: number;
  taskCount: number;
}
