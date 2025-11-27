package kg.taskflow.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.admin.AdminUserDto;
import kg.taskflow.dto.admin.AdminUserFilterRequest;
import kg.taskflow.dto.admin.CreateUserRequest;
import kg.taskflow.dto.admin.UpdateUserRequest;
import kg.taskflow.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "API для администрирования пользователей")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "Получить список пользователей с фильтрами и пагинацией")
    public ResponseEntity<Page<AdminUserDto>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID roleId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isDeleted,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        AdminUserFilterRequest filter = AdminUserFilterRequest.builder()
                .search(search)
                .roleId(roleId)
                .isActive(isActive)
                .isDeleted(isDeleted)
                .build();

        return ResponseEntity.ok(adminUserService.findAllWithFilters(filter, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID")
    public ResponseEntity<AdminUserDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Создать пользователя")
    public ResponseEntity<AdminUserDto> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createUser(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя")
    public ResponseEntity<AdminUserDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(id, request));
    }

    @PutMapping("/{id}/toggle-active")
    @Operation(summary = "Заблокировать/разблокировать пользователя")
    public ResponseEntity<Void> toggleActive(@PathVariable UUID id) {
        adminUserService.toggleActive(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Назначить роли пользователю")
    public ResponseEntity<Void> assignRoles(
            @PathVariable UUID id,
            @RequestBody List<UUID> roleIds) {
        adminUserService.assignRoles(id, roleIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        adminUserService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Восстановить удалённого пользователя")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        adminUserService.restore(id);
        return ResponseEntity.noContent().build();
    }
}
