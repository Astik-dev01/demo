package kg.taskflow.dto.task;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TaskListDto {
    private UUID id;
    private String key;
    private Integer number;
    private String title;

    private UUID projectId;
    private String projectName;
    private String projectKey;

    private UUID columnId;
    private String columnName;
    private Integer position;

    private Boolean isDeleted;

    private String assigneeName;
    private String assigneeAvatar;

    private String priorityName;
    private String priorityColor;
    private Integer priorityLevel;

    private String statusName;
    private String statusColor;

    private LocalDate dueDate;
    private Boolean isOverdue;

    private List<String> tagNames;
    private List<String> tagColors;

    private Long commentCount;
    private Long subtaskCount;

    private LocalDateTime createdAt;
}
