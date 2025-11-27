package kg.taskflow.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kg.taskflow.db.entity.Role;
import kg.taskflow.db.entity.RoleLinkedAvailableRoute;
import kg.taskflow.db.repository.RoleLinkedAvailableRouteRepository;
import kg.taskflow.db.repository.RoleRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Admin Roles", description = "API for managing system roles")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    private final RoleRepository roleRepository;
    private final RoleLinkedAvailableRouteRepository roleLinkedRouteRepository;

    @GetMapping
    @Operation(summary = "Get all system roles")
    public ResponseEntity<List<Role>> findAll() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active system roles")
    public ResponseEntity<List<Role>> findAllActive() {
        return ResponseEntity.ok(roleRepository.findByActiveTrue());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<RoleWithPermissionsDto> findById(@PathVariable UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<RoleLinkedAvailableRoute> permissions = roleLinkedRouteRepository.findByRoleCodeWithRoute(role.getCode());

        return ResponseEntity.ok(RoleWithPermissionsDto.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode())
                .nameRu(role.getNameRu())
                .nameEn(role.getNameEn())
                .description(role.getDescription())
                .priority(role.getPriority())
                .active(role.getActive())
                .isSystem(role.getIsSystem())
                .createdAt(role.getCreatedAt() != null ? role.getCreatedAt().toString() : null)
                .permissions(permissions.stream().map(p -> RolePermissionDto.builder()
                        .id(p.getId())
                        .routeId(p.getAvailableRoute().getId())
                        .routeCode(p.getAvailableRoute().getCode())
                        .routeDescriptionRu(p.getAvailableRoute().getDescriptionRu())
                        .routeDescriptionEn(p.getAvailableRoute().getDescriptionEn())
                        .methodGet(p.getMethodGet())
                        .methodPost(p.getMethodPost())
                        .methodPut(p.getMethodPut())
                        .methodDelete(p.getMethodDelete())
                        .build()).collect(Collectors.toList()))
                .build());
    }

    @PostMapping
    @Operation(summary = "Create new role")
    public ResponseEntity<Role> create(@Valid @RequestBody CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Role with this name already exists");
        }

        Role role = Role.builder()
                .name(request.getName())
                .code(request.getCode() != null ? request.getCode() : request.getName())
                .nameRu(request.getNameRu())
                .nameEn(request.getNameEn())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : 100)
                .active(true)
                .isSystem(false)
                .build();

        return ResponseEntity.ok(roleRepository.save(role));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role")
    public ResponseEntity<Role> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (role.getIsSystem()) {
            throw new RuntimeException("Cannot modify system role");
        }

        if (request.getName() != null) role.setName(request.getName());
        if (request.getCode() != null) role.setCode(request.getCode());
        if (request.getNameRu() != null) role.setNameRu(request.getNameRu());
        if (request.getNameEn() != null) role.setNameEn(request.getNameEn());
        if (request.getDescription() != null) role.setDescription(request.getDescription());
        if (request.getPriority() != null) role.setPriority(request.getPriority());
        if (request.getActive() != null) role.setActive(request.getActive());

        return ResponseEntity.ok(roleRepository.save(role));
    }

    @PutMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle role active status")
    public ResponseEntity<Void> toggleActive(@PathVariable UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (role.getIsSystem()) {
            throw new RuntimeException("Cannot modify system role");
        }

        role.setActive(!role.getActive());
        roleRepository.save(role);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (role.getIsSystem()) {
            throw new RuntimeException("Cannot delete system role");
        }

        role.setActive(false);
        roleRepository.save(role);

        return ResponseEntity.noContent().build();
    }

    // DTOs
    @Data
    public static class CreateRoleRequest {
        @NotBlank
        private String name;
        private String code;
        private String nameRu;
        private String nameEn;
        private String description;
        private Integer priority;
    }

    @Data
    public static class UpdateRoleRequest {
        private String name;
        private String code;
        private String nameRu;
        private String nameEn;
        private String description;
        private Integer priority;
        private Boolean active;
    }

    @Data
    @Builder
    public static class RoleWithPermissionsDto {
        private UUID id;
        private String name;
        private String code;
        private String nameRu;
        private String nameEn;
        private String description;
        private Integer priority;
        private Boolean active;
        private Boolean isSystem;
        private String createdAt;
        private List<RolePermissionDto> permissions;
    }

    @Data
    @Builder
    public static class RolePermissionDto {
        private UUID id;
        private UUID routeId;
        private String routeCode;
        private String routeDescriptionRu;
        private String routeDescriptionEn;
        private Boolean methodGet;
        private Boolean methodPost;
        private Boolean methodPut;
        private Boolean methodDelete;
    }
}
