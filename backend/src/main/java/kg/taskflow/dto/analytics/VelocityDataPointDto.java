package kg.taskflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VelocityDataPointDto {
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private long tasksCompleted;
    private BigDecimal hoursSpent;
}
