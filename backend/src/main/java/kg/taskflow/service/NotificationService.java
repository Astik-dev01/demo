package kg.taskflow.service;

import kg.taskflow.dto.notification.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface NotificationService {

    // Create notifications
    void notify(UUID userId, String type, String title, String message, Map<String, Object> data);

    void notifyTaskAssigned(UUID userId, UUID taskId, String taskKey, String taskTitle);

    void notifyCommentAdded(UUID userId, UUID taskId, String taskKey, String commenterName);

    void notifyMentioned(UUID userId, UUID taskId, String taskKey, String mentionerName);

    void notifyTaskCompleted(UUID userId, UUID taskId, String taskKey, String taskTitle, String completedByName);

    void notifyDeadlineReminder(UUID userId, UUID taskId, String taskKey, String taskTitle, int daysUntilDue);

    void notifyProjectInvite(UUID userId, UUID projectId, String projectName, String inviterName);

    void notifyTeamInvite(UUID userId, UUID teamId, String teamName, String inviterName);

    // Query
    Page<NotificationDto> getMyNotifications(Pageable pageable);

    List<NotificationDto> getUnreadNotifications();

    long getUnreadCount();

    // Actions
    void markAsRead(UUID notificationId);

    void markAllAsRead();

    void deleteRead();
}
