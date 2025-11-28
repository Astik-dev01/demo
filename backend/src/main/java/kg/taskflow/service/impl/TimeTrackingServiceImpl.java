package kg.taskflow.service.impl;

import kg.taskflow.db.entity.Task;
import kg.taskflow.db.entity.TimeEntry;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.repository.TaskRepository;
import kg.taskflow.db.repository.TimeEntryRepository;
import kg.taskflow.dto.time.*;
import kg.taskflow.exception.BadRequestException;
import kg.taskflow.exception.ForbiddenException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.TimeEntryMapper;
import kg.taskflow.service.ProjectService;
import kg.taskflow.service.TimeTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeTrackingServiceImpl implements TimeTrackingService {

    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryMapper timeEntryMapper;
    private final ProjectService projectService;

    @Override
    @Transactional
    public TimeEntryDto startTimer(UUID taskId, String description) {
        User currentUser = getCurrentUser();

        // Check if there's already a running timer
        timeEntryRepository.findRunningByUser(currentUser.getId())
                .ifPresent(entry -> {
                    throw new BadRequestException("You already have a running timer. Stop it first.");
                });

        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new NotFoundException("Task", taskId));

        checkProjectAccess(task.getProject().getId());

        TimeEntry entry = TimeEntry.builder()
                .task(task)
                .user(currentUser)
                .description(description)
                .isBillable(true)
                .build();
        entry.start();

        entry = timeEntryRepository.save(entry);

        // Update task spent hours
        updateTaskSpentHours(task);

        return timeEntryMapper.toDto(entry);
    }

    @Override
    @Transactional
    public TimeEntryDto stopTimer() {
        User currentUser = getCurrentUser();

        TimeEntry entry = timeEntryRepository.findRunningByUser(currentUser.getId())
                .orElseThrow(() -> new BadRequestException("No running timer found"));

        entry.stop();
        entry = timeEntryRepository.save(entry);

        // Update task spent hours
        updateTaskSpentHours(entry.getTask());

        return timeEntryMapper.toDto(entry);
    }

    @Override
    public TimeEntryDto getRunningTimer() {
        User currentUser = getCurrentUser();
        return timeEntryRepository.findRunningByUser(currentUser.getId())
                .map(timeEntryMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public TimeEntryDto create(CreateTimeEntryRequest request) {
        User currentUser = getCurrentUser();

        Task task = taskRepository.findByIdAndIsDeletedFalse(request.getTaskId())
                .orElseThrow(() -> new NotFoundException("Task", request.getTaskId()));

        checkProjectAccess(task.getProject().getId());

        TimeEntry entry = TimeEntry.builder()
                .task(task)
                .user(currentUser)
                .description(request.getDescription())
                .startedAt(request.getStartedAt() != null ? request.getStartedAt() : LocalDateTime.now())
                .endedAt(request.getEndedAt())
                .durationMinutes(request.getDurationMinutes())
                .isBillable(request.getIsBillable() != null ? request.getIsBillable() : true)
                .isRunning(false)
                .build();

        // Calculate duration if not provided
        if (entry.getDurationMinutes() == null && entry.getEndedAt() != null) {
            entry.setDurationMinutes(entry.calculateDuration());
        }

        entry = timeEntryRepository.save(entry);

        // Update task spent hours
        updateTaskSpentHours(task);

        return timeEntryMapper.toDto(entry);
    }

    @Override
    public TimeEntryDto getById(UUID id) {
        TimeEntry entry = findEntryById(id);
        checkProjectAccess(entry.getTask().getProject().getId());
        return timeEntryMapper.toDto(entry);
    }

    @Override
    @Transactional
    public TimeEntryDto update(UUID id, UpdateTimeEntryRequest request) {
        TimeEntry entry = findEntryById(id);
        User currentUser = getCurrentUser();

        if (!entry.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only edit your own time entries");
        }

        if (request.getDescription() != null) {
            entry.setDescription(request.getDescription());
        }
        if (request.getStartedAt() != null) {
            entry.setStartedAt(request.getStartedAt());
        }
        if (request.getEndedAt() != null) {
            entry.setEndedAt(request.getEndedAt());
            entry.setIsRunning(false);
        }
        if (request.getDurationMinutes() != null) {
            entry.setDurationMinutes(request.getDurationMinutes());
        } else if (entry.getEndedAt() != null) {
            entry.setDurationMinutes(entry.calculateDuration());
        }
        if (request.getIsBillable() != null) {
            entry.setIsBillable(request.getIsBillable());
        }

        entry = timeEntryRepository.save(entry);

        // Update task spent hours
        updateTaskSpentHours(entry.getTask());

        return timeEntryMapper.toDto(entry);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        TimeEntry entry = findEntryById(id);
        User currentUser = getCurrentUser();

        if (!entry.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own time entries");
        }

        Task task = entry.getTask();
        entry.setIsDeleted(true);
        timeEntryRepository.save(entry);

        // Update task spent hours
        updateTaskSpentHours(task);
    }

    @Override
    public List<TimeEntryDto> getByTask(UUID taskId) {
        Task task = taskRepository.findByIdAndIsDeletedFalse(taskId)
                .orElseThrow(() -> new NotFoundException("Task", taskId));
        checkProjectAccess(task.getProject().getId());

        List<TimeEntry> entries = timeEntryRepository.findByTask(taskId);
        return timeEntryMapper.toDtoList(entries);
    }

    @Override
    public Page<TimeEntryDto> getMyEntries(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<TimeEntry> entries = timeEntryRepository.findByUser(currentUser.getId(), pageable);
        return entries.map(timeEntryMapper::toDto);
    }

    @Override
    public List<TimeEntryDto> getMyEntriesForDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = getCurrentUser();
        List<TimeEntry> entries = timeEntryRepository.findByUserAndDateRange(
                currentUser.getId(),
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );
        return timeEntryMapper.toDtoList(entries);
    }

    @Override
    public Page<TimeEntryDto> getByProject(UUID projectId, Pageable pageable) {
        checkProjectAccess(projectId);
        Page<TimeEntry> entries = timeEntryRepository.findByProject(projectId, pageable);
        return entries.map(timeEntryMapper::toDto);
    }

    @Override
    public Integer getTotalMinutesByTask(UUID taskId) {
        return timeEntryRepository.getTotalMinutesByTask(taskId);
    }

    @Override
    public TimeReportDto getMyWeeklyReport() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return getMyReport(startOfWeek, endOfWeek);
    }

    @Override
    public TimeReportDto getMyReport(LocalDate startDate, LocalDate endDate) {
        User currentUser = getCurrentUser();
        UUID userId = currentUser.getId();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // Get completed entries totals
        Integer completedMinutes = timeEntryRepository.getTotalMinutesByUserAndDateRange(
                userId, startDateTime, endDateTime);
        Integer billableMinutes = timeEntryRepository.getBillableMinutesByUserAndDateRange(
                userId, startDateTime, endDateTime);

        // Add running timers duration
        List<TimeEntry> runningEntries = timeEntryRepository.findRunningByUserAndDateRange(
                userId, startDateTime, endDateTime);
        int runningMinutes = 0;
        int runningBillableMinutes = 0;
        for (TimeEntry entry : runningEntries) {
            int duration = entry.calculateDuration();
            runningMinutes += duration;
            if (Boolean.TRUE.equals(entry.getIsBillable())) {
                runningBillableMinutes += duration;
            }
        }

        int totalMinutes = (completedMinutes != null ? completedMinutes : 0) + runningMinutes;
        int totalBillable = (billableMinutes != null ? billableMinutes : 0) + runningBillableMinutes;

        // Build daily breakdown
        List<DailyTimeDto> dailyBreakdown = buildDailyBreakdown(userId, startDate, endDate, startDateTime, endDateTime);

        // Build project breakdown
        List<ProjectTimeDto> projectBreakdown = buildProjectBreakdown(userId, startDateTime, endDateTime);

        return TimeReportDto.builder()
                .userId(userId)
                .userName(currentUser.getFullName())
                .startDate(startDate)
                .endDate(endDate)
                .totalMinutes(totalMinutes)
                .billableMinutes(totalBillable)
                .dailyBreakdown(dailyBreakdown)
                .projectBreakdown(projectBreakdown)
                .build();
    }

    private List<DailyTimeDto> buildDailyBreakdown(UUID userId, LocalDate startDate, LocalDate endDate,
                                                    LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Get daily totals from DB
        List<Object[]> dailyTotals = timeEntryRepository.getDailyTotalsByUserAndDateRange(
                userId, startDateTime, endDateTime);
        List<Object[]> dailyBillable = timeEntryRepository.getDailyBillableByUserAndDateRange(
                userId, startDateTime, endDateTime);

        // Convert to maps for easy lookup
        Map<LocalDate, Integer> totalsByDate = new HashMap<>();
        Map<LocalDate, Integer> billableByDate = new HashMap<>();

        for (Object[] row : dailyTotals) {
            LocalDate date = (LocalDate) row[0];
            Integer total = ((Number) row[1]).intValue();
            totalsByDate.put(date, total);
        }
        for (Object[] row : dailyBillable) {
            LocalDate date = (LocalDate) row[0];
            Integer total = ((Number) row[1]).intValue();
            billableByDate.put(date, total);
        }

        // Build list for all days in range
        List<DailyTimeDto> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            result.add(DailyTimeDto.builder()
                    .date(current)
                    .totalMinutes(totalsByDate.getOrDefault(current, 0))
                    .billableMinutes(billableByDate.getOrDefault(current, 0))
                    .build());
            current = current.plusDays(1);
        }
        return result;
    }

    private List<ProjectTimeDto> buildProjectBreakdown(UUID userId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Object[]> projectTotals = timeEntryRepository.getProjectTotalsByUserAndDateRange(
                userId, startDateTime, endDateTime);
        List<Object[]> projectBillable = timeEntryRepository.getProjectBillableByUserAndDateRange(
                userId, startDateTime, endDateTime);

        // Convert billable to map
        Map<UUID, Integer> billableByProject = new HashMap<>();
        for (Object[] row : projectBillable) {
            UUID projectId = (UUID) row[0];
            Integer total = ((Number) row[1]).intValue();
            billableByProject.put(projectId, total);
        }

        // Build project breakdown
        List<ProjectTimeDto> result = new ArrayList<>();
        for (Object[] row : projectTotals) {
            UUID projectId = (UUID) row[0];
            String projectName = (String) row[1];
            Integer totalMinutes = ((Number) row[2]).intValue();
            Integer taskCount = ((Number) row[3]).intValue();

            result.add(ProjectTimeDto.builder()
                    .projectId(projectId)
                    .projectName(projectName)
                    .totalMinutes(totalMinutes)
                    .billableMinutes(billableByProject.getOrDefault(projectId, 0))
                    .taskCount(taskCount)
                    .build());
        }
        return result;
    }

    // Helper methods

    private TimeEntry findEntryById(UUID id) {
        return timeEntryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Time entry", id));
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void checkProjectAccess(UUID projectId) {
        User currentUser = getCurrentUser();
        if (!projectService.isMember(projectId, currentUser.getId())) {
            throw new ForbiddenException("You do not have access to this project");
        }
    }

    private void updateTaskSpentHours(Task task) {
        Integer totalMinutes = timeEntryRepository.getTotalMinutesByTask(task.getId());
        BigDecimal hours = BigDecimal.valueOf(totalMinutes != null ? totalMinutes : 0)
                .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        task.setSpentHours(hours);
        taskRepository.save(task);
    }
}
