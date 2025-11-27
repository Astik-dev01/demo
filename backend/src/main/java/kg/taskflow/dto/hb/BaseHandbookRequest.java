package kg.taskflow.dto.hb;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public abstract class BaseHandbookRequest {

    @NotBlank(message = "Alias is required")
    @Size(max = 50, message = "Alias must be at most 50 characters")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "Alias must be uppercase with underscores")
    private String alias;

    @NotBlank(message = "Russian name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String nameRu;

    @NotBlank(message = "Kyrgyz name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String nameKy;

    @Size(max = 100, message = "Name must be at most 100 characters")
    private String nameEn;
}
