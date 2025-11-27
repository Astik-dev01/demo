package kg.taskflow.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import kg.taskflow.dto.common.BasePageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Filter DTO for project search
 * Inherits page and size from BasePageRequest
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Filter for project search")
public class ProjectFilter extends BasePageRequest {

    @Schema(description = "Search by name (partial match)", example = "TaskFlow")
    private String name;

    @Schema(description = "Search by project key (partial match)", example = "TF")
    private String projectKey;

    @Schema(description = "Filter by project type ID")
    private UUID typeId;

    @Schema(description = "Filter by owner ID")
    private UUID ownerId;

    @Schema(description = "Show only archived projects", example = "false")
    private Boolean isArchived;

    @Schema(description = "Show only public projects", example = "true")
    private Boolean isPublic;

    @Schema(description = "Text search (name, key, description)", example = "mobile")
    private String searchText;
}
