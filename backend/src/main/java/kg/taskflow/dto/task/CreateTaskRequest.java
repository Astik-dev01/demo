package kg.taskflow.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateTaskRequest {

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotNull(message = "Board ID is required")
    private UUID boardId;

    @NotNull(message = "Column ID is required")
    private UUID columnId;

    private UUID parentTaskId;

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be at most 500 characters")
    private String title;

    private String description;

    private UUID assigneeId;

    private UUID priorityId;

    private UUID statusId;

    private LocalDate dueDate;

    private LocalDate startDate;

    private BigDecimal estimatedHours;

    private List<UUID> tagIds;
}
