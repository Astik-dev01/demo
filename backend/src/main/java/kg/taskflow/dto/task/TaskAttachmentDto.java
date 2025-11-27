package kg.taskflow.dto.task;

import kg.taskflow.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TaskAttachmentDto {
    private UUID id;
    private UUID taskId;
    private UserDto uploadedBy;
    private String fileName;
    private String filePath;
    private String url;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime createdAt;
}
