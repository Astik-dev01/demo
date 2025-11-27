package kg.taskflow.mapper;

import kg.taskflow.db.entity.TimeEntry;
import kg.taskflow.dto.time.TimeEntryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TimeEntryMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskKey", expression = "java(entry.getTask().getKey())")
    @Mapping(target = "taskTitle", source = "task.title")
    @Mapping(target = "projectId", source = "task.project.id")
    @Mapping(target = "projectName", source = "task.project.name")
    TimeEntryDto toDto(TimeEntry entry);

    List<TimeEntryDto> toDtoList(List<TimeEntry> entries);
}
