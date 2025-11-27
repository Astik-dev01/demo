package kg.taskflow.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.taskflow.db.entity.Project;
import kg.taskflow.db.repository.ProjectRepository;
import kg.taskflow.dto.project.ProjectDto;
import kg.taskflow.dto.project.UpdateProjectRequest;
import kg.taskflow.mapper.ProjectMapper;
import kg.taskflow.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
@Tag(name = "Admin Projects", description = "API для администрирования проектов")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Получить все проекты с пагинацией")
    public ResponseEntity<Page<ProjectDto>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isArchived,
            @RequestParam(required = false) Boolean isDeleted,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Project> projects;

        if (isDeleted != null && isDeleted) {
            projects = projectRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("isDeleted"), true),
                    pageable
            );
        } else {
            projects = projectRepository.findAllActive(pageable);
        }

        return ResponseEntity.ok(projects.map(projectMapper::toDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить проект по ID (включая удалённые)")
    public ResponseEntity<ProjectDto> findById(@PathVariable UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return ResponseEntity.ok(projectMapper.toDto(project));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить проект")
    public ResponseEntity<ProjectDto> update(
            @PathVariable UUID id,
            @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить проект (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Восстановить удалённый проект")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setIsDeleted(false);
        projectRepository.save(project);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Архивировать проект")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        projectService.archive(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unarchive")
    @Operation(summary = "Разархивировать проект")
    public ResponseEntity<Void> unarchive(@PathVariable UUID id) {
        projectService.unarchive(id);
        return ResponseEntity.ok().build();
    }
}
