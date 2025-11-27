package kg.taskflow.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public NotFoundException(String entityName, Object id) {
        super(entityName + " not found with id: " + id, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }
}
