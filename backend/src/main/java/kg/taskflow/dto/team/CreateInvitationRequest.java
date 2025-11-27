package kg.taskflow.dto.team;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateInvitationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Type is required")
    private String type; // PROJECT, TEAM

    @NotNull(message = "Target ID is required")
    private UUID targetId;

    private UUID roleId;
}
