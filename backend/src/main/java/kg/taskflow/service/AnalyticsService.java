package kg.taskflow.service;

import kg.taskflow.dto.analytics.*;

import java.time.LocalDate;
import java.util.UUID;

public interface AnalyticsService {

    DashboardOverviewDto getDashboardOverview();

    ProjectAnalyticsDto getProjectAnalytics(UUID projectId);

    BurndownChartDto getBurndownChart(UUID projectId, LocalDate startDate, LocalDate endDate);

    VelocityChartDto getVelocityChart(UUID projectId, int weeks);
}
