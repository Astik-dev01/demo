package kg.taskflow.dto.time;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
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
