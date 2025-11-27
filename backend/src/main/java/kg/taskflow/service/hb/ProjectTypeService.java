package kg.taskflow.service.hb;

import kg.taskflow.dto.hb.ProjectTypeDto;
import kg.taskflow.dto.hb.ProjectTypeRequest;

import java.util.List;
import java.util.UUID;

public interface ProjectTypeService {

    List<ProjectTypeDto> findAll(String locale);

    ProjectTypeDto findById(UUID id, String locale);

    ProjectTypeDto findByAlias(String alias, String locale);

    ProjectTypeDto create(ProjectTypeRequest request, String locale);

    ProjectTypeDto update(UUID id, ProjectTypeRequest request, String locale);

    void delete(UUID id);

    void restore(UUID id);
}
