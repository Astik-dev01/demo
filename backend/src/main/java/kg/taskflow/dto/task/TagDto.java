package kg.taskflow.dto.task;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TagDto {
    private UUID id;
    private String name;
    private String color;
    private String categoryName;
}
