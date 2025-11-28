package kg.taskflow.dto.time;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DailyTimeDto {
    private LocalDate date;
    private Integer totalMinutes;
    private Integer billableMinutes;
}
