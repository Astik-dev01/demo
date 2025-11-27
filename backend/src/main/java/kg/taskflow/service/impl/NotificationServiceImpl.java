package kg.taskflow.service.impl;

import kg.taskflow.db.entity.Notification;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.repository.NotificationRepository;
import kg.taskflow.db.repository.UserRepository;
import kg.taskflow.dto.notification.NotificationDto;
import kg.taskflow.exception.ForbiddenException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.NotificationMapper;
import kg.taskflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void notify(UUID userId, String type, String title, String message, Map<String, Object> data) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .data(data != null ? data : new HashMap<>())
                .build();

        notificationRepository.save(notification);
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
