package kg.taskflow.dto.hb;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class TaskPriorityRequest extends BaseHandbookRequest {

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color")
    private String color = "#6B7280";

    @Size(max = 50, message = "Icon must be at most 50 characters")
    private String icon;

    @Min(value = 0, message = "Level must be at least 0")
    @Max(value = 100, message = "Level must be at most 100")
    private Integer level = 0;
}
