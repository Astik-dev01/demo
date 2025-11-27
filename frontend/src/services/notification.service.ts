import api from './api';
import { Notification } from '@/types/notification.types';
import { Page } from '@/types/project.types';

export const notificationService = {
  async getMyNotifications(page = 0, size = 20): Promise<Page<Notification>> {
    const response = await api.get<Page<Notification>>('/notifications', {
      params: { page, size },
    });
    return response.data;
  },

  async getUnreadNotifications(): Promise<Notification[]> {
    const response = await api.get<Notification[]>('/notifications/unread');
    return response.data;
  },

  async getUnreadCount(): Promise<number> {
    const response = await api.get<number>('/notifications/unread/count');
    return response.data;
  },

  async markAsRead(id: string): Promise<void> {
    await api.post(`/notifications/${id}/read`);
  },

  async markAllAsRead(): Promise<void> {
    await api.post('/notifications/read-all');
  },

  async deleteRead(): Promise<void> {
    await api.delete('/notifications/read');
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/notifications/${id}`);
  },
};
