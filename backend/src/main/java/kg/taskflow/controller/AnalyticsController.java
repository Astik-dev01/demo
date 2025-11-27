package kg.taskflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.taskflow.dto.analytics.*;
import kg.taskflow.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics and reporting API")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard overview for current user")
    public ResponseEntity<DashboardOverviewDto> getDashboardOverview() {
        return ResponseEntity.ok(analyticsService.getDashboardOverview());
    }

    @GetMapping("/projects/{projectId}")
    @Operation(summary = "Get project analytics")
    public ResponseEntity<ProjectAnalyticsDto> getProjectAnalytics(@PathVariable UUID projectId) {
        return ResponseEntity.ok(analyticsService.getProjectAnalytics(projectId));
    }

    @GetMapping("/projects/{projectId}/burndown")
    @Operation(summary = "Get burndown chart data for a project")
    public ResponseEntity<BurndownChartDto> getBurndownChart(
            @PathVariable UUID projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getBurndownChart(projectId, startDate, endDate));
    }

    @GetMapping("/projects/{projectId}/velocity")
    @Operation(summary = "Get velocity chart data for a project")
    public ResponseEntity<VelocityChartDto> getVelocityChart(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "8") int weeks) {
        return ResponseEntity.ok(analyticsService.getVelocityChart(projectId, weeks));
    }
}
