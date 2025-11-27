package kg.taskflow.mapper.hb;

import kg.taskflow.db.entity.hb.HBRoleInProject;
import kg.taskflow.dto.hb.RoleInProjectDto;
import kg.taskflow.dto.hb.RoleInProjectRequest;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleInProjectMapper {

    @Mapping(target = "name", expression = "java(entity.getName(locale))")
    @Mapping(target = "description", expression = "java(entity.getDescription(locale))")
    RoleInProjectDto toDto(HBRoleInProject entity, @Context String locale);

    default List<RoleInProjectDto> toDtoList(List<HBRoleInProject> entities, String locale) {
        return entities.stream().map(e -> toDto(e, locale)).collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HBRoleInProject toEntity(RoleInProjectRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget HBRoleInProject entity, RoleInProjectRequest request);
}
