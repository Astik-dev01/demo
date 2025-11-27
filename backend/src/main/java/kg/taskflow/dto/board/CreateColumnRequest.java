package kg.taskflow.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateColumnRequest {

    @NotBlank(message = "Column name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Invalid color format")
    private String color = "#E5E7EB";

    private Integer wipLimit;

    private UUID statusId;
}
