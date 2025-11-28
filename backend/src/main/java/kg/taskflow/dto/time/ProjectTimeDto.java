package kg.taskflow.dto.time;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProjectTimeDto {
    private UUID projectId;
    private String projectName;
    private Integer totalMinutes;
    private Integer billableMinutes;
    private Integer taskCount;
}
