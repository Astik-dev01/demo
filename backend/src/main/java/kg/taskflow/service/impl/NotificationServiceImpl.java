package kg.taskflow.service.impl;

import kg.taskflow.db.entity.Notification;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.entity.UserNotificationSettings;
import kg.taskflow.db.repository.NotificationRepository;
import kg.taskflow.db.repository.UserNotificationSettingsRepository;
import kg.taskflow.db.repository.UserRepository;
import kg.taskflow.dto.notification.NotificationDto;
import kg.taskflow.exception.ForbiddenException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.NotificationMapper;
import kg.taskflow.service.NotificationService;
import kg.taskflow.telegram.TelegramBotHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final UserNotificationSettingsRepository settingsRepository;

    @Autowired(required = false)
    private TelegramBotHandler telegramBotHandler;

    @Override
    @Transactional
    public void notify(UUID userId, String type, String title, String message, Map<String, Object> data) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        // Get user notification settings
        Optional<UserNotificationSettings> settingsOpt = settingsRepository.findByUserId(userId);
        UserNotificationSettings settings = settingsOpt.orElse(null);

        // Check if this notification type is enabled
        if (settings != null && !isNotificationTypeEnabled(settings, type)) {
            log.debug("Notification type {} is disabled for user {}", type, userId);
            return;
        }

        // Save in-app notification if enabled (default: enabled)
        if (settings == null || settings.getInAppEnabled()) {
            Notification notification = Notification.builder()
                    .user(user)
                    .type(type)
                    .title(title)
                    .message(message)
                    .data(data != null ? data : new HashMap<>())
                    .build();
            notificationRepository.save(notification);
        }

        // Send Telegram notification if enabled and connected
        if (telegramBotHandler != null &&
            settings != null && settings.getTelegramEnabled() &&
            user.isTelegramRegistered()) {
            try {
                telegramBotHandler.sendNotification(user.getTelegramChatId(), title, message);
                log.debug("Telegram notification sent to user {}", userId);
            } catch (Exception e) {
                log.error("Failed to send Telegram notification to user {}: {}", userId, e.getMessage());
            }
        }
    }

    private boolean isNotificationTypeEnabled(UserNotificationSettings settings, String type) {
        return switch (type) {
            case "TASK_ASSIGNED" -> settings.getNotifyTaskAssigned();
            case "COMMENT_ADDED" -> settings.getNotifyTaskCommented();
            case "TASK_COMPLETED" -> settings.getNotifyTaskCompleted();
            case "MENTIONED" -> settings.getNotifyMentioned();
            case "DEADLINE_REMINDER" -> settings.getNotifyDeadlineReminder();
            case "PROJECT_INVITE", "TEAM_INVITE" -> settings.getNotifyProjectInvite();
            default -> true; // Enable unknown types by default
        };
    }

    @Override
    @Transactional
    public void notifyTaskAssigned(UUID userId, UUID taskId, String taskKey, String taskTitle) {
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId.toString());
        data.put("taskKey", taskKey);

        notify(userId, "TASK_ASSIGNED", "Task Assigned",
               "You have been assigned to task " + taskKey + ": " + taskTitle, data);
    }

    @Override
    @Transactional
    public void notifyCommentAdded(UUID userId, UUID taskId, String taskKey, String commenterName) {
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId.toString());
        data.put("taskKey", taskKey);

        notify(userId, "COMMENT_ADDED", "New Comment",
               commenterName + " commented on " + taskKey, data);
    }

    @Override
    @Transactional
    public void notifyMentioned(UUID userId, UUID taskId, String taskKey, String mentionerName) {
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId.toString());
        data.put("taskKey", taskKey);

        notify(userId, "MENTIONED", "You were mentioned",
               mentionerName + " mentioned you in " + taskKey, data);
    }

    @Override
    @Transactional
    public void notifyTaskCompleted(UUID userId, UUID taskId, String taskKey, String taskTitle, String completedByName) {
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId.toString());
        data.put("taskKey", taskKey);

        notify(userId, "TASK_COMPLETED", "Task Completed",
               taskKey + ": " + taskTitle + " was completed by " + completedByName, data);
    }

    @Override
    @Transactional
    public void notifyDeadlineReminder(UUID userId, UUID taskId, String taskKey, String taskTitle, int daysUntilDue) {
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId.toString());
        data.put("taskKey", taskKey);
        data.put("daysUntilDue", daysUntilDue);

        String message = daysUntilDue == 0
                ? taskKey + ": " + taskTitle + " is due today!"
                : taskKey + ": " + taskTitle + " is due in " + daysUntilDue + " day(s)";

        notify(userId, "DEADLINE_REMINDER", "Deadline Reminder", message, data);
    }

    @Override
    @Transactional
    public void notifyProjectInvite(UUID userId, UUID projectId, String projectName, String inviterName) {
        Map<String, Object> data = new HashMap<>();
        data.put("projectId", projectId.toString());

        notify(userId, "PROJECT_INVITE", "Project Invitation",
               inviterName + " invited you to project \"" + projectName + "\"", data);
    }

    @Override
    @Transactional
    public void notifyTeamInvite(UUID userId, UUID teamId, String teamName, String inviterName) {
        Map<String, Object> data = new HashMap<>();
        data.put("teamId", teamId.toString());

        notify(userId, "TEAM_INVITE", "Team Invitation",
               inviterName + " invited you to team \"" + teamName + "\"", data);
    }

    @Override
    public Page<NotificationDto> getMyNotifications(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Notification> notifications = notificationRepository.findByUser(currentUser.getId(), pageable);
        return notifications.map(notificationMapper::toDto);
    }

    @Override
    public List<NotificationDto> getUnreadNotifications() {
        User currentUser = getCurrentUser();
        List<Notification> notifications = notificationRepository.findUnreadByUser(currentUser.getId());
        return notificationMapper.toDtoList(notifications);
    }

    @Override
    public long getUnreadCount() {
        User currentUser = getCurrentUser();
        return notificationRepository.countUnreadByUser(currentUser.getId());
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification", notificationId));

        User currentUser = getCurrentUser();
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Cannot access this notification");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User currentUser = getCurrentUser();
        notificationRepository.markAllAsRead(currentUser.getId());
    }

    @Override
    @Transactional
    public void deleteRead() {
        User currentUser = getCurrentUser();
        notificationRepository.deleteReadByUser(currentUser.getId());
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
