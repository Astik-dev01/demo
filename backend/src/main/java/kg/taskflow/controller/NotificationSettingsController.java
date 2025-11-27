package kg.taskflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.taskflow.dto.notification.NotificationSettingsDto;
import kg.taskflow.dto.notification.TelegramLinkDto;
import kg.taskflow.dto.notification.TelegramStatusDto;
import kg.taskflow.dto.notification.UpdateNotificationSettingsRequest;
import kg.taskflow.service.NotificationSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification-settings")
@RequiredArgsConstructor
@Tag(name = "Notification Settings", description = "Notification settings management")
public class NotificationSettingsController {

    private final NotificationSettingsService notificationSettingsService;

    @GetMapping
    @Operation(summary = "Get notification settings", description = "Get current user notification settings")
    public ResponseEntity<NotificationSettingsDto> getSettings() {
        return ResponseEntity.ok(notificationSettingsService.getMySettings());
    }

    @PutMapping
    @Operation(summary = "Update notification settings", description = "Update current user notification settings")
    public ResponseEntity<NotificationSettingsDto> updateSettings(@RequestBody UpdateNotificationSettingsRequest request) {
        return ResponseEntity.ok(notificationSettingsService.updateSettings(request));
    }

    @GetMapping("/telegram/status")
    @Operation(summary = "Get Telegram status", description = "Get Telegram connection status for current user")
    public ResponseEntity<TelegramStatusDto> getTelegramStatus() {
        return ResponseEntity.ok(notificationSettingsService.getTelegramStatus());
    }

    @GetMapping("/telegram/link")
    @Operation(summary = "Generate Telegram link", description = "Generate Telegram deep link and QR code for registration")
    public ResponseEntity<TelegramLinkDto> generateTelegramLink() {
        return ResponseEntity.ok(notificationSettingsService.generateTelegramLink());
    }

    @DeleteMapping("/telegram")
    @Operation(summary = "Unlink Telegram", description = "Unlink Telegram from current user account")
    public ResponseEntity<Void> unlinkTelegram() {
        notificationSettingsService.unlinkTelegram();
        return ResponseEntity.ok().build();
    }
}
