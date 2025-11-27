package kg.taskflow.dto.project;

import kg.taskflow.dto.hb.RoleInProjectDto;
import kg.taskflow.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectMemberDto {
    private UUID id;
    private UserDto user;
    private RoleInProjectDto role;
    private LocalDateTime joinedAt;
    private UserDto invitedBy;
    private Boolean isActive;
}
