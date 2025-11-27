package kg.taskflow.service;

import kg.taskflow.dto.common.BasePageResponse;
import kg.taskflow.dto.project.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectDto create(CreateProjectRequest request);

    ProjectDto getById(UUID id);

    ProjectDto getByKey(String projectKey);

    Page<ProjectListDto> getMyProjects(Pageable pageable);

    Page<ProjectListDto> getPublicProjects(Pageable pageable);

    // New filter methods with proper pagination format
    BasePageResponse<ProjectListDto> filterMyProjects(ProjectFilter filter);

    BasePageResponse<ProjectListDto> filterPublicProjects(ProjectFilter filter);

    ProjectDto update(UUID id, UpdateProjectRequest request);

    void delete(UUID id);

    void archive(UUID id);

    void unarchive(UUID id);

    // Member management
    ProjectMemberDto addMember(UUID projectId, AddMemberRequest request);

    void removeMember(UUID projectId, UUID memberId);

    void updateMemberRole(UUID projectId, UUID memberId, UUID roleId);

    List<ProjectMemberDto> getMembers(UUID projectId);

    // Access checks
    boolean isMember(UUID projectId, UUID userId);

    boolean hasPermission(UUID projectId, UUID userId, String permission);

    boolean hasRoleLevel(UUID projectId, UUID userId, int minLevel);
}
