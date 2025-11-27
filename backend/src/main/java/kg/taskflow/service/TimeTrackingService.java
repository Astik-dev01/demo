package kg.taskflow.service;

import kg.taskflow.dto.time.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TimeTrackingService {

    // Timer operations
    TimeEntryDto startTimer(UUID taskId, String description);

    TimeEntryDto stopTimer();

    TimeEntryDto getRunningTimer();

    // CRUD operations
    TimeEntryDto create(CreateTimeEntryRequest request);

    TimeEntryDto getById(UUID id);

    TimeEntryDto update(UUID id, UpdateTimeEntryRequest request);

    void delete(UUID id);

    // Queries
    List<TimeEntryDto> getByTask(UUID taskId);

    Page<TimeEntryDto> getMyEntries(Pageable pageable);

    List<TimeEntryDto> getMyEntriesForDateRange(LocalDate startDate, LocalDate endDate);

    Page<TimeEntryDto> getByProject(UUID projectId, Pageable pageable);

    // Reports
    Integer getTotalMinutesByTask(UUID taskId);

    TimeReportDto getMyWeeklyReport();

    TimeReportDto getMyReport(LocalDate startDate, LocalDate endDate);
}
