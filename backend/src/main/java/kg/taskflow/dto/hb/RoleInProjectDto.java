package kg.taskflow.dto.hb;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class RoleInProjectDto extends BaseHandbookDto {
    private String descriptionRu;
    private String description; // Localized
    private List<String> permissions;
    private Integer level;
}
