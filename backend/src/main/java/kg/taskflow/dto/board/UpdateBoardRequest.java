package kg.taskflow.dto.board;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBoardRequest {

    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    private Integer position;
}
