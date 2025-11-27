package kg.taskflow.mapper;

import kg.taskflow.db.entity.Project;
import kg.taskflow.db.entity.ProjectMember;
import kg.taskflow.dto.project.*;
import kg.taskflow.mapper.hb.ProjectTypeMapper;
import kg.taskflow.mapper.hb.RoleInProjectMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ProjectTypeMapper.class, RoleInProjectMapper.class})
public interface ProjectMapper {

    @Mapping(target = "projectKey", source = "projectKey")
    @Mapping(target = "type", source = "type", qualifiedByName = "projectTypeToDto")
    @Mapping(target = "memberCount", ignore = true)
    @Mapping(target = "taskCount", ignore = true)
    ProjectDto toDto(Project project);

    @Mapping(target = "projectKey", source = "projectKey")
    @Mapping(target = "ownerName", expression = "java(project.getOwner().getFullName())")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "typeName", expression = "java(project.getType() != null ? project.getType().getNameRu() : null)")
    @Mapping(target = "memberCount", ignore = true)
    ProjectListDto toListDto(Project project);

    List<ProjectListDto> toListDtoList(List<Project> projects);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "isArchived", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "settings", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Project toEntity(CreateProjectRequest request);

    @Mapping(target = "projectKey", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "isArchived", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "settings", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Project project, UpdateProjectRequest request);

    @Mapping(target = "role", source = "role", qualifiedByName = "roleInProjectToDto")
    ProjectMemberDto toMemberDto(ProjectMember member);

    List<ProjectMemberDto> toMemberDtoList(List<ProjectMember> members);

    @Named("projectTypeToDto")
    default kg.taskflow.dto.hb.ProjectTypeDto projectTypeToDto(kg.taskflow.db.entity.hb.HBProjectType type) {
        if (type == null) return null;
        return kg.taskflow.dto.hb.ProjectTypeDto.builder()
                .id(type.getId())
                .alias(type.getAlias())
                .nameRu(type.getNameRu())
                .nameKy(type.getNameKy())
                .nameEn(type.getNameEn())
                .name(type.getNameRu())
                .icon(type.getIcon())
                .build();
    }

    @Named("roleInProjectToDto")
    default kg.taskflow.dto.hb.RoleInProjectDto roleInProjectToDto(kg.taskflow.db.entity.hb.HBRoleInProject role) {
        if (role == null) return null;
        return kg.taskflow.dto.hb.RoleInProjectDto.builder()
                .id(role.getId())
                .alias(role.getAlias())
                .nameRu(role.getNameRu())
                .nameKy(role.getNameKy())
                .nameEn(role.getNameEn())
                .name(role.getNameRu())
                .permissions(role.getPermissions())
                .level(role.getLevel())
                .build();
    }
}
