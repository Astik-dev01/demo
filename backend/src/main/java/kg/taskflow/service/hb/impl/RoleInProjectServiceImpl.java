package kg.taskflow.service.hb.impl;

import kg.taskflow.db.entity.hb.HBRoleInProject;
import kg.taskflow.db.repository.hb.HBRoleInProjectRepository;
import kg.taskflow.dto.hb.RoleInProjectDto;
import kg.taskflow.dto.hb.RoleInProjectRequest;
import kg.taskflow.exception.ConflictException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.hb.RoleInProjectMapper;
import kg.taskflow.service.hb.RoleInProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleInProjectServiceImpl implements RoleInProjectService {

    private final HBRoleInProjectRepository repository;
    private final RoleInProjectMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleInProjectDto> findAll(String locale) {
        return mapper.toDtoList(repository.findAllActive(), locale);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleInProjectDto findById(UUID id, String locale) {
        return mapper.toDto(getById(id), locale);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleInProjectDto findByAlias(String alias, String locale) {
        HBRoleInProject entity = repository.findByAliasAndDeletedFalse(alias)
                .orElseThrow(() -> new NotFoundException("Role in project not found: " + alias));
        return mapper.toDto(entity, locale);
    }

    @Override
    public RoleInProjectDto create(RoleInProjectRequest request, String locale) {
        if (repository.findByAlias(request.getAlias()).isPresent()) {
            throw new ConflictException("Role in project with alias already exists: " + request.getAlias());
        }

        HBRoleInProject entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toDto(entity, locale);
    }

    @Override
    public RoleInProjectDto update(UUID id, RoleInProjectRequest request, String locale) {
        HBRoleInProject entity = getById(id);

        if (repository.existsByAliasAndIdNot(request.getAlias(), id)) {
            throw new ConflictException("Role in project with alias already exists: " + request.getAlias());
        }

        mapper.updateEntity(entity, request);
        entity = repository.save(entity);
        return mapper.toDto(entity, locale);
    }

    @Override
    public void delete(UUID id) {
        HBRoleInProject entity = getById(id);
        entity.setDeleted(true);
        repository.save(entity);
    }

    @Override
    public void restore(UUID id) {
        HBRoleInProject entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role in project not found"));
        entity.setDeleted(false);
        repository.save(entity);
    }

    private HBRoleInProject getById(UUID id) {
        return repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NotFoundException("Role in project not found"));
    }
}
