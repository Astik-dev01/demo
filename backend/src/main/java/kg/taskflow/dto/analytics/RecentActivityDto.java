package kg.taskflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDto {
    private UUID taskId;
    private String taskKey;
    private String taskTitle;
    private String activityType;
    private String description;
    private String userName;
    private LocalDateTime timestamp;
}
