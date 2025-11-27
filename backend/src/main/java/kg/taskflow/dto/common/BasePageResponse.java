package kg.taskflow.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base class for paginated responses
 * Contains page metadata and total element counts
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BasePageResponse<T> {

    @Schema(description = "Current page (1-based)", example = "1")
    private int page;

    @Schema(description = "Page size", example = "15")
    private int size;

    @Schema(description = "Total number of elements", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "7")
    private int totalPages;

    @Schema(description = "Page data")
    private List<T> content;

    /**
     * Creates a BasePageResponse from a Spring Data Page
     * Converts 0-based page number to 1-based
     */
    public static <E, D> BasePageResponse<D> fromPage(Page<E> page, Function<E, D> mapper) {
        return BasePageResponse.<D>builder()
                .content(page.getContent().stream().map(mapper).collect(Collectors.toList()))
                .page(page.getNumber() + 1)  // 0-based -> 1-based
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * Creates a BasePageResponse from a Spring Data Page with pre-mapped content
     */
    public static <D> BasePageResponse<D> fromPage(Page<?> page, List<D> content) {
        return BasePageResponse.<D>builder()
                .content(content)
                .page(page.getNumber() + 1)  // 0-based -> 1-based
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
