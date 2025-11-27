package kg.taskflow.dto.analytics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardDto {
    private Long totalProjects;
    private Long totalTasks;
    private Long completedTasks;
    private Long overdueTasks;
    private Long totalTimeMinutes;
    private Double completionRate;
    private List<ProjectSummaryDto> recentProjects;
    private List<TaskSummaryDto> upcomingTasks;
    private List<ActivityDto> recentActivity;
}
