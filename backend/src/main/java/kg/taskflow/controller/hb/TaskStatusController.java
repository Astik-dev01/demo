package kg.taskflow.controller.hb;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.hb.TaskStatusDto;
import kg.taskflow.dto.hb.TaskStatusRequest;
import kg.taskflow.service.hb.TaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/handbooks/task-statuses")
@RequiredArgsConstructor
@Tag(name = "Task Statuses", description = "Справочник статусов задач")
public class TaskStatusController {

    private final TaskStatusService service;

    @GetMapping
    @Operation(summary = "Получить все статусы")
    public ResponseEntity<List<TaskStatusDto>> findAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findAll(locale));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить статус по ID")
    public ResponseEntity<TaskStatusDto> findById(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findById(id, locale));
    }

    @GetMapping("/alias/{alias}")
    @Operation(summary = "Получить статус по alias")
    public ResponseEntity<TaskStatusDto> findByAlias(
            @PathVariable String alias,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findByAlias(alias, locale));
    }

    @GetMapping("/default")
    @Operation(summary = "Получить статус по умолчанию")
    public ResponseEntity<TaskStatusDto> findDefault(
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findDefault(locale));
    }

    @GetMapping("/final")
    @Operation(summary = "Получить финальные статусы")
    public ResponseEntity<List<TaskStatusDto>> findFinalStatuses(
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findFinalStatuses(locale));
    }

    @PostMapping
    @Operation(summary = "Создать статус")
    public ResponseEntity<TaskStatusDto> create(
            @Valid @RequestBody TaskStatusRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, locale));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить статус")
    public ResponseEntity<TaskStatusDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody TaskStatusRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.update(id, request, locale));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить статус (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Восстановить статус")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        service.restore(id);
        return ResponseEntity.noContent().build();
    }
}
