package kg.taskflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.team.*;
import kg.taskflow.service.TeamService;
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
@RequestMapping("/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team management API")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "Create a new team")
    public ResponseEntity<TeamDto> create(@Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID")
    public ResponseEntity<TeamDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(teamService.getById(id));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's teams")
    public ResponseEntity<Page<TeamDto>> getMyTeams(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(teamService.getMyTeams(pageable));
    }

    @GetMapping("/public")
    @Operation(summary = "Get public teams")
    public ResponseEntity<Page<TeamDto>> getPublicTeams(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(teamService.getPublicTeams(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update team")
    public ResponseEntity<TeamDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTeamRequest request) {
        return ResponseEntity.ok(teamService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete team")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Member management

    @GetMapping("/{id}/members")
    @Operation(summary = "Get team members")
    public ResponseEntity<List<TeamMemberDto>> getMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(teamService.getMembers(id));
    }

    @PostMapping("/{teamId}/members/{userId}")
    @Operation(summary = "Add member to team")
    public ResponseEntity<TeamMemberDto> addMember(
            @PathVariable UUID teamId,
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "MEMBER") String role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.addMember(teamId, userId, role));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @Operation(summary = "Remove member from team")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID teamId,
            @PathVariable UUID userId) {
        teamService.removeMember(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{teamId}/members/{userId}/role")
    @Operation(summary = "Update member role")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable UUID teamId,
            @PathVariable UUID userId,
            @RequestParam String role) {
        teamService.updateMemberRole(teamId, userId, role);
        return ResponseEntity.ok().build();
    }
}
