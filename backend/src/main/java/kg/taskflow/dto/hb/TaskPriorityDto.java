package kg.taskflow.dto.hb;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class TaskPriorityDto extends BaseHandbookDto {
    private String color;
    private String icon;
    private Integer level;
}
