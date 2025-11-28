package kg.taskflow.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateTableRequest {

    @NotBlank(message = "Prompt is required")
    private String prompt;

    private String entityType; // optional: user, project, task, etc.
}
