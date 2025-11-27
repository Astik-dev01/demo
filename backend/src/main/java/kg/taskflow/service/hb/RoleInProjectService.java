package kg.taskflow.service.hb;

import kg.taskflow.dto.hb.RoleInProjectDto;
import kg.taskflow.dto.hb.RoleInProjectRequest;

import java.util.List;
import java.util.UUID;

public interface RoleInProjectService {

    List<RoleInProjectDto> findAll(String locale);

    RoleInProjectDto findById(UUID id, String locale);

    RoleInProjectDto findByAlias(String alias, String locale);

    RoleInProjectDto create(RoleInProjectRequest request, String locale);

    RoleInProjectDto update(UUID id, RoleInProjectRequest request, String locale);

    void delete(UUID id);

    void restore(UUID id);
}
