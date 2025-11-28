package kg.taskflow.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableColumnSchema {

    private String key;
    private String header;
    private String type; // text, number, date, boolean, badge, avatar, actions
    private boolean sortable;
    private boolean filterable;
    private String width; // e.g., "150px", "20%"
    private String align; // left, center, right
    private String format; // for dates, numbers
}
