package kg.taskflow.dto.time;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class TimeReportDto {
    private UUID userId;
    private String userName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalMinutes;
    private Integer billableMinutes;
    private List<DailyTimeDto> dailyBreakdown;
    private List<ProjectTimeDto> projectBreakdown;
}

@Data
@Builder
class DailyTimeDto {
    private LocalDate date;
    private Integer totalMinutes;
    private Integer billableMinutes;
}

@Data
@Builder
class ProjectTimeDto {
    private UUID projectId;
    private String projectName;
    private Integer totalMinutes;
    private Integer billableMinutes;
    private Integer taskCount;
}
