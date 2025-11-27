package kg.taskflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.time.*;
import kg.taskflow.service.TimeTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/time")
@RequiredArgsConstructor
@Tag(name = "Time Tracking", description = "Time tracking API")
public class TimeTrackingController {

    private final TimeTrackingService timeTrackingService;

    // Timer operations

    @PostMapping("/timer/start/{taskId}")
    @Operation(summary = "Start timer for a task")
    public ResponseEntity<TimeEntryDto> startTimer(
            @PathVariable UUID taskId,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeTrackingService.startTimer(taskId, description));
    }

    @PostMapping("/timer/stop")
    @Operation(summary = "Stop running timer")
    public ResponseEntity<TimeEntryDto> stopTimer() {
        return ResponseEntity.ok(timeTrackingService.stopTimer());
    }

    @GetMapping("/timer/running")
    @Operation(summary = "Get running timer")
    public ResponseEntity<TimeEntryDto> getRunningTimer() {
        TimeEntryDto timer = timeTrackingService.getRunningTimer();
        if (timer == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(timer);
    }

    // CRUD operations

    @PostMapping
    @Operation(summary = "Create time entry manually")
    public ResponseEntity<TimeEntryDto> create(@Valid @RequestBody CreateTimeEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeTrackingService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get time entry by ID")
    public ResponseEntity<TimeEntryDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(timeTrackingService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update time entry")
    public ResponseEntity<TimeEntryDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTimeEntryRequest request) {
        return ResponseEntity.ok(timeTrackingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete time entry")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        timeTrackingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Queries

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get time entries for a task")
    public ResponseEntity<List<TimeEntryDto>> getByTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(timeTrackingService.getByTask(taskId));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my time entries")
    public ResponseEntity<Page<TimeEntryDto>> getMyEntries(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(timeTrackingService.getMyEntries(pageable));
    }

    @GetMapping("/my/range")
    @Operation(summary = "Get my time entries for date range")
    public ResponseEntity<List<TimeEntryDto>> getMyEntriesForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(timeTrackingService.getMyEntriesForDateRange(startDate, endDate));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get time entries for a project")
    public ResponseEntity<Page<TimeEntryDto>> getByProject(
            @PathVariable UUID projectId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(timeTrackingService.getByProject(projectId, pageable));
    }

    // Reports

    @GetMapping("/task/{taskId}/total")
    @Operation(summary = "Get total minutes for a task")
    public ResponseEntity<Integer> getTotalMinutesByTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(timeTrackingService.getTotalMinutesByTask(taskId));
    }

    @GetMapping("/report/weekly")
    @Operation(summary = "Get my weekly report")
    public ResponseEntity<TimeReportDto> getMyWeeklyReport() {
        return ResponseEntity.ok(timeTrackingService.getMyWeeklyReport());
    }

    @GetMapping("/report")
    @Operation(summary = "Get my report for date range")
    public ResponseEntity<TimeReportDto> getMyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(timeTrackingService.getMyReport(startDate, endDate));
    }
}
