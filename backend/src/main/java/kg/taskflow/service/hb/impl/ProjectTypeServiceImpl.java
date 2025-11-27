package kg.taskflow.service.hb.impl;

import kg.taskflow.db.entity.hb.HBProjectType;
import kg.taskflow.db.repository.hb.HBProjectTypeRepository;
import kg.taskflow.dto.hb.ProjectTypeDto;
import kg.taskflow.dto.hb.ProjectTypeRequest;
import kg.taskflow.exception.ConflictException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.hb.ProjectTypeMapper;
import kg.taskflow.service.hb.ProjectTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectTypeServiceImpl implements ProjectTypeService {

    private final HBProjectTypeRepository repository;
    private final ProjectTypeMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectTypeDto> findAll(String locale) {
        return mapper.toDtoList(repository.findAllActive(), locale);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectTypeDto findById(UUID id, String locale) {
        return mapper.toDto(getById(id), locale);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectTypeDto findByAlias(String alias, String locale) {
        HBProjectType entity = repository.findByAliasAndDeletedFalse(alias)
                .orElseThrow(() -> new NotFoundException("Project type not found: " + alias));
        return mapper.toDto(entity, locale);
    }

    @Override
    public ProjectTypeDto create(ProjectTypeRequest request, String locale) {
        if (repository.findByAlias(request.getAlias()).isPresent()) {
            throw new ConflictException("Project type with alias already exists: " + request.getAlias());
        }

        HBProjectType entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toDto(entity, locale);
    }

    @Override
    public ProjectTypeDto update(UUID id, ProjectTypeRequest request, String locale) {
        HBProjectType entity = getById(id);

        if (repository.existsByAliasAndIdNot(request.getAlias(), id)) {
            throw new ConflictException("Project type with alias already exists: " + request.getAlias());
        }

        mapper.updateEntity(entity, request);
        entity = repository.save(entity);
        return mapper.toDto(entity, locale);
    }

    @Override
    public void delete(UUID id) {
        HBProjectType entity = getById(id);
        entity.setDeleted(true);
        repository.save(entity);
    }

    @Override
    public void restore(UUID id) {
        HBProjectType entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project type not found"));
        entity.setDeleted(false);
        repository.save(entity);
    }

    private HBProjectType getById(UUID id) {
        return repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NotFoundException("Project type not found"));
    }
}
