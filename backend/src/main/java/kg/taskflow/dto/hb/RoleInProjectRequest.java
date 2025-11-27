package kg.taskflow.dto.hb;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class RoleInProjectRequest extends BaseHandbookRequest {

    private String descriptionRu;

    private List<String> permissions = new ArrayList<>();

    @Min(value = 0, message = "Level must be at least 0")
    @Max(value = 100, message = "Level must be at most 100")
    private Integer level = 0;
}
