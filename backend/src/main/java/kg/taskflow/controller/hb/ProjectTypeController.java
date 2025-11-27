package kg.taskflow.controller.hb;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.hb.ProjectTypeDto;
import kg.taskflow.dto.hb.ProjectTypeRequest;
import kg.taskflow.service.hb.ProjectTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/handbooks/project-types")
@RequiredArgsConstructor
@Tag(name = "Project Types", description = "Справочник типов проектов")
public class ProjectTypeController {

    private final ProjectTypeService service;

    @GetMapping
    @Operation(summary = "Получить все типы проектов")
    public ResponseEntity<List<ProjectTypeDto>> findAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findAll(locale));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить тип проекта по ID")
    public ResponseEntity<ProjectTypeDto> findById(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findById(id, locale));
    }

    @GetMapping("/alias/{alias}")
    @Operation(summary = "Получить тип проекта по alias")
    public ResponseEntity<ProjectTypeDto> findByAlias(
            @PathVariable String alias,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findByAlias(alias, locale));
    }

    @PostMapping
    @Operation(summary = "Создать тип проекта")
    public ResponseEntity<ProjectTypeDto> create(
            @Valid @RequestBody ProjectTypeRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, locale));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить тип проекта")
    public ResponseEntity<ProjectTypeDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectTypeRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.update(id, request, locale));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить тип проекта (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Восстановить тип проекта")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        service.restore(id);
        return ResponseEntity.noContent().build();
    }
}
