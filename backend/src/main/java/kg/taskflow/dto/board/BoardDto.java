package kg.taskflow.dto.board;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class BoardDto {
    private UUID id;
    private UUID projectId;
    private String name;
    private String description;
    private Integer position;
    private Boolean isDefault;
    private Map<String, Object> settings;
    private List<BoardColumnDto> columns;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
