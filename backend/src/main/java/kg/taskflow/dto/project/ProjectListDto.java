package kg.taskflow.dto.project;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectListDto {
    private UUID id;
    private String name;
    private String projectKey;
    private String description;
    private String ownerName;
    private UUID ownerId;
    private String typeName;
    private String color;
    private String icon;
    private Boolean isArchived;
    private long memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
