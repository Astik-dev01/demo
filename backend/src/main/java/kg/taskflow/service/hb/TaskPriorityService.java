package kg.taskflow.service.hb;

import kg.taskflow.dto.hb.TaskPriorityDto;
import kg.taskflow.dto.hb.TaskPriorityRequest;

import java.util.List;
import java.util.UUID;

public interface TaskPriorityService {

    List<TaskPriorityDto> findAll(String locale);

    TaskPriorityDto findById(UUID id, String locale);

    TaskPriorityDto findByAlias(String alias, String locale);

    TaskPriorityDto create(TaskPriorityRequest request, String locale);

    TaskPriorityDto update(UUID id, TaskPriorityRequest request, String locale);

    void delete(UUID id);

    void restore(UUID id);
}
