package kg.taskflow.dto.hb;

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
public class TaskStatusRequest extends BaseHandbookRequest {

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color")
    private String color = "#6B7280";

    @Size(max = 50, message = "Icon must be at most 50 characters")
    private String icon;

    private boolean isFinal = false;

    private boolean isDefault = false;
}
