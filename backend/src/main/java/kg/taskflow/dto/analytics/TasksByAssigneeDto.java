package kg.taskflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TasksByAssigneeDto {
    private UUID userId;
    private String userName;
    private String avatarUrl;
    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
}
