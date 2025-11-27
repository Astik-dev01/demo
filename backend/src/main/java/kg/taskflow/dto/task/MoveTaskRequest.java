package kg.taskflow.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MoveTaskRequest {

    @NotNull(message = "Column ID is required")
    private UUID columnId;

    @NotNull(message = "Position is required")
    private Integer position;
}
