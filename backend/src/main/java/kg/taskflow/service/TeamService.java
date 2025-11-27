package kg.taskflow.service;

import kg.taskflow.dto.team.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    TeamDto create(CreateTeamRequest request);

    TeamDto getById(UUID id);

    Page<TeamDto> getMyTeams(Pageable pageable);

    Page<TeamDto> getPublicTeams(Pageable pageable);

    TeamDto update(UUID id, UpdateTeamRequest request);

    void delete(UUID id);

    // Member management
    TeamMemberDto addMember(UUID teamId, UUID userId, String role);

    void removeMember(UUID teamId, UUID userId);

    void updateMemberRole(UUID teamId, UUID userId, String role);

    List<TeamMemberDto> getMembers(UUID teamId);

    // Access checks
    boolean isMember(UUID teamId, UUID userId);

    boolean isAdmin(UUID teamId, UUID userId);
}
