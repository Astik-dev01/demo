package kg.taskflow.dto.project;

import kg.taskflow.dto.hb.ProjectTypeDto;
import kg.taskflow.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ProjectDto {
    private UUID id;
    private String name;
    private String projectKey;
    private String description;
    private UserDto owner;
    private ProjectTypeDto type;
    private String color;
    private String icon;
    private Boolean isPublic;
    private Boolean isArchived;
    private Boolean isDeleted;
    private LocalDateTime archivedAt;
    private Map<String, Object> settings;
    private long memberCount;
    private long taskCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
