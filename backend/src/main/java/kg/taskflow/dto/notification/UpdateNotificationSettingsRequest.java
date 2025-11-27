package kg.taskflow.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationSettingsRequest {
    // Channels
    private Boolean telegramEnabled;
    private Boolean emailEnabled;
    private Boolean inAppEnabled;

    // Notification types
    private Boolean notifyTaskAssigned;
    private Boolean notifyTaskCommented;
    private Boolean notifyTaskCompleted;
    private Boolean notifyMentioned;
    private Boolean notifyDeadlineReminder;
    private Boolean notifyProjectInvite;
}
