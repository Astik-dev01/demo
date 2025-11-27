package kg.taskflow.mapper;

import kg.taskflow.db.entity.Invitation;
import kg.taskflow.db.entity.Team;
import kg.taskflow.db.entity.TeamMember;
import kg.taskflow.dto.team.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TeamMapper {

    @Mapping(target = "memberCount", ignore = true)
    TeamDto toDto(Team team);

    List<TeamDto> toDtoList(List<Team> teams);

    TeamMemberDto toMemberDto(TeamMember member);

    List<TeamMemberDto> toMemberDtoList(List<TeamMember> members);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    Team toEntity(CreateTeamRequest request);

    @Mapping(target = "owner", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Team team, UpdateTeamRequest request);

    @Mapping(target = "targetName", ignore = true)
    InvitationDto toInvitationDto(Invitation invitation);

    List<InvitationDto> toInvitationDtoList(List<Invitation> invitations);
}
