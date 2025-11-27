package kg.taskflow.mapper.hb;

import kg.taskflow.db.entity.hb.HBProjectType;
import kg.taskflow.dto.hb.ProjectTypeDto;
import kg.taskflow.dto.hb.ProjectTypeRequest;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProjectTypeMapper {

    @Mapping(target = "name", expression = "java(entity.getName(locale))")
    @Mapping(target = "description", expression = "java(entity.getDescription(locale))")
    ProjectTypeDto toDto(HBProjectType entity, @Context String locale);

    default List<ProjectTypeDto> toDtoList(List<HBProjectType> entities, String locale) {
        return entities.stream().map(e -> toDto(e, locale)).collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HBProjectType toEntity(ProjectTypeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget HBProjectType entity, ProjectTypeRequest request);
}
