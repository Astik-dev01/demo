package kg.taskflow.dto.time;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateTimeEntryRequest {

    @NotNull(message = "Task ID is required")
    private UUID taskId;

    private String description;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Integer durationMinutes;

    private Boolean isBillable = true;
}
