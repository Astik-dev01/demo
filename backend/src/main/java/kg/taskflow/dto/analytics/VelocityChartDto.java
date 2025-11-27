package kg.taskflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VelocityChartDto {
    private BigDecimal averageVelocity;
    private long totalTasksCompleted;
    private BigDecimal totalHoursSpent;
    private List<VelocityDataPointDto> dataPoints;
}
