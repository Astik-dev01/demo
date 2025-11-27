package kg.taskflow.dto.time;

import kg.taskflow.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TimeEntryDto {
    private UUID id;
    private UUID taskId;
    private String taskKey;
    private String taskTitle;
    private UUID projectId;
    private String projectName;
    private UserDto user;
    private String description;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationMinutes;
    private Boolean isBillable;
    private Boolean isRunning;
    private LocalDateTime createdAt;
}
