package kg.taskflow.dto.task;

import kg.taskflow.dto.hb.TaskPriorityDto;
import kg.taskflow.dto.hb.TaskStatusDto;
import kg.taskflow.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TaskDto {
    private UUID id;
    private UUID projectId;
    private String projectKey;
    private UUID boardId;
    private UUID columnId;
    private String columnName;
    private UUID parentTaskId;

    private Integer number;
    private String key;
    private String title;
    private String description;

    private UserDto reporter;
    private UserDto assignee;

    private TaskPriorityDto priority;
    private TaskStatusDto status;

    private Integer position;

    private LocalDate dueDate;
    private LocalDate startDate;
    private LocalDateTime completedAt;

    private BigDecimal estimatedHours;
    private BigDecimal spentHours;

    private Boolean isArchived;

    private List<TagDto> tags;
    private Long commentCount;
    private Long attachmentCount;
    private Long subtaskCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
