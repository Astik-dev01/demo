import api from './api';
import {
  NotificationSettings,
  UpdateNotificationSettingsRequest,
  TelegramStatus,
  TelegramLink,
} from '@/types/notification-settings.types';

export const notificationSettingsService = {
  async getSettings(): Promise<NotificationSettings> {
    const response = await api.get<NotificationSettings>('/notification-settings');
    return response.data;
  },

  async updateSettings(request: UpdateNotificationSettingsRequest): Promise<NotificationSettings> {
    const response = await api.put<NotificationSettings>('/notification-settings', request);
    return response.data;
  },

  async getTelegramStatus(): Promise<TelegramStatus> {
    const response = await api.get<TelegramStatus>('/notification-settings/telegram/status');
    return response.data;
  },

  async generateTelegramLink(): Promise<TelegramLink> {
    const response = await api.get<TelegramLink>('/notification-settings/telegram/link');
    return response.data;
  },

  async unlinkTelegram(): Promise<void> {
    await api.delete('/notification-settings/telegram');
  },
};
