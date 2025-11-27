package kg.taskflow.dto.board;

import kg.taskflow.dto.hb.TaskStatusDto;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BoardColumnDto {
    private UUID id;
    private UUID boardId;
    private String name;
    private String color;
    private Integer position;
    private Integer wipLimit;
    private TaskStatusDto status;
    private Long taskCount;
}
