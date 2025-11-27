package kg.taskflow.service.hb;

import kg.taskflow.dto.hb.TaskStatusDto;
import kg.taskflow.dto.hb.TaskStatusRequest;

import java.util.List;
import java.util.UUID;

public interface TaskStatusService {

    List<TaskStatusDto> findAll(String locale);

    TaskStatusDto findById(UUID id, String locale);

    TaskStatusDto findByAlias(String alias, String locale);

    TaskStatusDto findDefault(String locale);

    List<TaskStatusDto> findFinalStatuses(String locale);

    TaskStatusDto create(TaskStatusRequest request, String locale);

    TaskStatusDto update(UUID id, TaskStatusRequest request, String locale);

    void delete(UUID id);

    void restore(UUID id);
}
