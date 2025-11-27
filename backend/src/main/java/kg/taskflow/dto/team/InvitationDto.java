package kg.taskflow.dto.team;

import kg.taskflow.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InvitationDto {
    private UUID id;
    private String email;
    private String type;
    private UUID targetId;
    private String targetName;
    private UUID roleId;
    private UserDto invitedBy;
    private LocalDateTime expiresAt;
    private String status;
    private LocalDateTime createdAt;
}
