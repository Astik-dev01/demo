package kg.taskflow.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kg.taskflow.db.entity.AvailableRoute;
import kg.taskflow.db.repository.AvailableRouteRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/routes")
@RequiredArgsConstructor
@Tag(name = "Admin Routes", description = "API for managing available routes")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRouteController {

    private final AvailableRouteRepository routeRepository;

    @GetMapping
    @Operation(summary = "Get all available routes")
    public ResponseEntity<List<AvailableRoute>> findAll() {
        return ResponseEntity.ok(routeRepository.findAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active routes")
    public ResponseEntity<List<AvailableRoute>> findAllActive() {
        return ResponseEntity.ok(routeRepository.findByIsDeletedFalse());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get route by ID")
    public ResponseEntity<AvailableRoute> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                routeRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Route not found"))
        );
    }

    @PostMapping
    @Operation(summary = "Create new route")
    public ResponseEntity<AvailableRoute> create(@Valid @RequestBody CreateRouteRequest request) {
        if (routeRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Route with this code already exists");
        }

        AvailableRoute route = AvailableRoute.builder()
                .code(request.getCode())
                .descriptionRu(request.getDescriptionRu())
                .descriptionEn(request.getDescriptionEn())
                .isDeleted(false)
                .build();

        return ResponseEntity.ok(routeRepository.save(route));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update route")
    public ResponseEntity<AvailableRoute> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRouteRequest request) {

        AvailableRoute route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (request.getCode() != null && !request.getCode().equals(route.getCode())) {
            if (routeRepository.existsByCode(request.getCode())) {
                throw new RuntimeException("Route with this code already exists");
            }
            route.setCode(request.getCode());
        }

        if (request.getDescriptionRu() != null) route.setDescriptionRu(request.getDescriptionRu());
        if (request.getDescriptionEn() != null) route.setDescriptionEn(request.getDescriptionEn());

        return ResponseEntity.ok(routeRepository.save(route));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete route (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        AvailableRoute route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        route.setIsDeleted(true);
        routeRepository.save(route);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore deleted route")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        AvailableRoute route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        route.setIsDeleted(false);
        routeRepository.save(route);

        return ResponseEntity.noContent().build();
    }

    // DTOs
    @Data
    public static class CreateRouteRequest {
        @NotBlank
        private String code;
        private String descriptionRu;
        private String descriptionEn;
    }

    @Data
    public static class UpdateRouteRequest {
        private String code;
        private String descriptionRu;
        private String descriptionEn;
    }
}
