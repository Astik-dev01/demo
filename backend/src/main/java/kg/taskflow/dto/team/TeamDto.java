package kg.taskflow.dto.team;

import kg.taskflow.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TeamDto {
    private UUID id;
    private String name;
    private String description;
    private UserDto owner;
    private String avatarUrl;
    private Boolean isPublic;
    private Long memberCount;
    private LocalDateTime createdAt;
}
