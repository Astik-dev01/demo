export interface NotificationSettings {
  telegramEnabled: boolean;
  emailEnabled: boolean;
  inAppEnabled: boolean;
  notifyTaskAssigned: boolean;
  notifyTaskCommented: boolean;
  notifyTaskCompleted: boolean;
  notifyMentioned: boolean;
  notifyDeadlineReminder: boolean;
  notifyProjectInvite: boolean;
}

export interface UpdateNotificationSettingsRequest {
  telegramEnabled?: boolean;
  emailEnabled?: boolean;
  inAppEnabled?: boolean;
  notifyTaskAssigned?: boolean;
  notifyTaskCommented?: boolean;
  notifyTaskCompleted?: boolean;
  notifyMentioned?: boolean;
  notifyDeadlineReminder?: boolean;
  notifyProjectInvite?: boolean;
}

export interface TelegramStatus {
  connected: boolean;
  telegramUsername: string | null;
  employeeCode: string;
}

export interface TelegramLink {
  employeeCode: string;
  botUsername: string;
  deepLink: string;
  qrCodeBase64: string | null;
}
