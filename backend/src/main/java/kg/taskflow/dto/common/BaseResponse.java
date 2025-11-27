package kg.taskflow.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Unified API response format
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    @Schema(description = "Operation success", example = "true")
    private boolean success;

    @Schema(description = "Message (for errors)")
    private Object message;

    @Schema(description = "Operation result")
    private T result;

    @Schema(description = "Response time", example = "2025-01-31 15:30:00")
    @Builder.Default
    private String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    @Schema(description = "API version", example = "1.0.0")
    @Builder.Default
    private String ver = "1.0.0";

    /**
     * Creates a successful response with data
     */
    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .result(data)
                .build();
    }

    /**
     * Creates a successful response with data and message
     */
    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .result(data)
                .message(message)
                .build();
    }

    /**
     * Creates an error response
     */
    public static <T> BaseResponse<T> error(String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
