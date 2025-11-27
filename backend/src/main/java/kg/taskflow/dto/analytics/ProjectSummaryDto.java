package kg.taskflow.dto.analytics;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProjectSummaryDto {
    private UUID id;
    private String name;
    private String projectKey;
    private String color;
    private Long totalTasks;
    private Long completedTasks;
    private Long memberCount;
    private Double progress;
}
