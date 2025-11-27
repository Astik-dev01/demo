package kg.taskflow.dto.hb;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class ProjectTypeRequest extends BaseHandbookRequest {

    @Size(max = 50, message = "Icon must be at most 50 characters")
    private String icon;

    private String descriptionRu;

    private String descriptionKy;
}
