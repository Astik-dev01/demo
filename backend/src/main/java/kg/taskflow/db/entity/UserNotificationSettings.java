package kg.taskflow.db.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_notification_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationSettings extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Notification channels
    @Column(name = "telegram_enabled")
    @Builder.Default
    private Boolean telegramEnabled = false;

    @Column(name = "email_enabled")
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "in_app_enabled")
    @Builder.Default
    private Boolean inAppEnabled = true;

    // Notification types
    @Column(name = "notify_task_assigned")
    @Builder.Default
    private Boolean notifyTaskAssigned = true;

    @Column(name = "notify_task_commented")
    @Builder.Default
    private Boolean notifyTaskCommented = true;

    @Column(name = "notify_task_completed")
    @Builder.Default
    private Boolean notifyTaskCompleted = true;

    @Column(name = "notify_mentioned")
    @Builder.Default
    private Boolean notifyMentioned = true;

    @Column(name = "notify_deadline_reminder")
    @Builder.Default
    private Boolean notifyDeadlineReminder = true;

    @Column(name = "notify_project_invite")
    @Builder.Default
    private Boolean notifyProjectInvite = true;
}
