package kg.taskflow.dto.analytics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDashboardDto {
    // Users stats
    private long totalUsers;
    private long activeUsers;
    private long newUsersThisMonth;

    // Projects stats
    private long totalProjects;
    private long activeProjects;
    private long archivedProjects;

    // Tasks stats
    private long totalTasks;
    private long completedTasks;
    private long overdueTasks;
    private long tasksCreatedThisMonth;

    // Teams stats
    private long totalTeams;

    // Time tracking stats
    private long totalTimeTrackedMinutes;
    private long timeTrackedThisMonthMinutes;

    // Activity
    private List<RecentActivityDto> recentActivity;

    // Top projects by tasks
    private List<ProjectStatsDto> topProjects;

    // Top users by completed tasks
    private List<UserStatsDto> topUsers;

    @Data
    @Builder
    public static class ProjectStatsDto {
        private String projectId;
        private String projectName;
        private String projectKey;
        private long taskCount;
        private long completedTaskCount;
        private long memberCount;
    }

    @Data
    @Builder
    public static class UserStatsDto {
        private String userId;
        private String userName;
        private String avatarUrl;
        private long completedTasks;
        private long totalTasks;
        private long timeTrackedMinutes;
    }
}
