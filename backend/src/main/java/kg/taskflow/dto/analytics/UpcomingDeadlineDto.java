package kg.taskflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingDeadlineDto {
    private UUID taskId;
    private String taskKey;
    private String taskTitle;
    private String projectName;
    private String priority;
    private String priorityColor;
    private LocalDate dueDate;
    private long daysRemaining;
}
