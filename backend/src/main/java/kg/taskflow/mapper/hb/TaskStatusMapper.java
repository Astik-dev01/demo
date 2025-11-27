package kg.taskflow.mapper.hb;

import kg.taskflow.db.entity.hb.HBTaskStatus;
import kg.taskflow.dto.hb.TaskStatusDto;
import kg.taskflow.dto.hb.TaskStatusRequest;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TaskStatusMapper {

    @Mapping(target = "name", expression = "java(entity.getName(locale))")
    TaskStatusDto toDto(HBTaskStatus entity, @Context String locale);

    default List<TaskStatusDto> toDtoList(List<HBTaskStatus> entities, String locale) {
        return entities.stream().map(e -> toDto(e, locale)).collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HBTaskStatus toEntity(TaskStatusRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget HBTaskStatus entity, TaskStatusRequest request);
}
