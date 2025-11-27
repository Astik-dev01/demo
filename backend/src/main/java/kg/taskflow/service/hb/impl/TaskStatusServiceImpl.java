package kg.taskflow.service.hb.impl;

import kg.taskflow.db.entity.hb.HBTaskStatus;
import kg.taskflow.db.repository.hb.HBTaskStatusRepository;
import kg.taskflow.dto.hb.TaskStatusDto;
import kg.taskflow.dto.hb.TaskStatusRequest;
import kg.taskflow.exception.ConflictException;
import kg.taskflow.exception.NotFoundException;
import kg.taskflow.mapper.hb.TaskStatusMapper;
import kg.taskflow.service.hb.TaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskStatusServiceImpl implements TaskStatusService {

    private final HBTaskStatusRepository repository;
    private final TaskStatusMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TaskStatusDto> findAll(String locale) {
        return mapper.toDtoList(repository.findAllActive(), locale);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStatusDto findById(UUID id, String locale) {
        return mapper.toDto(getById(id), locale);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStatusDto findByAlias(String alias, String locale) {
        HBTaskStatus entity = repository.findByAliasAndDeletedFalse(alias)
                .orElseThrow(() -> new NotFoundException("Task status not found: " + alias));
        return mapper.toDto(entity, locale);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStatusDto findDefault(String locale) {
        HBTaskStatus entity = repository.findDefault()
                .orElseThrow(() -> new NotFoundException("Default task status not found"));
        return mapper.toDto(entity, locale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskStatusDto> findFinalStatuses(String locale) {
        return mapper.toDtoList(repository.findFinalStatuses(), locale);
    }

    @Override
    public TaskStatusDto create(TaskStatusRequest request, String locale) {
        if (repository.findByAlias(request.getAlias()).isPresent()) {
            throw new ConflictException("Task status with alias already exists: " + request.getAlias());
        }

        HBTaskStatus entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toDto(entity, locale);
    }

    @Override
    public TaskStatusDto update(UUID id, TaskStatusRequest request, String locale) {
        HBTaskStatus entity = getById(id);

        if (repository.existsByAliasAndIdNot(request.getAlias(), id)) {
            throw new ConflictException("Task status with alias already exists: " + request.getAlias());
        }

        mapper.updateEntity(entity, request);
        entity = repository.save(entity);
        return mapper.toDto(entity, locale);
    }

    @Override
    public void delete(UUID id) {
        HBTaskStatus entity = getById(id);
        entity.setDeleted(true);
        repository.save(entity);
    }

    @Override
    public void restore(UUID id) {
        HBTaskStatus entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task status not found"));
        entity.setDeleted(false);
        repository.save(entity);
    }

    private HBTaskStatus getById(UUID id) {
        return repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NotFoundException("Task status not found"));
    }
}
