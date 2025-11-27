package kg.taskflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDto {
    private long totalProjects;
    private long activeProjects;
    private long totalTasks;
    private long completedTasks;
    private long overdueTasks;
    private long tasksInProgress;
    private Integer totalTimeSpentMinutes;
    private BigDecimal completionRate;
    private List<TasksByStatusDto> tasksByStatus;
    private List<TasksByPriorityDto> tasksByPriority;
    private List<RecentActivityDto> recentActivity;
    private List<UpcomingDeadlineDto> upcomingDeadlines;
}
