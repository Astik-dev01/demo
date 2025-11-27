package kg.taskflow.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 10, message = "Key must be 2-10 characters")
    @Pattern(regexp = "^[A-Z][A-Z0-9]*$", message = "Key must be uppercase alphanumeric")
    private String projectKey;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    private UUID typeId;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Invalid color format")
    private String color = "#3B82F6";

    @Size(max = 50)
    private String icon;

    private Boolean isPublic = false;
}
