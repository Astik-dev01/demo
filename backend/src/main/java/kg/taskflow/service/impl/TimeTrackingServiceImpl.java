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
import java.util.List;
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

        Integer totalMinutes = timeEntryRepository.getTotalMinutesByUserAndDateRange(
                currentUser.getId(),
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );

        return TimeReportDto.builder()
                .userId(currentUser.getId())
                .userName(currentUser.getFullName())
                .startDate(startDate)
                .endDate(endDate)
                .totalMinutes(totalMinutes != null ? totalMinutes : 0)
                .billableMinutes(totalMinutes != null ? totalMinutes : 0) // Simplified
                .build();
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
