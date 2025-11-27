package kg.taskflow.dto.analytics;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserAnalyticsDto {
    private UUID userId;
    private String userName;

    // Task stats
    private Long assignedTasks;
    private Long completedTasks;
    private Long overdueTasks;
    private Double completionRate;

    // Time stats
    private Long totalTimeMinutes;
    private Long thisWeekMinutes;
    private Long thisMonthMinutes;

    // Project involvement
    private Long projectCount;

    // Charts data
    private List<DailyTimeDto> dailyTime;
    private List<ProjectTimeDto> projectTime;
}

@Data
@Builder
class DailyTimeDto {
    private LocalDate date;
    private Long minutes;
}

@Data
@Builder
class ProjectTimeDto {
    private UUID projectId;
    private String projectName;
    private Long minutes;
    private Long taskCount;
}
