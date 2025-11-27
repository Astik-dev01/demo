package kg.taskflow.controller;

import kg.taskflow.dto.common.BaseResponse;
import org.springframework.http.ResponseEntity;

/**
 * Base controller with utilities for pagination and responses
 */
public abstract class BaseController {

    /**
     * Converts 1-based page number to 0-based for JPA
     *
     * @param page page number (1-based)
     * @return page number (0-based)
     */
    public static Integer getPage(Integer page) {
        if (page != null && page > 0) {
            return page - 1;
        }
        return 0;
    }

    /**
     * Creates a successful response
     */
    protected <T> ResponseEntity<BaseResponse<T>> success(T data) {
        return ResponseEntity.ok(BaseResponse.success(data));
    }

    /**
     * Creates a successful response with message
     */
    protected <T> ResponseEntity<BaseResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(BaseResponse.success(data, message));
    }

    /**
     * Creates an error response
     */
    protected <T> ResponseEntity<BaseResponse<T>> error(String message) {
        return ResponseEntity.badRequest().body(BaseResponse.error(message));
    }
}
