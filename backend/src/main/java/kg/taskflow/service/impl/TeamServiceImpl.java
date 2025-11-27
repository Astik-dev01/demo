package kg.taskflow.service.impl;

import kg.taskflow.db.entity.Team;
import kg.taskflow.db.entity.TeamMember;
import kg.taskflow.db.entity.User;
import kg.taskflow.db.repository.TeamMemberRepository;
import kg.taskflow.db.repository.TeamRepository;
import kg.taskflow.db.repository.UserRepository;
import kg.taskflow.dto.team.*;
import kg.taskflow.exception.BadRequestException;
import kg.taskflow.exception.ConflictException;
import kg.taskflow.exception.ForbiddenException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.TeamMapper;
import kg.taskflow.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;

    private static final String ROLE_OWNER = "OWNER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    @Override
    @Transactional
    public TeamDto create(CreateTeamRequest request) {
        User currentUser = getCurrentUser();

        Team team = teamMapper.toEntity(request);
        team.setOwner(currentUser);

        team = teamRepository.save(team);

        TeamDto dto = teamMapper.toDto(team);
        dto.setMemberCount(0L);
        return dto;
    }

    @Override
    public TeamDto getById(UUID id) {
        Team team = findTeamById(id);
        checkViewAccess(team);
        return getTeamDto(team);
    }

    @Override
    public Page<TeamDto> getMyTeams(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Team> teams = teamRepository.findUserTeams(currentUser.getId(), pageable);
        return teams.map(this::getTeamDto);
    }

    @Override
    public Page<TeamDto> getPublicTeams(Pageable pageable) {
        Page<Team> teams = teamRepository.findPublicTeams(pageable);
        return teams.map(this::getTeamDto);
    }

    @Override
    @Transactional
    public TeamDto update(UUID id, UpdateTeamRequest request) {
        Team team = findTeamById(id);
        checkManageAccess(team);

        teamMapper.updateEntity(team, request);
        team = teamRepository.save(team);

        return getTeamDto(team);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Team team = findTeamById(id);
        checkOwnerAccess(team);

        team.setIsDeleted(true);
        teamRepository.save(team);
    }

    @Override
    @Transactional
    public TeamMemberDto addMember(UUID teamId, UUID userId, String role) {
        Team team = findTeamById(teamId);
        checkManageAccess(team);

        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        if (team.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Cannot add team owner as a member");
        }

        if (memberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new ConflictException("User is already a member of this team");
        }

        if (ROLE_OWNER.equals(role)) {
            throw new BadRequestException("Cannot assign owner role");
        }

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(role != null ? role : ROLE_MEMBER)
                .build();

        member = memberRepository.save(member);
        return teamMapper.toMemberDto(member);
    }

    @Override
    @Transactional
    public void removeMember(UUID teamId, UUID userId) {
        Team team = findTeamById(teamId);
        checkManageAccess(team);

        TeamMember member = memberRepository.findByTeamAndUser(teamId, userId)
                .orElseThrow(() -> new NotFoundException("Team member not found"));

        memberRepository.delete(member);
    }

    @Override
    @Transactional
    public void updateMemberRole(UUID teamId, UUID userId, String role) {
        Team team = findTeamById(teamId);
        checkManageAccess(team);

        TeamMember member = memberRepository.findByTeamAndUser(teamId, userId)
                .orElseThrow(() -> new NotFoundException("Team member not found"));

        if (ROLE_OWNER.equals(role)) {
            throw new BadRequestException("Cannot assign owner role");
        }

        member.setRole(role);
        memberRepository.save(member);
    }

    @Override
    public List<TeamMemberDto> getMembers(UUID teamId) {
        Team team = findTeamById(teamId);
        checkViewAccess(team);

        List<TeamMember> members = memberRepository.findByTeam(teamId);
        return teamMapper.toMemberDtoList(members);
    }

    @Override
    public boolean isMember(UUID teamId, UUID userId) {
        Team team = findTeamById(teamId);

        if (team.getOwner().getId().equals(userId)) {
            return true;
        }

        return memberRepository.findByTeamAndUser(teamId, userId).isPresent();
    }

    @Override
    public boolean isAdmin(UUID teamId, UUID userId) {
        Team team = findTeamById(teamId);

        if (team.getOwner().getId().equals(userId)) {
            return true;
        }

        return memberRepository.findByTeamAndUser(teamId, userId)
                .map(member -> ROLE_ADMIN.equals(member.getRole()))
                .orElse(false);
    }

    // Helper methods

    private Team findTeamById(UUID id) {
        return teamRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Team", id));
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void checkViewAccess(Team team) {
        if (Boolean.TRUE.equals(team.getIsPublic())) {
            return;
        }

        User currentUser = getCurrentUser();
        UUID userId = currentUser.getId();

        if (team.getOwner().getId().equals(userId)) {
            return;
        }

        if (memberRepository.findByTeamAndUser(team.getId(), userId).isEmpty()) {
            throw new ForbiddenException("You do not have access to this team");
        }
    }

    private void checkManageAccess(Team team) {
        User currentUser = getCurrentUser();
        UUID userId = currentUser.getId();

        if (team.getOwner().getId().equals(userId)) {
            return;
        }

        if (!isAdmin(team.getId(), userId)) {
            throw new ForbiddenException("You do not have permission to manage this team");
        }
    }

    private void checkOwnerAccess(Team team) {
        User currentUser = getCurrentUser();

        if (!team.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the team owner can perform this action");
        }
    }

    private TeamDto getTeamDto(Team team) {
        TeamDto dto = teamMapper.toDto(team);
        dto.setMemberCount(memberRepository.countByTeam(team.getId()));
        return dto;
    }
}
