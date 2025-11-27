package kg.taskflow.mapper.hb;

import kg.taskflow.db.entity.hb.HBTaskPriority;
import kg.taskflow.dto.hb.TaskPriorityDto;
import kg.taskflow.dto.hb.TaskPriorityRequest;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TaskPriorityMapper {

    @Mapping(target = "name", expression = "java(entity.getName(locale))")
    TaskPriorityDto toDto(HBTaskPriority entity, @Context String locale);

    default List<TaskPriorityDto> toDtoList(List<HBTaskPriority> entities, String locale) {
        return entities.stream().map(e -> toDto(e, locale)).collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HBTaskPriority toEntity(TaskPriorityRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget HBTaskPriority entity, TaskPriorityRequest request);
}
