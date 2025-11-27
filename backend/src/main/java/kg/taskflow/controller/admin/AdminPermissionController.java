package kg.taskflow.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kg.taskflow.db.entity.AvailableRoute;
import kg.taskflow.db.entity.Role;
import kg.taskflow.db.entity.RoleLinkedAvailableRoute;
import kg.taskflow.db.repository.AvailableRouteRepository;
import kg.taskflow.db.repository.RoleLinkedAvailableRouteRepository;
import kg.taskflow.db.repository.RoleRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
@Tag(name = "Admin Permissions", description = "API for managing role-route permissions")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPermissionController {

    private final RoleLinkedAvailableRouteRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final AvailableRouteRepository routeRepository;

    @GetMapping
    @Operation(summary = "Get all permissions")
    public ResponseEntity<List<PermissionDto>> findAll() {
        List<RoleLinkedAvailableRoute> permissions = permissionRepository.findAllActiveWithRoleAndRoute();

        return ResponseEntity.ok(permissions.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/by-role/{roleId}")
    @Operation(summary = "Get permissions by role")
    public ResponseEntity<List<PermissionDto>> findByRole(@PathVariable UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<RoleLinkedAvailableRoute> permissions = permissionRepository.findByRoleCodeWithRoute(role.getCode());

        return ResponseEntity.ok(permissions.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/by-route/{routeId}")
    @Operation(summary = "Get permissions by route")
    public ResponseEntity<List<PermissionDto>> findByRoute(@PathVariable UUID routeId) {
        AvailableRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        List<RoleLinkedAvailableRoute> permissions = permissionRepository.findByRouteCodeWithRole(route.getCode());

        return ResponseEntity.ok(permissions.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create or update permission")
    @Transactional
    public ResponseEntity<PermissionDto> createOrUpdate(@Valid @RequestBody CreatePermissionRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        AvailableRoute route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));

        RoleLinkedAvailableRoute permission = permissionRepository
                .findByRoleIdAndAvailableRouteId(request.getRoleId(), request.getRouteId())
                .orElse(RoleLinkedAvailableRoute.builder()
                        .role(role)
                        .availableRoute(route)
                        .build());

        permission.setMethodGet(request.getMethodGet() != null ? request.getMethodGet() : false);
        permission.setMethodPost(request.getMethodPost() != null ? request.getMethodPost() : false);
        permission.setMethodPut(request.getMethodPut() != null ? request.getMethodPut() : false);
        permission.setMethodDelete(request.getMethodDelete() != null ? request.getMethodDelete() : false);

        permission = permissionRepository.save(permission);

        return ResponseEntity.ok(toDto(permission));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update permission")
    public ResponseEntity<PermissionDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionRequest request) {

        RoleLinkedAvailableRoute permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        if (request.getMethodGet() != null) permission.setMethodGet(request.getMethodGet());
        if (request.getMethodPost() != null) permission.setMethodPost(request.getMethodPost());
        if (request.getMethodPut() != null) permission.setMethodPut(request.getMethodPut());
        if (request.getMethodDelete() != null) permission.setMethodDelete(request.getMethodDelete());

        permission = permissionRepository.save(permission);

        return ResponseEntity.ok(toDto(permission));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete permission")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!permissionRepository.existsById(id)) {
            throw new RuntimeException("Permission not found");
        }

        permissionRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-role-route")
    @Operation(summary = "Delete permission by role and route")
    @Transactional
    public ResponseEntity<Void> deleteByRoleAndRoute(
            @RequestParam UUID roleId,
            @RequestParam UUID routeId) {

        permissionRepository.deleteByRoleIdAndAvailableRouteId(roleId, routeId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk update permissions for a role")
    @Transactional
    public ResponseEntity<List<PermissionDto>> bulkUpdate(@Valid @RequestBody BulkPermissionRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<PermissionDto> result = request.getPermissions().stream().map(p -> {
            AvailableRoute route = routeRepository.findById(p.getRouteId())
                    .orElseThrow(() -> new RuntimeException("Route not found: " + p.getRouteId()));

            RoleLinkedAvailableRoute permission = permissionRepository
                    .findByRoleIdAndAvailableRouteId(request.getRoleId(), p.getRouteId())
                    .orElse(RoleLinkedAvailableRoute.builder()
                            .role(role)
                            .availableRoute(route)
                            .build());

            permission.setMethodGet(p.getMethodGet() != null ? p.getMethodGet() : false);
            permission.setMethodPost(p.getMethodPost() != null ? p.getMethodPost() : false);
            permission.setMethodPut(p.getMethodPut() != null ? p.getMethodPut() : false);
            permission.setMethodDelete(p.getMethodDelete() != null ? p.getMethodDelete() : false);

            return toDto(permissionRepository.save(permission));
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private PermissionDto toDto(RoleLinkedAvailableRoute permission) {
        return PermissionDto.builder()
                .id(permission.getId())
                .roleId(permission.getRole().getId())
                .roleName(permission.getRole().getName())
                .roleCode(permission.getRole().getCode())
                .routeId(permission.getAvailableRoute().getId())
                .routeCode(permission.getAvailableRoute().getCode())
                .routeDescriptionRu(permission.getAvailableRoute().getDescriptionRu())
                .routeDescriptionEn(permission.getAvailableRoute().getDescriptionEn())
                .methodGet(permission.getMethodGet())
                .methodPost(permission.getMethodPost())
                .methodPut(permission.getMethodPut())
                .methodDelete(permission.getMethodDelete())
                .createdAt(permission.getCreatedAt() != null ? permission.getCreatedAt().toString() : null)
                .build();
    }

    // DTOs
    @Data
    @Builder
    public static class PermissionDto {
        private UUID id;
        private UUID roleId;
        private String roleName;
        private String roleCode;
        private UUID routeId;
        private String routeCode;
        private String routeDescriptionRu;
        private String routeDescriptionEn;
        private Boolean methodGet;
        private Boolean methodPost;
        private Boolean methodPut;
        private Boolean methodDelete;
        private String createdAt;
    }

    @Data
    public static class CreatePermissionRequest {
        @NotNull
        private UUID roleId;
        @NotNull
        private UUID routeId;
        private Boolean methodGet;
        private Boolean methodPost;
        private Boolean methodPut;
        private Boolean methodDelete;
    }

    @Data
    public static class UpdatePermissionRequest {
        private Boolean methodGet;
        private Boolean methodPost;
        private Boolean methodPut;
        private Boolean methodDelete;
    }

    @Data
    public static class BulkPermissionRequest {
        @NotNull
        private UUID roleId;
        private List<RoutePermission> permissions;
    }

    @Data
    public static class RoutePermission {
        @NotNull
        private UUID routeId;
        private Boolean methodGet;
        private Boolean methodPost;
        private Boolean methodPut;
        private Boolean methodDelete;
    }
}
