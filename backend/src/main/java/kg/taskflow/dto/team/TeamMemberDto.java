package kg.taskflow.dto.team;

import kg.taskflow.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TeamMemberDto {
    private UUID id;
    private UserDto user;
    private String role;
    private LocalDateTime joinedAt;
}
