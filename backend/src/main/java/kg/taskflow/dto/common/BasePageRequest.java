package kg.taskflow.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for all paginated filter requests
 * All filters should extend this class
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BasePageRequest {

    @Schema(description = "Page number (1-based)", example = "1", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "Page size", example = "15", defaultValue = "15")
    private Integer size = 15;

    @Schema(description = "Sort field", example = "createdAt")
    private String sortBy;

    @Schema(description = "Sort direction (ASC/DESC)", example = "DESC", defaultValue = "DESC")
    private String sortDirection = "DESC";
}
