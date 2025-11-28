package kg.taskflow.service.impl;

import kg.taskflow.db.entity.Project;
import kg.taskflow.db.entity.Task;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.repository.*;
import kg.taskflow.db.repository.hb.HBTaskPriorityRepository;
import kg.taskflow.db.repository.hb.HBTaskStatusRepository;
import kg.taskflow.dto.analytics.*;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final HBTaskStatusRepository statusRepository;
    private final HBTaskPriorityRepository priorityRepository;

    @Override
    public DashboardOverviewDto getDashboardOverview() {
        User currentUser = getCurrentUser();
        UUID userId = currentUser.getId();

        List<Project> userProjects = projectRepository.findUserProjects(userId);
        long totalProjects = userProjects.size();
        long activeProjects = userProjects.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsArchived()))
                .count();

        long totalTasks = taskRepository.countByAssignee(userId);
        long completedTasks = taskRepository.countCompletedByAssignee(userId);
        long overdueTasks = taskRepository.countOverdueByAssignee(userId);
        long tasksInProgress = totalTasks - completedTasks;

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        Integer totalTimeSpent = timeEntryRepository.getTotalMinutesByUserAndDateRange(
                userId, startOfMonth, endOfMonth);

        BigDecimal completionRate = totalTasks > 0
                ? BigDecimal.valueOf(completedTasks * 100.0 / totalTasks).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<TasksByStatusDto> tasksByStatus = getTasksByStatusForUser(userId);
        List<TasksByPriorityDto> tasksByPriority = getTasksByPriorityForUser(userId);
        List<RecentActivityDto> recentActivity = getRecentActivity(userId);
        List<UpcomingDeadlineDto> upcomingDeadlines = getUpcomingDeadlines(userId);

        return DashboardOverviewDto.builder()
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .overdueTasks(overdueTasks)
                .tasksInProgress(tasksInProgress)
                .totalTimeSpentMinutes(totalTimeSpent != null ? totalTimeSpent : 0)
                .completionRate(completionRate)
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .recentActivity(recentActivity)
                .upcomingDeadlines(upcomingDeadlines)
                .build();
    }

    @Override
    public AdminDashboardDto getAdminDashboard() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        // Users stats
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long newUsersThisMonth = userRepository.countNewUsersAfter(startOfMonth);

        // Projects stats
        long totalProjects = projectRepository.countAll();
        long activeProjects = projectRepository.countActive();
        long archivedProjects = projectRepository.countArchived();

        // Tasks stats
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countCompleted();
        long overdueTasks = taskRepository.countAllOverdue();
        long tasksCreatedThisMonth = taskRepository.countCreatedAfter(startOfMonth);

        // Teams stats
        long totalTeams = teamRepository.countActive();

        // Time tracking stats
        long totalTimeTracked = timeEntryRepository.getTotalMinutesAll();
        long timeTrackedThisMonth = timeEntryRepository.getTotalMinutesInRange(startOfMonth, endOfMonth);

        // Top projects
        List<AdminDashboardDto.ProjectStatsDto> topProjects = projectRepository.findTopProjectsByTasks(PageRequest.of(0, 5))
                .stream()
                .map(row -> AdminDashboardDto.ProjectStatsDto.builder()
                        .projectId(((UUID) row[0]).toString())
                        .projectName((String) row[1])
                        .projectKey((String) row[2])
                        .taskCount(((Number) row[3]).longValue())
                        .completedTaskCount(((Number) row[4]).longValue())
                        .memberCount(((Number) row[5]).longValue())
                        .build())
                .collect(Collectors.toList());

        // Top users
        List<AdminDashboardDto.UserStatsDto> topUsers = userRepository.findTopUsersByCompletedTasks(PageRequest.of(0, 5))
                .stream()
                .map(row -> AdminDashboardDto.UserStatsDto.builder()
                        .userId(((UUID) row[0]).toString())
                        .userName((String) row[1])
                        .avatarUrl((String) row[2])
                        .completedTasks(((Number) row[3]).longValue())
                        .totalTasks(((Number) row[4]).longValue())
                        .timeTrackedMinutes(row[5] != null ? ((Number) row[5]).longValue() : 0)
                        .build())
                .collect(Collectors.toList());

        // Recent system activity (latest tasks from all users)
        List<RecentActivityDto> recentActivity = taskRepository.findRecentTasks(PageRequest.of(0, 10))
                .stream()
                .map(t -> RecentActivityDto.builder()
                        .taskId(t.getId())
                        .taskKey(t.getKey())
                        .taskTitle(t.getTitle())
                        .activityType("updated")
                        .description("Task updated")
                        .userName(t.getReporter() != null ? t.getReporter().getFullName() : "Unknown")
                        .timestamp(t.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return AdminDashboardDto.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .archivedProjects(archivedProjects)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .overdueTasks(overdueTasks)
                .tasksCreatedThisMonth(tasksCreatedThisMonth)
                .totalTeams(totalTeams)
                .totalTimeTrackedMinutes(totalTimeTracked)
                .timeTrackedThisMonthMinutes(timeTrackedThisMonth)
                .recentActivity(recentActivity)
                .topProjects(topProjects)
                .topUsers(topUsers)
                .build();
    }

    @Override
    public ProjectAnalyticsDto getProjectAnalytics(UUID projectId) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Project", projectId));

        List<Task> tasks = taskRepository.findByProject(projectId);

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getCompletedAt() != null).count();
        long overdueTasks = taskRepository.countOverdueByProject(projectId);

        long inProgressTasks = tasks.stream()
                .filter(t -> t.getCompletedAt() == null)
                .filter(t -> t.getStatus() != null && !t.getStatus().isFinal())
                .count();

        long todoTasks = totalTasks - completedTasks - inProgressTasks;

        BigDecimal completionRate = totalTasks > 0
                ? BigDecimal.valueOf(completedTasks * 100.0 / totalTasks).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal estimatedHours = tasks.stream()
                .map(Task::getEstimatedHours)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal spentHours = tasks.stream()
                .map(Task::getSpentHours)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingHours = estimatedHours.subtract(spentHours).max(BigDecimal.ZERO);

        List<TasksByStatusDto> tasksByStatus = getTasksByStatusForProject(projectId);
        List<TasksByPriorityDto> tasksByPriority = getTasksByPriorityForProject(projectId);
        List<TasksByAssigneeDto> tasksByAssignee = getTasksByAssignee(projectId);
        List<TimeByDayDto> timeByDay = getTimeByDay(projectId);

        return ProjectAnalyticsDto.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .projectKey(project.getProjectKey())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .todoTasks(todoTasks)
                .overdueTasks(overdueTasks)
                .completionRate(completionRate)
                .estimatedHours(estimatedHours)
                .spentHours(spentHours)
                .remainingHours(remainingHours)
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .tasksByAssignee(tasksByAssignee)
                .timeByDay(timeByDay)
                .build();
    }

    @Override
    public BurndownChartDto getBurndownChart(UUID projectId, LocalDate startDate, LocalDate endDate) {
        projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Project", projectId));

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(14);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        long totalTasks = taskRepository.countByProject(projectId);
        long completedTasks = taskRepository.countCompletedByProject(projectId);

        List<BurndownDataPointDto> dataPoints = new ArrayList<>();
        List<BurndownDataPointDto> idealLine = new ArrayList<>();

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        double idealDecrement = (double) totalTasks / (daysBetween > 0 ? daysBetween : 1);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime dateTime = date.atStartOfDay().plusDays(1);
            long remaining = taskRepository.countRemainingAtDate(projectId, dateTime);
            long completed = totalTasks - remaining;

            dataPoints.add(BurndownDataPointDto.builder()
                    .date(date)
                    .remainingTasks(remaining)
                    .completedTasks(completed)
                    .build());

            long dayIndex = ChronoUnit.DAYS.between(startDate, date);
            long idealRemaining = Math.max(0, Math.round(totalTasks - (dayIndex * idealDecrement)));

            idealLine.add(BurndownDataPointDto.builder()
                    .date(date)
                    .remainingTasks(idealRemaining)
                    .completedTasks(totalTasks - idealRemaining)
                    .build());
        }

        return BurndownChartDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .dataPoints(dataPoints)
                .idealLine(idealLine)
                .build();
    }

    @Override
    public VelocityChartDto getVelocityChart(UUID projectId, int weeks) {
        projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new NotFoundException("Project", projectId));

        if (weeks <= 0) {
            weeks = 8;
        }

        List<VelocityDataPointDto> dataPoints = new ArrayList<>();
        long totalTasksCompleted = 0;
        BigDecimal totalHoursSpent = BigDecimal.ZERO;

        LocalDate today = LocalDate.now();

        for (int i = weeks - 1; i >= 0; i--) {
            LocalDate weekStart = today.minusWeeks(i + 1).with(java.time.DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(7);

            LocalDateTime startDateTime = weekStart.atStartOfDay();
            LocalDateTime endDateTime = weekEnd.atStartOfDay();

            long tasksCompleted = taskRepository.countCompletedInDateRange(projectId, startDateTime, endDateTime);
            totalTasksCompleted += tasksCompleted;

            List<Task> completedTasks = taskRepository.findCompletedInDateRange(projectId, startDateTime, endDateTime);
            BigDecimal hoursSpent = completedTasks.stream()
                    .map(Task::getSpentHours)
                    .filter(h -> h != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalHoursSpent = totalHoursSpent.add(hoursSpent);

            String period = "Week " + (weeks - i);

            dataPoints.add(VelocityDataPointDto.builder()
                    .period(period)
                    .startDate(weekStart)
                    .endDate(weekEnd.minusDays(1))
                    .tasksCompleted(tasksCompleted)
                    .hoursSpent(hoursSpent)
                    .build());
        }

        BigDecimal averageVelocity = weeks > 0
                ? BigDecimal.valueOf((double) totalTasksCompleted / weeks).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return VelocityChartDto.builder()
                .averageVelocity(averageVelocity)
                .totalTasksCompleted(totalTasksCompleted)
                .totalHoursSpent(totalHoursSpent)
                .dataPoints(dataPoints)
                .build();
    }

    // Helper methods

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private List<TasksByStatusDto> getTasksByStatusForUser(UUID userId) {
        List<Task> tasks = taskRepository.findByAssigneeWithDetails(userId);
        return tasks.stream()
                .filter(t -> t.getStatus() != null)
                .collect(Collectors.groupingBy(t -> t.getStatus().getNameRu(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    String color = tasks.stream()
                            .filter(t -> t.getStatus() != null && t.getStatus().getNameRu().equals(e.getKey()))
                            .findFirst()
                            .map(t -> t.getStatus().getColor())
                            .orElse("#6B7280");
                    return TasksByStatusDto.builder()
                            .status(e.getKey())
                            .color(color)
                            .count(e.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<TasksByStatusDto> getTasksByStatusForProject(UUID projectId) {
        List<Task> tasks = taskRepository.findByProjectWithDetails(projectId);
        return tasks.stream()
                .filter(t -> t.getStatus() != null)
                .collect(Collectors.groupingBy(t -> t.getStatus().getNameRu(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    String color = tasks.stream()
                            .filter(t -> t.getStatus() != null && t.getStatus().getNameRu().equals(e.getKey()))
                            .findFirst()
                            .map(t -> t.getStatus().getColor())
                            .orElse("#6B7280");
                    return TasksByStatusDto.builder()
                            .status(e.getKey())
                            .color(color)
                            .count(e.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<TasksByPriorityDto> getTasksByPriorityForUser(UUID userId) {
        List<Task> tasks = taskRepository.findByAssigneeWithDetails(userId);
        return tasks.stream()
                .filter(t -> t.getPriority() != null)
                .collect(Collectors.groupingBy(t -> t.getPriority().getNameRu(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    String color = tasks.stream()
                            .filter(t -> t.getPriority() != null && t.getPriority().getNameRu().equals(e.getKey()))
                            .findFirst()
                            .map(t -> t.getPriority().getColor())
                            .orElse("#6B7280");
                    return TasksByPriorityDto.builder()
                            .priority(e.getKey())
                            .color(color)
                            .count(e.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<TasksByPriorityDto> getTasksByPriorityForProject(UUID projectId) {
        List<Task> tasks = taskRepository.findByProjectWithDetails(projectId);
        return tasks.stream()
                .filter(t -> t.getPriority() != null)
                .collect(Collectors.groupingBy(t -> t.getPriority().getNameRu(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    String color = tasks.stream()
                            .filter(t -> t.getPriority() != null && t.getPriority().getNameRu().equals(e.getKey()))
                            .findFirst()
                            .map(t -> t.getPriority().getColor())
                            .orElse("#6B7280");
                    return TasksByPriorityDto.builder()
                            .priority(e.getKey())
                            .color(color)
                            .count(e.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<TasksByAssigneeDto> getTasksByAssignee(UUID projectId) {
        List<Task> tasks = taskRepository.findByProjectWithDetails(projectId);
        return tasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignee().getId()))
                .entrySet().stream()
                .map(e -> {
                    List<Task> userTasks = e.getValue();
                    User assignee = userTasks.get(0).getAssignee();
                    long completed = userTasks.stream().filter(t -> t.getCompletedAt() != null).count();
                    long inProgress = userTasks.stream()
                            .filter(t -> t.getCompletedAt() == null)
                            .filter(t -> t.getStatus() != null && !t.getStatus().isFinal())
                            .count();

                    return TasksByAssigneeDto.builder()
                            .userId(assignee.getId())
                            .userName(assignee.getFirstName() + " " + assignee.getLastName())
                            .avatarUrl(assignee.getAvatarUrl())
                            .totalTasks(userTasks.size())
                            .completedTasks(completed)
                            .inProgressTasks(inProgress)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<RecentActivityDto> getRecentActivity(UUID userId) {
        List<Task> recentTasks = taskRepository.findRecentTasksWithDetails(userId, PageRequest.of(0, 10));
        return recentTasks.stream()
                .map(t -> RecentActivityDto.builder()
                        .taskId(t.getId())
                        .taskKey(t.getKey())
                        .taskTitle(t.getTitle())
                        .activityType("updated")
                        .description("Task updated")
                        .userName(t.getReporter().getFirstName() + " " + t.getReporter().getLastName())
                        .timestamp(t.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private List<UpcomingDeadlineDto> getUpcomingDeadlines(UUID userId) {
        List<Task> tasks = taskRepository.findUpcomingDeadlinesWithDetails(userId, PageRequest.of(0, 10));
        LocalDate today = LocalDate.now();

        return tasks.stream()
                .map(t -> {
                    long daysRemaining = ChronoUnit.DAYS.between(today, t.getDueDate());
                    return UpcomingDeadlineDto.builder()
                            .taskId(t.getId())
                            .taskKey(t.getKey())
                            .taskTitle(t.getTitle())
                            .projectName(t.getProject().getName())
                            .priority(t.getPriority() != null ? t.getPriority().getNameRu() : null)
                            .priorityColor(t.getPriority() != null ? t.getPriority().getColor() : null)
                            .dueDate(t.getDueDate())
                            .daysRemaining(daysRemaining)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<TimeByDayDto> getTimeByDay(UUID projectId) {
        LocalDate today = LocalDate.now();
        List<TimeByDayDto> result = new ArrayList<>();

        for (int i = 13; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            long tasksCompleted = taskRepository.countCompletedInDateRange(projectId, startOfDay, endOfDay);

            result.add(TimeByDayDto.builder()
                    .date(date)
                    .minutesSpent(0)
                    .tasksCompleted(tasksCompleted)
                    .build());
        }

        return result;
    }
}
