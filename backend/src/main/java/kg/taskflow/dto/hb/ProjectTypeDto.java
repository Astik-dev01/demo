package kg.taskflow.dto.hb;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class ProjectTypeDto extends BaseHandbookDto {
    private String icon;
    private String descriptionRu;
    private String descriptionKy;
    private String description; // Localized
}
