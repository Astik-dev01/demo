package kg.taskflow.dto.analytics;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class TaskSummaryDto {
    private UUID id;
    private String key;
    private String title;
    private String projectName;
    private String projectKey;
    private String priorityName;
    private String priorityColor;
    private String statusName;
    private LocalDate dueDate;
    private Boolean isOverdue;
}
