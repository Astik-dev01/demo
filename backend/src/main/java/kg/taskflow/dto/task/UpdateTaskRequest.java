package kg.taskflow.dto.task;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateTaskRequest {

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
