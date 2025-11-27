package kg.taskflow.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.entity.UserNotificationSettings;
import kg.taskflow.db.repository.UserNotificationSettingsRepository;
import kg.taskflow.db.repository.UserRepository;
import kg.taskflow.dto.notification.NotificationSettingsDto;
import kg.taskflow.dto.notification.TelegramLinkDto;
import kg.taskflow.dto.notification.TelegramStatusDto;
import kg.taskflow.dto.notification.UpdateNotificationSettingsRequest;
import kg.taskflow.service.NotificationSettingsService;
import kg.taskflow.telegram.TelegramBotConfig;
import kg.taskflow.telegram.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSettingsServiceImpl implements NotificationSettingsService {

    private final UserNotificationSettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final TelegramBotConfig telegramBotConfig;
    private final TelegramBotService telegramBotService;

    @Override
    public NotificationSettingsDto getMySettings() {
        User user = getCurrentUser();
        UserNotificationSettings settings = getOrCreateSettings(user);
        return mapToDto(settings);
    }

    @Override
    @Transactional
    public NotificationSettingsDto updateSettings(UpdateNotificationSettingsRequest request) {
        User user = getCurrentUser();
        UserNotificationSettings settings = getOrCreateSettings(user);

        // Update channels
        if (request.getTelegramEnabled() != null) {
            // Only allow enabling telegram if user is registered
            if (request.getTelegramEnabled() && !user.isTelegramRegistered()) {
                throw new IllegalStateException("Cannot enable Telegram notifications: Telegram not connected");
            }
            settings.setTelegramEnabled(request.getTelegramEnabled());
        }
        if (request.getEmailEnabled() != null) {
            settings.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getInAppEnabled() != null) {
            settings.setInAppEnabled(request.getInAppEnabled());
        }

        // Update notification types
        if (request.getNotifyTaskAssigned() != null) {
            settings.setNotifyTaskAssigned(request.getNotifyTaskAssigned());
        }
        if (request.getNotifyTaskCommented() != null) {
            settings.setNotifyTaskCommented(request.getNotifyTaskCommented());
        }
        if (request.getNotifyTaskCompleted() != null) {
            settings.setNotifyTaskCompleted(request.getNotifyTaskCompleted());
        }
        if (request.getNotifyMentioned() != null) {
            settings.setNotifyMentioned(request.getNotifyMentioned());
        }
        if (request.getNotifyDeadlineReminder() != null) {
            settings.setNotifyDeadlineReminder(request.getNotifyDeadlineReminder());
        }
        if (request.getNotifyProjectInvite() != null) {
            settings.setNotifyProjectInvite(request.getNotifyProjectInvite());
        }

        settingsRepository.save(settings);
        return mapToDto(settings);
    }

    @Override
    public TelegramStatusDto getTelegramStatus() {
        User user = getCurrentUser();

        // Ensure user has employee code
        if (user.getEmployeeCode() == null) {
            user.generateEmployeeCode();
            userRepository.save(user);
        }

        return TelegramStatusDto.builder()
                .connected(user.isTelegramRegistered())
                .telegramUsername(user.getTelegramUsername())
                .employeeCode(user.getEmployeeCode())
                .build();
    }

    @Override
    @Transactional
    public TelegramLinkDto generateTelegramLink() {
        User user = getCurrentUser();

        // Generate employee code if not exists
        if (user.getEmployeeCode() == null) {
            user.generateEmployeeCode();
            userRepository.save(user);
        }

        String deepLink = telegramBotService.generateDeepLink(user.getEmployeeCode());
        String qrCodeBase64 = generateQRCode(deepLink);

        return TelegramLinkDto.builder()
                .employeeCode(user.getEmployeeCode())
                .botUsername(telegramBotConfig.getUsername())
                .deepLink(deepLink)
                .qrCodeBase64(qrCodeBase64)
                .build();
    }

    @Override
    @Transactional
    public void unlinkTelegram() {
        User user = getCurrentUser();
        telegramBotService.unregisterUser(user.getId());

        // Disable telegram notifications
        settingsRepository.findByUserId(user.getId()).ifPresent(settings -> {
            settings.setTelegramEnabled(false);
            settingsRepository.save(settings);
        });
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserNotificationSettings getOrCreateSettings(User user) {
        return settingsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserNotificationSettings newSettings = UserNotificationSettings.builder()
                            .user(user)
                            .build();
                    return settingsRepository.save(newSettings);
                });
    }

    private NotificationSettingsDto mapToDto(UserNotificationSettings settings) {
        return NotificationSettingsDto.builder()
                .telegramEnabled(settings.getTelegramEnabled())
                .emailEnabled(settings.getEmailEnabled())
                .inAppEnabled(settings.getInAppEnabled())
                .notifyTaskAssigned(settings.getNotifyTaskAssigned())
                .notifyTaskCommented(settings.getNotifyTaskCommented())
                .notifyTaskCompleted(settings.getNotifyTaskCompleted())
                .notifyMentioned(settings.getNotifyMentioned())
                .notifyDeadlineReminder(settings.getNotifyDeadlineReminder())
                .notifyProjectInvite(settings.getNotifyProjectInvite())
                .build();
    }

    private String generateQRCode(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            log.error("Error generating QR code: {}", e.getMessage());
            return null;
        }
    }
}
