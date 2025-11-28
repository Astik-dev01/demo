package kg.taskflow.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedTableSchema {

    private String title;
    private String description;
    private List<TableColumnSchema> columns;
    private boolean pagination;
    private boolean searchable;
    private List<String> actions; // create, edit, delete, export
}
