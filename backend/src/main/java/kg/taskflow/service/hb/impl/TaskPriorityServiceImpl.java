package kg.taskflow.service.hb.impl;

import kg.taskflow.db.entity.hb.HBTaskPriority;
import kg.taskflow.db.repository.hb.HBTaskPriorityRepository;
import kg.taskflow.dto.hb.TaskPriorityDto;
import kg.taskflow.dto.hb.TaskPriorityRequest;
import kg.taskflow.exception.ConflictException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.hb.TaskPriorityMapper;
import kg.taskflow.service.hb.TaskPriorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskPriorityServiceImpl implements TaskPriorityService {

    private final HBTaskPriorityRepository repository;
    private final TaskPriorityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TaskPriorityDto> findAll(String locale) {
        return mapper.toDtoList(repository.findAllActive(), locale);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskPriorityDto findById(UUID id, String locale) {
        return mapper.toDto(getById(id), locale);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskPriorityDto findByAlias(String alias, String locale) {
        HBTaskPriority entity = repository.findByAliasAndDeletedFalse(alias)
                .orElseThrow(() -> new NotFoundException("Task priority not found: " + alias));
        return mapper.toDto(entity, locale);
    }

    @Override
    public TaskPriorityDto create(TaskPriorityRequest request, String locale) {
        if (repository.findByAlias(request.getAlias()).isPresent()) {
            throw new ConflictException("Task priority with alias already exists: " + request.getAlias());
        }

        HBTaskPriority entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toDto(entity, locale);
    }

    @Override
    public TaskPriorityDto update(UUID id, TaskPriorityRequest request, String locale) {
        HBTaskPriority entity = getById(id);

        if (repository.existsByAliasAndIdNot(request.getAlias(), id)) {
            throw new ConflictException("Task priority with alias already exists: " + request.getAlias());
        }

        mapper.updateEntity(entity, request);
        entity = repository.save(entity);
        return mapper.toDto(entity, locale);
    }

    @Override
    public void delete(UUID id) {
        HBTaskPriority entity = getById(id);
        entity.setDeleted(true);
        repository.save(entity);
    }

    @Override
    public void restore(UUID id) {
        HBTaskPriority entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task priority not found"));
        entity.setDeleted(false);
        repository.save(entity);
    }

    private HBTaskPriority getById(UUID id) {
        return repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NotFoundException("Task priority not found"));
    }
}
