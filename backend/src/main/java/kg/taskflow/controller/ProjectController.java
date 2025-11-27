package kg.taskflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.common.BasePageResponse;
import kg.taskflow.dto.common.BaseResponse;
import kg.taskflow.dto.project.*;
import kg.taskflow.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management API")
public class ProjectController extends BaseController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectDto> create(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "Get project by key")
    public ResponseEntity<ProjectDto> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(projectService.getByKey(key));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's projects (deprecated, use POST /my/filter)")
    @Deprecated
    public ResponseEntity<Page<ProjectListDto>> getMyProjects(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(projectService.getMyProjects(pageable));
    }

    @PostMapping("/my/filter")
    @Operation(summary = "Filter current user's projects with pagination")
    public ResponseEntity<BaseResponse<BasePageResponse<ProjectListDto>>> filterMyProjects(
            @RequestBody ProjectFilter filter) {
        return success(projectService.filterMyProjects(filter));
    }

    @GetMapping("/public")
    @Operation(summary = "Get public projects (deprecated, use POST /public/filter)")
    @Deprecated
    public ResponseEntity<Page<ProjectListDto>> getPublicProjects(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(projectService.getPublicProjects(pageable));
    }

    @PostMapping("/public/filter")
    @Operation(summary = "Filter public projects with pagination")
    public ResponseEntity<BaseResponse<BasePageResponse<ProjectListDto>>> filterPublicProjects(
            @RequestBody ProjectFilter filter) {
        return success(projectService.filterPublicProjects(filter));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    public ResponseEntity<ProjectDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive project")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        projectService.archive(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unarchive")
    @Operation(summary = "Unarchive project")
    public ResponseEntity<Void> unarchive(@PathVariable UUID id) {
        projectService.unarchive(id);
        return ResponseEntity.ok().build();
    }

    // Member management endpoints

    @GetMapping("/{id}/members")
    @Operation(summary = "Get project members")
    public ResponseEntity<List<ProjectMemberDto>> getMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getMembers(id));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to project")
    public ResponseEntity<ProjectMemberDto> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.addMember(id, request));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @Operation(summary = "Remove member from project")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId) {
        projectService.removeMember(projectId, memberId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectId}/members/{memberId}/role")
    @Operation(summary = "Update member role")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId,
            @RequestParam UUID roleId) {
        projectService.updateMemberRole(projectId, memberId, roleId);
        return ResponseEntity.ok().build();
    }
}
