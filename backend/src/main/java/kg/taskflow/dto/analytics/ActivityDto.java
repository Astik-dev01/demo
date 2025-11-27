package kg.taskflow.dto.analytics;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ActivityDto {
    private UUID id;
    private String type; // TASK_CREATED, TASK_COMPLETED, COMMENT_ADDED, etc.
    private String description;
    private String userName;
    private String userAvatar;
    private UUID targetId;
    private String targetType;
    private String targetName;
    private LocalDateTime createdAt;
}
