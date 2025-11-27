package kg.taskflow.controller.hb;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.hb.TaskPriorityDto;
import kg.taskflow.dto.hb.TaskPriorityRequest;
import kg.taskflow.service.hb.TaskPriorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/handbooks/task-priorities")
@RequiredArgsConstructor
@Tag(name = "Task Priorities", description = "Справочник приоритетов задач")
public class TaskPriorityController {

    private final TaskPriorityService service;

    @GetMapping
    @Operation(summary = "Получить все приоритеты")
    public ResponseEntity<List<TaskPriorityDto>> findAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findAll(locale));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить приоритет по ID")
    public ResponseEntity<TaskPriorityDto> findById(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findById(id, locale));
    }

    @GetMapping("/alias/{alias}")
    @Operation(summary = "Получить приоритет по alias")
    public ResponseEntity<TaskPriorityDto> findByAlias(
            @PathVariable String alias,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findByAlias(alias, locale));
    }

    @PostMapping
    @Operation(summary = "Создать приоритет")
    public ResponseEntity<TaskPriorityDto> create(
            @Valid @RequestBody TaskPriorityRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, locale));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить приоритет")
    public ResponseEntity<TaskPriorityDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody TaskPriorityRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.update(id, request, locale));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить приоритет (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Восстановить приоритет")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        service.restore(id);
        return ResponseEntity.noContent().build();
    }
}
