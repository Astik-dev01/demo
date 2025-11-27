package kg.taskflow.controller.hb;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.hb.RoleInProjectDto;
import kg.taskflow.dto.hb.RoleInProjectRequest;
import kg.taskflow.service.hb.RoleInProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/handbooks/project-roles")
@RequiredArgsConstructor
@Tag(name = "Project Roles", description = "Справочник ролей в проекте")
public class RoleInProjectController {

    private final RoleInProjectService service;

    @GetMapping
    @Operation(summary = "Получить все роли в проекте")
    public ResponseEntity<List<RoleInProjectDto>> findAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findAll(locale));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить роль по ID")
    public ResponseEntity<RoleInProjectDto> findById(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findById(id, locale));
    }

    @GetMapping("/alias/{alias}")
    @Operation(summary = "Получить роль по alias")
    public ResponseEntity<RoleInProjectDto> findByAlias(
            @PathVariable String alias,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.findByAlias(alias, locale));
    }

    @PostMapping
    @Operation(summary = "Создать роль")
    public ResponseEntity<RoleInProjectDto> create(
            @Valid @RequestBody RoleInProjectRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, locale));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить роль")
    public ResponseEntity<RoleInProjectDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody RoleInProjectRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale) {
        return ResponseEntity.ok(service.update(id, request, locale));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить роль (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Восстановить роль")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        service.restore(id);
        return ResponseEntity.noContent().build();
    }
}
