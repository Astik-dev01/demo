import api from './api';
import { User } from '@/types/auth.types';

export const userService = {
  async getCurrentUser(): Promise<User> {
    const response = await api.get<User>('/users/me');
    return response.data;
  },

  async updateProfile(data: Partial<User>): Promise<User> {
    const response = await api.put<User>('/users/me', data);
    return response.data;
  },

  async changePassword(currentPassword: string, newPassword: string): Promise<void> {
    await api.put('/users/me/password', { currentPassword, newPassword });
  },

  async searchUsers(query?: string): Promise<User[]> {
    const params = query ? { query } : {};
    const response = await api.get<User[]>('/users', { params });
    return response.data;
  },

  async getAllUsers(): Promise<User[]> {
    const response = await api.get<User[]>('/users');
    return response.data;
  },
};
