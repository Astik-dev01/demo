package kg.taskflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAnalyticsDto {
    private UUID projectId;
    private String projectName;
    private String projectKey;

    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long todoTasks;
    private long overdueTasks;

    private BigDecimal completionRate;
    private BigDecimal estimatedHours;
    private BigDecimal spentHours;
    private BigDecimal remainingHours;

    private Integer totalTimeSpentMinutes;
    private Integer averageCompletionTimeMinutes;

    private List<TasksByStatusDto> tasksByStatus;
    private List<TasksByPriorityDto> tasksByPriority;
    private List<TasksByAssigneeDto> tasksByAssignee;
    private List<TimeByDayDto> timeByDay;
}
