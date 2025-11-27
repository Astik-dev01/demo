package kg.taskflow.service.impl;

import kg.taskflow.controller.BaseController;
import kg.taskflow.db.entity.Project;
import kg.taskflow.db.entity.ProjectMember;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.entity.hb.HBProjectType;
import kg.taskflow.db.entity.hb.HBRoleInProject;
import kg.taskflow.db.repository.ProjectMemberRepository;
import kg.taskflow.db.repository.ProjectRepository;
import kg.taskflow.db.repository.TaskRepository;
import kg.taskflow.db.repository.UserRepository;
import kg.taskflow.db.repository.hb.HBProjectTypeRepository;
import kg.taskflow.db.repository.hb.HBRoleInProjectRepository;
import kg.taskflow.dto.common.BasePageResponse;
import kg.taskflow.dto.project.*;
import kg.taskflow.exception.BadRequestException;
import kg.taskflow.exception.ConflictException;
import kg.taskflow.exception.ForbiddenException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.ProjectMapper;
import kg.taskflow.service.BoardService;
import kg.taskflow.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final HBProjectTypeRepository projectTypeRepository;
    private final HBRoleInProjectRepository roleRepository;
    private final ProjectMapper projectMapper;

    @Setter
    @Autowired
    @Lazy
    private BoardService boardService;

    private static final String ROLE_OWNER = "owner";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_MEMBER = "member";
    private static final int ADMIN_LEVEL = 80;
    private static final int MEMBER_LEVEL = 50;

    @Override
    @Transactional
    public ProjectDto create(CreateProjectRequest request) {
        User currentUser = getCurrentUser();

        if (projectRepository.existsByProjectKey(request.getProjectKey())) {
            throw new ConflictException("Project with key '" + request.getProjectKey() + "' already exists");
        }

        Project project = projectMapper.toEntity(request);
        project.setOwner(currentUser);

        if (request.getTypeId() != null) {
            HBProjectType type = projectTypeRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new NotFoundException("Project type", request.getTypeId()));
            project.setType(type);
        }

        project = projectRepository.save(project);

        // Create default board with columns for the new project
        boardService.createDefaultBoardForProject(project.getId());

        ProjectDto dto = projectMapper.toDto(project);
        dto.setMemberCount(0L);
        dto.setTaskCount(taskRepository.countByProject(project.getId()));
        return dto;
    }

    @Override
    public ProjectDto getById(UUID id) {
        Project project = findProjectById(id);
        checkViewAccess(project);

        ProjectDto dto = projectMapper.toDto(project);
        dto.setMemberCount(memberRepository.countByProject(id));
        dto.setTaskCount(taskRepository.countByProject(id));
        return dto;
    }

    @Override
    public ProjectDto getByKey(String projectKey) {
        Project project = projectRepository.findByProjectKey(projectKey)
                .orElseThrow(() -> new NotFoundException("Project not found with key: " + projectKey));

        if (Boolean.TRUE.equals(project.getIsDeleted())) {
            throw new NotFoundException("Project", projectKey);
        }

        checkViewAccess(project);

        ProjectDto dto = projectMapper.toDto(project);
        dto.setMemberCount(memberRepository.countByProject(project.getId()));
        dto.setTaskCount(taskRepository.countByProject(project.getId()));
        return dto;
    }

    @Override
    public Page<ProjectListDto> getMyProjects(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Project> projects = projectRepository.findUserProjects(currentUser.getId(), pageable);

        return projects.map(project -> {
            ProjectListDto dto = projectMapper.toListDto(project);
            dto.setMemberCount(memberRepository.countByProject(project.getId()));
            return dto;
        });
    }

    @Override
    public Page<ProjectListDto> getPublicProjects(Pageable pageable) {
        Page<Project> projects = projectRepository.findAllActive(pageable);

        return projects.map(project -> {
            ProjectListDto dto = projectMapper.toListDto(project);
            dto.setMemberCount(memberRepository.countByProject(project.getId()));
            return dto;
        });
    }

    @Override
    public BasePageResponse<ProjectListDto> filterMyProjects(ProjectFilter filter) {
        User currentUser = getCurrentUser();

        // Create pageable with proper conversion (1-based to 0-based)
        Pageable pageable = createPageable(filter);

        Page<Project> projects = projectRepository.findUserProjects(currentUser.getId(), pageable);

        List<ProjectListDto> content = projects.getContent().stream()
                .map(project -> {
                    ProjectListDto dto = projectMapper.toListDto(project);
                    dto.setMemberCount(memberRepository.countByProject(project.getId()));
                    return dto;
                })
                .toList();

        return BasePageResponse.fromPage(projects, content);
    }

    @Override
    public BasePageResponse<ProjectListDto> filterPublicProjects(ProjectFilter filter) {
        // Create pageable with proper conversion (1-based to 0-based)
        Pageable pageable = createPageable(filter);

        Page<Project> projects = projectRepository.findAllActive(pageable);

        List<ProjectListDto> content = projects.getContent().stream()
                .map(project -> {
                    ProjectListDto dto = projectMapper.toListDto(project);
                    dto.setMemberCount(memberRepository.countByProject(project.getId()));
                    return dto;
                })
                .toList();

        return BasePageResponse.fromPage(projects, content);
    }

    private Pageable createPageable(ProjectFilter filter) {
        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "createdAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(filter.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(
                BaseController.getPage(filter.getPage()),
                filter.getSize() != null ? filter.getSize() : 15,
                Sort.by(direction, sortBy)
        );
    }

    @Override
    @Transactional
    public ProjectDto update(UUID id, UpdateProjectRequest request) {
        Project project = findProjectById(id);
        checkManageAccess(project);

        projectMapper.updateEntity(project, request);

        if (request.getTypeId() != null) {
            HBProjectType type = projectTypeRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new NotFoundException("Project type", request.getTypeId()));
            project.setType(type);
        }

        project = projectRepository.save(project);

        ProjectDto dto = projectMapper.toDto(project);
        dto.setMemberCount(memberRepository.countByProject(id));
        dto.setTaskCount(taskRepository.countByProject(id));
        return dto;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Project project = findProjectById(id);
        checkOwnerAccess(project);

        User currentUser = getCurrentUser();
        project.softDelete(currentUser.getId());
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void archive(UUID id) {
        Project project = findProjectById(id);
        checkManageAccess(project);

        project.archive();
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void unarchive(UUID id) {
        Project project = findProjectById(id);
        checkManageAccess(project);

        project.unarchive();
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ProjectMemberDto addMember(UUID projectId, AddMemberRequest request) {
        Project project = findProjectById(projectId);
        checkManageAccess(project);

        User user = userRepository.findActiveById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User", request.getUserId()));

        if (project.getOwner().getId().equals(user.getId())) {
            throw new BadRequestException("Cannot add project owner as a member");
        }

        if (memberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new ConflictException("User is already a member of this project");
        }

        HBRoleInProject role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new NotFoundException("Role", request.getRoleId()));

        if (ROLE_OWNER.equals(role.getAlias())) {
            throw new BadRequestException("Cannot assign owner role to members");
        }

        User currentUser = getCurrentUser();

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(role)
                .invitedBy(currentUser)
                .build();

        member = memberRepository.save(member);
        return projectMapper.toMemberDto(member);
    }

    @Override
    @Transactional
    public void removeMember(UUID projectId, UUID memberId) {
        Project project = findProjectById(projectId);
        checkManageAccess(project);

        ProjectMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Project member", memberId));

        if (!member.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Member does not belong to this project");
        }

        member.setIsDeleted(true);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateMemberRole(UUID projectId, UUID memberId, UUID roleId) {
        Project project = findProjectById(projectId);
        checkManageAccess(project);

        ProjectMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Project member", memberId));

        if (!member.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Member does not belong to this project");
        }

        HBRoleInProject role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role", roleId));

        if (ROLE_OWNER.equals(role.getAlias())) {
            throw new BadRequestException("Cannot assign owner role to members");
        }

        member.setRole(role);
        memberRepository.save(member);
    }

    @Override
    public List<ProjectMemberDto> getMembers(UUID projectId) {
        Project project = findProjectById(projectId);
        checkViewAccess(project);

        List<ProjectMember> members = memberRepository.findByProject(projectId);
        return projectMapper.toMemberDtoList(members);
    }

    @Override
    public boolean isMember(UUID projectId, UUID userId) {
        Project project = findProjectById(projectId);

        if (project.getOwner().getId().equals(userId)) {
            return true;
        }

        return memberRepository.findByProjectAndUser(projectId, userId).isPresent();
    }

    @Override
    public boolean hasPermission(UUID projectId, UUID userId, String permission) {
        Project project = findProjectById(projectId);

        if (project.getOwner().getId().equals(userId)) {
            return true;
        }

        return memberRepository.findByProjectAndUser(projectId, userId)
                .map(member -> {
                    List<String> permissions = member.getRole().getPermissions();
                    return permissions != null && permissions.contains(permission);
                })
                .orElse(false);
    }

    @Override
    public boolean hasRoleLevel(UUID projectId, UUID userId, int minLevel) {
        Project project = findProjectById(projectId);

        if (project.getOwner().getId().equals(userId)) {
            return true;
        }

        return memberRepository.hasRoleLevel(projectId, userId, minLevel);
    }

    // Helper methods

    private Project findProjectById(UUID id) {
        return projectRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Project", id));
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void checkViewAccess(Project project) {
        if (Boolean.TRUE.equals(project.getIsPublic())) {
            return;
        }

        User currentUser = getCurrentUser();
        UUID userId = currentUser.getId();

        if (project.getOwner().getId().equals(userId)) {
            return;
        }

        if (memberRepository.findByProjectAndUser(project.getId(), userId).isEmpty()) {
            throw new ForbiddenException("You do not have access to this project");
        }
    }

    private void checkManageAccess(Project project) {
        User currentUser = getCurrentUser();
        UUID userId = currentUser.getId();

        if (project.getOwner().getId().equals(userId)) {
            return;
        }

        if (!memberRepository.hasRoleLevel(project.getId(), userId, ADMIN_LEVEL)) {
            throw new ForbiddenException("You do not have permission to manage this project");
        }
    }

    private void checkOwnerAccess(Project project) {
        User currentUser = getCurrentUser();

        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the project owner can perform this action");
        }
    }
}
