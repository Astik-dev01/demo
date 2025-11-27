package kg.taskflow.service;

import kg.taskflow.dto.notification.NotificationSettingsDto;
import kg.taskflow.dto.notification.TelegramLinkDto;
import kg.taskflow.dto.notification.TelegramStatusDto;
import kg.taskflow.dto.notification.UpdateNotificationSettingsRequest;

public interface NotificationSettingsService {
    NotificationSettingsDto getMySettings();
    NotificationSettingsDto updateSettings(UpdateNotificationSettingsRequest request);
    TelegramStatusDto getTelegramStatus();
    TelegramLinkDto generateTelegramLink();
    void unlinkTelegram();
}
