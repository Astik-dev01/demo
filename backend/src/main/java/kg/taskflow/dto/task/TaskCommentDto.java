package kg.taskflow.dto.task;

import kg.taskflow.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TaskCommentDto {
    private UUID id;
    private UUID taskId;
    private UserDto user;
    private UUID parentCommentId;
    private String content;
    private Boolean isEdited;
    private LocalDateTime editedAt;
    private List<TaskCommentDto> replies;
    private LocalDateTime createdAt;
}
